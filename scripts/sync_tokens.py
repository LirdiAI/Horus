import os
import shutil
from pathlib import Path

target_file = Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py")
with open(target_file, "r", encoding="utf-8") as f:
    text = f.read()

target1 = """def save_username(name):
    try:
        session_file = ROOT_DIR / "horus_session.txt"
        with open(session_file, "w", encoding="utf-8") as f:
            f.write(name)
    except Exception:
        pass"""

replacement1 = """def save_username(name):
    try:
        session_file = ROOT_DIR / "horus_session.txt"
        with open(session_file, "w", encoding="utf-8") as f:
            f.write(name)
    except Exception:
        pass

def sync_pulse_token():
    try:
        appdata_token = Path(os.environ.get("APPDATA", "")) / ".minecraft" / "pulse_cache" / "cosmetics_token.txt"
        run_token = ROOT_DIR / "run" / "pulse_cache" / "cosmetics_token.txt"
        if appdata_token.exists() and appdata_token.stat().st_size > 10:
            run_token.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(appdata_token, run_token)
        elif run_token.exists() and run_token.stat().st_size > 10:
            appdata_token.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(run_token, appdata_token)
    except Exception:
        pass"""

assert target1 in text, "target1 not found"
text = text.replace(target1, replacement1, 1)

target2 = """            save_username(username)
            try:
                with open(ROOT_DIR / "horus_ram.txt", "w", encoding="utf-8") as f:
                    f.write(ram)
            except Exception:
                pass"""

replacement2 = """            save_username(username)
            sync_pulse_token()
            try:
                with open(ROOT_DIR / "horus_ram.txt", "w", encoding="utf-8") as f:
                    f.write(ram)
            except Exception:
                pass"""

assert target2 in text, "target2 not found"
text = text.replace(target2, replacement2, 1)

target3 = """def start_server():"""
replacement3 = """def start_server():
    sync_pulse_token()"""
assert target3 in text, "target3 not found"
text = text.replace(target3, replacement3, 1)

with open(target_file, "w", encoding="utf-8") as f:
    f.write(text)

print("Updated launcher_app.py successfully!")
