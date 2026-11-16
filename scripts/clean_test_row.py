import urllib.request
SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"
headers = {"apikey": SUPABASE_KEY, "Authorization": f"Bearer {SUPABASE_KEY}"}
try:
    req = urllib.request.Request(f"{SUPABASE_URL}/rest/v1/profiles?username=eq.share_H-7F2A", headers=headers, method="DELETE")
    with urllib.request.urlopen(req) as resp:
        print("Cleaned test row:", resp.status)
except Exception as e:
    print("Delete error:", e)
