import subprocess, os
res = subprocess.run(["javap", "-p", "-c", "temp_decomp/net/minecraft/client/render/entity/feature/CapeFeatureRenderer.class"], capture_output=True, text=True)
for line in res.stdout.splitlines():
    print(line)
