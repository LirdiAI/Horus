#!/usr/bin/env python3
"""
Lumira Launcher Bridge & Local Sync Server
Порт: 38241

Функции:
1. Раздаёт интерфейс Лаунчера (http://127.0.0.1:38241/launcher) и Сайта (http://127.0.0.1:38241/).
2. Предоставляет эндпоинт POST /api/launch для реального запуска Minecraft через launch_game.bat при нажатии кнопки в интерфейсе.
3. При старте игры передаёт управление встроенному Java WebBridgeServer.
"""

import os
import sys
import json
import subprocess
import threading
from pathlib import Path
from http.server import HTTPServer, SimpleHTTPRequestHandler

PORT = 38241
SCRIPT_DIR = Path(__file__).resolve().parent
ROOT_DIR = SCRIPT_DIR.parent

class LumiraBridgeHandler(SimpleHTTPRequestHandler):
    def log_message(self, format, *args):
        pass

    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors_headers()
        self.end_headers()

    def do_POST(self):
        path = self.path.split("?")[0]
        if path == "/api/launch":
            # Запуск клиента игры
            bat_path = ROOT_DIR / "launch_game.bat"
            if not bat_path.exists():
                bat_path = SCRIPT_DIR / "launch_game.bat"

            if bat_path.exists():
                try:
                    subprocess.Popen(["cmd.exe", "/c", str(bat_path)], cwd=str(ROOT_DIR), creationflags=subprocess.CREATE_NEW_CONSOLE)
                    resp = {"success": True, "launched": True, "message": "Запуск игрового процесса..."}
                except Exception as e:
                    resp = {"success": False, "error": str(e)}
            else:
                resp = {"success": False, "error": "launch_game.bat not found"}

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
        path = self.path.split("?")[0]

        if path == "/api/status":
            resp = {
                "online": False,
                "launchable": True,
                "bridgeReady": True,
                "message": "Launcher Bridge активен. Игра не запущена."
            }
            data = json.dumps(resp, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_cors_headers()
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return

        if path == "/" or path == "/index.html" or path == "/website" or path == "/website/":
            target = ROOT_DIR / "website" / "index.html"
            self.serve_local_file(target, "text/html; charset=utf-8")
            return

        if path == "/launcher" or path == "/launcher/":
            target = ROOT_DIR / "launcher" / "index.html"
            self.serve_local_file(target, "text/html; charset=utf-8")
            return

        # Ищем файл в launcher или website
        clean = path.lstrip("/")
        if clean.startswith("launcher/"):
            clean = clean[len("launcher/"):]
            target = ROOT_DIR / "launcher" / clean
        elif clean.startswith("website/"):
            clean = clean[len("website/"):]
            target = ROOT_DIR / "website" / clean
        else:
            target = ROOT_DIR / "website" / clean
            if not target.exists():
                target = ROOT_DIR / "launcher" / clean

        if target.exists() and target.is_file():
            mime = self.guess_type(str(target))
            self.serve_local_file(target, mime)
            return

        self.send_response(404)
        self.end_headers()

    def serve_local_file(self, file_path, content_type):
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

def run():
    try:
        server = HTTPServer(("0.0.0.0", PORT), LumiraBridgeHandler)
        print("=" * 60)
        print("  LUMIRA LAUNCHER & WEB SYNC BRIDGE ЗАПУЩЕН")
        print(f"  Лаунчер:  http://127.0.0.1:{PORT}/launcher")
        print(f"  Сайт:     http://127.0.0.1:{PORT}/")
        print("=" * 60)
        server.serve_forever()
    except OSError:
        # Порт уже занят (например, игра уже запущена)
        print("Порт 38241 уже занят (вероятно, игра уже работает).")

if __name__ == "__main__":
    run()
