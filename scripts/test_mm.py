import pathlib

p = pathlib.Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\MainMenuScreen.java")
content = p.read_text(encoding="utf-8")

# Let's inspect current fields
print("Length:", len(content))
