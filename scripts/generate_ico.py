import os
from PIL import Image

launcher_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "launcher")
png_path = os.path.join(launcher_dir, "app_icon.png")
ico_path = os.path.join(launcher_dir, "launcher.ico")

img = Image.open(png_path)
img.save(ico_path, format="ICO", sizes=[(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
print(f"Generated launcher icon at {ico_path}, size: {os.path.getsize(ico_path)} bytes")
