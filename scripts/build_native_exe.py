import os
import sys
import shutil
import subprocess

ROOT_DIR = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
LAUNCHER_DIR = os.path.join(ROOT_DIR, "launcher")
APP_PY = os.path.join(LAUNCHER_DIR, "native_launcher.py")
ICO_PATH = os.path.join(LAUNCHER_DIR, "launcher.ico")
ITEMS_DIR = os.path.join(LAUNCHER_DIR, "items")

cmd = [
    sys.executable,
    "-m", "PyInstaller",
    "--noconsole",
    "--onefile",
    "--collect-all", "customtkinter",
    f"--icon={ICO_PATH}",
    "--name=HorusLauncher",
    f"--add-data={os.path.join(LAUNCHER_DIR, 'banner_bg.png')};launcher",
    f"--add-data={os.path.join(LAUNCHER_DIR, 'steve_art.png')};launcher",
    f"--add-data={os.path.join(LAUNCHER_DIR, 'app_icon.png')};launcher",
    f"--add-data={os.path.join(LAUNCHER_DIR, 'launcher.ico')};launcher",
    f"--add-data={ITEMS_DIR};launcher/items",
    APP_PY
]

print("Executing PyInstaller build for PulseVisuals / Horus Native Launcher...")
res = subprocess.run(cmd, cwd=ROOT_DIR)

if res.returncode == 0:
    dist_exe = os.path.join(ROOT_DIR, "dist", "HorusLauncher.exe")
    target_exe = os.path.join(ROOT_DIR, "HorusLauncher.exe")
    launcher_exe = os.path.join(LAUNCHER_DIR, "HorusLauncher.exe")
    if os.path.exists(dist_exe):
        shutil.copy2(dist_exe, target_exe)
        shutil.copy2(dist_exe, launcher_exe)
        size_mb = os.path.getsize(target_exe) / (1024 * 1024)
        print(f"\n[SUCCESS] Built Native HorusLauncher.exe ({size_mb:.2f} MB)")
        print(f"Copied to: {target_exe}")
        print(f"Copied to: {launcher_exe}")
    else:
        print(f"Error: {dist_exe} not found after build.")
else:
    print(f"PyInstaller failed with returncode {res.returncode}")
