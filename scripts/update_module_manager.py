import pathlib

p = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\module\ModuleManager.java")
text = p.read_text(encoding="utf-8")

if "import horus.client.module.render.FrameSyncModule;" not in text:
    text = text.replace("import horus.client.module.render.NoRenderModule;", "import horus.client.module.render.NoRenderModule;\nimport horus.client.module.render.FrameSyncModule;")

if "registerModule(FrameSyncModule.getInstance());" not in text:
    text = text.replace("registerModule(NoRenderModule.getInstance());", "registerModule(NoRenderModule.getInstance());\n         registerModule(FrameSyncModule.getInstance());")

p.write_text(text, encoding="utf-8")
print("ModuleManager.java updated with FrameSyncModule registration!")
