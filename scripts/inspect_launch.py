import pathlib

app_py = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py").read_text(encoding="utf-8")
lines = app_py.splitlines()
in_launch = False
launch_lines = []
for i, line in enumerate(lines):
    if "/api/launch" in line or "def handle_launch" in line or "def launch_minecraft" in line or "do_launch" in line:
        in_launch = True
    if in_launch:
        launch_lines.append(f"{i+1}: {line}")
        if len(launch_lines) > 100:
            break

print("\n".join(launch_lines))
