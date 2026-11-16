import subprocess
res = subprocess.run(["javap", "-p", "temp_decomp/net/minecraft/client/render/entity/model/PlayerCapeModel.class"], capture_output=True, text=True)
print(res.stdout)
