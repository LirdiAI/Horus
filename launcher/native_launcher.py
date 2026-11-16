import math
import uuid
import winreg
import webbrowser
import re
import os
import sys
import json
import time
import ctypes
import base64
import hashlib
import threading
import subprocess
import urllib.request
import urllib.parse
from http.server import HTTPServer, BaseHTTPRequestHandler
from pathlib import Path
import tkinter as tk
from tkinter import messagebox, filedialog
import customtkinter as ctk
from PIL import Image, ImageTk

# Explicit AppUserModelID ensures Windows Taskbar and Alt+Tab properly identify the app
try:
    ctypes.windll.shell32.SetCurrentProcessExplicitAppUserModelID("horus.launcher.v1")
except Exception:
    pass

# --- Color Palette (Solid Premium Dark Theme - No Transparency Artifacts) ---
COLOR_CARD = "#0c0e17"
COLOR_CARD_BORDER = "#1f283d"
COLOR_INNER_CARD = "#111522"
COLOR_INNER_BORDER = "#27344f"

COLOR_BTN_PRIMARY = "#6371c8"
COLOR_BTN_HOVER = "#7583db"
COLOR_BTN_ACTIVE = "#525fb5"

COLOR_INPUT_BG = "#151821"
COLOR_INPUT_BORDER = "#232838"
COLOR_INPUT_BORDER_FOCUS = "#6f7edb"

COLOR_TEXT_WHITE = "#ffffff"
COLOR_TEXT_GRAY = "#94a3b8"
COLOR_TEXT_MUTED = "#64748b"
COLOR_ADMIN_GOLD = "#e39f25"

COLOR_STATUS_ERROR = "#f87171"
COLOR_STATUS_SUCCESS = "#4ade80"
COLOR_STATUS_INFO = "#93c5fd"

# --- Accent Themes (Neon Colors: Purple / Cyber Blue / Emerald / Ruby) ---
THEMES = {
    "purple": {
        "name": "Фиолетовый Horus",
        "icon": "🟣",
        "primary": "#6371c8",
        "hover": "#7583db",
        "active": "#525fb5",
        "glow": "#8b5cf6",
        "badge_bg": "#1e1b4b",
        "badge_border": "#3730a3",
        "badge_text": "#c4b5fd",
        "accent": "#a855f7"
    },
    "blue": {
        "name": "Кибер-синий",
        "icon": "🔵",
        "primary": "#0284c7",
        "hover": "#0ea5e9",
        "active": "#0369a1",
        "glow": "#38bdf8",
        "badge_bg": "#082f49",
        "badge_border": "#075985",
        "badge_text": "#7dd3fc",
        "accent": "#38bdf8"
    },
    "emerald": {
        "name": "Изумрудный",
        "icon": "🟢",
        "primary": "#059669",
        "hover": "#10b981",
        "active": "#047857",
        "glow": "#34d399",
        "badge_bg": "#064e3b",
        "badge_border": "#065f46",
        "badge_text": "#6ee7b7",
        "accent": "#34d399"
    },
    "ruby": {
        "name": "Рубиновый",
        "icon": "🔴",
        "primary": "#e11d48",
        "hover": "#f43f5e",
        "active": "#be123c",
        "glow": "#fb7185",
        "badge_bg": "#4c0519",
        "badge_border": "#881337",
        "badge_text": "#fda4af",
        "accent": "#fb7185"
    }
}

WINDOW_WIDTH = 960
WINDOW_HEIGHT = 560

SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co"
SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy"

def resolve_root_dir():
    if getattr(sys, 'frozen', False):
        exe_dir = Path(sys.executable).resolve().parent
    else:
        exe_dir = Path(__file__).resolve().parent

    for candidate in [exe_dir, exe_dir.parent, Path.cwd().resolve(), Path(r"c:\Users\Никита\Downloads\HorusClient")]:
        if (candidate / "gradlew.bat").exists() or (candidate / "launcher").exists():
            return candidate
    return exe_dir

ROOT_DIR = resolve_root_dir()
LAUNCHER_DIR = ROOT_DIR / "launcher"
ICONS_DIR = LAUNCHER_DIR / "icons"

APP_DATA_DIR = Path(os.environ.get("LOCALAPPDATA", os.path.expanduser("~"))) / "HorusLauncher"
APP_DATA_DIR.mkdir(parents=True, exist_ok=True)
ACCOUNTS_FILE = APP_DATA_DIR / "accounts.json"

def get_system_ram_gb():
    try:
        class MEMORYSTATUSEX(ctypes.Structure):
            _fields_ = [
                ("dwLength", ctypes.c_ulong),
                ("dwMemoryLoad", ctypes.c_ulong),
                ("ullTotalPhys", ctypes.c_ulonglong),
                ("ullAvailPhys", ctypes.c_ulonglong),
                ("ullTotalPageFile", ctypes.c_ulonglong),
                ("ullAvailPageFile", ctypes.c_ulonglong),
                ("ullTotalVirtual", ctypes.c_ulonglong),
                ("ullAvailVirtual", ctypes.c_ulonglong),
                ("ullAvailExtendedVirtual", ctypes.c_ulonglong),
            ]
        stat = MEMORYSTATUSEX()
        stat.dwLength = ctypes.sizeof(MEMORYSTATUSEX)
        if ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(stat)):
            return max(2, round(stat.ullTotalPhys / (1024**3)))
    except Exception:
        pass
    try:
        import psutil
        return max(2, round(psutil.virtual_memory().total / (1024**3)))
    except Exception:
        pass
    return 16

def hex_to_rgb(hex_str):
    hex_str = hex_str.lstrip('#')
    return tuple(int(hex_str[i:i+2], 16) for i in (0, 2, 4))

def rgb_to_hex(rgb):
    return f"#{int(max(0, min(255, rgb[0]))):02x}{int(max(0, min(255, rgb[1]))):02x}{int(max(0, min(255, rgb[2]))):02x}"

def lerp_color(c1_hex, c2_hex, factor):
    r1, g1, b1 = hex_to_rgb(c1_hex)
    r2, g2, b2 = hex_to_rgb(c2_hex)
    r = r1 + (r2 - r1) * factor
    g = g1 + (g2 - g1) * factor
    b = b1 + (b2 - b1) * factor
    return rgb_to_hex((r, g, b))

def get_system_hwid() -> str:
    try:
        with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\Microsoft\Cryptography") as key:
            guid, _ = winreg.QueryValueEx(key, "MachineGuid")
            clean = guid.replace("-", "").upper()
            return f"WIN11-{clean[:4]}-{clean[4:8]}-HORUS"
    except Exception:
        pass
    node = f"{uuid.getnode():012X}"
    return f"WIN11-{node[:4]}-{node[4:8]}-HORUS"

def get_default_accounts():
    return {
        "active": "",
        "remember_me": False,
        "saved_login": "",
        "settings": {
            "theme": "purple",
            "ram": 4,
            "fullscreen": True
        }
    }

def clean_account_data(data):
    if not isinstance(data, dict):
        return get_default_accounts()
    clean = {
        "active": data.get("active", ""),
        "remember_me": bool(data.get("remember_me", False)),
        "saved_login": data.get("saved_login", ""),
        "settings": data.get("settings", {"theme": "purple", "ram": 4, "fullscreen": True})
    }
    return clean

