import urllib.request
import json

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Accept": "application/json"
}

# Test querying tables
for table in ["configs", "cloud_configs", "profiles"]:
    try:
        req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/{table}?select=*&limit=1", headers=headers)
        with urllib.request.urlopen(req) as resp:
            print(f"Table '{table}': {resp.status}, response: {resp.read().decode('utf-8')[:100]}")
    except Exception as e:
        print(f"Table '{table}': error {e}")
