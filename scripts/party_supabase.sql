-- ============================================================
-- Horus Party: таблицы для системы пати (Supabase SQL Editor)
-- Run this in: Supabase Dashboard -> SQL Editor -> New query
-- ============================================================

create table if not exists party_meta (
  code text primary key,
  owner text not null,
  locked boolean not null default false,
  created_at timestamptz not null default now()
);

create table if not exists party_members (
  id uuid primary key default gen_random_uuid(),
  code text not null,
  nick text not null,
  host text not null default '',
  dim text not null default '',
  x real not null default 0,
  y real not null default 0,
  z real not null default 0,
  hp real not null default 0,
  last_seen timestamptz not null default now(),
  unique (code, nick)
);

create table if not exists party_marks (
  id uuid primary key default gen_random_uuid(),
  code text not null,
  type text not null,
  target text not null default '',
  created_by text not null,
  host text not null default '',
  dim text not null default '',
  x real not null default 0,
  y real not null default 0,
  z real not null default 0,
  created_at timestamptz not null default now()
);

-- публичный (publishable) ключ клиента = роль anon; разрешаем ей всё,
-- как и в старом PHP-бэкенде (безопасность = секретность кода пати)
alter table party_meta enable row level security;
alter table party_members enable row level security;
alter table party_marks enable row level security;

drop policy if exists "anon all party_meta" on party_meta;
create policy "anon all party_meta" on party_meta for all to anon using (true) with check (true);

drop policy if exists "anon all party_members" on party_members;
create policy "anon all party_members" on party_members for all to anon using (true) with check (true);

drop policy if exists "anon all party_marks" on party_marks;
create policy "anon all party_marks" on party_marks for all to anon using (true) with check (true);
