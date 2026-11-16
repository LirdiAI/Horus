import os
import sys
import shutil
import subprocess

ROOT_DIR = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
LAUNCHER_DIR = os.path.join(ROOT_DIR, "launcher")
SPEC_FILE = os.path.join(ROOT_DIR, "HorusLauncher.spec")

# Kill any existing HorusLauncher.exe process to avoid file lock
try:
    subprocess.run(["taskkill", "/f", "/im", "HorusLauncher.exe"], capture_output=True)
except Exception:
    pass

cmd = [
    sys.executable,
    "-m", "PyInstaller",
    SPEC_FILE,
    "--noconfirm"
]

print("Executing PyInstaller build from spec:", SPEC_FILE)
res = subprocess.run(cmd, cwd=ROOT_DIR)

if res.returncode == 0:
    dist_exe = os.path.join(ROOT_DIR, "dist", "HorusLauncher.exe")
    target_exe = os.path.join(ROOT_DIR, "HorusLauncher.exe")
    launcher_exe = os.path.join(LAUNCHER_DIR, "HorusLauncher.exe")
    desktop_exe = os.path.join(os.path.expanduser("~"), "Desktop", "HorusLauncher.exe")
    system_dlc_exe = os.path.join(os.path.expanduser("~"), "Desktop", "SystemDLC Launcher.exe")
    deluxe_exe = r"C:\Users\Никита\Downloads\deluxe-source\HorusLauncher.exe"
    
    if os.path.exists(dist_exe):
        shutil.copy2(dist_exe, target_exe)
        shutil.copy2(dist_exe, launcher_exe)
        if os.path.exists(os.path.dirname(desktop_exe)):
            try:
                shutil.copy2(dist_exe, desktop_exe)
                print("Deployed to Desktop:", desktop_exe)
            except Exception as e:
                print("Error deploying to desktop:", e)
            try:
                shutil.copy2(dist_exe, system_dlc_exe)
                print("Deployed to Desktop SystemDLC:", system_dlc_exe)
            except Exception as e:
                print("Error deploying to system_dlc_exe:", e)
        if os.path.exists(os.path.dirname(deluxe_exe)):
            try:
                shutil.copy2(dist_exe, deluxe_exe)
                print("Deployed to deluxe-source:", deluxe_exe)
            except Exception as e:
                print("Error deploying to deluxe-source:", e)
        size_mb = os.path.getsize(target_exe) / (1024 * 1024)
        print(f"Build SUCCESS: {target_exe} ({size_mb:.2f} MB)")
    else:
        print("ERROR: Built executable not found in dist/")
        sys.exit(1)
else:
    print("ERROR: PyInstaller failed with code", res.returncode)
    sys.exit(res.returncode)
