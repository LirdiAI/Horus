import os
from PIL import Image, ImageDraw

launcher_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "launcher")
items_dir = os.path.join(launcher_dir, "items")
os.makedirs(items_dir, exist_ok=True)

items = [
    ("emote_67.png", "67", (130, 80, 240)),
    ("emote_twerk.png", "💃", (160, 90, 255)),
    ("wings_angel.png", "🪽", (100, 150, 255)),
    ("cape_wave.png", "🧣", (140, 70, 220)),
    ("crown_elite.png", "👑", (255, 200, 50)),
    ("aura_glow.png", "✨", (180, 100, 255))
]

for filename, text, color in items:
    path = os.path.join(items_dir, filename)
    img = Image.new("RGBA", (90, 75), (20, 22, 32, 255))
    draw = ImageDraw.Draw(img)
    draw.rounded_rectangle([0, 0, 89, 74], radius=10, fill=(24, 27, 38, 255), outline=(36, 42, 58, 255), width=1)
    # Draw soft glow in center
    draw.ellipse([25, 17, 65, 57], fill=(color[0]//4, color[1]//4, color[2]//4, 100))
    # Draw text/emoji in center
    draw.text((45, 37), text, fill=(color[0], color[1], color[2], 255), anchor="mm")
    img.save(path)

print("Generated all 6 cosmetics thumbnails!")
