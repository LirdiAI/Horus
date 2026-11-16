from PIL import Image

p1 = r"C:\Users\Никита\.gemini\antigravity\brain\461fbe3e-6ffe-4a62-9e70-0da80a564412\.user_uploaded\media_1793640957195_224a6cfd.png"
p2 = r"C:\Users\Никита\.gemini\antigravity\brain\461fbe3e-6ffe-4a62-9e70-0da80a564412\.user_uploaded\media_1793645774430_d21fe6fb.png"

im1 = Image.open(p1)
im2 = Image.open(p2)
# sample colors from im1
# Background near top-left of modal
print("Reference modal bg approx:", im1.getpixel((im1.width // 2, 50)))
# Button color
print("Reference button approx:", im1.getpixel((im1.width // 2, int(im1.height * 0.72))))
# Bottom profile bg
print("Reference profile bg approx:", im1.getpixel((im1.width // 2, int(im1.height * 0.88))))

# Let's save a side-by-side comparison for analysis
w = max(im1.width, im2.width)
h = max(im1.height, im2.height)
comp = Image.new("RGBA", (im1.width + im2.width + 20, max(im1.height, im2.height)), (20, 20, 20, 255))
comp.paste(im1, (0, 0))
comp.paste(im2, (im1.width + 20, 0))
comp.save(r"C:\Users\Никита\.gemini\antigravity\brain\461fbe3e-6ffe-4a62-9e70-0da80a564412\ui_comparison.png")
print("Saved comparison to ui_comparison.png")
