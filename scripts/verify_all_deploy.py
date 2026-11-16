import json
from pathlib import Path

# Verify accounts.json
accounts_path = Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\accounts.json")
with open(accounts_path, "r", encoding="utf-8") as f:
    data = json.load(f)
print("Accounts.json loaded:", data)

# Verify mod jar exists in run and .minecraft
run_mod = Path(r"C:\Users\Никита\Downloads\HorusClient\run\mods\horus-client-38.1.5-recovered.jar")
mc_mod = Path(r"C:\Users\Никита\AppData\Roaming\.minecraft\mods\horus-client-38.1.5-recovered.jar")
print("Run mod exists and size:", run_mod.exists(), run_mod.stat().st_size if run_mod.exists() else 0)
print("MC mod exists and size:", mc_mod.exists(), mc_mod.stat().st_size if mc_mod.exists() else 0)

# Verify desktop launcher
desktop_exe = Path(r"C:\Users\Никита\Desktop\HorusLauncher.exe")
print("Desktop exe exists and size:", desktop_exe.exists(), desktop_exe.stat().st_size if desktop_exe.exists() else 0)
