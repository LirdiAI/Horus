#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Lumira / Deluxe Client - Supabase Database Sync CLI Tool
Синхронизирует локальные файлы конфигураций, альт-аккаунтов и косметики с базой данных Supabase.
"""

import os
import sys
import json
import urllib.request
import urllib.error

CONFIGS_DIR = os.path.join(os.path.dirname(__file__), '..', 'run', 'config', 'deluxe', 'configs')
ALTS_FILE = os.path.join(os.path.dirname(__file__), '..', 'run', 'config', 'deluxe', 'altmanager.txt')
COSMETICS_FILE = os.path.join(os.path.dirname(__file__), '..', 'run', 'config', 'deluxe', 'cosmetics.json')

def post_to_supabase(url, key, table, payload):
    req_url = f"{url.rstrip('/')}/rest/v1/{table}"
    headers = {
        'apikey': key,
        'Authorization': f'Bearer {key}',
        'Content-Type': 'application/json',
        'Prefer': 'resolution=merge-duplicates'
    }
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(req_url, data=data, headers=headers, method='POST')
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status in (200, 201)
    except urllib.error.HTTPError as e:
        print(f"[-] HTTP Error {e.code}: {e.read().decode('utf-8')}")
        return False
    except Exception as e:
        print(f"[-] Connection Error: {e}")
        return False

def sync_configs(url, key, username="Nikita_2026"):
    if not os.path.isdir(CONFIGS_DIR):
        print(f"[-] Configs dir not found: {CONFIGS_DIR}")
        return
    print(f"\n[*] Синхронизация файлов конфигураций из {CONFIGS_DIR}...")
    for f in os.listdir(CONFIGS_DIR):
        if f.endswith('.json'):
            cfg_name = f[:-5]
            f_path = os.path.join(CONFIGS_DIR, f)
            size_kb = f"{round(os.path.getsize(f_path) / 1024, 1)} KB"
            try:
                with open(f_path, 'r', encoding='utf-8') as cf:
                    content = json.load(cf)
            except Exception:
                content = {}
            payload = {
                "username": username,
                "config_name": cfg_name,
                "config_data": content,
                "server_tag": "ReallyWorld" if "rw" in cfg_name.lower() else "All Servers",
                "file_size": size_kb,
                "is_public": True
            }
            ok = post_to_supabase(url, key, 'cloud_configs', [payload])
            print(f"  [+] Конфиг {f} ({size_kb}) -> {'Успешно' if ok else 'Ошибка'}")

def sync_alts(url, key, username="Nikita_2026"):
    if not os.path.isfile(ALTS_FILE):
        return
    print(f"\n[*] Синхронизация альт-аккаунтов из {ALTS_FILE}...")
    rows = []
    with open(ALTS_FILE, 'r', encoding='utf-8') as f:
        for line in f:
            parts = line.strip().split('\t')
            if parts and parts[0]:
                nick = parts[0]
                uuid = parts[1] if len(parts) > 1 else ""
                server = parts[3] if len(parts) > 3 else "Custom"
                rank = parts[4] if len(parts) > 4 else "игᴘоᴋ"
                rows.append({
                    "owner_username": username,
                    "username": nick,
                    "server": server,
                    "rank": rank,
                    "uuid": uuid
                })
    if rows:
        ok = post_to_supabase(url, key, 'alt_accounts', rows)
        print(f"  [+] Альт-аккаунты ({len(rows)} шт.) -> {'Успешно' if ok else 'Ошибка'}")

def main():
    print("=" * 60)
    print("  LUMIRA / DELUXE CLIENT - SUPABASE DB SYNC UTILITY")
    print("=" * 60)

    url = os.environ.get('SUPABASE_URL')
    key = os.environ.get('SUPABASE_KEY')

    if not url or not key:
        print("Введите URL вашего проекта Supabase (например: https://xyz.supabase.co):")
        url = input("URL: ").strip()
        print("Введите anon public key:")
        key = input("Key: ").strip()

    if not url or not key:
        print("[-] Ошибка: URL и ключ обязательны.")
        sys.exit(1)

    sync_configs(url, key)
    sync_alts(url, key)
    print("\n[✓] Синхронизация с Supabase завершена успешно!")

if __name__ == '__main__':
    main()
