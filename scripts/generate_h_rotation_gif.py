"""Generates a seamlessly looping GIF of the actual Horus client logo.

The real logo (white stylized H, assets/horus/textures/gui/mainmenu/icons/logo.png)
spins a full 360 degrees counterclockwise ("to the left") forever, with a soft
glow. No redesign - exactly the logo that already stands in the client.
"""
import math
import pathlib

import numpy as np
from PIL import Image, ImageFilter

ROOT = pathlib.Path(__file__).resolve().parent.parent
LOGO = ROOT / "src" / "main" / "resources" / "assets" / "horus" / "textures" / "gui" / "mainmenu" / "icons" / "logo.png"
OUT = ROOT / "assets" / "generated" / "horus_h_rotation.gif"
OUT.parent.mkdir(parents=True, exist_ok=True)

SIZE = 320
FRAMES = 64          # frames per full 360-degree turn
FPS = 25             # one full turn every 2.56 s
CX, CY = SIZE // 2, SIZE // 2

BG = (11, 13, 16)            # same as the client background
GLOW = (167, 139, 250)       # soft lavender glow

# ---- load the real logo and place it centered on a square tile ----
logo = Image.open(LOGO).convert("RGBA")
target_h = 190
scale = target_h / logo.height
logo = logo.resize((round(logo.width * scale), target_h), Image.LANCZOS)
lw, lh = logo.size
TILE = SIZE
tile = Image.new("RGBA", (TILE, TILE), (0, 0, 0, 0))
tile.paste(logo, (CX - lw // 2, CY - lh // 2), logo)


def rotate_ccw(angle_deg):
    """Rotate the logo counterclockwise (PIL positive angle = CCW = in left)."""
    return tile.rotate(angle_deg, resample=Image.BICUBIC, expand=False, fillcolor=(0, 0, 0, 0))


def ambient_bg(t):
    """Dark background with a very soft purple pulse behind the logo."""
    arr = np.zeros((SIZE, SIZE, 3), np.float32)
    arr[..., 0], arr[..., 1], arr[..., 2] = BG
    yy, xx = np.mgrid[0:SIZE, 0:SIZE]
    d = np.sqrt((xx - CX) ** 2 + (yy - CY) ** 2) / (SIZE * 0.7)
    pulse = 0.5 + 0.5 * math.sin(2 * math.pi * 2 * t)
    glow = np.clip(1.0 - d, 0, 1) ** 2 * (0.16 + 0.10 * pulse)
    tint = np.array([70, 40, 120], np.float32)
    arr += tint * glow[..., None]
    return Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGB")


print(f"Rendering {FRAMES} frames at {SIZE}x{SIZE} ({FPS} fps)...")
frames = []
for f in range(FRAMES):
    t = f / FRAMES
    frame = ambient_bg(t)

    rot = rotate_ccw(360 * t)          # full 360 degrees counterclockwise per loop
    mask = rot.split()[3]

    # wide soft glow + tighter inner glow behind the white logo
    for blur, boost in [(16, 0.75), (6, 0.9)]:
        gmask = mask.filter(ImageFilter.GaussianBlur(blur)).point(lambda v, b=boost: min(255, int(v * b)))
        frame.paste(Image.new("RGB", (TILE, TILE), GLOW), (0, 0), gmask)

    # the logo itself
    frame.paste(rot.convert("RGB"), (0, 0), mask)

    frames.append(frame)
    if (f + 1) % 16 == 0:
        print(f"  frame {f + 1}/{FRAMES}")

print("Saving GIF...")
frames[0].save(
    OUT,
    save_all=True,
    append_images=frames[1:],
    duration=1000 // FPS,
    loop=0,
    optimize=True,
)
print(f"Done! Saved to {OUT}")
print(f"Size: {OUT.stat().st_size / 1024:.1f} KB")
