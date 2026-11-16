path = r'C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\ClickGuiScreen.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace FontFace.REGULAR with FontFace.SF
content = content.replace('FontFace.REGULAR', 'FontFace.SF')

# Clean charTyped
char_typed_new = '''   @Override
   public boolean charTyped(CharInput charInput) {
      if (this.settingRenderContext.nested0492 != null) {
         if (charInput.codepoint() >= 32) {
            this.settingRenderContext.nested0492.processData20((char)charInput.codepoint());
         }

         return true;
      } else if (this.cloudInputActive) {
         if (charInput.codepoint() >= 32) {
            this.cloudCodeInput.insertText(String.valueOf((char)charInput.codepoint()));
         }
         return true;
      } else if (this.settingRenderContext.textSetting != null) {
         if (charInput.codepoint() >= 32) {
            this.settingRenderContext.textSetting.insertText(String.valueOf((char)charInput.codepoint()));
         }

         return true;
      } else if (this.flag5) {
         if (charInput.codepoint() >= 32) {
            this.textSetting.insertText(String.valueOf((char)charInput.codepoint()));
            this.settingRenderContext.strVal = this.textSetting.getText().toLowerCase();
            this.timeMillis3 = System.nanoTime() / 1000000L;
         }

         return true;
      } else {
         return super.charTyped(charInput);
      }
   }'''

# Find charTyped start and end
ct_start = content.find('   @Override\n   public boolean charTyped(CharInput charInput) {')
ct_end = content.find('   private static float sf() {')
if ct_start != -1 and ct_end != -1:
    content = content[:ct_start] + char_typed_new + '\n\n' + content[ct_end:]

# In keyPressed, insert cloudInputActive handling right after `int local1260_2 = keyInput.key();`
kp_target = '      int local1260_2 = keyInput.key();'
kp_insert = '''      int local1260_2 = keyInput.key();
      if (this.cloudInputActive) {
         if (local1260_2 == 256) {
            this.cloudInputActive = false;
            return true;
         } else if (local1260_2 == 257) {
            String cd = this.cloudCodeInput.getText();
            if (!cd.trim().isEmpty()) {
               this.cloudStatusText = "Загрузка...";
               horus.client.util.CloudConfigManager.loadConfigAsync(cd).thenAccept(ok -> {
                  this.cloudStatusText = horus.client.util.CloudConfigManager.getLastStatusMessage();
               });
            }
            return true;
         } else if (local1260_2 == 259) {
            this.cloudCodeInput.backspace();
            return true;
         } else if ((keyInput.modifiers() & 2) != 0 && local1260_2 == 86) {
            this.cloudCodeInput.insertText(MinecraftClient.getInstance().keyboard.getClipboard());
            return true;
         }
         return true;
      }'''

content = content.replace(kp_target, kp_insert, 1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("ClickGuiScreen.java cleaned!")
