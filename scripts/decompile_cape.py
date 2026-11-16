import zipfile
import glob
import subprocess
import os

jars = glob.glob(r"C:\Users\Никита\.gradle\caches\fabric-loom\**\*yarn*.jar", recursive=True)
for j in jars:
    if "1.21.11" in j:
        with zipfile.ZipFile(j, 'r') as zf:
            for n in zf.namelist():
                if "CapeFeatureRenderer" in n and n.endswith(".class"):
                    print("Found class:", n)
                    zf.extract(n, "temp_decomp")
                    subprocess.run(["javap", "-p", "-c", os.path.join("temp_decomp", n)])
                    break
            for n in zf.namelist():
                if "CapeModel" in n and n.endswith(".class") or "PlayerCapeModel" in n:
                    print("Found model:", n)
                    zf.extract(n, "temp_decomp")
                    subprocess.run(["javap", "-p", "-c", os.path.join("temp_decomp", n)])
                    break
        break
