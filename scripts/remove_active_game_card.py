import pathlib, base64

master_p = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\index.html")
text = master_p.read_text(encoding="utf-8")

start = text.find('T.isRunning?f.jsxs(f.Fragment')
end = text.find(':null,f.jsxs(f.Fragment', start) + len(':null,')

if start != -1 and end != -1:
    new_text = text[:start] + text[end:]
    master_p.write_text(new_text, encoding="utf-8")
    print(f"Removed active game panel from {master_p}!")

    # Synchronize to other locations
    other_locations = [
        r"c:\Users\Никита\Downloads\HorusClient\run\launcher\index.html",
        r"c:\Users\Никита\Downloads\HorusClient\src\main\resources\assets\horus\launcher\index.html",
        r"c:\Users\Никита\Downloads\HorusClient\build\resources\main\assets\horus\launcher\index.html",
        r"c:\Users\Никита\Downloads\deluxe-source\launcher\index.html"
    ]
    for loc in other_locations:
        p = pathlib.Path(loc)
        if p.parent.exists():
            p.write_text(new_text, encoding="utf-8")
            print(f"Synced to {loc}")

    # Update INDEX_HTML_FALLBACK_BYTES in launcher_app.py
    app_py_paths = [
        pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py"),
        pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\run\launcher\launcher_app.py")
    ]
    b64_str = base64.b64encode(new_text.encode("utf-8")).decode("ascii")
    for app_p in app_py_paths:
        app_text = app_p.read_text(encoding="utf-8")
        s_idx = app_text.find("INDEX_HTML_FALLBACK_BYTES = ")
        if s_idx != -1:
            e_idx = app_text.find("ACTIVE_PORT = 38240", s_idx)
            if e_idx != -1:
                new_decl = f'INDEX_HTML_FALLBACK_BYTES = base64.b64decode({repr(b64_str)})\n'
                app_text = app_text[:s_idx] + new_decl + app_text[e_idx:]
                app_p.write_text(app_text, encoding="utf-8")
                print(f"Updated INDEX_HTML_FALLBACK_BYTES in {app_p}!")
else:
    print("Could not find start or end of active game card block!")
