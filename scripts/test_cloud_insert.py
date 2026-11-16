import urllib.request
import json

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Accept": "application/json",
    "Content-Type": "application/json",
    "Prefer": "return=representation"
}

payload = {
    "share_code": "H-7F2A",
    "config_name": "funny",
    "config_data": "{\"modules\": {}}",
    "username": "Lumira"
}

try:
    req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/cloud_configs", data=json.dumps(payload).encode('utf-8'), headers=headers, method="POST")
    with urllib.request.urlopen(req) as resp:
        print("Success inserting:", resp.status, resp.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print("HTTP Error:", e.code, e.read().decode('utf-8'))
except Exception as e:
    print("Error:", e)
