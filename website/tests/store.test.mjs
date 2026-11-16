import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createStore } from '../server.mjs';

test('Аккаунты, защита, заказы, Lifetime и Beta, сохранение после перезапуска', async () => {
 const dir=mkdtempSync(join(tmpdir(),'horus-test-')), database=join(dir,'test.sqlite');
 let store=createStore({database}); await new Promise(r=>store.server.listen(0,'127.0.0.1',r));
 let base=`http://127.0.0.1:${store.server.address().port}`;
 function client(){let cookie='',csrf='';return async(path,method='GET',data)=>{const res=await fetch(base+path,{method,headers:{cookie,'Content-Type':'application/json','X-CSRF-Token':csrf},body:method==='POST'?JSON.stringify(data||{}):undefined});if(res.headers.get('set-cookie'))cookie=res.headers.get('set-cookie').split(';')[0];const json=await res.json();if(json.csrf)csrf=json.csrf;return {status:res.status,...json};};}
 const a=client(),b=client(),guest=client();
 try {
  assert.equal((await a('/api/session')).user,null);
  let result=await a('/api/register','POST',{username:'Tester',email:'test@example.com',password:'secure-test-1234'});assert.equal(result.status,200);assert.equal(result.user.lifetime,false);assert.equal(result.user.hwid,null);assert.equal(result.user.admin,false);
  assert.equal((await a('/api/orders','POST',{productId:'beta'})).status,409);
  assert.equal((await a('/api/admin/orders')).status,403);
  assert.equal((await guest('/api/orders')).status,401);
  const forged=await fetch(base+'/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:'{}'});assert.equal(forged.status,403);
  await b('/api/session');assert.equal((await b('/api/register','POST',{username:'Other',email:'other@example.com',password:'secure-other-1234'})).status,200);
  assert.equal((await b('/api/login','POST',{username:'Tester',password:'wrong-pass-1234'})).status,401);
  let order=(await a('/api/orders','POST',{productId:'lifetime',amount:1,lifetime:true})).order;assert.equal(order.amount,290);
  assert.equal((await a('/api/orders','POST',{productId:'30d'})).status,409);
  assert.equal((await b('/api/orders/'+order.id+'/cancel','POST')).status,409);
  assert.equal((await b('/api/orders')).orders.length,0);
  store.db.prepare('UPDATE users SET admin=1 WHERE username=?').run('Other');
  assert.equal((await b('/api/admin/orders/'+order.id,'POST',{action:'approve'})).status,200);
  assert.equal((await b('/api/admin/orders/'+order.id,'POST',{action:'approve'})).status,409);
  assert.equal((await a('/api/session')).user.lifetime,true);
  order=(await a('/api/orders','POST',{productId:'beta'})).order;assert.equal(order.amount,120);
  await b('/api/admin/orders/'+order.id,'POST',{action:'approve'});assert.equal((await a('/api/session')).user.beta,true);
  assert.equal((await a('/api/orders','POST',{productId:'beta'})).status,409);
  assert.equal((await a('/api/password','POST',{currentPassword:'not-correct-123',newPassword:'new-secure-1234'})).status,400);
  assert.equal((await a('/api/password','POST',{currentPassword:'secure-test-1234',newPassword:'new-secure-1234'})).status,200);
  await a('/api/logout','POST');assert.equal((await a('/api/orders')).status,401);
  assert.equal((await a('/api/login','POST',{username:'Tester',password:'secure-test-1234'})).status,401);
  assert.equal((await a('/api/login','POST',{username:'Tester',password:'new-secure-1234'})).status,200);
  assert.equal((await fetch(base+'/server.mjs')).status,404);assert.equal((await fetch(base+'/supabase-client.js')).status,404);
  await new Promise(r=>store.server.close(r));store=createStore({database});await new Promise(r=>store.server.listen(0,'127.0.0.1',r));base=`http://127.0.0.1:${store.server.address().port}`;
  assert.equal((await a('/api/session')).user.beta,true);assert.equal((await a('/api/orders')).orders.length,2);
 } finally {await new Promise(r=>store.server.close(r));rmSync(dir,{recursive:true,force:true});}
});