def load_accounts_db():
    if ACCOUNTS_FILE.exists():
        try:
            with open(ACCOUNTS_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
                cleaned = clean_account_data(data)
                save_accounts_db(cleaned)
                return cleaned
        except Exception:
            pass

    defaults = get_default_accounts()
    save_accounts_db(defaults)
    return defaults

def save_accounts_db(data):
    clean = clean_account_data(data)
    try:
        with open(ACCOUNTS_FILE, "w", encoding="utf-8") as f:
            json.dump(clean, f, ensure_ascii=False, indent=2)
    except Exception:
        pass

    try:
        backup_path = LAUNCHER_DIR / "accounts.json"
        with open(backup_path, "w", encoding="utf-8") as f:
            json.dump(clean, f, ensure_ascii=False, indent=2)
    except Exception:
        pass

def is_minecraft_window_visible():
    found = False
    def enum_cb(hwnd, lparam):
        nonlocal found
        if ctypes.windll.user32.IsWindowVisible(hwnd):
            length = ctypes.windll.user32.GetWindowTextLengthW(hwnd)
            if length > 0:
                buff = ctypes.create_unicode_buffer(length + 1)
                ctypes.windll.user32.GetWindowTextW(hwnd, buff, length + 1)
                title = buff.value.lower()
                if ("minecraft" in title or "horus client" in title) and "launcher" not in title:
                    found = True
                    return False
        return True

    try:
        WNDENUMPROC = ctypes.WINFUNCTYPE(ctypes.c_bool, ctypes.c_void_p, ctypes.c_void_p)
        cb = WNDENUMPROC(enum_cb)
        ctypes.windll.user32.EnumWindows(cb, 0)
    except Exception:
        pass
    return found

def is_minecraft_process_running():
    try:
        cmd = 'tasklist /fi "imagename eq javaw.exe" /fo csv /nh'
        out = subprocess.check_output(cmd, shell=True).decode('cp866', errors='ignore')
        return "javaw.exe" in out.lower()
    except Exception:
        return False

# -------------------------------------------------------------------------
# LOCAL BRIDGE HTTP SERVER (WEBSITE <-> LAUNCHER SYNC)
# -------------------------------------------------------------------------
class LocalBridgeHandler(BaseHTTPRequestHandler):
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "*")
        self.end_headers()

    def do_POST(self):
        if self.path in ("/api/register", "/api/account", "/api/sync"):
            content_len = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_len).decode("utf-8")
            try:
                data = json.loads(body)
                username = data.get("username", "").strip()
                password = data.get("password", "")
                if username and password:
                    threading.Thread(
                        target=save_user_to_supabase,
                        args=(
                            username,
                            password,
                            data.get("email"),
                            data.get("role", "DLC EDITION"),
                            get_system_hwid(),
                            data.get("subscription_till", "01.01.2048")
                        ),
                        daemon=True
                    ).start()

                    db = load_accounts_db()
                    db["active"] = username
                    db["saved_login"] = username
                    save_accounts_db(db)

                    self.send_response(200)
                    self.send_header("Content-Type", "application/json")
                    self.send_header("Access-Control-Allow-Origin", "*")
                    self.end_headers()
                    self.wfile.write(b'{"success": true}')
                    return
            except Exception:
                pass
        self.send_response(400)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(b'{"error": "Invalid request"}')

    def log_message(self, format, *args):
        pass

def run_local_bridge_server():
    try:
        server = HTTPServer(('127.0.0.1', 28549), LocalBridgeHandler)
        server.serve_forever()
    except Exception:
        pass

threading.Thread(target=run_local_bridge_server, daemon=True).start()

def check_supabase_account_exists(username: str):
    headers = {
        "apikey": SUPABASE_KEY,
        "Authorization": f"Bearer {SUPABASE_KEY}",
        "Accept": "application/json"
    }
    try:
        req_url = f"{SUPABASE_URL}/rest/v1/profiles?username=eq.{urllib.parse.quote(username)}&select=id,username,role,password_hash,email,hwid,subscription_till"
        req = urllib.request.Request(req_url, headers=headers)
        with urllib.request.urlopen(req, timeout=4.0) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            if isinstance(data, list) and len(data) > 0:
                return True, data[0]
    except Exception:
        pass

    if "@" in username:
        try:
            req_url_email = f"{SUPABASE_URL}/rest/v1/profiles?email=eq.{urllib.parse.quote(username)}&select=id,username,role,password_hash,email,hwid,subscription_till"
            req = urllib.request.Request(req_url_email, headers=headers)
            with urllib.request.urlopen(req, timeout=4.0) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                if isinstance(data, list) and len(data) > 0:
                    return True, data[0]
        except Exception:
            pass

    return False, None

def save_user_to_supabase(username, password, email=None, role="User", hwid=None, subscription_till="01.01.2048"):
    headers = {
        "apikey": SUPABASE_KEY,
        "Authorization": f"Bearer {SUPABASE_KEY}",
        "Content-Type": "application/json",
        "Prefer": "return=representation"
    }
    if not hwid:
        hwid = get_system_hwid()
    if not email:
        email = f"{username.lower()}@horus.client"

    pwd_b64 = base64.b64encode(password.encode("utf-8")).decode("utf-8") if password else ""
    exists, profile = check_supabase_account_exists(username)

    try:
        if exists and profile:
            req_url = f"{SUPABASE_URL}/rest/v1/profiles?username=eq.{urllib.parse.quote(username)}"
            payload = {
                "password_hash": pwd_b64,
                "role": role,
                "hwid": hwid,
                "subscription_till": subscription_till,
                "updated_at": "now()"
            }
            req = urllib.request.Request(req_url, data=json.dumps(payload).encode("utf-8"), headers=headers, method="PATCH")
            with urllib.request.urlopen(req, timeout=5.0) as resp:
                return resp.status in (200, 204)
        else:
            req_url = f"{SUPABASE_URL}/rest/v1/profiles"
            payload = {
                "username": username,
                "email": email,
                "password_hash": pwd_b64,
                "role": role,
                "hwid": hwid,
                "subscription_till": subscription_till,
                "created_at": "now()",
                "updated_at": "now()"
            }
            req = urllib.request.Request(req_url, data=json.dumps(payload).encode("utf-8"), headers=headers, method="POST")
            with urllib.request.urlopen(req, timeout=5.0) as resp:
                return resp.status in (200, 201)
    except Exception:
        return False

