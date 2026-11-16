import minecraft_launcher_lib

versions = minecraft_launcher_lib.fabric.get_all_loader_versions()
print("Fabric loader versions available:", len(versions))
if versions:
    print("Latest loader version:", versions[0]["version"])
