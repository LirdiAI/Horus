import { openDatabase } from './server.mjs';
import { fileURLToPath } from 'node:url';
const username = process.argv[2];
if (!username) { console.error('Использование: npm run admin -- Логин'); process.exit(1); }
const db = openDatabase(fileURLToPath(new URL('./data/store.sqlite',import.meta.url)));
const result = db.prepare('UPDATE users SET admin=1 WHERE username=?').run(username);
db.close();
if (!result.changes) { console.error('Сначала зарегистрируйте этот аккаунт на сайте.'); process.exitCode=1; }
else console.log('Права администратора назначены указанному аккаунту.');
