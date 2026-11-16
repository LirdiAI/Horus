import http from 'node:http';
import { DatabaseSync } from 'node:sqlite';
import { randomBytes, randomUUID, scrypt as rawScrypt, timingSafeEqual, createHash } from 'node:crypto';
import { promisify } from 'node:util';
import { mkdirSync, readFileSync, statSync } from 'node:fs';
import { dirname, join, extname } from 'node:path';
import { fileURLToPath } from 'node:url';
const root = dirname(fileURLToPath(import.meta.url));
const scrypt = promisify(rawScrypt);
const hash = value => createHash('sha256').update(value).digest('hex');
export const products = [
  { id: '30d', name: '30 Days', price: 67, days: 30 },
  { id: '90d', name: '90 Days', price: 139, days: 90 },
  { id: 'lifetime', name: 'Lifetime', price: 290, days: null },
  { id: 'beta', name: 'Beta', price: 120, days: null }
];
export function openDatabase(file) {
  mkdirSync(dirname(file), { recursive: true });
  const db = new DatabaseSync(file);
  db.exec(`PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON;
    CREATE TABLE IF NOT EXISTS users(id TEXT PRIMARY KEY,username TEXT NOT NULL UNIQUE COLLATE NOCASE,email TEXT NOT NULL UNIQUE COLLATE NOCASE,password TEXT NOT NULL,created_at TEXT NOT NULL,hwid TEXT,lifetime INTEGER NOT NULL DEFAULT 0,expires_at TEXT,beta INTEGER NOT NULL DEFAULT 0,admin INTEGER NOT NULL DEFAULT 0);
    CREATE TABLE IF NOT EXISTS sessions(token TEXT PRIMARY KEY,user_id TEXT REFERENCES users(id),csrf TEXT NOT NULL,expires INTEGER NOT NULL);
    CREATE TABLE IF NOT EXISTS orders(id TEXT PRIMARY KEY,user_id TEXT NOT NULL REFERENCES users(id),product_id TEXT NOT NULL,product_name TEXT NOT NULL,amount INTEGER NOT NULL,status TEXT NOT NULL DEFAULT 'pending',created_at TEXT NOT NULL,reviewed_by TEXT REFERENCES users(id),reviewed_at TEXT);
    CREATE TABLE IF NOT EXISTS limits(key TEXT PRIMARY KEY,count INTEGER NOT NULL,until INTEGER NOT NULL);`);
  return db;
}
const userView = u => u ? { id:u.id, username:u.username, email:u.email, created_at:u.created_at, hwid:u.hwid, lifetime:!!u.lifetime, expires_at:u.expires_at, beta:!!u.beta, admin:!!u.admin } : null;
async function passwordHash(password) {
  const salt = randomBytes(16).toString('hex');
  return salt + ':' + (await scrypt(password, salt, 64)).toString('hex');
}
async function passwordMatches(password, stored) {
  const [salt, digest] = stored.split(':');
  return timingSafeEqual(Buffer.from(digest, 'hex'), await scrypt(password, salt, 64));
}
function fail(status, message) { throw Object.assign(new Error(message), { status }); }
async function body(req) {
  if (!req.headers['content-type']?.startsWith('application/json')) fail(415, 'Ожидается JSON.');
  let data = ''; for await (const chunk of req) { data += chunk; if (data.length > 8192) fail(413, 'Слишком большой запрос.'); }
  try { const value = JSON.parse(data); if (!value || Array.isArray(value) || typeof value !== 'object') throw Error(); return value; }
  catch { fail(400, 'Некорректный запрос.'); }
}
export function createStore({ database = join(root, 'data/store.sqlite'), secure = process.env.NODE_ENV === 'production' } = {}) {
  const db = openDatabase(database);
  function limited(key, max) {
    const now = Date.now();
    db.prepare('DELETE FROM limits WHERE until < ?').run(now);
    db.prepare('INSERT INTO limits(key,count,until) VALUES(?,1,?) ON CONFLICT(key) DO UPDATE SET count=count+1').run(key, now + 900000);
    if (db.prepare('SELECT count FROM limits WHERE key=?').get(key).count > max) fail(429, 'Слишком много попыток. Попробуйте через 15 минут.');
  }
  const server = http.createServer(async (req, res) => {
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('Referrer-Policy', 'same-origin');
    res.setHeader('Content-Security-Policy', "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'");
    const send = (status, value) => { res.writeHead(status, { 'Content-Type':'application/json; charset=utf-8', 'Cache-Control':'no-store' }); res.end(JSON.stringify(value)); };
    try {
      const url = new URL(req.url, 'http://localhost');
      const path = url.pathname;
      if (!path.startsWith('/api/')) {
        if (!['GET','HEAD'].includes(req.method)) fail(405,'Метод не поддерживается.');
        const relative = path === '/' ? 'index.html' : path.slice(1);
        // Never serve backend code, databases, legacy auth scripts or configuration.
        if (!/^(index|shop|login|register|profile|admin)\.html$/.test(relative) && !/^(css\/store\.css|js\/store\.js|images\/[a-zA-Z0-9_-]+\.(svg|png|jpg|webp)|favicon\.ico)$/.test(relative)) fail(404,'Страница не найдена.');
        const file = join(root, relative);
        let data; try { if (statSync(file).size > 20000000) fail(404,'Файл недоступен.'); data = readFileSync(file); } catch { fail(404,'Страница не найдена.'); }
        const mime = {'.html':'text/html; charset=utf-8','.css':'text/css; charset=utf-8','.js':'text/javascript; charset=utf-8','.svg':'image/svg+xml','.png':'image/png','.jpg':'image/jpeg','.webp':'image/webp','.ico':'image/x-icon'};
        res.writeHead(200,{'Content-Type':mime[extname(file)],'Cache-Control':'no-cache'}); res.end(req.method === 'HEAD' ? undefined : data); return;
      }
      const now = Date.now();
      db.prepare('DELETE FROM sessions WHERE expires < ?').run(now);
      const token = /(?:^|;\s*)horus_session=([a-f0-9]{64})(?:;|$)/.exec(req.headers.cookie || '')?.[1];
      let session = token ? db.prepare('SELECT * FROM sessions WHERE token=?').get(hash(token)) : null;
      function newSession(userId = null) {
        const raw = randomBytes(32).toString('hex');
        session = { token:hash(raw), user_id:userId, csrf:randomBytes(32).toString('hex'), expires:now+604800000 };
        db.prepare('INSERT INTO sessions VALUES(?,?,?,?)').run(session.token, session.user_id, session.csrf, session.expires);
        res.setHeader('Set-Cookie', `horus_session=${raw}; Path=/; HttpOnly; SameSite=Strict; Max-Age=604800${secure ? '; Secure' : ''}`);
      }
      if (!session) newSession();
      let user = session.user_id ? db.prepare('SELECT * FROM users WHERE id=?').get(session.user_id) : null;
      if (req.method === 'POST') {
        if (req.headers.origin && req.headers.origin !== `${secure ? 'https' : 'http'}://${req.headers.host}`) fail(403,'Недопустимый источник запроса.');
        if (req.headers['x-csrf-token'] !== session.csrf) fail(403,'Обновите страницу и повторите действие.');
      }
      if (req.method === 'GET' && path === '/api/session') return send(200,{user:userView(user),csrf:session.csrf});
      if (req.method === 'GET' && path === '/api/catalog') return send(200,{products});
      if (req.method === 'POST' && ['/api/register','/api/login'].includes(path)) {
        limited('auth:'+req.socket.remoteAddress,30);
        const b = await body(req);
        const username = typeof b.username === 'string' ? b.username.trim() : '';
        const password = typeof b.password === 'string' ? b.password : '';
        if (password.length < 10 || password.length > 128) fail(400,'Пароль должен содержать от 10 до 128 символов.');
        if (!/^[a-zA-Z0-9_]{3,24}$/.test(username)) fail(400,'Логин: 3–24 латинские буквы, цифры или знак _.');
        if (path === '/api/register') {
          const email = typeof b.email === 'string' ? b.email.trim().toLowerCase() : '';
          if (email.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) fail(400,'Укажите корректную почту.');
          limited('register:'+req.socket.remoteAddress,8);
          const digest = await passwordHash(password);
          try { db.prepare('INSERT INTO users(id,username,email,password,created_at) VALUES(?,?,?,?,?)').run(randomUUID(),username,email,digest,new Date().toISOString()); }
          catch (e) { if (e.code?.includes('CONSTRAINT') || e.message.includes('UNIQUE')) fail(409,'Логин или почта уже используются.'); throw e; }
        }
        user = db.prepare('SELECT * FROM users WHERE username=?').get(username);
        // Equal-cost verification even for an unknown login.
        const dummy = '0123456789abcdef0123456789abcdef:'+'0'.repeat(128);
        if (!await passwordMatches(password,user?.password || dummy) || !user) fail(401,'Неверный логин или пароль.');
        db.prepare('DELETE FROM sessions WHERE token=?').run(session.token); newSession(user.id);
        return send(200,{user:userView(user),csrf:session.csrf});
      }
      if (!user) fail(401,'Войдите в аккаунт.');
      if (req.method === 'POST' && path === '/api/logout') {
        db.prepare('DELETE FROM sessions WHERE token=?').run(session.token); newSession(); return send(200,{ok:true,csrf:session.csrf});
      }
      if (req.method === 'POST' && path === '/api/password') {
        limited('password:'+user.id,10); const b = await body(req);
        if (typeof b.currentPassword !== 'string' || b.currentPassword.length > 128 || !await passwordMatches(b.currentPassword,user.password)) fail(400,'Текущий пароль неверен.');
        if (typeof b.newPassword !== 'string' || b.newPassword.length < 10 || b.newPassword.length > 128) fail(400,'Новый пароль: от 10 до 128 символов.');
        const digest = await passwordHash(b.newPassword);
        db.prepare('UPDATE users SET password=? WHERE id=?').run(digest,user.id);
        db.prepare('DELETE FROM sessions WHERE user_id=? AND token<>?').run(user.id,session.token); return send(200,{ok:true});
      }
      if (req.method === 'GET' && path === '/api/orders') return send(200,{orders:db.prepare('SELECT * FROM orders WHERE user_id=? ORDER BY created_at DESC').all(user.id)});
      if (req.method === 'POST' && path === '/api/orders') {
        limited('order:'+user.id,30); const b = await body(req); const p = products.find(p=>p.id===b.productId);
        if (!p) fail(400,'Тариф не найден.');
        if (p.id==='beta' && !user.lifetime) fail(409,'Beta доступна только владельцам Lifetime.');
        if ((p.id==='beta' && user.beta) || (p.id!=='beta' && user.lifetime)) fail(409,'Этот доступ уже есть у вашего аккаунта.');
        const existing = db.prepare("SELECT * FROM orders WHERE user_id=? AND status='pending'").get(user.id);
        if (existing) fail(409,'У вас уже есть заказ на рассмотрении. Отмените его в профиле или дождитесь подтверждения.');
        const order = {id:randomUUID(),product_id:p.id,product_name:p.name,amount:p.price,status:'pending',created_at:new Date().toISOString()};
        db.prepare('INSERT INTO orders(id,user_id,product_id,product_name,amount,created_at) VALUES(?,?,?,?,?,?)').run(order.id,user.id,p.id,p.name,p.price,order.created_at);
        return send(201,{order});
      }
      const cancel = /^\/api\/orders\/([a-f0-9-]+)\/cancel$/.exec(path);
      if (req.method==='POST' && cancel) {
        const result = db.prepare("UPDATE orders SET status='cancelled' WHERE id=? AND user_id=? AND status='pending'").run(cancel[1],user.id);
        if (!result.changes) fail(409,'Заказ не найден или уже обработан.'); return send(200,{ok:true});
      }
      if (path.startsWith('/api/admin/')) {
        if (!user.admin) fail(403,'Доступ только для администратора.');
        if (req.method==='GET' && path==='/api/admin/orders') return send(200,{orders:db.prepare('SELECT orders.*,users.username FROM orders JOIN users ON users.id=orders.user_id ORDER BY orders.created_at DESC').all()});
        const match = /^\/api\/admin\/orders\/([a-f0-9-]+)$/.exec(path);
        if (req.method==='POST' && match) {
          const b = await body(req); if (!['approve','reject'].includes(b.action)) fail(400,'Неизвестное действие.');
          db.exec('BEGIN IMMEDIATE');
          try {
            const order = db.prepare('SELECT * FROM orders WHERE id=?').get(match[1]);
            if (!order || order.status!=='pending') fail(409,'Заказ уже обработан или не найден.');
            const owner = db.prepare('SELECT * FROM users WHERE id=?').get(order.user_id);
            if (b.action==='approve') {
              if (order.product_id==='beta') { if (!owner.lifetime) fail(409,'Для Beta требуется Lifetime.'); db.prepare('UPDATE users SET beta=1 WHERE id=?').run(owner.id); }
              else if (order.product_id==='lifetime') db.prepare('UPDATE users SET lifetime=1,expires_at=NULL WHERE id=?').run(owner.id);
              else {
                if (owner.lifetime) fail(409,'У пользователя уже есть Lifetime.');
                const days = products.find(p=>p.id===order.product_id).days;
                const expiry = new Date(Math.max(now,Date.parse(owner.expires_at)||0)+days*86400000).toISOString();
                db.prepare('UPDATE users SET expires_at=? WHERE id=?').run(expiry,owner.id);
              }
            }
            db.prepare('UPDATE orders SET status=?,reviewed_by=?,reviewed_at=? WHERE id=?').run(b.action==='approve'?'approved':'rejected',user.id,new Date().toISOString(),order.id);
            db.exec('COMMIT'); return send(200,{ok:true});
          } catch(e) { db.exec('ROLLBACK'); throw e; }
        }
      }
      fail(404,'Страница не найдена.');
    } catch(e) { if (!res.headersSent) send(e.status||500,{error:e.status ? e.message : 'Ошибка сервера. Повторите попытку позже.'}); else res.end(); }
  });
  server.on('close',()=>db.close());
  return {server,db};
}
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const {server}=createStore();
  server.listen(Number(process.env.PORT||0),process.env.HOST||'127.0.0.1',()=>console.log(`Horus Store: http://${process.env.HOST||'127.0.0.1'}:${server.address().port}`));
}
