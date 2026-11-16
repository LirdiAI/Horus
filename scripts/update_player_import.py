import pathlib

# 1. Update ModuleManager.java
mm_path = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\module\ModuleManager.java")
text = mm_path.read_text(encoding="utf-8")
text = text.replace("import horus.client.module.render.FrameSyncModule;", "import horus.client.module.player.FrameSyncModule;")
mm_path.write_text(text, encoding="utf-8")

# 2. Update FrameTimer.java
ft_path = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\util\FrameTimer.java")
text = ft_path.read_text(encoding="utf-8")
text = text.replace("import horus.client.module.render.FrameSyncModule;", "import horus.client.module.player.FrameSyncModule;")
ft_path.write_text(text, encoding="utf-8")

# 3. Update EntityRendererMixin.java
er_path = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java\horus\mixin\EntityRendererMixin.java")
text = er_path.read_text(encoding="utf-8")
text = text.replace("import horus.client.module.render.FrameSyncModule;", "import horus.client.module.player.FrameSyncModule;")
er_path.write_text(text, encoding="utf-8")

print("All imports updated to horus.client.module.player.FrameSyncModule!")
