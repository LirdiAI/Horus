import pathlib, re

app_py = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py").read_text(encoding="utf-8")
for i, line in enumerate(app_py.splitlines()):
    if "index.html" in line:
        print(f"{i+1}: {line}")
