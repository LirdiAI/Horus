import pathlib

p = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\HorusLauncher.spec")
text = p.read_text(encoding="utf-8")
text = text.replace("hiddenimports = []", "hiddenimports = ['minecraft_launcher_lib', 'minecraft_launcher_lib.fabric', 'minecraft_launcher_lib.command', 'minecraft_launcher_lib.utils', 'minecraft_launcher_lib.install']")
p.write_text(text, encoding="utf-8")
print("HorusLauncher.spec updated with hiddenimports!")
