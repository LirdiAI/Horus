import pathlib

text = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\launcher\index.html").read_text(encoding="utf-8")
idx = text.find("T.isRunning")
print("Found T.isRunning at:", idx)
while idx != -1:
    print("--- AT", idx, "---")
    print(text[idx-50:idx+400])
    idx = text.find("T.isRunning", idx+1)
