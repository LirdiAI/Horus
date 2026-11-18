package horus.client.module.render;

import horus.client.event.EventListener;
import horus.client.event.HudRenderEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.module.combat.AttackAuraModule;
import horus.client.render.CornerRadii;
import horus.client.render.FontText;
import horus.client.render.ItemRenderCache;
import horus.client.render.ShapeDrawing;
import horus.client.ui.hud.HudRenderContext;
import horus.client.setting.BooleanSetting;
import horus.client.setting.LegacyNumberSetting;
import horus.client.util.Colors;
import horus.client.util.EntityUtils;
import horus.client.render.FontFace;
import horus.client.util.GlintMode;
import horus.client.util.ItemDrawOptions;
import horus.client.ui.hud.HudStyle;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@ModuleInfo(
   name = "InvTarget",
   description = "Панель снаряжения последней цели: руки, броня, прочность, эффекты и замеченные предметы.",
   category = ModuleCategory.RENDER
)
public final class InvTargetModule extends Module {
   private static InvTargetModule invTargetModule;
   public LegacyNumberSetting showTimeSetting;
   public BooleanSetting effectsSetting;
   public BooleanSetting historySetting;

   private AbstractClientPlayerEntity target;
   private long lastSeenMillis;
   private long lastFrameMillis;

   private final Map<String, LinkedHashSet<String>> historyKeys = new HashMap<>();
   private final Map<String, ArrayList<ItemStack>> historyStacks = new HashMap<>();

   private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
      if (!this.isEnabled()) {
         this.target = null;
         return;
      }

      long now = System.currentTimeMillis();
      float delta = this.lastFrameMillis == 0L ? 0.05F : Math.min(0.1F, (now - this.lastFrameMillis) / 1000.0F);
      this.lastFrameMillis = now;

      if (mc.player == null || mc.world == null) {
         this.target = null;
         return;
      }

      AbstractClientPlayerEntity candidate = this.pickTarget();
      if (candidate != null) {
         if (candidate != this.target) {
            this.target = candidate;
         }

         this.lastSeenMillis = now;
         this.trackEquipment(candidate);
      }

      if (this.target == null || this.target.isRemoved() || !this.target.isAlive()) {
         this.target = null;
         return;
      }

      long hideAfter = (long) (this.showTimeSetting.getValue() * 1000.0);
      if (now - this.lastSeenMillis > hideAfter) {
         return;
      }

      float alpha = MathHelper.clamp((now - this.lastSeenMillis) > hideAfter - 400L
         ? (hideAfter - (now - this.lastSeenMillis)) / 400.0F
         : 1.0F, 0.0F, 1.0F);

