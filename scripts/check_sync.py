import pathlib

app_py = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py").read_text(encoding="utf-8")
index_html = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\index.html").read_text(encoding="utf-8")

print("index.html size:", len(index_html))
print("Is index.html embedded in launcher_app.py?", index_html in app_py)
