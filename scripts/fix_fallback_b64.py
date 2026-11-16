import pathlib
import base64
import re

app_py_path = pathlib.Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py")
content = app_py_path.read_text(encoding="utf-8")

master_index_bytes = pathlib.Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\index.html").read_bytes()
b64_str = base64.b64encode(master_index_bytes).decode('ascii')

# Replace INDEX_HTML_FALLBACK_BYTES with base64 decode
# Find where INDEX_HTML_FALLBACK_BYTES begins and ends
start_idx = content.find("INDEX_HTML_FALLBACK_BYTES = ")
if start_idx != -1:
    end_idx = content.find("ACTIVE_PORT = 38240", start_idx)
    if end_idx != -1:
        new_decl = f'INDEX_HTML_FALLBACK_BYTES = base64.b64decode({repr(b64_str)})\n'
        content = content[:start_idx] + new_decl + content[end_idx:]
        app_py_path.write_text(content, encoding="utf-8")
        print("Successfully replaced INDEX_HTML_FALLBACK_BYTES using clean base64!")
    else:
        print("ACTIVE_PORT not found after INDEX_HTML_FALLBACK_BYTES")
else:
    print("INDEX_HTML_FALLBACK_BYTES start not found")
