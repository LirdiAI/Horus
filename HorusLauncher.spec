# -*- mode: python ; coding: utf-8 -*-
from pathlib import Path
import os

SPEC_DIR = Path(SPEC).resolve().parent
launcher_dir = SPEC_DIR / "launcher"

datas = [
    (str(launcher_dir / "index.html"), "launcher"),
    (str(launcher_dir / "index.html"), "."),
    (str(launcher_dir / "launcher.ico"), "launcher"),
    (str(launcher_dir / "launcher.ico"), "."),
    (str(launcher_dir / "logo.png"), "launcher"),
    (str(launcher_dir / "app_icon.png"), "launcher"),
    (str(launcher_dir / "app_icon.png"), "."),
    (str(launcher_dir / "favicon.ico"), "launcher"),
    (str(launcher_dir / "favicon.ico"), "."),
    (str(launcher_dir / "user_avatar.png"), "launcher"),
    (str(launcher_dir / "user_avatar.png"), "."),
    (str(SPEC_DIR / "build" / "libs" / "horus-client-38.1.5-recovered.jar"), "mod")
]
binaries = []
hiddenimports = ['minecraft_launcher_lib', 'minecraft_launcher_lib.fabric', 'minecraft_launcher_lib.command', 'minecraft_launcher_lib.utils', 'minecraft_launcher_lib.install']

a = Analysis(
    [str(launcher_dir / "launcher_app.py")],
    pathex=[],
    binaries=binaries,
    datas=datas,
    hiddenimports=hiddenimports,
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=['tkinter', 'customtkinter', 'matplotlib', 'numpy', 'scipy', 'torch'],
    noarchive=False,
    optimize=1,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name='HorusLauncher',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon=[str(launcher_dir / "launcher.ico")],
)
