import pathlib

app_py = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py").read_text(encoding="utf-8")
lines = app_py.splitlines()
for i, line in enumerate(lines):
    if 'path == "/api/launch"' in line:
        for j in range(i, min(len(lines), i + 120)):
            print(f"{j+1}: {lines[j]}")
        break
