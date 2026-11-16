-- ==============================================================================
--   LUMIRA CREATED / DELUXE CLIENT EDITION - SUPABASE DATABASE SCHEMA
-- ==============================================================================
--   Инструкция по установке:
--   1. Откройте ваш проект в https://supabase.com
--   2. Перейдите в раздел "SQL Editor" слева
--   3. Вставьте содержимое этого файла и нажмите "Run" (Запустить)
-- ==============================================================================

-- 1. ТАБЛИЦА ПОЛЬЗОВАТЕЛЕЙ И ПРОФИЛЕЙ (PROFILES)
create table if not exists public.profiles (
    id uuid default gen_random_uuid() primary key,
    username text unique not null,
    email text,
    password_hash text,
    uid serial,
    role text default 'ELITE',
    balance integer default 1250,
    hwid text default 'WIN11-D8A4-9F2B-HORUS',
    subscription_till text default '01.01.2048',
    avatar_url text,
    created_at timestamp with time zone default timezone('utc'::text, now()),
    updated_at timestamp with time zone default timezone('utc'::text, now())
);

-- 2. ТАБЛИЦА ОБЛАЧНЫХ КОНФИГУРАЦИЙ (CLOUD CONFIGS)
create table if not exists public.cloud_configs (
    id uuid default gen_random_uuid() primary key,
    username text not null,
    config_name text not null,
    config_data jsonb not null default '{}'::jsonb,
    server_tag text default 'All Servers',
    file_size text default '12.4 KB',
    downloads integer default 0,
    share_code text,
    is_public boolean default true,
    created_at timestamp with time zone default timezone('utc'::text, now()),
    updated_at timestamp with time zone default timezone('utc'::text, now()),
    constraint unique_user_config unique (username, config_name)
);

-- 3. ТАБЛИЦА АЛЬТ-АККАУНТОВ (ALT ACCOUNTS)
create table if not exists public.alt_accounts (
    id uuid default gen_random_uuid() primary key,
    owner_username text not null,
    username text not null,
    server text default 'Whitegrief',
    rank text default 'игᴘоᴋ',
    uuid text,
    created_at timestamp with time zone default timezone('utc'::text, now())
);

-- 4. ТАБЛИЦА КАСТОМИЗАЦИИ И ПЛАЩЕЙ (USER COSMETICS)
create table if not exists public.user_cosmetics (
    id uuid default gen_random_uuid() primary key,
    username text unique not null,
    cosmetics jsonb default '{"cape": true, "wings": true, "halo": false, "pet": false}'::jsonb,
    updated_at timestamp with time zone default timezone('utc'::text, now())
);

-- 5. ТАБЛИЦА ЛОГОВ АКТИВНОСТИ И ИНЪЕКЦИЙ (AUDIT LOGS)
create table if not exists public.launcher_logs (
    id uuid default gen_random_uuid() primary key,
    username text,
    event_type text not null, -- 'LAUNCH', 'CONFIG_LOAD', 'LOGIN', 'HWID_RESET'
    details text,
    client_version text default '2.0',
    created_at timestamp with time zone default timezone('utc'::text, now())
);

-- ==============================================================================
--   НАСТРОЙКА БЕЗОПАСНОСТИ ROW LEVEL SECURITY (RLS)
--   Разрешаем публичный доступ (через anon key) для быстрого старта клиента
-- ==============================================================================

alter table public.profiles enable row level security;
alter table public.cloud_configs enable row level security;
alter table public.alt_accounts enable row level security;
alter table public.user_cosmetics enable row level security;
alter table public.launcher_logs enable row level security;

-- Политики для Profiles
drop policy if exists "Public profiles access" on public.profiles;
create policy "Public profiles access" on public.profiles for all using (true) with check (true);

-- Политики для Cloud Configs
drop policy if exists "Public configs access" on public.cloud_configs;
create policy "Public configs access" on public.cloud_configs for all using (true) with check (true);

-- Политики для Alt Accounts
drop policy if exists "Public alt accounts access" on public.alt_accounts;
create policy "Public alt accounts access" on public.alt_accounts for all using (true) with check (true);

-- Политики для Cosmetics
drop policy if exists "Public cosmetics access" on public.user_cosmetics;
create policy "Public cosmetics access" on public.user_cosmetics for all using (true) with check (true);

-- Политики для Logs
drop policy if exists "Public logs access" on public.launcher_logs;
create policy "Public logs access" on public.launcher_logs for all using (true) with check (true);

-- ==============================================================================
--   НАЧАЛЬНЫЕ ДАННЫЕ (SEED DATA)
-- ==============================================================================

-- Дефолтный премиум профиль Nikita_2026
insert into public.profiles (username, email, role, balance, hwid, subscription_till)
values ('Nikita_2026', 'nikita@lumira.client', 'ELITE', 1250, 'WIN11-D8A4-9F2B-HORUS', '01.01.2048')
on conflict (username) do update 
set role = 'ELITE', balance = 1250, subscription_till = '01.01.2048';

-- Дефолтные конфигурации ReallyWorld и FunTime
insert into public.cloud_configs (username, config_name, server_tag, file_size, downloads, share_code, config_data)
values 
  ('Nikita_2026', 'rw', 'ReallyWorld', '12.4 KB', 142, 'HORUS-CFG-RW-9921', '{"AttackAura": {"enabled": true}, "HitBoxes": {"enabled": true}, "AutoAnchor": {"enabled": true}}'::jsonb),
  ('Nikita_2026', 'sp', 'FunTime', '14.1 KB', 89, 'HORUS-CFG-FT-4122', '{"AutoSpear": {"enabled": true}, "FastElytra": {"enabled": true}, "MaceHelper": {"enabled": true}}'::jsonb),
  ('Nikita_2026', 'default', 'All Servers', '8.2 KB', 55, 'HORUS-CFG-DEF-1001', '{"AimAssist": {"enabled": true}, "AutoSprint": {"enabled": true}}'::jsonb)
on conflict (username, config_name) do nothing;

-- Дефолтные альт-аккаунты из altmanager.txt
insert into public.alt_accounts (owner_username, username, server, rank, uuid)
values
  ('Nikita_2026', 'Lumira', 'Whitegrief', 'ꔶ', 'ccb50d5c-36bc-355a-8dc7-581398d66db8'),
  ('Nikita_2026', 'HorusPegas', 'Whitegrief', 'ʜʏᴅʀᴀ', '7235ce35-1000-3e37-89b9-d5b7d81d868b'),
  ('Nikita_2026', 'Nikita_2026', 'All Servers', '👑 ELITE', '461fbe3e-6ffe-4a62-9e70-0da80a564412'),
  ('Nikita_2026', 'NoMakoron', 'Bvx', 'игᴘоᴋ', '405f2f3a-d219-3e97-98d0-ecf69246d793'),
  ('Nikita_2026', 'Makoronov', 'Bvx', '7игᴘоᴋ', '8c9b6270-b745-3586-af13-6ae2ea6e2aff');

-- Дефолтная кастомизация
insert into public.user_cosmetics (username, cosmetics)
values ('Nikita_2026', '{"cape": true, "wings": true, "halo": false, "pet": false}'::jsonb)
on conflict (username) do nothing;
