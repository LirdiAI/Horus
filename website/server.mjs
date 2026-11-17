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
    CREATE TABLE IF NOT EXISTS sessions(token TEXT PRIMARY KEY,user_id TEXT,csrf TEXT NOT NULL,expires INTEGER NOT NULL);
    CREATE TABLE IF NOT EXISTS orders(id TEXT PRIMARY KEY,user_id TEXT NOT NULL REFERENCES users(id),product_id TEXT NOT NULL,product_name TEXT NOT NULL,amount INTEGER NOT NULL,status TEXT NOT NULL DEFAULT 'pending',created_at TEXT NOT NULL,reviewed_by TEXT REFERENCES users(id),reviewed_at TEXT);
    CREATE TABLE IF NOT EXISTS limits(key TEXT PRIMARY KEY,count INTEGER NOT NULL,until INTEGER NOT NULL);`);
  return db;
}
const userView = u => u ? { id:u.id, username:u.username, email:u.email, created_at:u.created_at, hwid:u.hwid, lifetime:!!u.lifetime, expires_at:u.expires_at, beta:!!u.beta, admin:!!u.admin, frozen:!!u.frozen, avatar_url:u.avatar_url||null, banner_url:u.banner_url||null } : null;
async function passwordHash(password) {
  const salt = randomBytes(16).toString('hex');
  return salt + ':' + (await scrypt(password, salt, 64)).toString('hex');
}
async function passwordMatches(password, stored) {
  const [salt, digest] = stored.split(':');
  return timingSafeEqual(Buffer.from(digest, 'hex'), await scrypt(password, salt, 64));
}

// ===== Supabase REST storage: аккаунты и заказы магазина живут в Supabase =====
const SB_URL = process.env.SUPABASE_URL || 'https://swarweprhwoelirrmztv.supabase.co/rest/v1';
const SB_KEY = process.env.SUPABASE_KEY || 'sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy';
const userCache = new Map(); // id -> {u,t}: короткий кэш, чтобы не ходить в Supabase на каждый запрос
async function sb(method, path, bodyObj, prefer) {
  const headers = { apikey: SB_KEY, Authorization: `Bearer ${SB_KEY}`, 'Content-Type': 'application/json' };
  if (prefer) headers.Prefer = prefer;
  const res = await fetch(SB_URL + path, { method, headers, body: bodyObj === undefined ? undefined : JSON.stringify(bodyObj) });
  const text = await res.text();
  if (!res.ok) {
    if (res.status === 409 || (res.status === 422 && /duplicate|unique/i.test(text))) { const e = new Error('UNIQUE'); e.code = 'UNIQUE'; throw e; }
    throw new Error(`Supabase ${res.status} ${text.slice(0, 200)}`);
  }
  return text ? JSON.parse(text) : null;
}
const sbRows = async path => (await sb('GET', path)) || [];
const sbFirst = async path => (await sbRows(path))[0] || null;
const sbUserById = async id => sbFirst(`/store_users?id=eq.${encodeURIComponent(id)}&select=*`);
const sbUserByName = async name => sbFirst(`/store_users?username=eq.${encodeURIComponent(name)}&select=*`);
async function sbUserCached(id) {
  const hit = userCache.get(id);
  if (hit && Date.now() - hit.t < 10000) return hit.u;
  const u = await sbUserById(id);
  if (u) userCache.set(id, { u, t: Date.now() }); else userCache.delete(id);
  return u;
}
const sbUserPut = u => { if (u) userCache.set(u.id, { u, t: Date.now() }); };
function fail(status, message) { throw Object.assign(new Error(message), { status }); }
async function body(req, max = 8192) {
  if (!req.headers['content-type']?.startsWith('application/json')) fail(415, 'Ожидается JSON.');
  let data = ''; for await (const chunk of req) { data += chunk; if (data.length > max) fail(413, 'Слишком большой запрос.'); }
  try { const value = JSON.parse(data); if (!value || Array.isArray(value) || typeof value !== 'object') throw Error(); return value; }
  catch { fail(400, 'Некорректный запрос.'); }
}
export function createStore({ database = join(root, 'data/sessions.sqlite'), secure = process.env.NODE_ENV === 'production' } = {}) {
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
    res.setHeader('Content-Security-Policy', "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'");
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
      let user = session.user_id ? await sbUserCached(session.user_id) : null;
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
          try { await sb('POST', '/store_users', { id:randomUUID(), username, email, password:digest, created_at:new Date().toISOString() }, 'return=minimal'); }
          catch (e) { if (e.code === 'UNIQUE') fail(409,'Логин или почта уже используются.'); throw e; }
        }
        user = await sbUserByName(username);
        // Bootstrap-админка для хостинга (Render): если ADMIN_USER совпадает с логином — выдаём права
        if (user && !user.admin && process.env.ADMIN_USER && username.toLowerCase() === process.env.ADMIN_USER.toLowerCase()) {
          await sb('PATCH', `/store_users?id=eq.${encodeURIComponent(user.id)}`, { admin:true }, 'return=minimal');
          user = { ...user, admin:true }; sbUserPut(user);
        }
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
        await sb('PATCH', `/store_users?id=eq.${encodeURIComponent(user.id)}`, { password:digest }, 'return=minimal');
        db.prepare('DELETE FROM sessions WHERE user_id=? AND token<>?').run(user.id,session.token); return send(200,{ok:true});
      }
      const imgMatch = /^\/api\/profile\/(avatar|banner)$/.exec(path);
      if (req.method === 'POST' && imgMatch) {
        limited('img:'+user.id,15);
        const b = await body(req, 560000);
        const img = typeof b.image === 'string' ? b.image : '';
        if (!/^data:image\/(png|jpeg|webp);base64,[A-Za-z0-9+/=]+$/.test(img)) fail(400,'Нужна картинка PNG, JPEG или WebP.');
        if (img.length > (imgMatch[1]==='avatar' ? 300000 : 540000)) fail(400,'Картинка слишком большая. Сожмите её или выберите файл поменьше.');
        const field = imgMatch[1]==='avatar' ? 'avatar_url' : 'banner_url';
        await sb('PATCH', `/store_users?id=eq.${encodeURIComponent(user.id)}`, { [field]:img }, 'return=minimal');
        user = { ...user, [field]:img }; sbUserPut(user); return send(200,{ok:true});
      }
      if (req.method === 'POST' && path === '/api/profile/clear-image') {
        const b = await body(req); const field = b.field==='avatar' ? 'avatar_url' : b.field==='banner' ? 'banner_url' : null;
        if (!field) fail(400,'Неизвестное поле.');
        await sb('PATCH', `/store_users?id=eq.${encodeURIComponent(user.id)}`, { [field]:null }, 'return=minimal');
        user = { ...user, [field]:null }; sbUserPut(user); return send(200,{ok:true});
      }
      if (req.method === 'POST' && path === '/api/promo/validate') {
        const b = await body(req); const code = String(b.code||'').trim().toUpperCase();
        if (!code) fail(400,'Введите промокод.');
        const promo = await sbFirst(`/store_promos?code=eq.${encodeURIComponent(code)}&select=*`);
        if (!promo) fail(400,'Промокод не найден.');
        if (promo.uses_left <= 0) fail(409,'Промокод больше не действует.');
        if (promo.expires_at && Date.parse(promo.expires_at) < now) fail(409,'Срок действия промокода истёк.');
        return send(200,{discount_percent:promo.discount_percent});
      }
      if (req.method === 'GET' && path === '/api/orders') return send(200,{orders:await sbRows(`/store_orders?user_id=eq.${encodeURIComponent(user.id)}&select=*&order=created_at.desc`)});
      if (req.method === 'POST' && path === '/api/orders') {
        if (user.frozen) fail(403,'Аккаунт заморожен администрацией. Обратитесь к владельцу магазина.');
        limited('order:'+user.id,30); const b = await body(req); const p = products.find(p=>p.id===b.productId);
        if (!p) fail(400,'Тариф не найден.');
        if (p.id==='beta' && !user.lifetime) fail(409,'Beta доступна только владельцам Lifetime.');
        if ((p.id==='beta' && user.beta) || (p.id!=='beta' && user.lifetime)) fail(409,'Этот доступ уже есть у вашего аккаунта.');
        const existing = await sbFirst(`/store_orders?user_id=eq.${encodeURIComponent(user.id)}&status=eq.pending&select=id`);
        if (existing) fail(409,'У вас уже есть заказ на рассмотрении. Отмените его в профиле или дождитесь подтверждения.');
        const promoCode = String(b.promo||'').trim().toUpperCase();
        let discount = 0;
        if (promoCode) {
          const promo = await sbFirst(`/store_promos?code=eq.${encodeURIComponent(promoCode)}&select=*`);
          if (!promo) fail(400,'Промокод не найден.');
          if (promo.uses_left <= 0) fail(409,'Промокод больше не действует.');
          if (promo.expires_at && Date.parse(promo.expires_at) < now) fail(409,'Срок действия промокода истёк.');
          discount = promo.discount_percent;
        }
        const amount = Math.max(0, p.price - Math.round(p.price * discount / 100));
        const order = {id:randomUUID(),user_id:user.id,username:user.username,product_id:p.id,product_name:p.name,amount,discount,promo_code:discount?promoCode:null,status:'pending',created_at:new Date().toISOString()};
        await sb('POST', '/store_orders', order, 'return=minimal');
        if (discount) await sb('PATCH', `/store_promos?code=eq.${encodeURIComponent(promoCode)}&uses_left=gt.0`, { uses_left:Math.max(0,promo.uses_left-1) }, 'return=minimal');
        return send(201,{order});
      }
      const cancel = /^\/api\/orders\/([a-f0-9-]+)\/cancel$/.exec(path);
      if (req.method==='POST' && cancel) {
        const rows = await sb('PATCH', `/store_orders?id=eq.${cancel[1]}&user_id=eq.${encodeURIComponent(user.id)}&status=eq.pending`, { status:'cancelled' }, 'return=representation');
        if (!rows || !rows.length) fail(409,'Заказ не найден или уже обработан.'); return send(200,{ok:true});
      }
      if (path.startsWith('/api/admin/')) {
        if (!user.admin) fail(403,'Доступ только для администратора.');
        if (req.method==='GET' && path==='/api/admin/promos') return send(200,{promos:await sbRows('/store_promos?select=*&order=created_at.desc&limit=50')});
        if (req.method==='POST' && path==='/api/admin/promos') {
          limited('promo:'+user.id,30); const b = await body(req);
          const code = String(b.code||'').trim().toUpperCase();
          if (!/^[A-Z0-9_-]{3,24}$/.test(code)) fail(400,'Код: 3–24 символа — латиница, цифры, _ или -.');
          const discount = Math.floor(Number(b.discount));
          if (!Number.isFinite(discount) || discount < 1 || discount > 90) fail(400,'Скидка — целое число от 1 до 90%.');
          const uses = Math.max(1, Math.min(9999, Math.floor(Number(b.uses)||1)));
          const days = Math.max(0, Math.min(365, Math.floor(Number(b.days)||0)));
          const expires_at = days ? new Date(now + days*86400000).toISOString() : null;
          try { await sb('POST', '/store_promos', { code, discount_percent:discount, uses_left:uses, expires_at, created_by:user.username }, 'return=minimal'); }
          catch(e) { if (e.code==='UNIQUE') fail(409,'Такой промокод уже существует.'); throw e; }
          return send(201,{ok:true});
        }
        if (req.method==='POST' && path==='/api/admin/promos/delete') {
          const b = await body(req); const code = String(b.code||'').trim().toUpperCase();
          if (!/^[A-Z0-9_-]{3,24}$/.test(code)) fail(400,'Некорректный код.');
          await sb('DELETE', `/store_promos?code=eq.${encodeURIComponent(code)}`); return send(200,{ok:true});
        }
        if (req.method==='POST' && path==='/api/admin/grant') {
          const b = await body(req); const target = await sbUserByName(String(b.username||'').trim());
          if (!target) fail(404,'Аккаунт с таким логином не найден.');
          let patch;
          if (b.product==='beta') patch = { beta:true };
          else if (b.product==='lifetime') patch = { lifetime:true, expires_at:null };
          else {
            const p = products.find(p=>p.id===b.product);
            if (!p) fail(400,'Тариф не найден.');
            patch = { expires_at:new Date(Math.max(now, Date.parse(target.expires_at)||0) + p.days*86400000).toISOString(), lifetime:false };
          }
          await sb('PATCH', `/store_users?id=eq.${encodeURIComponent(target.id)}`, patch, 'return=minimal');
          sbUserPut({ ...target, ...patch });
          return send(200,{ok:true, username:target.username});
        }
        if (req.method==='POST' && path==='/api/admin/freeze') {
          const b = await body(req); const target = await sbUserByName(String(b.username||'').trim());
          if (!target) fail(404,'Аккаунт с таким логином не найден.');
          if (target.id === user.id) fail(400,'Нельзя заморозить собственный аккаунт.');
          await sb('PATCH', `/store_users?id=eq.${encodeURIComponent(target.id)}`, { frozen:!!b.frozen }, 'return=minimal');
          sbUserPut({ ...target, frozen:!!b.frozen });
          return send(200,{ok:true, frozen:!!b.frozen, username:target.username});
        }
        if (req.method==='GET' && path==='/api/admin/orders') return send(200,{orders:await sbRows('/store_orders?select=*&order=created_at.desc')});
        const match = /^\/api\/admin\/orders\/([a-f0-9-]+)$/.exec(path);
        if (req.method==='POST' && match) {
          const b = await body(req); if (!['approve','reject'].includes(b.action)) fail(400,'Неизвестное действие.');
          const order = await sbFirst(`/store_orders?id=eq.${match[1]}&select=*`);
          if (!order || order.status!=='pending') fail(409,'Заказ уже обработан или не найден.');
          const owner = await sbUserById(order.user_id);
          if (!owner) fail(409,'Аккаунт пользователя не найден.');
          let ownerPatch = null;
          if (b.action==='approve') {
            if (order.product_id==='beta') { if (!owner.lifetime) fail(409,'Для Beta требуется Lifetime.'); ownerPatch = { beta:true }; }
            else if (order.product_id==='lifetime') ownerPatch = { lifetime:true, expires_at:null };
            else {
              if (owner.lifetime) fail(409,'У пользователя уже есть Lifetime.');
              const days = products.find(p=>p.id===order.product_id).days;
              ownerPatch = { expires_at:new Date(Math.max(now,Date.parse(owner.expires_at)||0)+days*86400000).toISOString() };
            }
            await sb('PATCH', `/store_users?id=eq.${encodeURIComponent(owner.id)}`, ownerPatch, 'return=minimal');
            sbUserPut({ ...owner, ...ownerPatch });
          }
          const patched = await sb('PATCH', `/store_orders?id=eq.${order.id}&status=eq.pending`, { status:b.action==='approve'?'approved':'rejected', reviewed_by:user.id, reviewed_at:new Date().toISOString() }, 'return=representation');
          if (!patched || !patched.length) fail(409,'Заказ уже обработан или не найден.');
          return send(200,{ok:true});
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
