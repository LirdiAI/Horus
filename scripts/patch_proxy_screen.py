import pathlib

p = pathlib.Path(r"C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\ProxyScreen.java")
content = p.read_text(encoding="utf-8")

# Add AnimatedValue imports if needed
if "import horus.client.ui.AnimatedValue;" not in content:
    content = content.replace("import horus.client.proxy.ProxyManager;", "import horus.client.proxy.ProxyManager;\nimport horus.client.ui.AnimatedValue;\nimport horus.client.ui.UiTheme;")

# Add openAnim, closeAnim, closing fields
fields_target = '   private final List<ClickArea> clickAreas = new ArrayList<>();'
fields_replacement = '''   private final List<ClickArea> clickAreas = new ArrayList<>();
   private final AnimatedValue openAnim = new AnimatedValue(260L);
   private final AnimatedValue closeAnim = new AnimatedValue(180L);
   private boolean closing = false;'''
content = content.replace(fields_target, fields_replacement)

# Update close()
close_target = '''   @Override
   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }'''

close_replacement = '''   @Override
   public void close() {
      if (!this.closing) {
         this.closing = true;
         this.closeAnim.getAnimatedValue(0.0);
      }
   }'''
content = content.replace(close_target, close_replacement)

# Update init() to reset openAnim
init_code = '''   @Override
   protected void init() {
      super.init();
      this.closing = false;
      this.openAnim.getAnimatedValue(0.0);
      this.closeAnim.getAnimatedValue(0.0);
   }
'''
content = content.replace("   @Override\n   public boolean shouldPause()", init_code + "\n   @Override\n   public boolean shouldPause()")

# Update render() to handle animation and drift
render_start_target = '''      Render2D.renderElement5(drawContext);

      // Deep solid minimalist dark background (#0B0D10)
      Render2D.processData51(0.0F, 0.0F, screenW, screenH, 0.0F, FallenUiHelper.COLOR_BG);'''

render_start_replacement = '''      Render2D.renderElement5(drawContext);

      this.openAnim.processData(1.0);
      float oVal = UiTheme.computeFloat2((float) this.openAnim.computeDouble());

      if (this.closing) {
         this.closeAnim.processData(1.0);
         float cVal = UiTheme.computeFloat2((float) this.closeAnim.computeDouble());
         if (cVal >= 0.98F) {
            if (this.client != null) {
               this.client.setScreen(this.parent);
            }
            return;
         }
         oVal *= (1.0F - cVal);
      }

      // Deep solid minimalist dark background (#0B0D10)
      Render2D.processData51(0.0F, 0.0F, screenW, screenH, 0.0F, FallenUiHelper.COLOR_BG);

      if (oVal < 0.999F) {
         Render2D.processData46(Math.max(0.01F, oVal));
      }'''
content = content.replace(render_start_target, render_start_replacement)

# Add drift to cardY
card_target = '''      float cardX = (screenW - cardW) / 2.0F;
      float cardY = (screenH - cardH) / 2.0F;'''

card_replacement = '''      float cardX = (screenW - cardW) / 2.0F;
      float enterDrift = (1.0F - oVal) * 16.0F * sf();
      float cardY = (screenH - cardH) / 2.0F + enterDrift;'''
content = content.replace(card_target, card_replacement)

# In mouseClicked, if closing return true
mouse_target = '''   @Override
   public boolean mouseClicked(Click click, boolean doubled) {'''
mouse_replacement = '''   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      if (this.closing) {
         return true;
      }'''
content = content.replace(mouse_target, mouse_replacement)

p.write_text(content, encoding="utf-8")
print("ProxyScreen.java patched with smooth open/close animations!")
