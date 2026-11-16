import pathlib
import re

app_py_path = pathlib.Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py")
content = app_py_path.read_text(encoding="utf-8")

# 1. Update INDEX_HTML_FALLBACK_BYTES with master index.html
master_index = pathlib.Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\index.html").read_text(encoding="utf-8")
pattern = r'INDEX_HTML_FALLBACK_BYTES = b"""[\s\S]*?"""'
if not re.search(pattern, content):
    pattern = r"INDEX_HTML_FALLBACK_BYTES = b'[\s\S]*?'"

if re.search(pattern, content):
    # Escape quotes properly
    # Using raw string or base64 or raw triple double quotes
    escaped = master_index.replace('\\', '\\\\').replace('"', '\\"')
    # Safer: write as raw bytes or read from file at runtime, but having raw fallback
    replacement = f'INDEX_HTML_FALLBACK_BYTES = """{escaped}""".encode("utf-8")'
    content = re.sub(pattern, replacement, content)
    print("Replaced INDEX_HTML_FALLBACK_BYTES!")
else:
    print("Pattern for INDEX_HTML_FALLBACK_BYTES not found!")

# 2. Add deploy_mod_jar function
deploy_func = '''
def ensure_mod_deployed():
    try:
        mc_mods = Path(os.path.expandvars(r"%APPDATA%\\.minecraft\\mods"))
        mc_mods.mkdir(parents=True, exist_ok=True)
        dest_jar = mc_mods / "horus-client-38.1.5-recovered.jar"

        candidates = [
            BUNDLE_DIR / "mod" / "horus-client-38.1.5-recovered.jar",
            ROOT_DIR / "build" / "libs" / "horus-client-38.1.5-recovered.jar",
            ROOT_DIR / "horus-client-38.1.5-recovered.jar",
            Path.home() / "Downloads" / "HorusClient" / "build" / "libs" / "horus-client-38.1.5-recovered.jar",
            Path.home() / "Downloads" / "deluxe-source" / "horus-client-38.1.5-recovered.jar",
        ]
        for c in candidates:
            if c.exists() and c.is_file() and c.stat().st_size > 1000000:
                if not dest_jar.exists() or dest_jar.stat().st_size != c.stat().st_size:
                    shutil.copy2(c, dest_jar)
                    print(f"[Horus] Deployed mod jar to {dest_jar} ({c.stat().st_size} bytes)")
                break
    except Exception as e:
        print(f"[Horus] Warning deploying mod: {e}")
'''

if "def ensure_mod_deployed():" not in content:
    # insert before def get_index_html_bytes
    idx = content.find("def get_index_html_bytes():")
    if idx != -1:
        content = content[:idx] + deploy_func + "\n\n" + content[idx:]
        print("Inserted ensure_mod_deployed()!")

# 3. Add standalone launch logic if gradlew.bat not found
standalone_launch_code = '''
def launch_standalone_fabric(username="Lumira", ram="6", fullscreen=False):
    ensure_mod_deployed()
    mc_dir = os.path.expandvars(r"%APPDATA%\\.minecraft")
    try:
        import minecraft_launcher_lib
        installed = [v["id"] for v in minecraft_launcher_lib.utils.get_installed_versions(mc_dir)]
        target_version = None
        for v in installed:
            if "fabric" in v.lower() and "1.21.11" in v:
                target_version = v
                break
        if not target_version:
            for v in installed:
                if "1.21.11" in v:
                    target_version = v
                    break
        if not target_version:
            for v in installed:
                if "fabric" in v.lower():
                    target_version = v
                    break

        if not target_version:
            print("[Horus] Installing Fabric 1.21.11...")
            minecraft_launcher_lib.fabric.install_fabric("1.21.11", mc_dir)
            target_version = "fabric-loader-0.16.14-1.21.11"

        # Find Java 21
        java_cmd = "javaw.exe"
        candidates = [
            r"C:\\Program Files\\Java\\jdk-21.0.11\\bin\\javaw.exe",
            r"C:\\Program Files\\Java\\jdk-21\\bin\\javaw.exe",
            r"C:\\Program Files\\Eclipse Adoptium\\jdk-21\\bin\\javaw.exe",
            os.path.expandvars(r"%LOCALAPPDATA%\\Programs\\Eclipse Adoptium\\jdk-21\\bin\\javaw.exe"),
            os.path.expandvars(r"%APPDATA%\\.minecraft\\runtime\\java-runtime-gamma\\windows\\java-runtime-gamma\\bin\\javaw.exe"),
            os.path.expandvars(r"%APPDATA%\\.minecraft\\runtime\\java-runtime-delta\\windows\\java-runtime-delta\\bin\\javaw.exe")
        ]
        for c in candidates:
            if os.path.exists(c):
                java_cmd = c
                break

        options = {
            "username": username,
            "uuid": "",
            "token": "",
            "jvmArguments": [f"-Xmx{ram}G", f"-Xms2G"],
            "executablePath": java_cmd
        }
        cmd = minecraft_launcher_lib.command.get_minecraft_command(target_version, mc_dir, options)
        subprocess.Popen(cmd, cwd=mc_dir)
        return True
    except Exception as ex:
        print(f"[Horus] Standalone launch exception: {ex}")
        return False
'''

if "def launch_standalone_fabric(" not in content:
    idx = content.find("def main():")
    if idx != -1:
        content = content[:idx] + standalone_launch_code + "\n\n" + content[idx:]
        print("Inserted launch_standalone_fabric()!")

# 4. In /api/launch handler, call ensure_mod_deployed() and fallback to launch_standalone_fabric
old_launch_block = '''            try:
                if vbs_path.exists():'''

new_launch_block = '''            try:
                ensure_mod_deployed()
                is_dev = (ROOT_DIR / "gradlew.bat").exists()
                if is_dev and vbs_path.exists():'''

if old_launch_block in content:
    content = content.replace(old_launch_block, new_launch_block)
    print("Updated /api/launch to check is_dev!")

old_fallback = '''                else:
                    raise FileNotFoundError(f"Launch scripts not found in {ROOT_DIR}")'''

new_fallback = '''                else:
                    ok = launch_standalone_fabric(username, ram, fullscreen)
                    if not ok:
                        raise RuntimeError("Не удалось запустить Fabric Minecraft через автономный загрузчик")'''

if old_fallback in content:
    content = content.replace(old_fallback, new_fallback)
    print("Updated fallback to launch_standalone_fabric!")

# 5. In main(), call ensure_mod_deployed() at startup
if "ensure_mod_deployed()" not in content[content.find("def main():"):]:
    content = content.replace("def main():\n    global last_activity, has_connected", "def main():\n    global last_activity, has_connected\n    ensure_mod_deployed()")
    print("Added ensure_mod_deployed() to main()!")

app_py_path.write_text(content, encoding="utf-8")
print("Saved patched launcher_app.py successfully!")
