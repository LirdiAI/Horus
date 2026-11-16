-- ============================================================
-- HorusClient Store: аккаунты и заказы магазина в Supabase
-- Run this in: Supabase Dashboard -> SQL Editor -> New query
-- (нужно выполнить ОДИН раз; парти-таблицы — в party_supabase.sql)
-- ============================================================

create extension if not exists citext;

-- Аккаунты магазина (пароли в формате salt:scrypt, как раньше)
create table if not exists store_users (
  id uuid primary key,
  username citext not null unique,
  email citext not null unique,
  password text not null,
  created_at text not null,
  hwid text,
  lifetime boolean not null default false,
  expires_at text,
  beta boolean not null default false,
  admin boolean not null default false
);

-- Заказы (username дублируется для списка админки без JOIN)
create table if not exists store_orders (
  id uuid primary key,
  user_id uuid not null,
  username text not null default '',
  product_id text not null,
  product_name text not null,
  amount integer not null,
  status text not null default 'pending',
  created_at text not null,
  reviewed_by text,
  reviewed_at text
);

-- publishable-ключ клиента = роль anon; как и раньше в store.sqlite —
-- безопасность обеспечивается сервером сайта (сессии/CSRF/лимиты)
alter table store_users enable row level security;
alter table store_orders enable row level security;

drop policy if exists "anon all store_users" on store_users;
create policy "anon all store_users" on store_users for all to anon using (true) with check (true);

drop policy if exists "anon all store_orders" on store_orders;
create policy "anon all store_orders" on store_orders for all to anon using (true) with check (true);
