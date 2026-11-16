# -*- coding: utf-8 -*-
import base64
from pathlib import Path

ROOT = Path(r"C:\Users\Никита\Downloads\HorusClient")
logo_path = ROOT / "launcher" / "logo.png"
with open(logo_path, "rb") as f:
    logo_b64 = base64.b64encode(f.read()).decode("ascii")

template_path = ROOT / "scripts" / "launcher_template.html"
with open(template_path, "r", encoding="utf-8") as f:
    html_content = f.read().replace("__LOGO_B64__", logo_b64)

destinations = [
    ROOT / "launcher" / "index.html",
    ROOT / "run" / "launcher" / "index.html",
    ROOT / "src" / "main" / "resources" / "assets" / "horus" / "launcher" / "index.html",
    Path(r"C:\Users\Никита\Downloads\deluxe-source\launcher\index.html")
]

for dst in destinations:
    dst.parent.mkdir(parents=True, exist_ok=True)
    with open(dst, "w", encoding="utf-8") as f:
        f.write(html_content)
    print(f"Wrote to {dst} ({len(html_content)} bytes)")
