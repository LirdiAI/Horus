import minecraft_launcher_lib
import os
import pathlib

mc_dir = os.path.expandvars(r"%APPDATA%\.minecraft")
print("MC dir:", mc_dir)
installed = [v["id"] for v in minecraft_launcher_lib.utils.get_installed_versions(mc_dir)]
print("Installed versions in .minecraft:", installed)
