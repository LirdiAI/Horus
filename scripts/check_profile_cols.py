import urllib.request
import json

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Accept": "application/json"
}

req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/profiles?select=*&limit=1", headers=headers)
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode('utf-8'))
    if data:
        print("Profiles columns:", list(data[0].keys()))
        print("Profiles sample:", data[0])
