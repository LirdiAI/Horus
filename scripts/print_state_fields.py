import zipfile, glob, subprocess, os
jars = glob.glob(r"C:\Users\Никита\.gradle\caches\fabric-loom\**\*yarn*1_21_11*.jar", recursive=True)
for j in jars:
    with zipfile.ZipFile(j, 'r') as zf:
        for n in zf.namelist():
            if "PlayerEntityRenderState" in n and n.endswith(".class"):
                zf.extract(n, "temp_decomp")
                res = subprocess.run(["javap", "-p", os.path.join("temp_decomp", n)], capture_output=True, text=True)
                for line in res.stdout.splitlines():
                    if "float" in line or "cape" in line.lower():
                        print(line)
                break
    break
