import urllib.request
import json

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

headers = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Accept": "application/json"
}

req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/?apikey={SUPABASE_KEY}", headers=headers)
with urllib.request.urlopen(req) as resp:
    schema = json.loads(resp.read().decode('utf-8'))
    print("Definitions:", list(schema.get("definitions", {}).keys()))
    if "cloud_configs" in schema.get("definitions", {}):
        print("cloud_configs properties:", json.dumps(schema["definitions"]["cloud_configs"], indent=2))
