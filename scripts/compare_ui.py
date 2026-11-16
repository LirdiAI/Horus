from PIL import Image

p1 = r"C:\Users\Никита\.gemini\antigravity\brain\461fbe3e-6ffe-4a62-9e70-0da80a564412\.user_uploaded\media_1793640957195_224a6cfd.png"
p2 = r"C:\Users\Никита\.gemini\antigravity\brain\461fbe3e-6ffe-4a62-9e70-0da80a564412\.user_uploaded\media_1793645774430_d21fe6fb.png"

im1 = Image.open(p1)
im2 = Image.open(p2)

print("Reference size:", im1.size)
print("Current size:  ", im2.size)
