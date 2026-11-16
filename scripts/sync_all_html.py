import pathlib

locations = [
    r"c:\Users\Никита\Downloads\HorusClient\launcher\index.html",
    r"c:\Users\Никита\Downloads\HorusClient\run\launcher\index.html",
    r"c:\Users\Никита\Downloads\HorusClient\src\main\resources\assets\horus\launcher\index.html",
    r"c:\Users\Никита\Downloads\HorusClient\build\resources\main\assets\horus\launcher\index.html",
    r"c:\Users\Никита\Downloads\deluxe-source\launcher\index.html"
]

src_html = pathlib.Path(locations[0]).read_text(encoding="utf-8")
print(f"Master index.html size: {len(src_html)}")

for loc in locations[1:]:
    p = pathlib.Path(loc)
    if p.exists():
        p_html = p.read_text(encoding="utf-8")
        print(f"{loc}: exists, size {len(p_html)}, matches master: {p_html == src_html}")
    else:
        print(f"{loc}: does not exist, creating from master...")
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(src_html, encoding="utf-8")
        print(f"{loc}: created!")
