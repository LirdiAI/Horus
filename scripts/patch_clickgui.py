import pathlib

p = pathlib.Path(r"c:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\ClickGuiScreen.java")
text = p.read_text(encoding="utf-8")

# Add parentScreen field and setter
if "private Screen parentScreen;" not in text:
    target = "   private static ClickGuiScreen clickGuiScreen;"
    replacement = "   private static ClickGuiScreen clickGuiScreen;\n   private Screen parentScreen;\n\n   public void setParentScreen(Screen parent) {\n      this.parentScreen = parent;\n   }"
    text = text.replace(target, replacement)

# Update close() method to respect parentScreen
close_target = """         if (MinecraftClient.getInstance().currentScreen == this) {
            MinecraftClient.getInstance().setScreen(null);
         }"""
close_replacement = """         if (MinecraftClient.getInstance().currentScreen == this) {
            Screen back = this.parentScreen;
            this.parentScreen = null;
            MinecraftClient.getInstance().setScreen(back);
         }"""

if close_target in text:
    text = text.replace(close_target, close_replacement)
    p.write_text(text, encoding="utf-8")
    print("ClickGuiScreen updated with parentScreen support!")
else:
    print("close_target pattern not found, checking...")
