-- Хорус магазин: расширение — промокоды, баннер/аватар, заморозка
-- Запустить в Supabase SQL Editor (после store_supabase.sql)

alter table store_users add column if not exists avatar_url text;
alter table store_users add column if not exists banner_url text;
alter table store_users add column if not exists frozen boolean not null default false;

create table if not exists store_promos (
  code text primary key,
  discount_percent int not null check (discount_percent between 1 and 90),
  uses_left int not null default 1 check (uses_left >= 0),
  expires_at timestamptz,
  created_by text,
  created_at timestamptz default now()
);

alter table store_promos enable row level security;
drop policy if exists "anon all store_promos" on store_promos;
create policy "anon all store_promos" on store_promos for all to anon using (true) with check (true);

alter table store_orders add column if not exists promo_code text;
alter table store_orders add column if not exists discount int not null default 0;
