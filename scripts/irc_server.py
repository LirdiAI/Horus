#!/usr/bin/env python3
"""
Standalone IRC Server for Lumira / Deluxe Client.
Zero dependencies (standard Python library only).

Usage:
    python scripts/irc_server.py [port]
    Default port: 8080

In-game setup:
    .irc url http://<your-ip-or-domain>:8080/irc
    .irc on
    Писать в чат: @всем привет
"""

import sys
import json
import time
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8080

lock = threading.Lock()
messages = []
online_users = {}  # name.lower(): {"name": ..., "role": ..., "nick": ..., "last_active": ...}
user_cursors = {}  # name.lower(): last_seen_msg_id
message_id_counter = 1

# Seed welcome message
messages.append({
    "id": message_id_counter,
    "name": "Lumira",
    "role": "ADMIN",
    "nick": "Console",
    "text": "IRC сервер успешно запущен! Приятного общения.",
    "time": int(time.time()),
    "emoji": 0
})
message_id_counter += 1

class IrcRequestHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        # Clean custom logger
        return

    def _set_headers(self, status=200, content_type="application/json; charset=utf-8"):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Deluxe-Key, X-Requested-With")
        self.end_headers()

    def do_OPTIONS(self):
        self._set_headers(204)

    def do_GET(self):
        now = time.time()
        with lock:
            # Clean users inactive > 25s
            dead = [k for k, v in online_users.items() if now - v["last_active"] > 25]
            for k in dead:
                del online_users[k]

            active_list = list(online_users.values())
            total_msgs = len(messages)

        html = f"""<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="utf-8">
<title>Lumira IRC Server Status</title>
<style>
body {{ background: #0c0d14; color: #fff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }}
.card {{ background: rgba(22, 24, 38, 0.85); border: 1px solid rgba(255, 255, 255, 0.08); backdrop-filter: blur(20px); border-radius: 20px; padding: 36px 44px; width: 480px; box-shadow: 0 24px 60px rgba(0,0,0,0.5); }}
h1 {{ margin: 0 0 8px 0; font-size: 24px; background: linear-gradient(135deg, #a855f7, #6366f1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }}
.sub {{ color: #94a3b8; font-size: 13px; margin-bottom: 24px; }}
.badge {{ display: inline-flex; align-items: center; gap: 6px; padding: 6px 12px; background: rgba(34, 197, 94, 0.15); border: 1px solid rgba(34, 197, 94, 0.3); color: #4ade80; border-radius: 9999px; font-size: 12px; font-weight: 600; margin-bottom: 20px; }}
.stat-row {{ display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.06); font-size: 14px; }}
.stat-label {{ color: #94a3b8; }}
.stat-val {{ font-weight: 600; color: #f1f5f9; }}
.users-box {{ margin-top: 18px; padding: 14px; background: rgba(0,0,0,0.25); border-radius: 12px; font-size: 13px; color: #cbd5e1; }}
.cmd-box {{ margin-top: 20px; padding: 12px; background: rgba(147, 51, 234, 0.1); border: 1px dashed rgba(168, 85, 247, 0.4); border-radius: 10px; font-family: monospace; font-size: 12px; color: #c084fc; text-align: center; }}
</style>
</head>
<body>
<div class="card">
    <div class="badge"><span style="width:8px;height:8px;border-radius:50%;background:#22c55e;display:inline-block"></span> СЕРВЕР АКТИВЕН</div>
    <h1>Lumira IRC Server</h1>
    <div class="sub">Внутриигровой зашифрованный чат Lumira / Deluxe</div>
    <div class="stat-row"><span class="stat-label">Порт:</span><span class="stat-val">{PORT}</span></div>
    <div class="stat-row"><span class="stat-label">Сообщений в памяти:</span><span class="stat-val">{total_msgs}</span></div>
    <div class="stat-row"><span class="stat-label">Игроков онлайн:</span><span class="stat-val">{len(active_list)}</span></div>
    <div class="users-box">
        <strong>В чате:</strong> {', '.join([u['name'] for u in active_list]) if active_list else 'Никого нет'}
    </div>
    <div class="cmd-box">
        В игре: .irc url http://&lt;IP&gt;:{PORT}/irc
    </div>
</div>
</body>
</html>"""
        self._set_headers(200, "text/html; charset=utf-8")
        self.wfile.write(html.encode("utf-8"))

    def do_POST(self):
        length = int(self.headers.get("Content-Length", 0))
        raw_body = self.rfile.read(length).decode("utf-8") if length > 0 else "{}"
        try:
            req = json.loads(raw_body)
        except Exception:
            req = {}

        action = req.get("action", "poll")
        name = req.get("name", "User")
        role = req.get("role", "DEFAULT")
        nick = req.get("nick", name)
        name_key = name.lower()

        now = time.time()
        global message_id_counter

        with lock:
            # Clean dead users (> 25s)
            dead = [k for k, v in online_users.items() if now - v["last_active"] > 25]
            for k in dead:
                del online_users[k]

            if action == "poll":
                online_users[name_key] = {
                    "name": name,
                    "role": role,
                    "nick": nick,
                    "last_active": now
                }

                last_idx = user_cursors.get(name_key)
                new_cursor = last_idx if last_idx is not None else 0
                out_msgs = []

                if last_idx is None:
                    # New user: return last 15 messages as history
                    recent = messages[-15:] if len(messages) > 15 else messages
                    for m in recent:
                        out_msgs.append(m)
                        new_cursor = max(new_cursor, m.get("id", 0))
                else:
                    for m in messages:
                        if m.get("id", 0) > last_idx:
                            out_msgs.append(m)
                            new_cursor = max(new_cursor, m.get("id", 0))

                user_cursors[name_key] = new_cursor
                resp = {"ok": True, "messages": out_msgs}
                self._set_headers(200)
                self.wfile.write(json.dumps(resp, ensure_ascii=False).encode("utf-8"))
                return

            if action == "send":
                text = req.get("text", "").strip()
                if text:
                    msg = {
                        "id": message_id_counter,
                        "name": name,
                        "role": role,
                        "nick": nick,
                        "text": text,
                        "time": int(now),
                        "emoji": abs(hash(name)) % 20
                    }
                    message_id_counter += 1
                    if "replyTo" in req and req["replyTo"]:
                        msg["replyTo"] = req["replyTo"]
                    if "replyToNick" in req and req["replyToNick"]:
                        msg["replyToNick"] = req["replyToNick"]

                    messages.append(msg)
                    if len(messages) > 200:
                        messages.pop(0)

                    online_users[name_key] = {
                        "name": name,
                        "role": role,
                        "nick": nick,
                        "last_active": now
                    }
                    print(f"[{time.strftime('%H:%M:%S')}] [{role}] {name} ({nick}): {text}")

                self._set_headers(200)
                self.wfile.write(json.dumps({"ok": True}).encode("utf-8"))
                return

            if action == "online":
                online_users[name_key] = {
                    "name": name,
                    "role": role,
                    "nick": nick,
                    "last_active": now
                }
                user_list = [{"name": u["name"], "role": u["role"], "nick": u["nick"]} for u in online_users.values()]
                self._set_headers(200)
                self.wfile.write(json.dumps({"ok": True, "online": user_list}, ensure_ascii=False).encode("utf-8"))
                return

            if action == "leave":
                if name_key in online_users:
                    del online_users[name_key]
                if name_key in user_cursors:
                    del user_cursors[name_key]
                print(f"[{time.strftime('%H:%M:%S')}] {name} вышел из IRC")
                self._set_headers(200)
                self.wfile.write(json.dumps({"ok": True}).encode("utf-8"))
                return

        self._set_headers(400)
        self.wfile.write(json.dumps({"ok": False, "error": "Unknown action"}).encode("utf-8"))

def run():
    server = HTTPServer(("0.0.0.0", PORT), IrcRequestHandler)
    print("=" * 60)
    print(f"  Lumira / Deluxe IRC Server запущен на порту: {PORT}")
    print(f"  Локальный адрес: http://127.0.0.1:{PORT}/irc")
    print(f"  Внешний адрес:   http://<ваш-IP>:{PORT}/irc")
    print("  Для проверки в браузере откройте: http://127.0.0.1:" + str(PORT))
    print("=" * 60)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nСервер остановлен.")
        server.server_close()

if __name__ == "__main__":
    run()
