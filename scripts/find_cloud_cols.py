import urllib.request
import json

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Accept": "application/json"
}

cols = ["id", "code", "share_code", "name", "config_name", "data", "config", "config_data", "content", "json", "username", "user", "creator", "created_at", "updated_at", "hwid", "key", "slug", "value"]

found = []
for c in cols:
    try:
        req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/cloud_configs?select={c}&limit=1", headers=headers)
        with urllib.request.urlopen(req) as resp:
            found.append(c)
    except urllib.error.HTTPError as e:
        pass
    except Exception:
        pass

print("Discovered columns in cloud_configs:", found)
