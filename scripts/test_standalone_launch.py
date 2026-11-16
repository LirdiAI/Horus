import os
import sys
import glob
import uuid
import shutil
import subprocess
from pathlib import Path

mc_dir = Path(os.environ.get("APPDATA", "")) / ".minecraft"
print("Minecraft dir:", mc_dir)

# Test import minecraft_launcher_lib
import minecraft_launcher_lib
print("minecraft_launcher_lib loaded successfully")

def find_java21():
    candidates = [
        r'C:\Program Files\Java\jdk-21*\bin\javaw.exe',
        r'C:\Program Files\Eclipse Adoptium\jdk-21*\bin\javaw.exe',
        r'C:\Program Files\BellSoft\LibericaJDK-21*\bin\javaw.exe',
        str(mc_dir / "runtime" / "java-runtime-delta" / "windows" / "java-runtime-delta" / "bin" / "javaw.exe"),
        str(mc_dir / "runtime" / "java-runtime-gamma" / "windows" / "java-runtime-gamma" / "bin" / "javaw.exe"),
        os.path.expandvars(r'%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21*\bin\javaw.exe')
    ]
    for pattern in candidates:
        for match in glob.glob(pattern):
            if os.path.exists(match):
                return match
    sh = shutil.which("javaw") or shutil.which("java")
    return sh or "javaw.exe"

print("Detected Java 21:", find_java21())

# Test finding the mod jar
mod_candidates = [
    Path(r"C:\Users\Никита\Downloads\HorusClient\build\libs\horus-client-38.1.5-recovered.jar"),
    Path(r"C:\Users\Никита\Downloads\deluxe-source\horus-client-38.1.5-recovered.jar"),
    mc_dir / "mods" / "horus-client-38.1.5-recovered.jar"
]
for m in mod_candidates:
    print(f"Mod candidate {m.name}: exists={m.exists()} ({m.stat().st_size if m.exists() else 0} bytes)")
