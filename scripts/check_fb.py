import pathlib, re

app_py = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py").read_text(encoding="utf-8")
m = re.search(r'INDEX_HTML_FALLBACK_BYTES = b"""([\s\S]*?)"""', app_py)
if m:
    print("Found fallback bytes in app_py! Length:", len(m.group(1)))
    fb = m.group(1)
    print("Has compOpen in fallback:", "compOpen" in fb)
    print("Has AutomationControlled in fallback:", "AutomationControlled" in app_py)
else:
    print("No fallback bytes found!")
