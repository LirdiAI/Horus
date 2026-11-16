import pathlib

app_py = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\launcher_app.py").read_text(encoding="utf-8")
idx = app_py.find("api/launch")
while idx != -1:
    print(app_py[idx-100:idx+600])
    print("="*40)
    idx = app_py.find("api/launch", idx+1)
