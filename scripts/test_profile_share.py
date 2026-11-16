import urllib.request
import json

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Accept": "application/json",
    "Content-Type": "application/json",
    "Prefer": "resolution=merge-duplicates,return=representation"
}

body = {
    "username": "share_H-7F2A",
    "email": "share_H-7F2A@horus.client",
    "role": "funny",
    "avatar_url": "{\"test\": 123}",
    "hwid": "CLOUD_SHARE"
}

try:
    req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/profiles", data=json.dumps(body).encode('utf-8'), headers=headers, method="POST")
    with urllib.request.urlopen(req) as resp:
        print("Success profiles insert:", resp.status, resp.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print("HTTP Error:", e.code, e.read().decode('utf-8'))
except Exception as e:
    print("Error:", e)
