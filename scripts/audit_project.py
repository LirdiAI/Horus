import os
import glob
import re
import json

root = r"C:\Users\Никита\Downloads\HorusClient"

# 1. Modules audit
modules = glob.glob(os.path.join(root, "src", "main", "java", "horus", "client", "module", "**", "*.java"), recursive=True)
by_cat = {}
module_details = []

for m in modules:
    cat = os.path.basename(os.path.dirname(m))
    with open(m, "r", encoding="utf-8", errors="ignore") as f:
        content = f.read()
    match = re.search(r'@ModuleInfo\s*\(\s*name\s*=\s*"([^"]+)"(?:,\s*description\s*=\s*"([^"]+)")?', content)
    name = match.group(1) if match else os.path.basename(m)[:-5]
    desc = match.group(2) if match and match.group(2) else ""
    by_cat.setdefault(cat, []).append(name)
    module_details.append({"category": cat, "name": name, "file": os.path.basename(m), "desc": desc})

print("=== MODULES OVERVIEW ===")
for cat, names in sorted(by_cat.items()):
    print(f"{cat} ({len(names)}): {', '.join(sorted(names))}")

# 2. Commands audit
cmds = glob.glob(os.path.join(root, "src", "main", "java", "horus", "client", "command", "**", "*.java"), recursive=True)
cmd_names = [os.path.basename(c)[:-5] for c in cmds]
print("\n=== COMMANDS ===")
print(", ".join(sorted(cmd_names)))

# 3. Cosmetics & Shaders
shaders = glob.glob(os.path.join(root, "src", "main", "resources", "assets", "horus", "shaders", "**", "*.*"), recursive=True)
print("\n=== SHADERS ===")
print([os.path.basename(s) for s in shaders])

# 4. Launcher features
launcher_html = os.path.join(root, "launcher", "index.html")
if os.path.exists(launcher_html):
    size_kb = os.path.getsize(launcher_html) / 1024
    print(f"\n=== LAUNCHER HTML === Size: {size_kb:.1f} KB")

# 5. UI features
ui_files = glob.glob(os.path.join(root, "src", "main", "java", "horus", "client", "ui", "**", "*.java"), recursive=True)
print(f"\n=== UI CLASSES ({len(ui_files)}) ===")
print([os.path.basename(u)[:-5] for u in ui_files[:20]])
