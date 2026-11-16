import zipfile, glob
jars = glob.glob(r"C:\Users\Никита\.gradle\caches\fabric-loom\**\*yarn*1_21_11*.jar", recursive=True)
for j in jars:
    with zipfile.ZipFile(j, 'r') as zf:
        for n in zf.namelist():
            if "Elytra" in n:
                print("Found:", n)
    break
