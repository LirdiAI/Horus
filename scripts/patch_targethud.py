path = r'C:\Users\Никита\Downloads\HorusClient\src\main\java\horus\client\ui\hud\TargetHud.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add fields
fields_target = '   private static final Animation animator5 = new Animation();'
fields_replacement = '''   private static final Animation animator5 = new Animation();
   private static final Animation damageAnimator = new Animation();
   private static float lastHealthValue = 1.0F;
   private static long lastDamageTime = 0L;'''

content = content.replace(fields_target, fields_replacement, 1)

# 2. In processData: damage animation tracking
process_target = '''         animator2.update();
         animator2.animateTo(targetValue, 0.5, Easings.EASE_OUT_EXPO);'''
process_replacement = '''         animator2.update();
         animator2.animateTo(targetValue, 0.5, Easings.EASE_OUT_EXPO);

         if (targetValue < lastHealthValue - 0.005F) {
            lastDamageTime = local586_4;
         } else if (targetValue > lastHealthValue) {
            damageAnimator.resetTo(targetValue);
         }
         lastHealthValue = targetValue;
         damageAnimator.update();
         if (local586_4 - lastDamageTime > 250L) {
            damageAnimator.animateTo(targetValue, 0.45, Easings.EASE_OUT_EXPO);
         }'''

content = content.replace(process_target, process_replacement, 1)

# 3. In processData2: draw damage trail
data2_target = '''      int local587_10 = Colors.computeInt6(local587_5, local587_3);
      int local587_11 = Colors.computeInt6(local587_6, local587_3);
      float local587_12 = local587_2 * MathHelper.clamp(animator2.floatValue(), 0.0F, 1.0F);
      if (local587_12 > 0.5F) {
         ShapeDrawing.processData6(local587_0, local587_1, local587_12, 5.0F, local587_10, local587_10, local587_11, local587_11, 1.0F, cornerRadii);
      }'''

data2_replacement = '''      float damageRatio = MathHelper.clamp(damageAnimator.floatValue(), 0.0F, 1.0F);
      float currentRatio = MathHelper.clamp(animator2.floatValue(), 0.0F, 1.0F);
      if (damageRatio > currentRatio) {
         float damageWidth = local587_2 * damageRatio;
         if (damageWidth > 0.5F) {
            int trail1 = Colors.computeInt6(0xFFFFAFA4, local587_3 * 0.9F);
            int trail2 = Colors.computeInt6(0xFFFF7A6A, local587_3 * 0.8F);
            ShapeDrawing.processData6(local587_0, local587_1, damageWidth, 5.0F, trail1, trail1, trail2, trail2, 1.0F, cornerRadii);
         }
      }

      int local587_10 = Colors.computeInt6(local587_5, local587_3);
      int local587_11 = Colors.computeInt6(local587_6, local587_3);
      float local587_12 = local587_2 * currentRatio;
      if (local587_12 > 0.5F) {
         ShapeDrawing.processData6(local587_0, local587_1, local587_12, 5.0F, local587_10, local587_10, local587_11, local587_11, 1.0F, cornerRadii);
      }'''

content = content.replace(data2_target, data2_replacement, 1)

# 4. In processData3: armor durability
data3_target = '''            ItemRenderCache.processData(equippedStack, local589_8, local589_6, 8.0F, ItemDrawOptions.decorated(local589_4).glint(GlintMode.OFF));
            local589_8 += 9.0F;'''

data3_replacement = '''            ItemRenderCache.processData(equippedStack, local589_8, local589_6, 8.0F, ItemDrawOptions.decorated(local589_4).glint(GlintMode.OFF));

            if (equippedStack.isDamageable()) {
               int maxDmg = Math.max(1, equippedStack.getMaxDamage());
               int curDmg = equippedStack.getDamage();
               float durRatio = MathHelper.clamp(1.0F - (float)curDmg / (float)maxDmg, 0.0F, 1.0F);
               int pct = Math.round(durRatio * 100.0F);

               int durColor = pct > 60 ? 0xFF00F5C4 : (pct > 25 ? 0xFFFFCF70 : 0xFFFF4E4E);
               int durBg = Colors.computeInt6(0xFF000000, local589_4 * 0.75F);
               int durFg = Colors.computeInt6(durColor, local589_4 * 0.95F);

               ShapeDrawing.processData5(local589_8 + 0.5F, local589_6 + 7.8F, 7.0F, 1.2F, 0.0F, durBg, local589_4 * 0.65F, CornerRadii.uniform(0.5F));
               float barW = 7.0F * durRatio;
               if (barW > 0.4F) {
                  ShapeDrawing.processData5(local589_8 + 0.5F, local589_6 + 7.8F, barW, 1.2F, 0.0F, durFg, local589_4, CornerRadii.uniform(0.5F));
               }
            }

            local589_8 += 9.5F;'''

content = content.replace(data3_target, data3_replacement, 1)

# 5. In processData5: Pulse badge
data5_target = '''   private static void processData5(LivingEntity livingEntity, float local595_1, float local595_2, float local595_3, float local595_4) {
      String string = livingEntity.getName().getString();
      float local595_6 = local595_1 + Math.max(0.0F, local595_3);
      FontText.processData7(
         FontFace.SEMI_BOLD,
         string,
         local595_1,
         local595_2,
         6.0F,
         UiTheme.computeInt(HudStyle.computeInt4(1.0F), local595_4),
         local595_1,
         local595_6,
         4.0F,
         false,
         true
      );
   }'''

data5_replacement = '''   private static void processData5(LivingEntity livingEntity, float local595_1, float local595_2, float local595_3, float local595_4) {
      String string = livingEntity.getName().getString();
      float textX = local595_1;

      if (livingEntity instanceof PlayerEntity) {
         float badgeW = 20.0F;
         float badgeH = 7.0F;
         int badgeBorder = Colors.computeInt6(0xFF00F5C4, local595_4 * 0.85F);
         int badgeBg = Colors.computeInt6(0xFF051C18, local595_4 * 0.75F);
         ShapeDrawing.processData5(textX, local595_2 - 0.5F, badgeW, badgeH, 0.5F, badgeBorder, local595_4 * 0.75F, CornerRadii.uniform(2.0F));
         ShapeDrawing.processData5(textX + 0.5F, local595_2, badgeW - 1.0F, badgeH - 1.0F, 0.0F, badgeBg, local595_4 * 0.85F, CornerRadii.uniform(1.5F));
         FontText.processData3(FontFace.SEMI_BOLD, "PULSE", textX + 2.5F, local595_2 + 0.6F, 4.0F, Colors.computeInt6(0xFF00F5C4, local595_4));
         textX += badgeW + 3.0F;
      }

      float local595_6 = local595_1 + Math.max(0.0F, local595_3);
      FontText.processData7(
         FontFace.SEMI_BOLD,
         string,
         textX,
         local595_2,
         6.0F,
         UiTheme.computeInt(HudStyle.computeInt4(1.0F), local595_4),
         textX,
         local595_6,
         4.0F,
         false,
         true
      );
   }'''

content = content.replace(data5_target, data5_replacement, 1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("TargetHud.java successfully updated!")