      this.renderPanel(hudRenderEvent, alpha);
   };

   private AbstractClientPlayerEntity pickTarget() {
      if (AttackAuraModule.getInstance().isEnabled() && AttackAuraModule.getInstance().getTarget() != null) {
         if (AttackAuraModule.getInstance().getTarget() instanceof AbstractClientPlayerEntity playerTarget) {
            return playerTarget;
         }
      }

      if (mc.player.getAttacking() instanceof AbstractClientPlayerEntity attacked && attacked != mc.player) {
         return attacked;
      }

      return null;
   }

   private void trackEquipment(AbstractClientPlayerEntity playerTarget) {
      String nick = playerTarget.getGameProfile().name();
      LinkedHashSet<String> keys = this.historyKeys.computeIfAbsent(nick, key -> new LinkedHashSet<>());
      ArrayList<ItemStack> stacks = this.historyStacks.computeIfAbsent(nick, key -> new ArrayList<>());

      for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
         ItemStack stack = playerTarget.getEquippedStack(equipmentSlot);
         if (!stack.isEmpty()) {
            String key = stack.getItem().toString() + "#" + stack.getCount();
            if (keys.add(key)) {
               stacks.add(stack.copy());
               if (stacks.size() > 27) {
                  stacks.remove(0);
                  keys.remove(keys.iterator().next());
               }
            }
         }
      }
   }

   private void renderPanel(HudRenderEvent hudRenderEvent, float alpha) {
      HudRenderContext.renderElement(hudRenderEvent.getDrawContext());

      int width = hudRenderEvent.computeInt2();
      int height = hudRenderEvent.computeInt();
      boolean showEffects = this.effectsSetting.isChecked() && !this.target.getStatusEffects().isEmpty();
      ArrayList<ItemStack> history = this.historyStacks.get(this.target.getGameProfile().name());
      int historyRows = this.historySetting.isChecked() && history != null && !history.isEmpty()
         ? (Math.min(history.size(), 27) + 8) / 9
         : 0;

      float x = width - 82.0F;
      float y = height / 2.0F - 42.0F;
      float panelH = 26.0F + (showEffects ? 9.0F : 0.0F) + historyRows * 12.0F;

      HudStyle.processData2(x, y, 70.0F, panelH, 1.0F, 4.5F, ShapeDrawing.TypeOption.LOW);

      float health = EntityUtils.getTotalHealth(this.target);
      float maxHealth = Math.max(1.0F, this.target.getMaxHealth());
      float healthRatio = MathHelper.clamp(health / maxHealth, 0.0F, 1.0F);
      float absorption = this.target.getAbsorptionAmount();

      // Ник и HP
      String nick = this.target.getGameProfile().name();
      FontText.processData3(FontFace.SEMI_BOLD, nick, x + 6.0F, y + 5.0F, 6.5F, HudStyle.computeInt2(1.0F));
      String hpText = Math.round(health + absorption) + "hp";
      FontText.processData3(
         FontFace.SEMI_BOLD,
         hpText,
         x + 70.0F - 6.0F - FontText.computeFloat(FontFace.SEMI_BOLD, hpText, 6.5F),
         y + 5.0F,
         6.5F,
         HudStyle.computeInt4(1.0F)
      );

      // Полоска HP
      float barY = y + 13.5F;
      ShapeDrawing.processData5(x + 6.0F, barY, 58.0F, 2.6F, 0.0F, Colors.computeInt6(0xFF000000, alpha * 0.55F), alpha, CornerRadii.uniform(1.0F));
      ShapeDrawing.processData5(
         x + 6.0F, barY, Math.max(1.0F, 58.0F * healthRatio), 2.6F, 0.0F, Colors.computeInt6(0xFF00F5C4, alpha * 0.95F), alpha, CornerRadii.uniform(1.0F)
      );
      if (absorption > 0.01F) {
         float absorptionRatio = MathHelper.clamp(absorption / maxHealth, 0.0F, 1.0F - healthRatio);
         ShapeDrawing.processData5(
            x + 6.0F + 58.0F * healthRatio, barY, Math.max(1.0F, 58.0F * absorptionRatio), 2.6F, 0.0F, Colors.computeInt6(0xFFFFD24A, alpha * 0.9F), alpha, CornerRadii.uniform(1.0F)
         );
      }

      // Ряд снаряжения: обе руки + броня
      float slotsY = barY + 5.0F;
      float slotX = x + 6.0F;
      EquipmentSlot[] equipmentSlots = new EquipmentSlot[]{
         EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
      };

      for (EquipmentSlot equipmentSlot : equipmentSlots) {
         ItemStack stack = this.target.getEquippedStack(equipmentSlot);
         if (stack.isEmpty()) {
            ShapeDrawing.processData5(slotX, slotsY, 8.0F, 8.0F, 0.0F, Colors.computeInt6(0xFFFFFFFF, alpha * 0.05F), alpha, CornerRadii.uniform(1.5F));
         } else {
            ItemRenderCache.processData(stack, slotX, slotsY, 8.0F, ItemDrawOptions.decorated(alpha).glint(GlintMode.OFF));
            if (stack.isDamageable()) {
               float durabilityRatio = 1.0F - (float) stack.getDamage() / (float) Math.max(1, stack.getMaxDamage());
               int durabilityColor = durabilityRatio > 0.6 ? 0xFF00F5C4 : (durabilityRatio > 0.25 ? 0xFFFFCF70 : 0xFFFF4E4E);
               ShapeDrawing.processData5(slotX + 0.5F, slotsY + 7.8F, 7.0F, 1.2F, 0.0F, Colors.computeInt6(0xFF000000, alpha * 0.75F), alpha, CornerRadii.uniform(0.5F));
               if (7.0F * durabilityRatio > 0.4F) {
                  ShapeDrawing.processData5(
                     slotX + 0.5F, slotsY + 7.8F, 7.0F * durabilityRatio, 1.2F, 0.0F, Colors.computeInt6(durabilityColor, alpha * 0.95F), alpha, CornerRadii.uniform(0.5F)
                  );
               }
            }
         }

         slotX += 9.5F;
      }

      float cursorY = slotsY + 12.0F;

      // Эффекты зелий
      if (showEffects) {
         StringBuilder effectsText = new StringBuilder();
         int count = 0;

         for (StatusEffectInstance statusEffectInstance : this.target.getStatusEffects()) {
            if (count >= 3) {
               break;
            }

            if (count > 0) {
               effectsText.append(", ");
            }

            effectsText.append(statusEffectInstance.getEffectType().value().getName().getString());
            if (statusEffectInstance.getAmplifier() > 0) {
               effectsText.append(" ").append(statusEffectInstance.getAmplifier() + 1);
            }

            count++;
         }

         if (this.target.getStatusEffects().size() > 3) {
            effectsText.append(" +").append(this.target.getStatusEffects().size() - 3);
         }

         String trimmed = trimToWidth(effectsText.toString(), 58.0F);
         FontText.processData3(FontFace.SEMI_BOLD, trimmed, x + 6.0F, cursorY + 1.0F, 5.5F, HudStyle.computeInt4(1.0F));
         cursorY += 9.0F;
      }

      // История замеченных предметов (руки за всё время боя)
      if (historyRows > 0 && history != null) {
         int index = 0;

         for (ItemStack stack : history) {
            float iconX = x + 6.0F + index % 9 * 9.5F;
            float iconY = cursorY + index / 9 * 12.0F;
            ItemRenderCache.processData(stack, iconX, iconY, 8.0F, ItemDrawOptions.decorated(alpha).glint(GlintMode.OFF));
            index++;
         }
      }

      HudRenderContext.updateState();
   }

   private static String trimToWidth(String text, float maxWidth) {
      if (FontText.computeFloat(FontFace.SEMI_BOLD, text, 5.5F) <= maxWidth) {
         return text;
      }

      for (int i = text.length(); i > 3; i--) {
         String candidate = text.substring(0, i) + "…";
         if (FontText.computeFloat(FontFace.SEMI_BOLD, candidate, 5.5F) <= maxWidth) {
            return candidate;
         }
      }

      return text.substring(0, 3) + "…";
   }

   public static InvTargetModule getInstance() {
      if (invTargetModule == null) {
         invTargetModule = new InvTargetModule();
      }

      return invTargetModule;
   }

   private InvTargetModule() {
      this.showTimeSetting = this.addSetting(new LegacyNumberSetting("Время показа", "Сколько секунд панель остаётся после последнего удара.", 5.0, 1.0, 15.0, 1.0));
      this.effectsSetting = this.addSetting(new BooleanSetting("Эффекты", "Показывать активные эффекты зелий цели.", true));
      this.historySetting = this.addSetting(new BooleanSetting("История предметов", "Показывать все предметы, замеченные в руках цели.", true));
   }
}
