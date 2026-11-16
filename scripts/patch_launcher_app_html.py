# -*- coding: utf-8 -*-
from pathlib import Path

ROOT = Path(r"C:\Users\Никита\Downloads\HorusClient")
launcher_py = ROOT / "launcher" / "launcher_app.py"
index_html = ROOT / "launcher" / "index.html"

with open(index_html, "rb") as f:
    html_bytes = f.read()

with open(launcher_py, "r", encoding="utf-8") as f:
    py_text = f.read()

# Replace INDEX_HTML_FALLBACK_BYTES
start_marker = "INDEX_HTML_FALLBACK_BYTES = "
end_marker = "\nACTIVE_PORT = "

idx1 = py_text.find(start_marker)
idx2 = py_text.find(end_marker)

if idx1 != -1 and idx2 != -1:
    new_fallback = f"INDEX_HTML_FALLBACK_BYTES = {repr(html_bytes)}\n"
    py_text = py_text[:idx1] + new_fallback + py_text[idx2+1:]
    print("Updated INDEX_HTML_FALLBACK_BYTES")
else:
    print("Could not find INDEX_HTML_FALLBACK_BYTES markers")

# Also copy to run/launcher/launcher_app.py
with open(launcher_py, "w", encoding="utf-8") as f:
    f.write(py_text)

run_py = ROOT / "run" / "launcher" / "launcher_app.py"
run_py.parent.mkdir(parents=True, exist_ok=True)
with open(run_py, "w", encoding="utf-8") as f:
    f.write(py_text)

print("Saved launcher_app.py")
