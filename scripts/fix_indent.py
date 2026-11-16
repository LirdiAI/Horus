app_path = r"C:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py"
with open(app_path, "r", encoding="utf-8") as f:
    text = f.read()

text = text.replace('\nif path == "/api/accounts":', '\n        if path == "/api/accounts":')
with open(app_path, "w", encoding="utf-8") as f:
    f.write(text)

# Also test python compile
import py_compile
py_compile.compile(app_path, doraise=True)
print("launcher_app.py compiled cleanly with zero syntax errors!")
