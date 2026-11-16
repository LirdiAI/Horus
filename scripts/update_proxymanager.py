from pathlib import Path

path = Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\proxy\ProxyManager.java")
with open(path, "r", encoding="utf-8") as f:
    text = f.read()

lines = text.splitlines(keepends=True)
new_lines = []
for line in lines:
    if "Local SOCKS5" in line or "Tor Network" in line or "// Default presets" in line:
        continue
    new_lines.append(line)

with open(path, "w", encoding="utf-8") as f:
    f.writelines(new_lines)

print("ProxyManager.java successfully cleaned of presets!")
