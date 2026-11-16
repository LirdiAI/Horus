import pathlib

spec_path = pathlib.Path(r"C:\Users\Никита\Downloads\HorusClient\HorusLauncher.spec")
content = spec_path.read_text(encoding="utf-8")

# Check if mod jar is in datas
if "horus-client-38.1.5-recovered.jar" not in content:
    old_datas = """datas = [
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
    (str(launcher_dir / "user_avatar.png"), ".")
]"""
    new_datas = """datas = [
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
]"""
    if old_datas in content:
        content = content.replace(old_datas, new_datas)
        spec_path.write_text(content, encoding="utf-8")
        print("Updated HorusLauncher.spec with mod jar!")
    else:
        print("old_datas pattern not matched, please check HorusLauncher.spec")
else:
    print("horus-client-38.1.5-recovered.jar already in HorusLauncher.spec")
