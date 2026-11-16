from pathlib import Path

target_file = Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\native_launcher.py")
with open(target_file, "r", encoding="utf-8") as f:
    text = f.read()

target1 = """    def check_pulse_token_exists(self):
        try:
            p = Path(os.environ.get("APPDATA", "")) / ".minecraft" / "pulse_cache" / "cosmetics_token.txt"
            if p.exists() and len(p.read_text(encoding="utf-8").strip()) > 10:
                return True
        except Exception:
            pass
        return False

    def toggle_pulse_auth(self):
        p = Path(os.environ.get("APPDATA", "")) / ".minecraft" / "pulse_cache" / "cosmetics_token.txt"
        if self.check_pulse_token_exists():
            try:
                p.unlink(missing_ok=True)
            except Exception:
                pass"""

replacement1 = """    def check_pulse_token_exists(self):
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
                pass"""

assert target1 in text, "target1 not found"
text = text.replace(target1, replacement1, 1)

target2 = """                            tok = pdata.get("token", "")
                            p.parent.mkdir(parents=True, exist_ok=True)
                            p.write_text(tok, encoding="utf-8")"""

replacement2 = """                            tok = pdata.get("token", "")
                            p1.parent.mkdir(parents=True, exist_ok=True)
                            p1.write_text(tok, encoding="utf-8")
                            p2.parent.mkdir(parents=True, exist_ok=True)
                            p2.write_text(tok, encoding="utf-8")"""

assert target2 in text, "target2 not found"
text = text.replace(target2, replacement2, 1)

with open(target_file, "w", encoding="utf-8") as f:
    f.write(text)

print("Updated native_launcher.py successfully!")
