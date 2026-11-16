import os
import re

app_path = r"C:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py"
with open(app_path, "r", encoding="utf-8") as f:
    code = f.read()

# Accounts management functions
accounts_helper = '''
def get_accounts_file():
    candidates = [
        LAUNCHER_DIR / "accounts.json",
        ROOT_DIR / "launcher" / "accounts.json",
        ROOT_DIR / "accounts.json"
    ]
    for c in candidates:
        if c.exists():
            return c
    return LAUNCHER_DIR / "accounts.json"

def load_accounts_data():
    fpath = get_accounts_file()
    if fpath.exists():
        try:
            with open(fpath, "r", encoding="utf-8") as f:
                data = json.load(f)
                if "accounts" in data and isinstance(data["accounts"], list):
                    return data
        except Exception:
            pass
    u = get_saved_username()
    return {
        "active": u,
        "accounts": [
            {"username": u, "role": "ADMIN", "avatar": f"https://mc-heads.net/avatar/{u}/64"},
            {"username": "Lumira", "role": "ADMIN", "avatar": "https://mc-heads.net/avatar/Lumira/64"}
        ]
    }

def save_accounts_data(data):
    fpath = get_accounts_file()
    try:
        fpath.parent.mkdir(parents=True, exist_ok=True)
        with open(fpath, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
    except Exception:
        pass
    if "active" in data:
        save_username(data["active"])

def query_game_bridge(endpoint, method="GET", payload=None):
    for p in [38241, 38242]:
        try:
            url = f"http://127.0.0.1:{p}{endpoint}"
            data_bytes = json.dumps(payload).encode("utf-8") if payload else None
            req = urllib.request.Request(url, data=data_bytes, headers={"Content-Type": "application/json", "User-Agent": "HorusLauncher"}, method=method)
            with urllib.request.urlopen(req, timeout=1.2) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except Exception:
            pass
    return None
'''

if "def load_accounts_data():" not in code:
    idx = code.find("def save_username(name):")
    next_def = code.find("\ndef ", idx + 20)
    code = code[:next_def] + "\n" + accounts_helper + "\n" + code[next_def:]

# In do_GET: add /api/accounts and enrich /api/status
get_accounts_endpoint = '''        if path == "/api/accounts":
            data = json.dumps(load_accounts_data(), ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        if path == "/api/status":
            bridge_status = query_game_bridge("/api/status")
            if bridge_status and bridge_status.get("online"):
                bridge_status["companion_mode"] = True
                data = json.dumps(bridge_status, ensure_ascii=False).encode("utf-8")
            else:
                resp = {
                    "status": "online",
                    "online": False,
                    "companion_mode": False,
                    "client": "Horus 1.21.11",
                    "version": "1.21.11",
                    "loader": "Fabric 0.19.3",
                    "root_dir": str(ROOT_DIR),
                    "launchable": True,
                    "server": "В главном меню",
                    "fps": 0,
                    "ping": 0,
                    "activeConfig": "funny",
                    "message": "Horus Launcher Bridge готов."
                }
                data = json.dumps(resp, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return
'''

if 'path == "/api/accounts"' not in code:
    code = re.sub(r'        if path == "/api/status":.*?return', get_accounts_endpoint.strip(), code, flags=re.DOTALL)

# In do_POST: add accounts CRUD and action proxying
post_endpoints = '''        if path.startswith("/api/action/"):
            action = path[len("/api/action/"):]
            game_resp = query_game_bridge(path, method="POST")
            if game_resp:
                resp = game_resp
            else:
                if action == "open-logs":
                    folder = ROOT_DIR / "logs"
                    if not folder.exists():
                        folder = Path(os.environ.get("APPDATA", "")) / ".minecraft" / "logs"
                    try:
                        os.startfile(folder)
                        resp = {"success": True, "message": "Папка логов открыта"}
                    except Exception as e:
                        resp = {"success": False, "error": str(e)}
                else:
                    resp = {"success": False, "error": "Игра не запущена"}
            data = json.dumps(resp, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        if path == "/api/accounts/add":
            length = int(self.headers.get('Content-Length', 0))
            payload = json.loads(self.rfile.read(length).decode('utf-8')) if length > 0 else {}
            new_user = payload.get("username", "").strip()
            acc_data = load_accounts_data()
            if new_user:
                names = [a["username"].lower() for a in acc_data["accounts"]]
                if new_user.lower() not in names:
                    acc_data["accounts"].append({
                        "username": new_user,
                        "role": "ADMIN",
                        "avatar": f"https://mc-heads.net/avatar/{new_user}/64"
                    })
                acc_data["active"] = new_user
                save_accounts_data(acc_data)
            data = json.dumps(acc_data, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        if path == "/api/accounts/select":
            length = int(self.headers.get('Content-Length', 0))
            payload = json.loads(self.rfile.read(length).decode('utf-8')) if length > 0 else {}
            sel_user = payload.get("username", "").strip()
            acc_data = load_accounts_data()
            if sel_user:
                acc_data["active"] = sel_user
                save_accounts_data(acc_data)
            data = json.dumps(acc_data, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        if path == "/api/accounts/remove":
            length = int(self.headers.get('Content-Length', 0))
            payload = json.loads(self.rfile.read(length).decode('utf-8')) if length > 0 else {}
            rem_user = payload.get("username", "").strip()
            acc_data = load_accounts_data()
            if rem_user and len(acc_data["accounts"]) > 1:
                acc_data["accounts"] = [a for a in acc_data["accounts"] if a["username"].lower() != rem_user.lower()]
                if acc_data["active"].lower() == rem_user.lower():
                    acc_data["active"] = acc_data["accounts"][0]["username"]
                save_accounts_data(acc_data)
            data = json.dumps(acc_data, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return
'''

if 'path == "/api/accounts/add"' not in code:
    idx = code.find("def do_POST(self):")
    idx_block = code.find("        path = self.path.split(\"?\")[0]", idx)
    insert_pos = code.find("\n", idx_block) + 1
    code = code[:insert_pos] + "\n" + post_endpoints + "\n" + code[insert_pos:]

with open(app_path, "w", encoding="utf-8") as f:
    f.write(code)

print("launcher_app.py successfully upgraded!")
