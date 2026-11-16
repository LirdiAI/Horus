import urllib.request
import json

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Accept": "application/json"
}

tables = ["profiles", "cloud_configs", "configs", "configurations", "shares", "user_configs", "cosmetics", "pulse_cosmetics", "tokens", "sessions", "news", "settings"]

for t in tables:
    try:
        req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/{t}?select=*&limit=1", headers=headers)
        with urllib.request.urlopen(req) as resp:
            data = resp.read().decode('utf-8')
            print(f"Table '{t}': EXISTS! {data[:80]}")
    except urllib.error.HTTPError as e:
        if e.code != 404:
            print(f"Table '{t}': code {e.code}")
    except Exception as e:
        pass
