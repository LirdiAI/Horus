package horus.client.ui.hud;

import horus.client.render.FontText;
import horus.client.util.ServerItemType;
import horus.client.render.ThemeColors;
import horus.client.ui.UiTheme;

import horus.client.util.Animation;

import horus.client.util.Easings;
import horus.client.render.FontFace;
import horus.client.module.render.InterfaceModule;
import horus.client.render.Render2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class PotionsHud {
   private static final Set<RegistryEntry<StatusEffect>> set = Set.of(
      StatusEffects.STRENGTH,
      StatusEffects.SPEED,
      StatusEffects.FIRE_RESISTANCE,
      StatusEffects.RESISTANCE,
      StatusEffects.ABSORPTION,
      StatusEffects.REGENERATION
   );
   private static final List<RegistryEntry<StatusEffect>> list = List.of(
      StatusEffects.SPEED,
      StatusEffects.STRENGTH,
      StatusEffects.REGENERATION,
      StatusEffects.FIRE_RESISTANCE,
      StatusEffects.WATER_BREATHING,
      StatusEffects.POISON
   );
   private static final PotionsHud.SubContext nested0477 = new PotionsHud.SubContext("Example Potion", 0);
   private static long timeMillis;
   private static int intVal;
   private static final Map<ServerItemType, PotionsHud.SubContext> map = new EnumMap<>(ServerItemType.class);
   private static final Map<ServerItemType, Long> map2 = new EnumMap<>(ServerItemType.class);
   private static final Map<RegistryEntry<StatusEffect>, PotionsHud.SubContext> map3 = new IdentityHashMap<>();
   private static final List<PotionsHud.SubContext> list2 = new ArrayList<>();
   private static final Animation animator = new Animation();
   private static final Animation animator2 = new Animation();
   private static final Animation animator3 = new Animation();
   private static final Animation animator4 = new Animation();
   private static float floatVal;
   private static float floatVal2;

   private static float computeFloat(PotionsHud.SubContext local5166_0) {
      return FontText.computeFloat(FontFace.SEMI_BOLD, local5166_0.strVal, 6.0F)
         + FontText.computeFloat(FontFace.SEMI_BOLD, local5166_0.strVal2, 6.0F);
   }

   private static void processData(MinecraftClient minecraftClient) {
      list2.clear();
      map3.values().forEach(local5171_0 -> local5171_0.visible = false);
      if (minecraftClient.player != null) {
         for (StatusEffectInstance statusEffectInstance : minecraftClient.player.getStatusEffects()) {
            PotionsHud.SubContext local5169_3 = map3.computeIfAbsent(statusEffectInstance.getEffectType(), PotionsHud.SubContext::new);
            int currentAmplifier = statusEffectInstance.getAmplifier();
            if (currentAmplifier != local5169_3.intVal3) {
               local5169_3.intVal3 = currentAmplifier;
               local5169_3.strVal2 = currentAmplifier > 0 ? " " + (currentAmplifier + 1) : "";
            }

            local5169_3.statusEffectInstance3 = statusEffectInstance;
            local5169_3.strVal3 = formatText(statusEffectInstance);
            local5169_3.visible = true;
         }
      }

      processData3(minecraftClient);

      for (PotionsHud.SubContext local5169_2 : map3.values()) {
         if (local5169_2.visible || local5169_2.visibilityAnimation.floatValue() > 0.001) {
            list2.add(local5169_2);
         }
      }

      for (PotionsHud.SubContext local5169_2 : map.values()) {
         if (local5169_2.visible || local5169_2.visibilityAnimation.floatValue() > 0.001) {
            list2.add(local5169_2);
         }
      }

      map3.entrySet()
         .removeIf(
            local5172_0 -> !local5172_0.getValue().visible
               && local5172_0.getValue().visibilityAnimation.floatValue() <= 0.001
               && local5172_0.getValue().visibilityAnimation.isFinished()
         );
      map.entrySet()
         .removeIf(
            local5181_0 -> !local5181_0.getValue().visible
               && local5181_0.getValue().visibilityAnimation.floatValue() <= 0.001
               && local5181_0.getValue().visibilityAnimation.isFinished()
         );
      nested0477.visible = list2.isEmpty() && HudRenderContext.checkState();
      if (nested0477.visible) {
         long local5169_1 = System.currentTimeMillis();
         float local5169_3 = (float)(10000L - (local5169_1 - timeMillis)) / 1000.0F;
         if (local5169_3 <= 0.0F) {
            timeMillis = local5169_1;
            intVal++;
            local5169_3 = 10.0F;
         }

         nested0477.statusEffectInstance3 = new StatusEffectInstance(list.get(intVal % list.size()), (int)Math.ceil(Math.max(1.0F, local5169_3)) * 20, 0);
         nested0477.strVal3 = formatText(nested0477.statusEffectInstance3);
      } else {
         timeMillis = 0L;
      }

      if (nested0477.visible || nested0477.visibilityAnimation.floatValue() > 0.001) {
         list2.add(nested0477);
      }

      list2.sort(
         Comparator.<PotionsHud.SubContext, Integer>comparing(local5170_0 -> local5170_0.intVal2)
            .thenComparing(Comparator.<PotionsHud.SubContext>comparingDouble(local5180_0 -> computeFloat(local5180_0)).reversed())
      );

      for (PotionsHud.SubContext local5169_2 : list2) {
         local5169_2.visibilityAnimation.update();
         local5169_2.visibilityAnimation.animateTo(local5169_2.visible ? 1.0 : 0.0, 0.6, Easings.EASE_OUT_QUART);
      }
   }

   static {
      animator2.resetTo(1.0);
   }

   private static String formatText(StatusEffectInstance statusEffectInstance) {
      if (statusEffectInstance.isInfinite()) {
         return "INF";
      }

      int local5174_1 = statusEffectInstance.getDuration() / 20;
      int local5174_2 = local5174_1 / 60;
      int local5174_3 = local5174_1 % 60;
      return local5174_2 > 0 ? local5174_2 + "м " + local5174_3 + "с" : local5174_3 + "с";
   }

   public static float computeFloat2(MinecraftClient minecraftClient, float local5175_1, float local5175_2) {
      processData(minecraftClient);
      float local5175_3 = HudStyle.computeFloat5("Potions");
      float local5175_4 = 0.0F;
      float local5175_5 = 0.0F;
      float local5175_6 = 13.0F;

      for (PotionsHud.SubContext local5175_8 : list2) {
         local5175_4 = Math.max(local5175_4, HudStyle.computeFloat(8.0F, computeFloat3(local5175_8)));
         local5175_5 = Math.max(local5175_5, HudStyle.computeFloat4(computeFloat(local5175_8)));
         local5175_6 += 14.0F * local5175_8.visibilityAnimation.floatValue();
      }

      float local5175_7 = local5175_4 + (local5175_4 > 0.0F && local5175_5 > 0.0F ? 2.0F : 0.0F) + local5175_5;
      float local5175_8 = Math.max(local5175_3, Math.max(local5175_7, 1.0F));
      boolean local5175_9 = !list2.isEmpty() || HudRenderContext.checkState();
      animator.update();
      animator.animateTo(local5175_9 ? 1.0 : 0.0, 0.4, Easings.EASE_OUT_QUART);
      animator2.update();
      if (!list2.isEmpty()) {
         animator2.animateTo(Math.max(local5175_8, 1.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      animator3.update();
      if (!list2.isEmpty()) {
         animator3.animateTo(Math.max(local5175_8, 1.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      animator4.update();
      if (!list2.isEmpty()) {
         animator4.animateTo(Math.max(local5175_4, 0.0F), 0.6, Easings.EASE_OUT_QUART);
      }

      floatVal = animator2.floatValue();
      floatVal2 = Math.max(local5175_6, 1.0F);
      float local5175_10 = animator.floatValue();
      if (local5175_10 <= 0.001F) {
         return 0.0F;
      }

      float[] floatArray = HudManager.computeFloat5("potions", local5175_1 - floatVal, local5175_2);
      float local5175_12 = floatArray[0];
      float local5175_13 = floatArray[1];
      HudManager.processData9("potions", local5175_12, local5175_13, floatVal, floatVal2);
      HudRenderContext.processData4(local5175_12 + floatVal / 2.0F, local5175_13 + floatVal2 / 2.0F, local5175_10);
      HudStyle.processData7(local5175_12, local5175_13, Math.max(animator3.floatValue(), 1.0F), local5175_10, HudIcons.POTIONS, "Potions");
      float local5175_14 = local5175_13 + 15.0F;

      for (PotionsHud.SubContext local5175_16 : list2) {
         processData4(local5175_12, local5175_14, local5175_10, local5175_16);
         local5175_14 += 13.0F * local5175_16.visibilityAnimation.floatValue();
      }

      HudRenderContext.updateState();
      return floatVal2 * local5175_10 + 8.0F * local5175_10;
   }

   public static void processData2(ServerItemType serverItemType, BlockPos blockPos) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient.player != null && !(minecraftClient.player.getBoundingBox().squaredMagnitude(blockPos.toCenterPos()) > 16.0)) {
         map2.put(serverItemType, System.currentTimeMillis());
      }
   }

   static int computeInt(RegistryEntry<StatusEffect> local5178_0) {
      if (set.contains(local5178_0)) {
         return 0;
      } else {
         StatusEffectCategory category = local5178_0.value().getCategory();
         if (category == StatusEffectCategory.BENEFICIAL) {
            return 1;
         } else {
            return category == StatusEffectCategory.NEUTRAL ? 2 : 3;
         }
      }
   }

   private PotionsHud() {
   }

   private static float computeFloat3(PotionsHud.SubContext local5182_0) {
      return FontText.computeFloat(FontFace.SEMI_BOLD, local5182_0.strVal3, 6.0F);
   }

   private static void processData3(MinecraftClient minecraftClient) {
      Map<ServerItemType, Boolean> local5183_1 = new EnumMap<>(ServerItemType.class);
      map.forEach((serverItemType, local5167_2) -> local5183_1.put(serverItemType, local5167_2.visible));
      map.values().forEach(local5176_0 -> local5176_0.visible = false);
      if (minecraftClient.player != null && InterfaceModule.getInstance().showDonationPotions.isChecked() && ServerItemType.checkState()) {
         for (ServerItemType serverItemType2 : ServerItemType.values()) {
            StatusEffectInstance statusEffectInstance = null;

            for (ServerItemType.DataRecord local5183_8 : serverItemType2.getList()) {
               StatusEffectInstance statusEffectInstance2 = minecraftClient.player.getStatusEffect(local5183_8.effect());
               if (statusEffectInstance2 == null || !ServerItemType.matchesCriteria(statusEffectInstance2.getAmplifier(), local5183_8.amplifier())) {
                  statusEffectInstance = null;
                  break;
               }

               if (statusEffectInstance == null || statusEffectInstance2.getDuration() > statusEffectInstance.getDuration()) {
                  statusEffectInstance = statusEffectInstance2;
               }
            }

            boolean local5183_7 = local5183_1.getOrDefault(serverItemType2, false) || System.currentTimeMillis() - map2.getOrDefault(serverItemType2, 0L) < 3000L;
            if (statusEffectInstance != null && local5183_7) {
               PotionsHud.SubContext local5183_8 = map.computeIfAbsent(serverItemType2, serverItemType3 -> {
                  PotionsHud.SubContext local5168_1 = new PotionsHud.SubContext(serverItemType3.formatText(), 0);
                  local5168_1.text2 = serverItemType3.getText2();
                  return local5168_1;
               });

               for (ServerItemType.DataRecord local5183_10 : serverItemType2.getList()) {
                  PotionsHud.SubContext local5183_11 = map3.get(local5183_10.effect());
                  if (local5183_11 != null) {
                     local5183_11.visible = false;
                  }
               }

               local5183_8.statusEffectInstance3 = statusEffectInstance;
               local5183_8.strVal3 = formatText(statusEffectInstance);
               local5183_8.visible = true;
            }
         }
      }
   }

   private static void processData4(float local5184_0, float local5184_1, float local5184_2, PotionsHud.SubContext local5184_3) {
      float local5184_4 = local5184_3.visibilityAnimation.floatValue() * local5184_2;
      if (!(local5184_4 <= 0.001F)) {
         float local5184_5 = HudStyle.computeFloat2(local5184_4);
         float local5184_6 = computeFloat3(local5184_3);
         float local5184_7 = HudStyle.computeFloat(8.0F, local5184_6);
         float local5184_8 = HudStyle.computeFloat4(computeFloat(local5184_3));
         float local5184_9 = local5184_0 - local5184_5;
         float local5184_10 = local5184_0 + animator4.floatValue() + 2.0F - local5184_5;
         float local5184_11 = HudStyle.computeFloat3(local5184_1);
         HudStyle.processData6(local5184_9, local5184_1, local5184_7, 11.0F, local5184_4);
         float local5184_12 = local5184_9 + 2.0F + 8.0F + 1.0F;
         float local5184_13 = local5184_12 + 0.25F;
         HudStyle.processData(local5184_12, local5184_1 + 5.5F, local5184_4, 3.0F);
         if (local5184_3.statusEffectInstance3 != null) {
            Render2D.processData46(local5184_4);
            Render2D.processData69(
               local5184_3.statusEffectInstance3.getEffectType(), local5184_9 + (local5184_13 - local5184_9 - 8.0F) / 2.0F, local5184_1 + 5.5F - 4.0F, 8.0F
            );
            Render2D.updateState();
         }

         FontText.processData3(
            FontFace.SEMI_BOLD,
            local5184_3.strVal3,
            local5184_13 + (local5184_9 + local5184_7 - local5184_13 - local5184_6) / 2.0F,
            local5184_11,
            6.0F,
            UiTheme.computeInt(HudStyle.computeInt4(1.0F), local5184_4)
         );
         HudStyle.processData6(local5184_10, local5184_1, local5184_8, 11.0F, local5184_4);
         boolean local5184_14 = local5184_3.statusEffectInstance3 != null && local5184_3.statusEffectInstance3.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL;
         int local5184_15 = UiTheme.computeInt(local5184_14 ? -44976 : -1, local5184_4);
         if (local5184_3.text2 != null) {
            FontText.processData5(
               FontFace.SEMI_BOLD, local5184_3.text2, local5184_10 + 4.0F, local5184_11, 6.0F, UiTheme.computeInt(-1, local5184_4)
            );
         } else {
            FontText.processData3(FontFace.SEMI_BOLD, local5184_3.strVal, local5184_10 + 4.0F, local5184_11, 6.0F, local5184_15);
         }

         if (!local5184_3.strVal2.isEmpty()) {
            FontText.processData3(
               FontFace.SEMI_BOLD,
               local5184_3.strVal2,
               local5184_10 + 4.0F + FontText.computeFloat(FontFace.SEMI_BOLD, local5184_3.strVal, 6.0F),
               local5184_11,
               6.0F,
               UiTheme.computeInt(ThemeColors.intVal9, local5184_4)
            );
         }
      }
   }

   private static final class SubContext extends DraggableHudElement {
      final String strVal;
      String strVal2 = "";
      String strVal3 = "";
      StatusEffectInstance statusEffectInstance3;
      final int intVal2;
      int intVal3 = -1;
      Text text2;

      SubContext(String local5185_1, int local5185_2) {
         this.strVal = local5185_1;
         this.intVal2 = local5185_2;
      }

      SubContext(RegistryEntry<StatusEffect> local5186_1) {
         this.strVal = local5186_1.value().getName().getString();
         this.intVal2 = PotionsHud.computeInt(local5186_1);
      }
   }
}
