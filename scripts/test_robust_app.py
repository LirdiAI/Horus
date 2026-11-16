import os
import sys
import json
import time
import socket
import urllib.parse
import threading
import subprocess
import webbrowser
from http.server import HTTPServer, SimpleHTTPRequestHandler
from pathlib import Path

PORT = 38241

def resolve_root_dir():
    if getattr(sys, 'frozen', False):
        exe_dir = Path(sys.executable).resolve().parent
    else:
        exe_dir = Path(__file__).resolve().parent

    for candidate in [exe_dir, exe_dir.parent, Path.cwd().resolve(), Path(r"c:\Users\Никита\Downloads\HorusClient")]:
        if (candidate / "gradlew.bat").exists() or (candidate / "launcher" / "index.html").exists():
            return candidate
    return exe_dir

ROOT_DIR = resolve_root_dir()
LAUNCHER_DIR = ROOT_DIR / "launcher"
BUNDLE_DIR = Path(getattr(sys, '_MEIPASS', LAUNCHER_DIR))

last_activity = time.time()
shutdown_event = threading.Event()

class ResilientServer(HTTPServer):
    allow_reuse_address = True

class LauncherRequestHandler(SimpleHTTPRequestHandler):
    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors_headers()
        self.end_headers()

    def do_POST(self):
        global last_activity
        last_activity = time.time()
        path = self.path.split("?")[0]

        if path == "/api/exit":
            self.send_response(200)
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(b'{"bye": true}')
            def delayed_exit():
                time.sleep(0.5)
                shutdown_event.set()
            threading.Thread(target=delayed_exit, daemon=True).start()
            return

        if path == "/api/heartbeat":
            self.send_response(200)
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(b'{"ok": true}')
            return

        if path == "/api/launch":
            bat_path = ROOT_DIR / "launch_game.bat"
            if not bat_path.exists():
                bat_path = LAUNCHER_DIR / "launch_game.bat"

            if bat_path.exists():
                try:
                    subprocess.Popen(
                        ["cmd.exe", "/c", str(bat_path)],
                        cwd=str(ROOT_DIR),
                        creationflags=subprocess.CREATE_NEW_CONSOLE
                    )
                    resp = {"success": True, "launched": True, "message": "Запуск игрового процесса..."}
                except Exception as e:
                    resp = {"success": False, "error": str(e)}
            else:
                resp = {"success": False, "error": f"launch_game.bat not found in {ROOT_DIR}"}

            data = json.dumps(resp, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        self.send_response(404)
        self.end_headers()

    def do_GET(self):
        global last_activity
        last_activity = time.time()
        path = self.path.split("?")[0]

        if path == "/api/status":
            resp = {
                "status": "online",
                "client": "Horus 1.21.11",
                "version": "1.21.11",
                "loader": "Fabric 0.19.3",
                "root_dir": str(ROOT_DIR),
                "launchable": True,
                "message": "Horus Launcher Bridge активен."
            }
            data = json.dumps(resp, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        if path == "/" or path == "/launcher" or path == "/launcher/":
            target = LAUNCHER_DIR / "index.html"
            if not target.exists():
                target = BUNDLE_DIR / "index.html"
            self.serve_file(target, "text/html; charset=utf-8")
            return

        clean = path.lstrip("/")
        if clean.startswith("launcher/"):
            clean = clean[len("launcher/"):]

        target = LAUNCHER_DIR / clean
        if not target.exists():
            target = BUNDLE_DIR / clean
        if not target.exists():
            target = ROOT_DIR / clean

        if target.exists() and target.is_file():
            mime = self.guess_mime(str(target))
            self.serve_file(target, mime)
            return

        self.send_response(404)
        self.end_headers()

    def guess_mime(self, filename):
        ext = os.path.splitext(filename)[1].lower()
        mimes = {
            ".html": "text/html; charset=utf-8",
            ".js": "application/javascript; charset=utf-8",
            ".css": "text/css; charset=utf-8",
            ".json": "application/json; charset=utf-8",
            ".png": "image/png",
            ".jpg": "image/jpeg",
            ".jpeg": "image/jpeg",
            ".ico": "image/x-icon",
            ".svg": "image/svg+xml",
            ".txt": "text/plain; charset=utf-8",
        }
        return mimes.get(ext, "application/octet-stream")

    def serve_file(self, file_path, content_type):
        try:
            with open(file_path, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", content_type)
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
        except Exception:
            self.send_response(500)
            self.end_headers()

    def log_message(self, format, *args):
        pass

def is_server_alive():
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(0.3)
            return s.connect_ex(('127.0.0.1', PORT)) == 0
    except Exception:
        return False

def start_server():
    if not is_server_alive():
        try:
            httpd = ResilientServer(('127.0.0.1', PORT), LauncherRequestHandler)
            t = threading.Thread(target=httpd.serve_forever, daemon=True)
            t.start()
        except Exception as e:
            pass

    # Wait up to 3 seconds until server is definitely listening
    for _ in range(30):
        if is_server_alive():
            break
        time.sleep(0.1)

def find_browser():
    candidates = [
        r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
        r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
        os.path.expandvars(r"%LOCALAPPDATA%\Microsoft\Edge\Application\msedge.exe"),
        r"C:\Program Files\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
        os.path.expandvars(r"%LOCALAPPDATA%\Google\Chrome\Application\chrome.exe"),
    ]
    for path in candidates:
        if os.path.isfile(path):
            return path
    for name in ["msedge.exe", "chrome.exe"]:
        p = shutil.which(name)
        if p:
            return p
    return None

def launch_ui():
    url = f"http://127.0.0.1:{PORT}/launcher"
    browser = find_browser()
    if browser:
        args = [
            browser,
            f"--app={url}",
            "--window-size=1260,820",
            "--no-proxy-server",
            "--proxy-bypass-list=127.0.0.1;localhost",
            "--disable-features=Translate",
            "--no-first-run",
            "--no-default-browser-check"
        ]
        try:
            subprocess.Popen(args)
            return True
        except Exception:
            pass

    webbrowser.open(url)
    return True

print("Test robust app definition complete")