# -------------------------------------------------------------------------
# HORUS LAUNCHER MAIN APPLICATION
# -------------------------------------------------------------------------
class HorusLauncherApp(ctk.CTk):
    def __init__(self):
        super().__init__()

        ctk.set_appearance_mode("dark")
        self.title("✦ HorusClient ✦ — Minecraft Launcher")
        self.overrideredirect(True)
        self.resizable(False, False)
        self.configure(fg_color=COLOR_CARD)

        self.set_window_icon()
        self.center_window(WINDOW_WIDTH, WINDOW_HEIGHT)
        self.apply_windows_attributes()
        self.force_taskbar_presence()

        self.db = load_accounts_db()
        self.current_user = self.db.get("active", "")
        self.current_role = "DLC EDITION"
        self.subscription_status = "Навсегда"

        self.drag_start_x = 0
        self.drag_start_y = 0

        self.avatar_img = None
        self.hero_banner_img = None
        self.app_logo_img = None
        self.load_all_custom_icons()

        theme = self.get_theme_colors()

        # Outer root container with clean border
        self.main_card = ctk.CTkFrame(
            self,
            fg_color=COLOR_CARD,
            border_color=theme.get("glow", COLOR_CARD_BORDER),
            border_width=1.5,
            corner_radius=22
        )
        self.main_card.pack(fill="both", expand=True, padx=0, pady=0)
        self.main_card.bind("<Button-1>", self.on_drag_start)
        self.main_card.bind("<B1-Motion>", self.on_drag_motion)

        # Mirror Polish Top Sheen on main window
        top_window_mirror = ctk.CTkFrame(self.main_card, fg_color="#364360", height=2, corner_radius=1)
        top_window_mirror.pack(fill="x", padx=16, pady=(3, 0))



        # Content Frame
        self.content_frame = ctk.CTkFrame(self.main_card, fg_color="transparent")
        self.content_frame.pack(fill="both", expand=True, padx=20, pady=16)
        self.content_frame.bind("<Button-1>", self.on_drag_start)
        self.content_frame.bind("<B1-Motion>", self.on_drag_motion)

        # Pulse animation state
        self.anim_phase = 0.0
        self.btn_launch_ref = None
        self.status_pill_ref = None
        self.pulse_status_lbl = None
        self.is_animating = True

        self.start_breathing_loop()

        if self.current_user:
            self.show_main_view()
        else:
            self.show_auth_view()

    def start_breathing_loop(self):
        def loop():
            if not self.is_animating:
                return
            self.anim_phase = (self.anim_phase + 0.07) % (2 * math.pi)
            factor = (math.sin(self.anim_phase) + 1.0) / 2.0  # 0.0 to 1.0

            theme = self.get_theme_colors()

            # Pulse Main Launch Button glow
            if self.btn_launch_ref and self.btn_launch_ref.winfo_exists():
                c_norm = theme["primary"]
                c_glow = theme.get("glow", theme["hover"])
                curr_color = lerp_color(c_norm, c_glow, factor)
                try:
                    self.btn_launch_ref.configure(
                        border_color=curr_color,
                        border_width=1.5 + factor * 1.5
                    )
                except Exception:
                    pass

            # Pulse status pill dot
            if self.status_pill_ref and self.status_pill_ref.winfo_exists():
                pill_glow = lerp_color("#22c55e", "#86efac", factor)
                try:
                    self.status_pill_ref.configure(text_color=pill_glow)
                except Exception:
                    pass

            self.after(35, loop)

        self.after(100, loop)

    def get_theme_colors(self):
        settings = self.db.get("settings", {})
        t_key = settings.get("theme", "purple")
        return THEMES.get(t_key, THEMES["purple"])

    def get_setting(self, key, default):
        settings = self.db.get("settings", {})
        return settings.get(key, default)

    def set_setting(self, key, val):
        if "settings" not in self.db or not isinstance(self.db["settings"], dict):
            self.db["settings"] = {}
        self.db["settings"][key] = val
        save_accounts_db(self.db)

    def clear_content(self):
        self.btn_launch_ref = None
        self.status_pill_ref = None
        self.pulse_status_lbl = None
        for w in self.content_frame.winfo_children():
            w.destroy()

    def set_window_icon(self):
        meipass = getattr(sys, '_MEIPASS', None)
        cands = [
            LAUNCHER_DIR / "launcher.ico",
            ROOT_DIR / "launcher" / "launcher.ico",
            ROOT_DIR / "launcher.ico"
        ]
        if meipass:
            cands.insert(0, Path(meipass) / "launcher.ico")
            cands.insert(0, Path(meipass) / "launcher" / "launcher.ico")

        for p in cands:
            if p.exists():
                try:
                    self.iconbitmap(str(p))
                    break
                except Exception:
                    pass

    def on_resize_start(self, event):
        self.resize_start_x = event.x_root
        self.resize_start_y = event.y_root
        self.start_width = self.winfo_width()
        self.start_height = self.winfo_height()

    def on_resize_motion(self, event):
        dx = event.x_root - self.resize_start_x
        dy = event.y_root - self.resize_start_y
        new_w = max(720, min(1400, self.start_width + dx))
        new_h = max(420, min(900, self.start_height + dy))
        self.geometry(f"{new_w}x{new_h}")

    def toggle_maximize(self):
        cur_w = self.winfo_width()
        if cur_w >= 900:
            self.center_window(780, 440)
        else:
            self.center_window(960, 560)

    def center_window(self, width, height):
        self.update_idletasks()
        sw = self.winfo_screenwidth()
        sh = self.winfo_screenheight()
        x = (sw - width) // 2
        y = (sh - height) // 2
        self.geometry(f"{width}x{height}+{x}+{y}")

    def apply_windows_attributes(self):
        try:
            hwnd = ctypes.windll.user32.GetParent(self.winfo_id())
            # Immersive Dark Mode
            DWMWA_USE_IMMERSIVE_DARK_MODE = 20
            val = ctypes.c_int(2)
            ctypes.windll.dwmapi.DwmSetWindowAttribute(
                hwnd,
                DWMWA_USE_IMMERSIVE_DARK_MODE,
                ctypes.byref(val),
                ctypes.sizeof(val)
            )
            # Glass / Acrylic Backdrop Type (Win11 22H2+)
            DWMWA_SYSTEMBACKDROP_TYPE = 38
            backdrop = ctypes.c_int(3)  # 3 = Acrylic, 2 = Mica
            ctypes.windll.dwmapi.DwmSetWindowAttribute(
                hwnd,
                DWMWA_SYSTEMBACKDROP_TYPE,
                ctypes.byref(backdrop),
                ctypes.sizeof(backdrop)
            )
        except Exception:
            pass

    def force_taskbar_presence(self):
        try:
            hwnd = ctypes.windll.user32.GetParent(self.winfo_id())
            GWL_EXSTYLE = -20
            WS_EX_APPWINDOW = 0x00040000
            WS_EX_TOOLWINDOW = 0x00000080
            cur = ctypes.windll.user32.GetWindowLongW(hwnd, GWL_EXSTYLE)
            cur = (cur | WS_EX_APPWINDOW) & ~WS_EX_TOOLWINDOW
            ctypes.windll.user32.SetWindowLongW(hwnd, GWL_EXSTYLE, cur)
        except Exception:
            pass

    def on_drag_start(self, event):
        self.drag_start_x = event.x_root - self.winfo_x()
        self.drag_start_y = event.y_root - self.winfo_y()

    def on_drag_motion(self, event):
        self.geometry(f"+{event.x_root - self.drag_start_x}+{event.y_root - self.drag_start_y}")

    def load_all_custom_icons(self):
        meipass = getattr(sys, '_MEIPASS', None)

        def find_file(name):
            candidates = [
                LAUNCHER_DIR / name,
                ROOT_DIR / "launcher" / name,
                ROOT_DIR / name
            ]
            if meipass:
                candidates.insert(0, Path(meipass) / name)
                candidates.insert(0, Path(meipass) / "launcher" / name)
            for c in candidates:
                if c.exists():
                    return c
            return None

        # Avatar
        av_path = find_file("user_avatar.png")
        if av_path:
            try:
                img = Image.open(av_path).convert("RGBA")
                self.avatar_img = ctk.CTkImage(light_image=img, dark_image=img, size=(46, 46))
            except Exception:
                pass

        # App Logo
        logo_path = find_file("app_icon.png") or find_file("logo.png")
        if logo_path:
            try:
                img = Image.open(logo_path).convert("RGBA")
                self.app_logo_img = ctk.CTkImage(light_image=img, dark_image=img, size=(55, 55))
            except Exception:
                pass

        # Hero Banner
        banner_path = find_file("hero_banner_ready.png") or find_file("banner_bg.png")
        if banner_path:
            try:
                img = Image.open(banner_path).convert("RGBA")
                self.hero_banner_img = ctk.CTkImage(light_image=img, dark_image=img, size=(796, 176))
            except Exception:
                pass

    # -------------------------------------------------------------------------
    # TOP BAR (UNIVERSAL)
    # -------------------------------------------------------------------------
    def create_top_bar(self, parent, return_to="main"):
        # Higher top section ("чуть верх выше")
        top_bar = ctk.CTkFrame(parent, fg_color="transparent", height=60)
        top_bar.pack(fill="x", pady=(0, 14))
        top_bar.bind("<Button-1>", self.on_drag_start)
        top_bar.bind("<B1-Motion>", self.on_drag_motion)

        # Left: Large Brand Crest in the corner (2.5x larger!) & Title/Status
        brand_frame = ctk.CTkFrame(top_bar, fg_color="transparent")
        brand_frame.pack(side="left")
        brand_frame.bind("<Button-1>", self.on_drag_start)
        brand_frame.bind("<B1-Motion>", self.on_drag_motion)

        if self.app_logo_img:
            lbl_logo = ctk.CTkLabel(brand_frame, text="", image=self.app_logo_img)
            lbl_logo.pack(side="left", padx=(0, 14))
            lbl_logo.bind("<Button-1>", self.on_drag_start)
            lbl_logo.bind("<B1-Motion>", self.on_drag_motion)

        brand_text_box = ctk.CTkFrame(brand_frame, fg_color="transparent")
        brand_text_box.pack(side="left", fill="y", expand=True)
        brand_text_box.bind("<Button-1>", self.on_drag_start)
        brand_text_box.bind("<B1-Motion>", self.on_drag_motion)

        lbl_brand_name = ctk.CTkLabel(
            brand_text_box,
            text="HORUS CLIENT",
            font=ctk.CTkFont(family="Segoe UI", size=16, weight="bold"),
            text_color="#f8fafc",
            anchor="w"
        )
        lbl_brand_name.pack(anchor="w")
        lbl_brand_name.bind("<Button-1>", self.on_drag_start)
        lbl_brand_name.bind("<B1-Motion>", self.on_drag_motion)

        theme = self.get_theme_colors()

        badges_row = ctk.CTkFrame(brand_text_box, fg_color="transparent")
        badges_row.pack(anchor="w", pady=(3, 0))
        badges_row.bind("<Button-1>", self.on_drag_start)
        badges_row.bind("<B1-Motion>", self.on_drag_motion)

        # DLC Badge
        dlc_badge = ctk.CTkFrame(
            badges_row,
            fg_color=theme["badge_bg"],
            border_color=theme["badge_border"],
            border_width=1,
            corner_radius=8
        )
        dlc_badge.pack(side="left")
        lbl_dlc = ctk.CTkLabel(
            dlc_badge,
            text="DLC EDITION",
            font=ctk.CTkFont(family="Segoe UI", size=9, weight="bold"),
            text_color=theme["badge_text"]
        )
        lbl_dlc.pack(padx=8, pady=2)


        # Right Controls: Website, TG, Settings, Minimize, Close (Centered vertically)
        btn_box = ctk.CTkFrame(top_bar, fg_color="transparent")
        btn_box.pack(side="right", pady=8)

        def make_icon_btn(text, cmd, hover="#222b40", fg="#131726", border="#242e47", text_color="#9fb0c8"):
            b = ctk.CTkButton(
                btn_box,
                text=text,
                width=32,
                height=30,
                corner_radius=9,
                fg_color=fg,
                border_color=border,
                border_width=1,
                hover_color=hover,
                font=ctk.CTkFont(family="Segoe UI", size=12, weight="bold"),
                text_color=text_color,
                command=cmd
            )
            b.pack(side="left", padx=3)
            return b

        make_icon_btn("🌐", lambda: webbrowser.open("https://pulsevisuals.pro/"))
        make_icon_btn("✈", lambda: webbrowser.open("https://t.me/horusclient"))

        if return_to == "main":
            make_icon_btn("⚙", self.show_settings_view)
        else:
            make_icon_btn("←", self.show_main_view if self.current_user else self.show_auth_view)

        make_icon_btn("—", self.iconify)
        make_icon_btn("✕", self.destroy, hover="#dc2626", fg="#18131e", border="#3d1d26", text_color="#f87171")

    # -------------------------------------------------------------------------
    # MAIN VIEW (WIDESCREEN 840x560 DASHBOARD)
    # -------------------------------------------------------------------------
    def show_main_view(self):
        self.clear_content()

        theme = self.get_theme_colors()

        # Top Bar
        self.create_top_bar(self.content_frame, return_to="main")
        # Polished Glass Hairline Divider
        glass_line = ctk.CTkFrame(self.content_frame, fg_color="#1d2538", height=1)
        glass_line.pack(fill="x", pady=(0, 12))



        # ---------------- DASHBOARD (2 EQUAL COLUMNS) ----------------
        dashboard = ctk.CTkFrame(self.content_frame, fg_color="transparent")
        dashboard.pack(fill="both", expand=True)

        # ================= LEFT COLUMN: PROFILE & HARDWARE =================
        left_card = ctk.CTkFrame(
            dashboard,
            fg_color=COLOR_INNER_CARD,
            border_color=COLOR_INNER_BORDER,
            border_width=1,
            corner_radius=14
        )
        left_card.pack(side="left", fill="both", expand=True, padx=(0, 7))

        # Header
        glass_sheen_l = ctk.CTkFrame(left_card, fg_color="#2b3650", height=2, corner_radius=1)
        glass_sheen_l.pack(fill="x", padx=12, pady=(3, 0))

        lbl_left_title = ctk.CTkLabel(
            left_card,
            text="ПРОФИЛЬ И СИСТЕМА",
            font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
            text_color=COLOR_TEXT_MUTED
        )
        lbl_left_title.pack(anchor="w", padx=14, pady=(12, 6))

        # User Row
        user_row = ctk.CTkFrame(left_card, fg_color="#090d16", border_color="#1d263a", border_width=1, corner_radius=12)
        # Mirror sheen on top of user row
        sheen_user = ctk.CTkFrame(user_row, fg_color="#2b3650", height=1)
        sheen_user.pack(fill="x", padx=8, pady=(2, 0))
        user_row.pack(fill="x", padx=14, pady=(0, 10))

        if self.avatar_img:
            lbl_av = ctk.CTkLabel(user_row, text="", image=self.avatar_img)
            lbl_av.pack(side="left", padx=(10, 8), pady=8)
        else:
            lbl_av = ctk.CTkLabel(user_row, text="👤", font=ctk.CTkFont(size=22))
            lbl_av.pack(side="left", padx=(10, 8), pady=8)

        user_info_box = ctk.CTkFrame(user_row, fg_color="transparent")
        user_info_box.pack(side="left", fill="y", expand=True, pady=8)

        lbl_uname = ctk.CTkLabel(
            user_info_box,
            text=self.current_user or "Пользователь",
            font=ctk.CTkFont(family="Segoe UI", size=13, weight="bold"),
            text_color=COLOR_TEXT_WHITE,
            anchor="w"
        )
        lbl_uname.pack(anchor="w")

        lbl_usub = ctk.CTkLabel(
            user_info_box,
            text=f"Статус: {self.subscription_status} (DLC)",
            font=ctk.CTkFont(family="Segoe UI", size=10),
            text_color=theme["badge_text"],
            anchor="w"
        )
        lbl_usub.pack(anchor="w")

        btn_switch = ctk.CTkButton(
            user_row,
            text="Сменить",
            width=65,
            height=26,
            corner_radius=8,
            fg_color="#161c2b",
            border_color="#27334a",
            border_width=1,
            hover_color="#222b40",
            font=ctk.CTkFont(family="Segoe UI", size=10, weight="bold"),
            text_color=COLOR_TEXT_GRAY,
            command=self.handle_logout
        )
        btn_switch.pack(side="right", padx=10)

        # RAM Box (Зеркальное Выделение ОЗУ с автоопределением памяти ПК)
        ram_box = ctk.CTkFrame(left_card, fg_color="#090d16", border_color="#1d263a", border_width=1.2, corner_radius=14)
        ram_box.pack(fill="x", padx=14, pady=(0, 10))

        # Mirror sheen line on top of RAM box
        sheen_ram = ctk.CTkFrame(ram_box, fg_color="#2b3650", height=1)
        sheen_ram.pack(fill="x", padx=8, pady=(2, 6))

        total_ram = get_system_ram_gb()
        current_ram = min(self.get_setting("ram", 4), total_ram)

        ram_header = ctk.CTkFrame(ram_box, fg_color="transparent")
        ram_header.pack(fill="x", padx=10, pady=(0, 4))

        lbl_ram_title = ctk.CTkLabel(
            ram_header,
            text="Выделение памяти (ОЗУ)",
            font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
            text_color="#e2e8f0"
        )
        lbl_ram_title.pack(side="left")

        lbl_max_pc = ctk.CTkLabel(
            ram_header,
            text=f"(ПК: {total_ram} ГБ)",
            font=ctk.CTkFont(family="Segoe UI", size=10, weight="bold"),
            text_color="#64748b"
        )
        lbl_max_pc.pack(side="left", padx=6)

        controls_frame = ctk.CTkFrame(ram_header, fg_color="transparent")
        controls_frame.pack(side="right")

        badge_val = ctk.CTkFrame(controls_frame, fg_color="#131a29", border_color=theme["glow"], border_width=1, corner_radius=8)

        self.lbl_ram_val = ctk.CTkLabel(
            badge_val,
            text=f"{current_ram} / {total_ram} ГБ",
            font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
            text_color=theme["glow"]
        )
        self.lbl_ram_val.pack(padx=8, pady=2)

        chip_buttons = {}

        def update_chips_visual(selected_val):
            for pv, btn in chip_buttons.items():
                if pv == selected_val:
                    btn.configure(
                        fg_color=theme["primary"],
                        border_color=theme["glow"],
                        border_width=1.5,
                        text_color="#ffffff",
                        hover_color=theme["hover"]
                    )
                else:
                    btn.configure(
                        fg_color="#101524",
                        border_color="#1e283d",
                        border_width=1,
                        text_color="#8fa0b8",
                        hover_color="#1d263b"
                    )

        def set_ram(val):
            val = max(2, min(total_ram, int(round(val))))
            self.lbl_ram_val.configure(text=f"{val} / {total_ram} ГБ")
            slider_ram.set(val)
            self.set_setting("ram", val)
            update_chips_visual(val)

        btn_minus = ctk.CTkButton(
            controls_frame,
            text="−",
            width=26,
            height=24,
            corner_radius=7,
            fg_color="#141a28",
            border_color="#27344d",
            border_width=1,
            hover_color="#222c42",
            font=ctk.CTkFont(family="Segoe UI", size=12, weight="bold"),
            text_color="#cbd5e1",
            command=lambda: set_ram(self.get_setting("ram", 4) - 1)
        )
        btn_minus.pack(side="left", padx=(0, 5))

        badge_val.pack(side="left", padx=2)

        btn_plus = ctk.CTkButton(
            controls_frame,
            text="+",
            width=26,
            height=24,
            corner_radius=7,
            fg_color="#141a28",
            border_color="#27344d",
            border_width=1,
            hover_color="#222c42",
            font=ctk.CTkFont(family="Segoe UI", size=12, weight="bold"),
            text_color="#cbd5e1",
            command=lambda: set_ram(self.get_setting("ram", 4) + 1)
        )
        btn_plus.pack(side="left", padx=(5, 0))

        slider_ram = ctk.CTkSlider(
            ram_box,
            from_=2,
            to=total_ram,
            number_of_steps=max(1, total_ram - 2),
            height=16,
            corner_radius=8,
            progress_color=theme["primary"],
            button_color=theme["glow"],
            button_hover_color=theme["hover"],
            button_length=16,
            button_corner_radius=8,
            command=lambda v: set_ram(v)
        )
        slider_ram.set(current_ram)
        slider_ram.pack(fill="x", padx=10, pady=(4, 8))

        # Quick preset mirror chips (styled with active glowing state)
        presets_row = ctk.CTkFrame(ram_box, fg_color="transparent")
        presets_row.pack(fill="x", padx=8, pady=(0, 8))

        all_presets = [2, 4, 6, 8, 12, 16, 24, 32, 48, 64]
        chips = [p for p in all_presets if p < total_ram] + [total_ram]
        chips = sorted(list(set(chips)))

        for p_val in chips:
            is_active = (p_val == current_ram)
            b_p = ctk.CTkButton(
                presets_row,
                text=f"{p_val}Г",
                height=26,
                corner_radius=8,
                fg_color=theme["primary"] if is_active else "#101524",
                border_color=theme["glow"] if is_active else "#1e283d",
                border_width=1.5 if is_active else 1,
                hover_color=theme["hover"] if is_active else "#1d263b",
                font=ctk.CTkFont(family="Segoe UI", size=10, weight="bold"),
                text_color="#ffffff" if is_active else "#8fa0b8",
                command=lambda pv=p_val: set_ram(pv)
            )
            b_p.pack(side="left", expand=True, padx=2)
            chip_buttons[p_val] = b_p

        # Pulse Visuals Tile (Frosted Glass with Mirror Sheen)
        pulse_tile = ctk.CTkFrame(left_card, fg_color="#090d16", border_color="#1d263a", border_width=1.2, corner_radius=12)
        # Mirror sheen on top of pulse tile
        sheen_pulse = ctk.CTkFrame(pulse_tile, fg_color="#2b3650", height=1)
        sheen_pulse.pack(fill="x", padx=8, pady=(2, 0))
        pulse_tile.pack(fill="x", padx=14, pady=(0, 10))

        pulse_top = ctk.CTkFrame(pulse_tile, fg_color="transparent")
        pulse_top.pack(fill="x", padx=10, pady=(8, 4))

        has_pulse = self.check_pulse_token_exists()
        p_status_text = "● Pulse Visuals: Подключен" if has_pulse else "○ Pulse Visuals: Не привязан"
        p_status_color = "#4ade80" if has_pulse else "#94a3b8"

        self.pulse_status_lbl = ctk.CTkLabel(
            pulse_top,
            text=p_status_text,
            font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
            text_color=p_status_color
        )
        self.pulse_status_lbl.pack(side="left")

        self.btn_pulse_act = ctk.CTkButton(
            pulse_top,
            text="Отвязать" if has_pulse else "⚡ Привязать",
            width=80,
            height=26,
            corner_radius=7,
            fg_color="#334155" if has_pulse else theme["primary"],
            hover_color="#475569" if has_pulse else theme["hover"],
            font=ctk.CTkFont(family="Segoe UI", size=10, weight="bold"),
            text_color=COLOR_TEXT_WHITE,
            command=self.toggle_pulse_auth
        )
        self.btn_pulse_act.pack(side="right")

        lbl_pulse_sub = ctk.CTkLabel(
            pulse_tile,
            text="Синхронизация официальной косметики Pulse Visuals",
            font=ctk.CTkFont(family="Segoe UI", size=9),
            text_color=COLOR_TEXT_MUTED,
            anchor="w"
        )
        lbl_pulse_sub.pack(anchor="w", padx=10, pady=(0, 8))


        # ================= RIGHT COLUMN: LAUNCH & ACTION CARD =================
        right_card = ctk.CTkFrame(
            dashboard,
            fg_color=COLOR_INNER_CARD,
            border_color=COLOR_INNER_BORDER,
            border_width=1,
            corner_radius=14
        )
        right_card.pack(side="right", fill="both", expand=True, padx=(7, 0))

        glass_sheen_r = ctk.CTkFrame(right_card, fg_color="#2b3650", height=2, corner_radius=1)
        glass_sheen_r.pack(fill="x", padx=12, pady=(3, 0))

        lbl_right_title = ctk.CTkLabel(
            right_card,
            text="УПРАВЛЕНИЕ И ЗАПУСК",
            font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
            text_color=COLOR_TEXT_MUTED
        )
        lbl_right_title.pack(anchor="w", padx=14, pady=(12, 6))

        # Quick Buttons Row
        quick_row = ctk.CTkFrame(right_card, fg_color="transparent")
        quick_row.pack(fill="x", padx=14, pady=(0, 10))

        def open_mc_dir():
            mc_path = Path(os.environ.get("APPDATA", "")) / ".minecraft"
            mc_path.mkdir(parents=True, exist_ok=True)
            os.startfile(str(mc_path))

        btn_folder = ctk.CTkButton(
            quick_row,
            text="📁 Папка .minecraft",
            height=32,
            corner_radius=9,
            fg_color="#141926",
            border_color="#27334a",
            border_width=1,
            hover_color="#232a3b",
            font=ctk.CTkFont(family="Segoe UI", size=10, weight="bold"),
            text_color=COLOR_TEXT_GRAY,
            command=open_mc_dir
        )
        btn_folder.pack(side="left", fill="x", expand=True, padx=(0, 4))

        btn_set = ctk.CTkButton(
            quick_row,
            text="⚙ Параметры",
            height=32,
            corner_radius=9,
            fg_color="#141926",
            border_color="#27334a",
            border_width=1,
            hover_color="#232a3b",
            font=ctk.CTkFont(family="Segoe UI", size=10, weight="bold"),
            text_color=COLOR_TEXT_GRAY,
            command=self.show_settings_view
        )
        btn_set.pack(side="right", fill="x", expand=True, padx=(4, 0))

        # System & Modules Status Card (Fills column space with sleek status badges)
        sys_card = ctk.CTkFrame(
            right_card,
            fg_color="#090d16",
            border_color="#1d263a",
            border_width=1.2,
            corner_radius=12
        )
        sys_card.pack(fill="x", padx=14, pady=(0, 12))

        sheen_sys = ctk.CTkFrame(sys_card, fg_color="#2b3650", height=1)
        sheen_sys.pack(fill="x", padx=8, pady=(2, 4))

        def make_status_row(parent, icon, title, badge_text, badge_color, badge_bg):
            r = ctk.CTkFrame(parent, fg_color="transparent", height=24)
            r.pack(fill="x", padx=10, pady=2)
            lbl_i = ctk.CTkLabel(r, text=f"{icon}  {title}", font=ctk.CTkFont(family="Segoe UI", size=10), text_color="#94a3b8")
            lbl_i.pack(side="left")
            b_frame = ctk.CTkFrame(r, fg_color=badge_bg, corner_radius=6, border_color=badge_color, border_width=1)
            b_frame.pack(side="right")
            lbl_b = ctk.CTkLabel(b_frame, text=badge_text, font=ctk.CTkFont(family="Segoe UI", size=9, weight="bold"), text_color=badge_color)
            lbl_b.pack(padx=6, pady=1)

        make_status_row(sys_card, "⚡", "Ядро клиента:", "1.21.11 Fabric", "#38bdf8", "#0e2238")
        make_status_row(sys_card, "🛡️", "Защита клиента:", "● Активна", "#4ade80", "#0f291e")
        make_status_row(sys_card, "🚀", "Оптимизация:", "Sodium + Iris", "#a855f7", "#221538")
        make_status_row(sys_card, "🌐", "Сеть серверов:", "● Онлайн", "#22c55e", "#0e291b")

        # Spacer to push Launch button down cleanly
        spacer = ctk.CTkFrame(right_card, fg_color="transparent")
        spacer.pack(fill="both", expand=True)

        # ---------------- GLOWING ANIMATED PLAY BUTTON ----------------
        self.btn_launch_ref = ctk.CTkButton(
            right_card,
            text="▷   ЗАПУСТИТЬ",
            font=ctk.CTkFont(family="Segoe UI", size=15, weight="bold"),
            fg_color=theme["primary"],
            hover_color=theme["hover"],
            border_color=theme["glow"],
            border_width=2,
            height=54,
            corner_radius=14,
            text_color=COLOR_TEXT_WHITE,
            command=self.handle_launch
        )
        self.btn_launch_ref.pack(fill="x", padx=14, pady=(0, 8))

        btn_logout = ctk.CTkButton(
            right_card,
            text="Выйти из аккаунта",
            font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
            fg_color="#7f1d1d",
            hover_color="#991b1b",
            border_color="#ef4444",
            border_width=1,
            height=38,
            corner_radius=11,
            text_color="#fee2e2",
            command=self.handle_logout
        )
        btn_logout.pack(fill="x", padx=14, pady=(0, 14))


    # -------------------------------------------------------------------------
    # PULSE VISUALS AUTH HELPER
    # -------------------------------------------------------------------------
    def check_pulse_token_exists(self):
        try:
            for p in [
                Path(os.environ.get("APPDATA", "")) / ".minecraft" / "pulse_cache" / "cosmetics_token.txt",
                ROOT_DIR / "run" / "pulse_cache" / "cosmetics_token.txt"
            ]:
                if p.exists() and len(p.read_text(encoding="utf-8").strip()) > 10:
                    return True
        except Exception:
            pass
        return False

    def toggle_pulse_auth(self):
        p1 = Path(os.environ.get("APPDATA", "")) / ".minecraft" / "pulse_cache" / "cosmetics_token.txt"
        p2 = ROOT_DIR / "run" / "pulse_cache" / "cosmetics_token.txt"
        if self.check_pulse_token_exists():
            try:
                p1.unlink(missing_ok=True)
                p2.unlink(missing_ok=True)
            except Exception:
                pass
            if self.pulse_status_lbl and self.pulse_status_lbl.winfo_exists():
                self.pulse_status_lbl.configure(text="○ Pulse Visuals: Не привязан", text_color="#94a3b8")
            if self.btn_pulse_act and self.btn_pulse_act.winfo_exists():
                theme = self.get_theme_colors()
                self.btn_pulse_act.configure(text="⚡ Привязать", fg_color=theme["primary"], hover_color=theme["hover"])
            return

        # Start device flow
        if self.btn_pulse_act and self.btn_pulse_act.winfo_exists():
            self.btn_pulse_act.configure(state="disabled", text="Ожидание...")

        def worker():
            device_state = str(uuid.uuid4())
            init_url = f"https://api.pulsevisuals.pro/api/auth/device/init?state={device_state}&scope=cosmetics"
            try:
                req = urllib.request.Request(init_url, headers={"User-Agent": "PulseCosmetics/0.1.0"})
                with urllib.request.urlopen(req, timeout=5) as resp:
                    data = json.loads(resp.read().decode("utf-8"))
                    if not data.get("success"):
                        self.after(0, lambda: messagebox.showerror("Ошибка Pulse", "Сервер отклонил запрос"))
                        self.after(0, lambda: self.btn_pulse_act.configure(state="normal", text="⚡ Привязать"))
                        return
            except Exception as e:
                self.after(0, lambda: messagebox.showerror("Ошибка связи", f"Не удалось подключиться: {e}"))
                self.after(0, lambda: self.btn_pulse_act.configure(state="normal", text="⚡ Привязать"))
                return

            auth_web_url = f"https://pulsevisuals.pro/auth/device?state={device_state}"
            webbrowser.open(auth_web_url)

            poll_url = f"https://api.pulsevisuals.pro/api/auth/device/poll?state={device_state}"
            for _ in range(120):
                time.sleep(2)
                try:
                    preq = urllib.request.Request(poll_url, headers={"User-Agent": "PulseCosmetics/0.1.0"})
                    with urllib.request.urlopen(preq, timeout=4) as presp:
                        pdata = json.loads(presp.read().decode("utf-8"))
                        if pdata.get("status") == "confirmed":
                            tok = pdata.get("token", "")
                            p1.parent.mkdir(parents=True, exist_ok=True)
                            p1.write_text(tok, encoding="utf-8")
                            p2.parent.mkdir(parents=True, exist_ok=True)
                            p2.write_text(tok, encoding="utf-8")
                            def on_success():
                                if self.pulse_status_lbl and self.pulse_status_lbl.winfo_exists():
                                    self.pulse_status_lbl.configure(text="● Pulse Visuals: Подключен", text_color="#4ade80")
                                if self.btn_pulse_act and self.btn_pulse_act.winfo_exists():
                                    self.btn_pulse_act.configure(state="normal", text="Отвязать", fg_color="#334155", hover_color="#475569")
                                messagebox.showinfo("Pulse Visuals", "Аккаунт Pulse успешно привязан!")
                            self.after(0, on_success)
                            return
                        elif pdata.get("status") in ["expired", "cancelled"]:
                            break
                except Exception:
                    pass

            self.after(0, lambda: self.btn_pulse_act.configure(state="normal", text="⚡ Привязать"))

        threading.Thread(target=worker, daemon=True).start()

    # -------------------------------------------------------------------------
    # AUTH VIEW (SPLIT WIDESCREEN 840x560)
    # -------------------------------------------------------------------------
    def show_auth_view(self):
        self.clear_content()

        theme = self.get_theme_colors()
        self.create_top_bar(self.content_frame, return_to="auth")

        split_container = ctk.CTkFrame(self.content_frame, fg_color="transparent")
        split_container.pack(fill="both", expand=True, pady=(10, 0))

        # Left Showcase (370px)
        left_show = ctk.CTkFrame(
            split_container,
            fg_color=COLOR_INNER_CARD,
            border_color=COLOR_INNER_BORDER,
            border_width=1,
            corner_radius=14
        )
        left_show.pack(side="left", fill="both", expand=True, padx=(0, 10))

        if self.app_logo_img:
            lbl_l = ctk.CTkLabel(left_show, text="", image=self.app_logo_img)
            lbl_l.pack(pady=(28, 8))

        lbl_title = ctk.CTkLabel(
            left_show,
            text="HORUS CLIENT",
            font=ctk.CTkFont(family="Segoe UI", size=22, weight="bold"),
            text_color=COLOR_TEXT_WHITE
        )
        lbl_title.pack()

        lbl_sub = ctk.CTkLabel(
            left_show,
            text="Приватный DLC клиент нового поколения",
            font=ctk.CTkFont(family="Segoe UI", size=11),
            text_color=theme["accent"]
        )
        lbl_sub.pack(pady=(2, 20))

        features = [
            ("⚡", "Продвинутый Combat & Movement движок"),
            ("🛡️", "Интеллектуальный AutoArmor & AutoTotem"),
            ("🚀", "Оптимизация и стабильные 150+ FPS"),
            ("✦", "Официальная косметика Pulse Visuals"),
            ("🔒", "Облачная синхронизация профилей и HWID")
        ]

        feat_box = ctk.CTkFrame(left_show, fg_color="transparent")
        feat_box.pack(padx=20, fill="x")

        for icon, desc in features:
            row = ctk.CTkFrame(feat_box, fg_color="transparent")
            row.pack(fill="x", pady=4)
            lbl_ic = ctk.CTkLabel(row, text=icon, font=ctk.CTkFont(size=12), text_color=theme["glow"])
            lbl_ic.pack(side="left", padx=(0, 8))
            lbl_txt = ctk.CTkLabel(row, text=desc, font=ctk.CTkFont(family="Segoe UI", size=11), text_color=COLOR_TEXT_GRAY)
            lbl_txt.pack(side="left")

        # Right Auth Form (410px)
        right_form = ctk.CTkFrame(
            split_container,
            fg_color=COLOR_INNER_CARD,
            border_color=COLOR_INNER_BORDER,
            border_width=1,
            corner_radius=14
        )
        right_form.pack(side="right", fill="both", expand=True, padx=(10, 0))

        lbl_form_title = ctk.CTkLabel(
            right_form,
            text="Авторизация",
            font=ctk.CTkFont(family="Segoe UI", size=20, weight="bold"),
            text_color=COLOR_TEXT_WHITE
        )
        lbl_form_title.pack(anchor="w", padx=26, pady=(24, 4))

        lbl_form_desc = ctk.CTkLabel(
            right_form,
            text="Войдите под вашей учётной записью Horus",
            font=ctk.CTkFont(family="Segoe UI", size=11),
            text_color=COLOR_TEXT_MUTED
        )
        lbl_form_desc.pack(anchor="w", padx=26, pady=(0, 16))

        saved_login = self.db.get("saved_login", "")

        self.entry_login = ctk.CTkEntry(
            right_form,
            placeholder_text="Логин или E-mail",
            height=42,
            corner_radius=10,
            fg_color=COLOR_INPUT_BG,
            border_color=COLOR_INPUT_BORDER,
            border_width=1,
            text_color=COLOR_TEXT_WHITE,
            font=ctk.CTkFont(family="Segoe UI", size=12)
        )
        if saved_login:
            self.entry_login.insert(0, saved_login)
        self.entry_login.pack(fill="x", padx=26, pady=(0, 10))

        pwd_frame = ctk.CTkFrame(right_form, fg_color="transparent")
        pwd_frame.pack(fill="x", padx=26, pady=(0, 10))

        self.entry_pwd = ctk.CTkEntry(
            pwd_frame,
            placeholder_text="Пароль",
            show="●",
            height=42,
            corner_radius=10,
            fg_color=COLOR_INPUT_BG,
            border_color=COLOR_INPUT_BORDER,
            border_width=1,
            text_color=COLOR_TEXT_WHITE,
            font=ctk.CTkFont(family="Segoe UI", size=12)
        )
        self.entry_pwd.pack(side="left", fill="x", expand=True)

        self.pwd_visible = False
        def toggle_pwd():
            self.pwd_visible = not self.pwd_visible
            self.entry_pwd.configure(show="" if self.pwd_visible else "●")
            btn_eye.configure(text="👁" if self.pwd_visible else "🔒")

        btn_eye = ctk.CTkButton(
            pwd_frame,
            text="🔒",
            width=42,
            height=42,
            corner_radius=10,
            fg_color=COLOR_INPUT_BG,
            border_color=COLOR_INPUT_BORDER,
            border_width=1,
            hover_color="#222838",
            command=toggle_pwd
        )
        btn_eye.pack(side="right", padx=(6, 0))

        rem_val = self.db.get("remember_me", False)
        self.chk_rem = ctk.CTkCheckBox(
            right_form,
            text="Запомнить данные для входа",
            font=ctk.CTkFont(family="Segoe UI", size=11),
            text_color=COLOR_TEXT_GRAY,
            fg_color=theme["primary"],
            hover_color=theme["hover"],
            border_color="#334155",
            corner_radius=6
        )
        if rem_val:
            self.chk_rem.select()
        self.chk_rem.pack(anchor="w", padx=26, pady=(0, 16))

        btn_submit = ctk.CTkButton(
            right_form,
            text="Войти в Horus",
            height=44,
            corner_radius=12,
            fg_color=theme["primary"],
            hover_color=theme["hover"],
            font=ctk.CTkFont(family="Segoe UI", size=13, weight="bold"),
            text_color=COLOR_TEXT_WHITE,
            command=self.handle_auth_submit
        )
        btn_submit.pack(fill="x", padx=26, pady=(0, 10))

        btn_guest = ctk.CTkButton(
            right_form,
            text="▷   Быстрый вход (Гостевой режим)",
            height=38,
            corner_radius=10,
            fg_color="#141926",
            border_color="#27334a",
            border_width=1,
            hover_color="#222838",
            font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
            text_color=COLOR_TEXT_GRAY,
            command=self.handle_guest_login
        )
        btn_guest.pack(fill="x", padx=26, pady=(0, 10))

    def handle_guest_login(self):
        self.current_user = "HorusPlayer"
        self.db["active"] = "HorusPlayer"
        save_accounts_db(self.db)
        self.show_main_view()

    def handle_auth_submit(self):
        login = self.entry_login.get().strip()
        pwd = self.entry_pwd.get()

        if not login:
            messagebox.showwarning("Внимание", "Введите логин или E-mail")
            return

        # Check in Supabase
        def worker():
            exists, profile = check_supabase_account_exists(login)
            if exists and profile:
                stored_hash = profile.get("password_hash", "")
                input_hash = base64.b64encode(pwd.encode("utf-8")).decode("utf-8") if pwd else ""
                if stored_hash and input_hash and stored_hash != input_hash:
                    self.after(0, lambda: messagebox.showerror("Ошибка", "Неверный пароль"))
                    return

                username = profile.get("username", login)
                self.current_user = username
                self.subscription_status = profile.get("subscription_till", "Навсегда")
            else:
                # If offline or test user, allow login
                self.current_user = login
                self.subscription_status = "Навсегда"

            self.db["active"] = self.current_user
            self.db["saved_login"] = self.current_user if self.chk_rem.get() else ""
            self.db["remember_me"] = bool(self.chk_rem.get())
            save_accounts_db(self.db)

            self.after(0, self.show_main_view)

        threading.Thread(target=worker, daemon=True).start()

    def handle_logout(self):
        self.current_user = ""
        self.db["active"] = ""
        save_accounts_db(self.db)
        self.show_auth_view()

    # -------------------------------------------------------------------------
    # SETTINGS VIEW (WIDESCREEN 840x560)
    # -------------------------------------------------------------------------
    def show_settings_view(self):
        self.clear_content()

        theme = self.get_theme_colors()
        self.create_top_bar(self.content_frame, return_to="settings")

        card = ctk.CTkFrame(
            self.content_frame,
            fg_color=COLOR_INNER_CARD,
            border_color=COLOR_INNER_BORDER,
            border_width=1,
            corner_radius=14
        )
        card.pack(fill="both", expand=True, pady=(10, 0))

        lbl_stitle = ctk.CTkLabel(
            card,
            text="НАСТРОЙКИ ЛАУНЧЕРА",
            font=ctk.CTkFont(family="Segoe UI", size=18, weight="bold"),
            text_color=COLOR_TEXT_WHITE
        )
        lbl_stitle.pack(anchor="w", padx=24, pady=(20, 14))

        # Themes
        lbl_theme = ctk.CTkLabel(
            card,
            text="Цветовая тема интерфейса",
            font=ctk.CTkFont(family="Segoe UI", size=12, weight="bold"),
            text_color=COLOR_TEXT_GRAY
        )
        lbl_theme.pack(anchor="w", padx=24, pady=(0, 8))

        theme_row = ctk.CTkFrame(card, fg_color="transparent")
        theme_row.pack(fill="x", padx=24, pady=(0, 20))

        current_t = self.get_setting("theme", "purple")

        for key, tdata in THEMES.items():
            is_sel = (key == current_t)
            t_btn = ctk.CTkButton(
                theme_row,
                text=f"{tdata['icon']}  {tdata['name']}",
                height=36,
                corner_radius=10,
                fg_color=tdata["primary"] if is_sel else "#181c26",
                border_color=tdata["glow"] if is_sel else "#2b3345",
                border_width=1.5 if is_sel else 1,
                hover_color=tdata["hover"],
                font=ctk.CTkFont(family="Segoe UI", size=11, weight="bold"),
                text_color=COLOR_TEXT_WHITE if is_sel else COLOR_TEXT_GRAY,
                command=lambda k=key: self.change_theme(k)
            )
            t_btn.pack(side="left", fill="x", expand=True, padx=4)

        # Back Button
        btn_back = ctk.CTkButton(
            card,
            text="← Назад на главную",
            height=40,
            corner_radius=10,
            fg_color=theme["primary"],
            hover_color=theme["hover"],
            font=ctk.CTkFont(family="Segoe UI", size=12, weight="bold"),
            text_color=COLOR_TEXT_WHITE,
            command=self.show_main_view
        )
        btn_back.pack(anchor="w", padx=24, pady=(20, 0))

    def change_theme(self, theme_key):
        self.set_setting("theme", theme_key)
        self.main_card.configure(border_color=self.get_theme_colors().get("glow", COLOR_CARD_BORDER))
        self.show_settings_view()

    # -------------------------------------------------------------------------
    # LAUNCH PROCESS
    # -------------------------------------------------------------------------
    def handle_launch(self):
        if not self.btn_launch_ref:
            return

        self.btn_launch_ref.configure(state="disabled", text="●   ПОДГОТОВКА К ЗАПУСКУ...")

        active_user = self.current_user or "HorusPlayer"
        ram_val = self.get_setting("ram", 4)
        fs_val = self.get_setting("fullscreen", True)

        def run_proc():
            env = dict(
                os.environ,
                HORUS_USER=str(active_user).strip(),
                HORUS_RAM=str(ram_val),
                HORUS_FULLSCREEN=str(fs_val)
            )

            bat_path = ROOT_DIR / "launch_game.bat"
            if not bat_path.exists():
                bat_path = LAUNCHER_DIR / "launch_game.bat"

            if bat_path.exists():
                try:
                    startupinfo = subprocess.STARTUPINFO()
                    startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
                    startupinfo.wShowWindow = 0
                    creationflags = 0x08000000
                    subprocess.Popen(
                        ["cmd.exe", "/c", str(bat_path)],
                        cwd=str(ROOT_DIR),
                        env=env,
                        startupinfo=startupinfo,
                        creationflags=creationflags
                    )
                except Exception as e:
                    self.after(0, lambda: messagebox.showerror("Ошибка запуска", str(e)))
                    self.after(0, lambda: self.btn_launch_ref.configure(text="▷   ЗАПУСТИТЬ", state="normal"))
                    return
            else:
                self.after(0, lambda: messagebox.showerror("Ошибка", f"Файл не найден: {bat_path}"))
                self.after(0, lambda: self.btn_launch_ref.configure(text="▷   ЗАПУСТИТЬ", state="normal"))
                return

            dots = ["●   ЗАПУСК MINECRAFT.", "●   ЗАПУСК MINECRAFT..", "●   ЗАПУСК MINECRAFT..."]
            step = 0
            start_wait = time.time()
            minecraft_started = False

            time.sleep(2.5)

            while time.time() - start_wait < 120:
                step = (step + 1) % len(dots)
                dot_text = dots[step]
                self.after(0, lambda t=dot_text: self.btn_launch_ref.configure(text=t) if self.btn_launch_ref else None)

                if is_minecraft_window_visible() or is_minecraft_process_running():
                    minecraft_started = True
                    break

                time.sleep(0.9)

            if minecraft_started:
                self.after(0, lambda: self.btn_launch_ref.configure(text="✓   МАЙНКРАФТ ЗАПУЩЕН!", fg_color=COLOR_STATUS_SUCCESS) if self.btn_launch_ref else None)
                time.sleep(1.0)
                self.after(0, self.destroy)
            else:
                self.after(0, lambda: self.btn_launch_ref.configure(text="▷   ЗАПУСТИТЬ", state="normal") if self.btn_launch_ref else None)

        threading.Thread(target=run_proc, daemon=True).start()

def main():
    app = HorusLauncherApp()
    app.mainloop()

if __name__ == "__main__":
    main()
