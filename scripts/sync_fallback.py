with open(r"C:\Users\Никита\Downloads\HorusClient\launcher\index.html", "rb") as f:
    raw_bytes = f.read()

app_path = r"C:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py"
with open(app_path, "r", encoding="utf-8") as f:
    text = f.read()

prefix = 'INDEX_HTML_FALLBACK_BYTES = '
idx1 = text.find(prefix)
if idx1 != -1:
    idx2 = text.find('\n\nACTIVE_PORT =', idx1)
    if idx2 == -1:
        idx2 = text.find('\nACTIVE_PORT =', idx1)
    
    new_text = text[:idx1 + len(prefix)] + repr(raw_bytes) + text[idx2:]
    with open(app_path, "w", encoding="utf-8") as f:
        f.write(new_text)
    print("Embedded index.html bytes into launcher_app.py!")
else:
    print("Could not find fallback prefix in launcher_app.py")
