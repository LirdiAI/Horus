import difflib

f1 = r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\MainMenuScreen.java"
f2 = r"C:\Users\Никита\Downloads\deluxe-source\src\main\java\horus\client\ui\MainMenuScreen.java"

with open(f1, "r", encoding="utf-8", errors="ignore") as a:
    lines1 = a.readlines()

with open(f2, "r", encoding="utf-8", errors="ignore") as b:
    lines2 = b.readlines()

diff = list(difflib.unified_diff(lines2, lines1, fromfile="deluxe-source", tofile="HorusClient"))
for line in diff[:100]:
    print(line, end="")
