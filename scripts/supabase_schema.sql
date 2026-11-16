-- ==============================================================================
--   LUMIRA CREATED / DELUXE CLIENT EDITION v2.0 - SUPABASE DATABASE SCHEMA
-- ==============================================================================
-- Скопируйте и выполните этот скрипт в SQL Editor вашего проекта Supabase:
-- https://supabase.com/dashboard/project/_/sql

-- 1. Таблица профилей пользователей
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT UNIQUE NOT NULL,
    email TEXT,
    password_hash TEXT,
    role TEXT DEFAULT 'ELITE',
    balance NUMERIC DEFAULT 1000,
    hwid TEXT,
    subscription_till TEXT DEFAULT '01.01.2048',
    avatar_url TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Таблица облачных конфигураций (Cloud Configs)
CREATE TABLE IF NOT EXISTS public.cloud_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT NOT NULL,
    config_name TEXT NOT NULL,
    config_data JSONB,
    server_tag TEXT DEFAULT 'All Servers',
    file_size TEXT DEFAULT '12 KB',
    share_code TEXT,
    is_public BOOLEAN DEFAULT false,
    downloads INTEGER DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (username, config_name)
);

-- 3. Таблица кастомизации и косметики (User Cosmetics)
CREATE TABLE IF NOT EXISTS public.user_cosmetics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT UNIQUE NOT NULL,
    cosmetics JSONB DEFAULT '{}'::jsonb,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 4. Таблица логов аудита и действий (Audit Logs)
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT NOT NULL,
    event_type TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 5. Таблица аккаунтов лаунчера (Launcher Accounts)
CREATE TABLE IF NOT EXISTS public.launcher_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT UNIQUE NOT NULL,
    accounts_json JSONB DEFAULT '[]'::jsonb,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ==============================================================================
-- Отключение RLS (Row Level Security) для открытого демо-доступа по Anon Key
-- ==============================================================================
ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.cloud_configs DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_cosmetics DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.launcher_accounts DISABLE ROW LEVEL SECURITY;

-- Создание дефолтного админ-профиля
INSERT INTO public.profiles (username, email, role, balance, hwid, subscription_till)
VALUES ('Nikita', 'nikita@lumira.client', 'ADMIN', 7777, 'WIN11-D8A4-9F2B-HORUS', '01.01.2048')
ON CONFLICT (username) DO NOTHING;

-- Подтверждение успешного создания
SELECT 'Схема базы данных Lumira успешно создана!' AS status;
