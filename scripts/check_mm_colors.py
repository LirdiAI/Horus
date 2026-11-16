import pathlib, re

content = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\MainMenuScreen.java").read_text(encoding="utf-8")
for i, line in enumerate(content.splitlines()):
    if "0x" in line or "Color" in line or "UiTheme" in line:
        print(f"{i+1}: {line.strip()}")
