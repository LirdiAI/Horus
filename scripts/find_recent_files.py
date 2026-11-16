import os
import time

now = time.time()
cutoff = now - 2 * 3600  # last 2 hours

for root_dir in [r"C:\Users\Никита\Downloads\deluxe-source", r"C:\Users\Никита\Downloads\HorusClient"]:
    print(f"--- Checking {root_dir} ---")
    for r, d, files in os.walk(root_dir):
        if ".gradle" in r or "build" in r or ".git" in r or "__pycache__" in r:
            continue
        for f in files:
            p = os.path.join(r, f)
            try:
                mtime = os.path.getmtime(p)
                if mtime > cutoff:
                    print(f"{time.ctime(mtime)}: {p}")
            except Exception:
                pass
