path = r'C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\HorusClient.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

target = 'local6629_1.add(new horus.client.command.SafeModCommand("safe"));'
idx = text.find(target)
print("Found target at:", idx)
if idx != -1:
    replacement = target + '\n\n      local6629_1.add(new horus.client.command.CloudCommand("cloud"));\n\n      local6629_1.add(new horus.client.command.CloudCommand("share"));'
    new_text = text.replace(target, replacement, 1)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new_text)
    print("Successfully patched HorusClient.java!")
