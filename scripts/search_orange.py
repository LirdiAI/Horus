import pathlib, re

src_dir = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java")
orange_patterns = [
    re.compile(r'0x[0-9a-fA-F]*[Ff][Ff][89aAbBcCdDeE][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]', re.I),
    re.compile(r'orange', re.I),
    re.compile(r'#f97316', re.I),
    re.compile(r'#ff8800', re.I),
    re.compile(r'#fb923c', re.I),
    re.compile(r'#fdba74', re.I),
]

for p in src_dir.rglob("*.java"):
    if "MainMenuScreen" in p.name or "FallenUi" in p.name or "Screen" in p.name:
        content = p.read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(content.splitlines()):
            if "Color(" in line or "COLOR" in line or "0xFF" in line or "0xff" in line:
                # check if line contains warm orange/amber like FF88, FF99, FFA5, FF77, FF66
                if re.search(r'0x(?:ff|FF)(?:f|F)[0-9a-fA-F][0-9a-fA-F]{4}', line) or \
                   re.search(r'0x(?:ff|FF)(?:e|E)[0-9a-fA-F][0-9a-fA-F]{4}', line) or \
                   re.search(r'orange', line, re.I):
                    print(f"{p.name}:{i+1}: {line.strip()}")
