package horus.client.module.render;

import horus.client.module.movement.AirStuckModule;
import horus.client.setting.BooleanSetting;
import horus.client.util.Colors;
import horus.client.event.EventListener;
import horus.client.render.FontFace;
import horus.client.storage.FriendStore;
import horus.client.event.HudRenderEvent;
import horus.client.util.ItemDrawOptions;
import horus.client.setting.LegacyNumberSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.setting.MultiChoiceSetting;
import horus.client.render.PlayerOutlineRenderer;
import horus.client.event.PlayerTickEvent;
import horus.client.util.GuiScale;
import horus.client.render.WorldProjection;
import horus.client.render.FontText;
import horus.client.render.ItemRenderCache;
import horus.client.util.ServerItemType;
import horus.client.util.PlayerItemCooldowns;
import horus.client.util.EntityUtils;
import horus.client.ui.UiTheme;
import horus.client.setting.ColorSetting;
import horus.client.util.GlintMode;
import horus.client.render.ThemeColors;
import horus.client.ui.hud.HudRenderContext;
import horus.client.ui.hud.HudStyle;
import horus.client.render.Render2D;
import horus.client.render.RasterTextDrawData;
import horus.client.setting.SectionSetting;
import horus.client.module.player.ServerHelperModule;
import horus.client.event.WorldRenderEvent;
import horus.client.util.FormattedText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.client.gui.DrawContext;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import horus.client.render.SoftGlowDrawData;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.joml.Vector4f;

@ModuleInfo(name = "Tags", description = "Ники, предметы, броня и руки игроков поверх мира.", category = ModuleCategory.RENDER)
public final class TagsModule extends Module implements horus.client.render.PlayerOutlineParams {
   private static TagsModule tagsModule;
   private static int intVal = new Color(15, 15, 15, 155).getRGB();
   private static int intVal2 = new Color(0, 255, 0, 40).getRGB();
   private static int intVal3 = new Color(255, 70, 70, 255).getRGB();
   private static int intVal4 = new Color(190, 30, 30, 150).getRGB();
   private static int intVal5 = new Color(160, 160, 160, 255).getRGB();
   private static int intVal6 = new Color(112, 255, 112, 255).getRGB();
   public MultiChoiceSetting display;
   public MultiChoiceSetting elements;
   public BooleanSetting showRpPrefixes;
   public BooleanSetting showRankGlyphs;
   public BooleanSetting showPlayerDonationPotions;
   public BooleanSetting applyShader;
   public LegacyNumberSetting outline;
   public LegacyNumberSetting highlight;
   public LegacyNumberSetting fill;
   public BooleanSetting customColor;
   public ColorSetting color;
   public BooleanSetting box;
   public BooleanSetting customColor2;
   public ColorSetting color2;
   private final HashMap<Integer, Vec3d> map = new HashMap<>();
   private final HashMap<Integer, Integer> map2 = new HashMap<>();
   private final HashMap<Integer, Integer> map3 = new HashMap<>();
   private final HashMap<Integer, Integer> map4 = new HashMap<>();
   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (mc.world != null) {
         if (this.map.size() > 256) {
            this.map.clear();
            this.map2.clear();
            this.map3.clear();
            this.map4.clear();
         }

         for (AbstractClientPlayerEntity abstractClientPlayerEntity : mc.world.getPlayers()) {
            Vec3d lerpedPos = abstractClientPlayerEntity.getInterpolator().getLerpedPos();
            Vec3d vec3d = this.map.put(abstractClientPlayerEntity.getId(), lerpedPos);
            boolean local6875_6 = !abstractClientPlayerEntity.hasVehicle()
               && !abstractClientPlayerEntity.isGliding()
               && abstractClientPlayerEntity.getGameMode() != GameMode.CREATIVE
               && abstractClientPlayerEntity.getGameMode() != GameMode.SPECTATOR
               && !abstractClientPlayerEntity.isTouchingWater()
               && !abstractClientPlayerEntity.isInLava()
               && !abstractClientPlayerEntity.isClimbing()
               && mc.world.isSpaceEmpty(abstractClientPlayerEntity, abstractClientPlayerEntity.getBoundingBox().offset(0.0, -0.0625, 0.0));
            boolean local6875_7 = vec3d != null && vec3d.squaredDistanceTo(lerpedPos) < 1.0E-4;
            int local6875_8 = local6875_6 && vec3d != null && !local6875_7 ? this.map3.getOrDefault(abstractClientPlayerEntity.getId(), 0) + 1 : 0;
            this.map3.put(abstractClientPlayerEntity.getId(), local6875_8);
            int local6875_9 = local6875_8 >= 25 ? 40 : Math.max(0, this.map4.getOrDefault(abstractClientPlayerEntity.getId(), 0) - 1);
            this.map4.put(abstractClientPlayerEntity.getId(), local6875_9);
            this.map2
               .put(abstractClientPlayerEntity.getId(), local6875_6 && local6875_7 && local6875_9 == 0 ? this.map2.getOrDefault(abstractClientPlayerEntity.getId(), 0) + 1 : 0);
         }
      }
   };
   private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
      if (mc.player != null && mc.world != null && mc.gameRenderer != null) {
         HudRenderContext.renderElement(hudRenderEvent.getDrawContext());
         int scaledWidth = mc.getWindow().getScaledWidth();
         int scaledHeight = mc.getWindow().getScaledHeight();
         float local6889_4 = GuiScale.computeInt() / GuiScale.computeFloat();
         if (this.clientWorld != mc.world || this.map5.size() > 512) {
            this.map5.clear();
      this.playerRankCache.clear();
            this.clientWorld = mc.world;
         }

         if (this.checkState3()) {
            PlayerItemCooldowns.processData2(mc.world);
         }

         for (Entity entity : mc.world.getEntities()) {
            boolean local6889_7 = false;
            if (entity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity2 && this.box.isChecked()) {
               this.processData3(abstractClientPlayerEntity2, hudRenderEvent.computeFloat(), scaledWidth, scaledHeight, local6889_4);
            }

            if (entity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity3) {
               local6889_7 = this.matchesCriteria4(abstractClientPlayerEntity3, hudRenderEvent.computeFloat(), scaledWidth, scaledHeight, local6889_4);
            } else if (entity instanceof ItemEntity itemEntity && this.elements.isIndexSelected(1)) {
               local6889_7 = this.matchesCriteria6(itemEntity, hudRenderEvent.computeFloat(), scaledWidth, scaledHeight, local6889_4);
            } else if (entity instanceof ProjectileEntity projectileEntity && this.elements.isIndexSelected(1)) {
               local6889_7 = this.matchesCriteria10(projectileEntity, hudRenderEvent.computeFloat(), scaledWidth, scaledHeight, local6889_4);
            }

            if (local6889_7) {
               Render2D.renderElement4(hudRenderEvent.getDrawContext());
            }
         }
      }
   };
   private final HashMap<Integer, TagsModule.DataRecord> map5 = new HashMap<>();
   private ClientWorld clientWorld;
   private final EventListener<WorldRenderEvent> worldRenderListener = worldRenderEvent -> {};
   private static final int[][] intValArray = new int[][]{{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};

   private ItemStack getItemStack2(ProjectileEntity projectileEntity) {
      ItemStack itemStack = projectileEntity instanceof PersistentProjectileEntity persistentProjectileEntity
         ? persistentProjectileEntity.getItemStack()
         : (projectileEntity instanceof FlyingItemEntity flyingItemEntity ? flyingItemEntity.getStack() : ItemStack.EMPTY);
      return itemStack.isEmpty() && projectileEntity instanceof AbstractWindChargeEntity ? Items.WIND_CHARGE.getDefaultStack() : itemStack;
   }

   private int computeInt(AbstractClientPlayerEntity abstractClientPlayerEntity, boolean local6849_2) {
      return this.matchesCriteria8(abstractClientPlayerEntity) ? intVal4 : (local6849_2 ? intVal2 : Colors.computeInt7(ThemeColors.intVal, 133));
   }

   private void processData(ItemStack itemStack, float local6851_2, float local6851_3) {
      int local6851_5 = 0;
      String local6851_4;
      if (!this.matchesCriteria7(itemStack) && !this.matchesCriteria(itemStack)) {
         if (itemStack.isIn(ItemTags.SWORDS) && (local6851_5 = this.computeInt4(itemStack, "minecraft:knockback")) > 0) {
            local6851_4 = "От";
         } else {
            if (!this.matchesCriteria3(itemStack)) {
               return;
            }

            local6851_4 = "По";
            local6851_5 = this.computeInt4(itemStack, "minecraft:mending");
         }
      } else {
         local6851_4 = "Кр";
      }

      String local6851_6 = local6851_5 > 0 ? String.valueOf(local6851_5) : "";
      float local6851_7 = FontText.computeFloat(FontFace.SEMI_BOLD, local6851_4, 4.5F);
      float local6851_8 = local6851_2 + 4.0F - (local6851_7 + FontText.computeFloat(FontFace.SEMI_BOLD, local6851_6, 4.5F)) / 2.0F;
      FontText.processData3(FontFace.SEMI_BOLD, local6851_4, local6851_8, local6851_3 - 5.5F, 4.5F, ThemeColors.intVal5);
      if (!local6851_6.isEmpty()) {
         FontText.processData3(FontFace.SEMI_BOLD, local6851_6, local6851_8 + local6851_7, local6851_3 - 5.5F, 4.5F, intVal3);
      }
   }

   private boolean matchesCriteria(ItemStack itemStack) {
      if (!itemStack.isIn(ItemTags.SWORDS)) {
         return false;
      }

      HashSet<String> local6852_2 = new HashSet<>();

      for (Entry<RegistryEntry<Enchantment>> local6852_4 : itemStack.getEnchantments().getEnchantmentEntries()) {
         local6852_2.add(((RegistryEntry)local6852_4.getKey()).getIdAsString() + local6852_4.getIntValue());
      }

      return local6852_2.contains("minecraft:sharpness8");
   }

   private boolean matchesCriteria2(ItemStack itemStack) {
      if (!itemStack.isIn(ItemTags.SWORDS)) {
         return false;
      }

      HashSet<String> local6856_2 = new HashSet<>();

      for (Entry<RegistryEntry<Enchantment>> local6856_4 : itemStack.getEnchantments().getEnchantmentEntries()) {
         local6856_2.add(((RegistryEntry)local6856_4.getKey()).getIdAsString() + local6856_4.getIntValue());
      }

      return local6856_2.contains("minecraft:knockback1") || local6856_2.contains("minecraft:knockback2") || local6856_2.contains("minecraft:sharpness8");
   }

   private boolean matchesCriteria3(ItemStack itemStack) {
      HashSet<String> local6857_2 = new HashSet<>();

      for (Entry<RegistryEntry<Enchantment>> local6857_4 : itemStack.getEnchantments().getEnchantmentEntries()) {
         local6857_2.add(((RegistryEntry)local6857_4.getKey()).getIdAsString() + local6857_4.getIntValue());
      }

      return local6857_2.contains("minecraft:protection5") && (local6857_2.contains("minecraft:mending1") || local6857_2.contains("minecraft:mending2"));
   }

   @Override
   protected void onDisable() {
      PlayerOutlineRenderer.getInstance().updateState();
      this.map.clear();
      this.map2.clear();
      this.map3.clear();
      this.map4.clear();
      this.map5.clear();
      this.clientWorld = null;
   }

   public boolean checkState() {
      return this.isEnabled() && this.applyShader.isChecked();
   }

   private TagsModule() {
      this.display = this.addSetting(
         new MultiChoiceSetting("Отображать", "На ком рисовать элементы и шейдер.")
            .withOptions("Игроков", "Друзей", "Себя")
            .withSelection("Игроков", "Друзей", "Себя")
      );
      this.elements = this.addSetting(
         new MultiChoiceSetting("Элементы", "Что рисовать поверх игроков и предметов.")
            .withOptions("Ники", "Предметы", "Броню", "Руки")
            .withSelection("Ники", "Предметы", "Руки")
      );
      this.showRpPrefixes = this.addSetting(
         new BooleanSetting("Показывать RP-префиксы", "Отображать префикс команды перед ником игрока в собственных NameTags.", true)
      );
      this.showRpPrefixes.withVisibility(() -> this.elements.isIndexSelected(0));
      this.showRankGlyphs = this.addSetting(
         new BooleanSetting("Значки донатов", "Отображать значки доната и привилегий (ReallyWorld) над игроками.", false)
      );
      this.showRankGlyphs.withVisibility(() -> this.elements.isIndexSelected(0));
      this.showPlayerDonationPotions = this.addSetting(
         new BooleanSetting("Показывать донатные зелья игроков", "Донатные зелья, которыми задело игроков и тебя, и сколько осталось.", true)
      );
      this.addSetting(new SectionSetting("Применить шейдер"));
      this.applyShader = this.addSetting(new BooleanSetting("Применить шейдер", "Обводка и тень вокруг игроков.", false));
      this.outline = this.addSetting(new LegacyNumberSetting("Оутлайн", "Сила обводки.", 0.8, 0.0, 1.0, 0.05));
      this.highlight = this.addSetting(new LegacyNumberSetting("Подсветка", "Сила свечения вокруг игроков.", 0.55, 0.0, 3.0, 0.05));
      this.fill = this.addSetting(new LegacyNumberSetting("Заливка", "Заливка игроков цветом.", 0.0, 0.0, 1.0, 0.05));
      this.customColor = this.addSetting(new BooleanSetting("Кастомный цвет", "Свой цвет вместо акцента темы.", false));
      this.color = this.addSetting(new ColorSetting("Цвет", "Цвет обводки и тени."));
      this.outline.withVisibility(() -> this.applyShader.isChecked());
      this.highlight.withVisibility(() -> this.applyShader.isChecked());
      this.fill.withVisibility(() -> this.applyShader.isChecked());
      this.customColor.withVisibility(() -> this.applyShader.isChecked());
      this.color.withVisibility(() -> this.applyShader.isChecked() && this.customColor.isChecked());
      this.addSetting(new SectionSetting("Бокс"));
      this.box = this.addSetting(new BooleanSetting("Бокс", "Показывать хитбоксы игроков в мире.", false));
      this.customColor2 = this.addSetting(new BooleanSetting("Кастомный цвет", "Свой цвет вместо акцента темы.", false));
      this.color2 = this.addSetting(new ColorSetting("Цвет", "Цвет обводки и заливки хитбокса."));
      this.customColor2.withVisibility(() -> this.box.isChecked());
      this.color2.withVisibility(() -> this.box.isChecked() && this.customColor2.isChecked());
   }

   public float computeFloat(float local6863_1) {
      return Math.max(0.65F, 1.1F - local6863_1 * 0.004F);
   }

   public float computeFloat2(float local6864_1) {
      return this.isEnabled() && this.elements.isIndexSelected(1) ? GuiScale.computeFloat4(11.0F * this.computeFloat(local6864_1)) : 0.0F;
   }

   private void processData2(float local6865_1, float local6865_2, float local6865_3, float local6865_4) {
      Render2D.processData51(
         Math.min(local6865_1, local6865_1 + local6865_3) - (local6865_3 == 0.0F ? 0.875F : 0.5F),
         Math.min(local6865_2, local6865_2 + local6865_4) - (local6865_4 == 0.0F ? 0.875F : 0.5F),
         (local6865_3 == 0.0F ? 0.75F : Math.abs(local6865_3)) + 1.0F,
         (local6865_4 == 0.0F ? 0.75F : Math.abs(local6865_4)) + 1.0F,
         0.0F,
         -16777216
      );
      Render2D.processData51(
         Math.min(local6865_1, local6865_1 + local6865_3) - (local6865_3 == 0.0F ? 0.375F : 0.0F),
         Math.min(local6865_2, local6865_2 + local6865_4) - (local6865_4 == 0.0F ? 0.375F : 0.0F),
         local6865_3 == 0.0F ? 0.75F : Math.abs(local6865_3),
         local6865_4 == 0.0F ? 0.75F : Math.abs(local6865_4),
         0.0F,
         0xFF000000 | this.computeInt3() & 16777215
      );
   }

   private boolean matchesCriteria4(AbstractClientPlayerEntity abstractClientPlayerEntity, float local6866_2, int local6866_3, int local6866_4, float local6866_5) {
      if (this.shouldHideSelfTag(abstractClientPlayerEntity)) {
         return false;
      }

      if (!this.matchesCriteria5(abstractClientPlayerEntity)) {
         return false;
      }

      boolean local6866_6 = FriendStore.isFriend(abstractClientPlayerEntity.getGameProfile().name());
      float local6866_7 = (float)mc.gameRenderer.getCamera().getCameraPos().distanceTo(abstractClientPlayerEntity.getLerpedPos(local6866_2));
      float local6866_8 = Math.max(0.3F, 1.0F - local6866_7 * 0.008F);
      Vec3d vec3d = WorldProjection.getVec3d2(abstractClientPlayerEntity.getLerpedPos(local6866_2), local6866_3, local6866_4);
      if (vec3d != null) {
         float local6866_10 = (float)vec3d.y * local6866_5 + 2.0F;
         if (this.elements.isIndexSelected(3)) {
            local6866_10 = this.computeFloat3(
               abstractClientPlayerEntity, local6866_6, (float)vec3d.x * local6866_5, local6866_10, local6866_8 * Math.max(0.7F, 1.0F - local6866_7 * 0.005F)
            );
         }

         if (this.checkState3()) {
            this.processData4(abstractClientPlayerEntity, (float)vec3d.x * local6866_5, local6866_10, local6866_8 * Math.max(0.7F, 1.0F - local6866_7 * 0.005F));
         }
      }

      if (this.elements.isIndexSelected(0)) {
         Vec3d vec3d2 = WorldProjection.getVec3d2(
            abstractClientPlayerEntity.getLerpedPos(local6866_2).add(0.0, abstractClientPlayerEntity.getBoundingBox().maxY - abstractClientPlayerEntity.getBoundingBox().minY + 0.3, 0.0),
            local6866_3,
            local6866_4
         );
         if (vec3d2 != null) {
            this.processData5(abstractClientPlayerEntity, local6866_6, (float)vec3d2.x * local6866_5, (float)vec3d2.y * local6866_5, local6866_7, local6866_8);
         }
      }

      return true;
   }

   public int computeInt2() {
      return this.customColor.isChecked() ? this.color.getValue() : UiTheme.intVal7;
   }

   @Override
   public boolean outlineEnabled() {
      return this.checkState();
   }

   @Override
   public int outlineColor() {
      return this.computeInt2();
   }

   @Override
   public float fillValue() {
      return this.fill.getValue();
   }

   @Override
   public float outlineValue() {
      return this.outline.getValue();
   }

   @Override
   public float highlightValue() {
      return this.highlight.getValue();
   }

   private float computeFloat3(AbstractClientPlayerEntity abstractClientPlayerEntity, boolean local6868_2, float local6868_3, float local6868_4, float local6868_5) {
      ItemStack mainHandStack = abstractClientPlayerEntity.getMainHandStack();
      ItemStack offHandStack = abstractClientPlayerEntity.getOffHandStack();
      if (mainHandStack.isEmpty() && offHandStack.isEmpty()) {
         return local6868_4;
      }

      float local6868_8 = 12.0F;
      HudRenderContext.processData3(local6868_3, local6868_4, local6868_5);
      float local6868_9 = local6868_4 + 1.0F;

      for (ItemStack itemStack : new ItemStack[]{mainHandStack, offHandStack}) {
         if (!itemStack.isEmpty()) {
            String local6868_14 = itemStack.getCount() > 1 ? " " + itemStack.getCount() + "x" : "";
            float local6868_15 = FontText.computeFloat(FontFace.SEMI_BOLD, itemStack.getName().getString(), 6.0F)
               + FontText.computeFloat(FontFace.SEMI_BOLD, local6868_14, 6.0F);
            Render2D.processData51(
               local6868_3 - local6868_15 / 2.0F - 2.0F, local6868_9, local6868_15 + 4.0F, local6868_8, 0.0F, this.computeInt(abstractClientPlayerEntity, local6868_2)
            );
            FontText.processData5(
               FontFace.SEMI_BOLD,
               itemStack.getName()
                  .copy()
                  .styled(
                     style -> style.getColor() != null && (style.getColor().getRgb() & 16777215) != 16777215
                        ? style
                        : style.withColor(TextColor.fromRgb(ThemeColors.intVal5 & 16777215))
                  ),
               local6868_3 - local6868_15 / 2.0F,
               local6868_9 + local6868_8 / 2.0F - 3.65F,
               6.0F,
               ThemeColors.intVal5
            );
            if (!local6868_14.isEmpty()) {
               FontText.processData3(
                  FontFace.SEMI_BOLD,
                  local6868_14,
                  local6868_3 - local6868_15 / 2.0F + FontText.computeFloat(FontFace.SEMI_BOLD, itemStack.getName().getString(), 6.0F),
                  local6868_9 + local6868_8 / 2.0F - 3.65F,
                  6.0F,
                  ThemeColors.intVal5
               );
            }

            local6868_9 += local6868_8 + 1.0F;
         }
      }

      HudRenderContext.updateState();
      return local6868_4 + (local6868_9 - local6868_4) * local6868_5;
   }

   private void processData3(AbstractClientPlayerEntity abstractClientPlayerEntity, float local6870_2, int local6870_3, int local6870_4, float local6870_5) {
      if (!this.shouldHideSelfTag(abstractClientPlayerEntity) && this.matchesCriteria5(abstractClientPlayerEntity)) {
         Vector4f vector4f = WorldProjection.getVector4f(
            abstractClientPlayerEntity.getBoundingBox().offset(abstractClientPlayerEntity.getLerpedPos(local6870_2).subtract(abstractClientPlayerEntity.getEntityPos())), local6870_3, local6870_4
         );
         if (vector4f != null && !(vector4f.z <= 0.0F) && !(vector4f.w <= 0.0F)) {
            this.processData2(vector4f.x * local6870_5, vector4f.y * local6870_5, vector4f.z * local6870_5, 0.0F);
            this.processData2(vector4f.x * local6870_5, (vector4f.y + vector4f.w) * local6870_5, vector4f.z * local6870_5, 0.0F);
            this.processData2(vector4f.x * local6870_5, vector4f.y * local6870_5, 0.0F, vector4f.w * local6870_5);
            this.processData2((vector4f.x + vector4f.z) * local6870_5, vector4f.y * local6870_5, 0.0F, vector4f.w * local6870_5);
         }
      }
   }

   private boolean shouldHideSelfTag(AbstractClientPlayerEntity player) {
      return player == mc.player
         && mc.options.getPerspective().isFirstPerson();
   }

   public boolean matchesCriteria5(AbstractClientPlayerEntity abstractClientPlayerEntity) {
      if (abstractClientPlayerEntity == mc.player) {
         return this.display.matchesOption("Себя");
      } else {
         return FriendStore.isFriend(abstractClientPlayerEntity.getGameProfile().name())
            ? this.display.matchesOption("Друзей")
            : this.display.matchesOption("Игроков");
      }
   }

   public static TagsModule getInstance() {
      if (tagsModule == null) {
         tagsModule = new TagsModule();
      }

      return tagsModule;
   }

   private boolean matchesCriteria6(ItemEntity itemEntity, float local6873_2, int local6873_3, int local6873_4, float local6873_5) {
      return this.matchesCriteria9(itemEntity.getStack(), itemEntity.getLerpedPos(local6873_2), itemEntity.getId(), local6873_3, local6873_4, local6873_5);
   }

   private void processData4(AbstractClientPlayerEntity abstractClientPlayerEntity, float local6874_2, float local6874_3, float local6874_4) {
      Map<ServerItemType, Long> local6874_5 = PlayerItemCooldowns.getMap(abstractClientPlayerEntity);
      if (!local6874_5.isEmpty()) {
         long local6874_6 = System.currentTimeMillis();
         float local6874_8 = local6874_3 + 2.0F;
         HudRenderContext.processData3(local6874_2, local6874_3, local6874_4);

         for (java.util.Map.Entry<ServerItemType, Long> local6874_10 : local6874_5.entrySet()) {
            String local6874_11 = " " + Math.max(1L, (local6874_10.getValue() - local6874_6 + 999L) / 1000L) + "с";
            float local6874_12 = FontText.computeFloat(FontFace.SEMI_BOLD, local6874_10.getKey().formatText(), 5.0F);
            float local6874_13 = local6874_12 + FontText.computeFloat(FontFace.SEMI_BOLD, local6874_11, 5.0F);
            FontText.processData5(
               FontFace.SEMI_BOLD,
               local6874_10.getKey().getText2(),
               local6874_2 - local6874_13 / 2.0F,
               local6874_8,
               5.0F,
               ThemeColors.intVal5
            );
            FontText.processData3(
               FontFace.SEMI_BOLD,
               local6874_11,
               local6874_2 - local6874_13 / 2.0F + local6874_12,
               local6874_8,
               5.0F,
               HudStyle.computeInt2(1.0F)
            );
            local6874_8 += 6.5F;
         }

         HudRenderContext.updateState();
      }
   }

   public int computeInt3() {
      return this.customColor2.isChecked() ? this.color2.getValue() : UiTheme.intVal7;
   }

   private boolean matchesCriteria7(ItemStack itemStack) {
      if (!itemStack.isOf(Items.MACE)) {
         return false;
      }

      HashSet<String> local6878_2 = new HashSet<>();

      for (Entry<RegistryEntry<Enchantment>> local6878_4 : itemStack.getEnchantments().getEnchantmentEntries()) {
         local6878_2.add(((RegistryEntry)local6878_4.getKey()).getIdAsString() + local6878_4.getIntValue());
      }

      return local6878_2.contains("minecraft:breach3") && local6878_2.contains("minecraft:density5");
   }

   private int computeInt4(ItemStack itemStack, String local6879_2) {
      for (Entry<RegistryEntry<Enchantment>> local6879_4 : itemStack.getEnchantments().getEnchantmentEntries()) {
         if (((RegistryEntry)local6879_4.getKey()).getIdAsString().equals(local6879_2)) {
            return local6879_4.getIntValue();
         }
      }

      return 0;
   }

   private boolean checkState2() {
      return this.elements.isIndexSelected(2)
         || ServerHelperModule.getInstance().isEnabled() && ServerHelperModule.getInstance().modeSetting.isSelected("SpookyTime");
   }

   private static final Map<String, Text> CHAT_PREFIX_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

   private static final java.util.regex.Pattern CHAT_PREFIX_REGEX = java.util.regex.Pattern.compile(
      "^(?:\\[[GLГЛgl!]+\\]\\s*)?((?:\\[[^\\]]+\\]\\s*)+)\\s*([A-Za-z0-9_]{3,16})\\s*[:»›>]"
   );

   public static void recordChatPrefix(String rawText, Text formattedText) {
      if (rawText == null || rawText.isBlank()) {
         return;
      }
      java.util.regex.Matcher matcher = CHAT_PREFIX_REGEX.matcher(rawText);
      if (matcher.find()) {
         String prefixStr = matcher.group(1).trim();
         String nick = matcher.group(2).trim().toLowerCase();
         if (!prefixStr.equalsIgnoreCase("[лс]") && !prefixStr.equalsIgnoreCase("[pm]") && !prefixStr.equalsIgnoreCase("[msg]")) {
            CHAT_PREFIX_CACHE.put(nick, Text.literal(prefixStr));
         }
      }
   }

   public static Text extractEntityText(Entity entity) {
      if (entity == null) {
         return null;
      }
      if (entity instanceof DisplayEntity.TextDisplayEntity textDisplay) {
         Text t = textDisplay.getText();
         if (t != null && !t.getString().isBlank()) {
            return t;
         }
      }
      if (entity.getCustomName() != null && !entity.getCustomName().getString().isBlank()) {
         return entity.getCustomName();
      }
      return null;
   }

   private Text getOverheadEntityTag(AbstractClientPlayerEntity player) {
      if (mc.world == null) {
         return null;
      }

      String playerName = player.getGameProfile().name();

      // 1. Check passenger tree
      for (Entity passenger : player.getPassengerList()) {
         Text t = extractEntityText(passenger);
         if (t != null && !t.getString().trim().equalsIgnoreCase(playerName)) {
            return t;
         }
         for (Entity child : passenger.getPassengerList()) {
            Text ct = extractEntityText(child);
            if (ct != null && !ct.getString().trim().equalsIgnoreCase(playerName)) {
               return ct;
            }
         }
      }

      // 2. Check nearby floating entities (TextDisplays & ArmorStands within 1.5 blocks X/Z and -0.2 to +3.6 Y)
      double px = player.getX();
      double py = player.getY();
      double pz = player.getZ();

      Text bestTag = null;
      double closestDist = Double.MAX_VALUE;

      for (Entity other : mc.world.getEntities()) {
         if (other == player) {
            continue;
         }
         boolean isTagType = other instanceof DisplayEntity.TextDisplayEntity
            || other instanceof ArmorStandEntity;

         if (!isTagType && other.getCustomName() == null) {
            continue;
         }

         Text text = extractEntityText(other);
         if (text == null || text.getString().isBlank()) {
            continue;
         }

         String raw = text.getString().trim();
         if (raw.equalsIgnoreCase(playerName)) {
            continue;
         }

         double dx = other.getX() - px;
         double dy = other.getY() - py;
         double dz = other.getZ() - pz;
         double hDistSq = dx * dx + dz * dz;

         if (hDistSq <= 2.25 && dy >= -0.2 && dy <= 3.6) {
            if (raw.toLowerCase().contains(playerName.toLowerCase())) {
               return text;
            }
            if (hDistSq < closestDist) {
               closestDist = hDistSq;
               bestTag = text;
            }
         }
      }

      return bestTag;
   }

   private Team findScoreboardTeam(AbstractClientPlayerEntity player, String playerName) {
      if (mc.world == null) {
         return null;
      }

      // 1. Try Tab list entry's team first
      if (mc.getNetworkHandler() != null) {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
         if (entry == null) {
            entry = mc.getNetworkHandler().getPlayerListEntry(playerName);
         }
         if (entry != null && entry.getScoreboardTeam() != null && hasTeamContent(entry.getScoreboardTeam())) {
            return entry.getScoreboardTeam();
         }
      }

      // 2. Try player's own team
      Team team = player.getScoreboardTeam();
      if (team != null && hasTeamContent(team)) {
         return team;
      }

      // 3. Try scoreboard lookup by various keys
      Scoreboard scoreboard = mc.world.getScoreboard();
      if (scoreboard != null) {
         team = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());
         if (team != null && hasTeamContent(team)) {
            return team;
         }

         team = scoreboard.getScoreHolderTeam(playerName);
         if (team != null && hasTeamContent(team)) {
            return team;
         }

         team = scoreboard.getScoreHolderTeam(player.getUuidAsString());
         if (team != null && hasTeamContent(team)) {
            return team;
         }

         for (Team t : scoreboard.getTeams()) {
            if (!hasTeamContent(t)) {
               continue;
            }
            for (String member : t.getPlayerList()) {
               if (member.equalsIgnoreCase(playerName)
                  || member.equalsIgnoreCase(player.getNameForScoreboard())
                  || member.equalsIgnoreCase(player.getUuidAsString())) {
                  return t;
               }
            }
         }
      }

      return null;
   }

   private static boolean hasTeamContent(Team team) {
      if (team == null) {
         return false;
      }
      Text prefix = team.getPrefix();
      Text suffix = team.getSuffix();
      return (prefix != null && !prefix.getString().isEmpty())
         || (suffix != null && !suffix.getString().isEmpty());
   }

   // --- ReallyWorld & Custom Server Rank Glyphs ---
   public enum RankGlyph {
      // Compound staff ranks (checked first)
      D_ADMIN("\uA538", '\uA538', "d.admin", "d_admin", "d-admin", "д.админ", "д-админ", "dadmin"),
      ML_ADMIN("\uA533", '\uA533', "ml.admin", "ml_admin", "ml-admin", "мл.админ", "мл-админ", "младший админ", "mladmin"),
      ADMIN("\uA537", '\uA537', "admin", "админ", "администратор"),

      GL_MODER("\uA525", '\uA525', "gl.moder", "gl_moder", "gl-moder", "гл.модер", "гл-модер", "главный модер", "glmoder"),
      ST_MODER("\uA521", '\uA521', "st.moder", "st_moder", "st-moder", "ст.модер", "ст-модер", "старший модер", "stmoder"),
      ML_MODER("\uA513", '\uA513', "ml.moder", "ml_moder", "ml-moder", "мл.модер", "мл-модер", "младший модер", "mlmoder"),
      D_MODER("\uA529", '\uA529', "d.moder", "d_moder", "d-moder", "д.модер", "д-модер", "dmoder"),
      MODER("\uA517", '\uA517', "moder", "модер", "модератор"),

      D_HELPER("\uA560", '\uA560', "d.helper", "d_helper", "d-helper", "д.хелпер", "д-хелпер", "dhelper"),
      HELPER("\uA509", '\uA509', "helper", "хелпер"),

      YT("\uA505", '\uA505', "youtube", "ютуб", "yt", "ют"),
      MEDIA("\uA501", '\uA501', "media", "медиа"),

      // Donor ranks
      HORUS("\uA531", '\uA531', "horus", "хорус"),
      INFINITY("\uA530", '\uA530', "infinity", "инфинити", "бесконечность"),
      PEGAS("\uA549", '\uA549', "pegas", "пегас", "pegasus"),
      DIABLO("\uA545", '\uA545', "diablo", "диабло", "дьябло"),
      GOD("\uA541", '\uA541', "god", "бог"),
      COBRA("\uA548", '\uA548', "cobra", "кобра"),
      HYDRA("\uA540", '\uA540', "hydra", "гидра", "хайдра"),
      RABBIT("\uA552", '\uA552', "rabbit", "рэббит", "крол"),
      DRACULA("\uA544", '\uA544', "dracula", "дракула"),
      BUNNY("\uA556", '\uA556', "bunny", "банни", "кролик", "заяц"),
      TIGER("\uA536", '\uA536', "tiger", "тигр", "тайгер"),
      BULL("\uA532", '\uA532', "bull", "булл", "бык"),
      DRAGON("\uA528", '\uA528', "dragon", "дракон", "драг"),
      IMPERATOR("\uA524", '\uA524', "imperator", "император", "имп"),
      MAGISTER("\uA520", '\uA520', "magister", "магистр", "маг"),
      OVERLORD("\uA516", '\uA516', "overlord", "оверлорд", "овер"),
      AVENGER("\uA512", '\uA512', "avenger", "мститель", "авенджер"),
      TITAN("\uA508", '\uA508', "titan", "титан"),
      HERO("\uA504", '\uA504', "hero", "герой", "херо"),
      PLAYER("", '\0', "player", "игрок");

      public final String glyph;
      public final char character;
      public final String[] keywords;

      RankGlyph(String glyph, char character, String... keywords) {
         this.glyph = glyph;
         this.character = character;
         this.keywords = keywords;
      }

      public static RankGlyph fromChar(char c) {
         for (RankGlyph r : values()) {
            if (r.character == c) {
               return r;
            }
         }
         return null;
      }
   }

   private static class CachedRank {
      final RankGlyph rank;
      final long timestamp;

      CachedRank(RankGlyph rank, long timestamp) {
         this.rank = rank;
         this.timestamp = timestamp;
      }
   }

   private final Map<UUID, CachedRank> playerRankCache = new HashMap<>();

   public static boolean containsRankGlyph(String str) {
      if (str == null || str.isEmpty()) {
         return false;
      }
      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if (c >= '\uA500' && c <= '\uA565') {
            return true;
         }
      }
      return false;
   }

   public static String stripRankGlyphs(String str) {
      if (str == null || str.isEmpty()) {
         return str;
      }
      StringBuilder sb = new StringBuilder(str.length());
      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if (c >= '\uA500' && c <= '\uA570') {
            continue;
         }
         sb.append(c);
      }
      return sb.toString();
   }

   public static Text stripGlyphsFromText(Text text) {
      if (text == null) {
         return null;
      }
      if (!containsRankGlyph(text.getString())) {
         return text;
      }
      MutableText result = Text.empty();
      text.visit((style, part) -> {
         String cleaned = stripRankGlyphs(part);
         if (!cleaned.isEmpty()) {
            result.append(Text.literal(cleaned).setStyle(style));
         }
         return java.util.Optional.empty();
      }, Style.EMPTY);
      return result;
   }


   private static String cleanStringRankKeywords(String str, RankGlyph rank) {
      if (str == null || str.isEmpty() || rank == null || rank == RankGlyph.PLAYER) {
         return str;
      }
      for (String kw : rank.keywords) {
         String pattern = "(?i)\\s*[\\[(<]?" + java.util.regex.Pattern.quote(kw) + "[\\])>]?\\s*";
         str = str.replaceAll(pattern, " ");
      }
      return str.trim();
   }

   private static Text stripRankKeyword(Text text, RankGlyph rank) {
      if (text == null || rank == null || rank == RankGlyph.PLAYER) {
         return text;
      }
      String raw = text.getString();
      if (raw.isBlank()) {
         return null;
      }
      boolean hasKw = false;
      for (String kw : rank.keywords) {
         if (raw.toLowerCase().contains(kw.toLowerCase())) {
            hasKw = true;
            break;
         }
      }
      if (!hasKw) {
         return text;
      }
      String cleaned = cleanStringRankKeywords(raw, rank);
      if (cleaned.isBlank()) {
         return null;
      }
      return Text.literal(cleaned).setStyle(text.getStyle());
   }

   private RankGlyph computePlayerRank(AbstractClientPlayerEntity player, String playerName, String candidateText) {
      if (candidateText != null) {
         for (int i = 0; i < candidateText.length(); i++) {
            RankGlyph rg = RankGlyph.fromChar(candidateText.charAt(i));
            if (rg != null) {
               return rg;
            }
         }
      }

      StringBuilder sb = new StringBuilder();
      if (candidateText != null) {
         sb.append(candidateText).append(' ');
      }
      Team team = this.findScoreboardTeam(player, playerName);
      if (team != null) {
         if (team.getPrefix() != null) sb.append(team.getPrefix().getString()).append(' ');
         if (team.getSuffix() != null) sb.append(team.getSuffix().getString()).append(' ');
      }
      if (mc.getNetworkHandler() != null) {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
         if (entry == null) {
            entry = mc.getNetworkHandler().getPlayerListEntry(playerName);
         }
         if (entry != null && entry.getDisplayName() != null) {
            sb.append(entry.getDisplayName().getString()).append(' ');
         }
      }
      Text chat = CHAT_PREFIX_CACHE.get(playerName.toLowerCase());
      if (chat != null) {
         sb.append(chat.getString()).append(' ');
      }
      if (player.getCustomName() != null) {
         sb.append(player.getCustomName().getString()).append(' ');
      }
      if (player.getDisplayName() != null) {
         sb.append(player.getDisplayName().getString()).append(' ');
      }

      String combined = sb.toString();

      for (int i = 0; i < combined.length(); i++) {
         RankGlyph rg = RankGlyph.fromChar(combined.charAt(i));
         if (rg != null) {
            return rg;
         }
      }

      String clean = combined.replaceAll("§[0-9a-fk-or]", " ");
      if (playerName != null && !playerName.isEmpty()) {
         clean = clean.replaceAll("(?i)" + java.util.regex.Pattern.quote(playerName), " ");
      }
      clean = clean.toLowerCase();

      for (RankGlyph rg : RankGlyph.values()) {
         if (rg == RankGlyph.PLAYER) continue;
         for (String kw : rg.keywords) {
            String pattern = "(?<![a-zA-Zа-яА-Я0-9])" + java.util.regex.Pattern.quote(kw) + "(?![a-zA-Zа-яА-Я0-9])";
            if (java.util.regex.Pattern.compile(pattern).matcher(clean).find()) {
               return rg;
            }
         }
      }

      return RankGlyph.PLAYER;
   }

   private RankGlyph detectPlayerRank(AbstractClientPlayerEntity player, String playerName, String candidateText) {
      long now = System.currentTimeMillis();
      UUID uuid = player.getUuid();
      CachedRank cached = this.playerRankCache.get(uuid);
      if (cached != null && (now - cached.timestamp < 1500L)) {
         return cached.rank;
      }
      RankGlyph rank = this.computePlayerRank(player, playerName, candidateText);
      this.playerRankCache.put(uuid, new CachedRank(rank, now));
      return rank;
   }

   private Text getNameTagText(AbstractClientPlayerEntity player) {
      String playerName = player.getGameProfile().name();
      boolean showPrefixes = this.showRpPrefixes.isChecked();
      boolean showGlyphs = this.showRankGlyphs.isChecked();
      if ((!showPrefixes && !showGlyphs) || mc.world == null) {
         return Text.literal(playerName);
      }

      // 1. Overhead entities (TextDisplay / ArmorStand / Passengers)
      Text overhead = this.getOverheadEntityTag(player);
      if (overhead != null && (containsRankGlyph(overhead.getString()) || showPrefixes)) {
         Text cleanedOverhead = showGlyphs ? overhead : stripGlyphsFromText(overhead);
         String overStr = cleanedOverhead != null ? cleanedOverhead.getString().trim() : "";
         if (overStr.toLowerCase().contains(playerName.toLowerCase())) {
            return cleanedOverhead;
         }
      }

      // 2. Tab list display name from network handler
      PlayerListEntry entry = null;
      if (mc.getNetworkHandler() != null) {
         entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
         if (entry == null) {
            entry = mc.getNetworkHandler().getPlayerListEntry(playerName);
         }
      }
      if (entry != null && entry.getDisplayName() != null && (containsRankGlyph(entry.getDisplayName().getString()) || showPrefixes)) {
         Text disp = showGlyphs ? entry.getDisplayName() : stripGlyphsFromText(entry.getDisplayName());
         String dispStr = disp != null ? disp.getString().trim() : "";
         if (!dispStr.equalsIgnoreCase(playerName)) {
            if (overhead != null && !dispStr.contains(overhead.getString().trim())) {
               Text oh = showGlyphs ? overhead : stripGlyphsFromText(overhead);
               return Text.empty().append(oh).append(" ").append(disp);
            }
            return disp;
         }
      }

      // 3. Scoreboard Team Prefix + Suffix
      Team team = this.findScoreboardTeam(player, playerName);
      if (team != null && team.getPrefix() != null && containsRankGlyph(team.getPrefix().getString())) {
         MutableText result = Text.empty();
         if (overhead != null) {
            result.append(overhead).append(" ");
         }
         result.append(team.getPrefix());
         MutableText nameComp = Text.literal(playerName);
         if (team.getColor() != null && team.getColor() != Formatting.RESET) {
            nameComp.formatted(team.getColor());
         }
         result.append(nameComp);
         if (team.getSuffix() != null) {
            result.append(team.getSuffix());
         }
         return result;
      }

      // 4. Chat Prefix Cache
      Text cachedChatPrefix = CHAT_PREFIX_CACHE.get(playerName.toLowerCase());
      if (cachedChatPrefix != null && containsRankGlyph(cachedChatPrefix.getString())) {
         MutableText result = Text.empty();
         if (overhead != null) {
            result.append(overhead).append(" ");
         }
         result.append(cachedChatPrefix).append(" ");
         MutableText nameComp = Text.literal(playerName);
         if (team != null && team.getColor() != null && team.getColor() != Formatting.RESET) {
            nameComp.formatted(team.getColor());
         }
         result.append(nameComp);
         return result;
      }

      // Determine rank if enabled
      RankGlyph rank = showGlyphs ? this.detectPlayerRank(player, playerName, overhead != null ? overhead.getString() : null) : null;

      // Base player name component
      MutableText nameComp = Text.literal(playerName);
      if (team != null && team.getColor() != null && team.getColor() != Formatting.RESET) {
         nameComp.formatted(team.getColor());
      }

      MutableText result = Text.empty();

      // Prepend rank glyph if enabled
      if (showGlyphs && rank != null && rank != RankGlyph.PLAYER && rank.glyph != null && !rank.glyph.isEmpty()) {
         result.append(Text.literal(rank.glyph + " "));
      }

      // If showRpPrefixes is enabled, append prefixes
      if (showPrefixes) {
         if (overhead != null) {
            Text cleanedOverhead = stripRankKeyword(overhead, rank);
            if (cleanedOverhead != null && !cleanedOverhead.getString().isBlank() && !cleanedOverhead.getString().equalsIgnoreCase(playerName)) {
               result.append(cleanedOverhead).append(" ");
            }
         }

         Text prefix = team != null ? team.getPrefix() : null;
         Text suffix = team != null ? team.getSuffix() : null;
         boolean hasPrefix = (prefix != null && !prefix.getString().trim().isEmpty());
         boolean hasSuffix = (suffix != null && !suffix.getString().trim().isEmpty());

         if (hasPrefix) {
            Text cleanedPrefix = stripRankKeyword(prefix, rank);
            if (cleanedPrefix != null && !cleanedPrefix.getString().isBlank()) {
               result.append(cleanedPrefix);
            }
         } else if (cachedChatPrefix != null) {
            Text cleanedChat = stripRankKeyword(cachedChatPrefix, rank);
            if (cleanedChat != null && !cleanedChat.getString().isBlank()) {
               result.append(cleanedChat).append(" ");
            }
         } else if (entry != null && entry.getDisplayName() != null) {
            String dispStr = entry.getDisplayName().getString().trim();
            if (!dispStr.equalsIgnoreCase(playerName)) {
               Text cleanedTab = stripRankKeyword(entry.getDisplayName(), rank);
               if (cleanedTab != null && !cleanedTab.getString().isBlank()) {
                  if (cleanedTab.getString().toLowerCase().contains(playerName.toLowerCase())) {
                     result.append(cleanedTab);
                     return result;
                  } else {
                     result.append(cleanedTab).append(" ");
                  }
               }
            }
         }

         result.append(nameComp);

         if (hasSuffix && suffix != null) {
            result.append(suffix);
         }
         return result;
      }

      result.append(nameComp);
      return result;
   }

   private static boolean hasResourcePackGlyphs(Text text) {
      if (text == null) {
         return false;
      }
      String str = text.getString();
      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if ((c >= 0xA500 && c <= 0xA63F) || (c >= 0xE000 && c <= 0xF8FF) || Character.isSupplementaryCodePoint(c)) {
            return true;
         }
      }
      boolean[] customFont = new boolean[1];
      text.getContent().visit((style, s) -> {
         if (style != null && style.getFont() != null && !style.getFont().equals(StyleSpriteSource.DEFAULT)) {
            customFont[0] = true;
         }
         return Optional.empty();
      }, text.getStyle());
      if (customFont[0]) {
         return true;
      }
      for (Text sibling : text.getSiblings()) {
         if (hasResourcePackGlyphs(sibling)) {
            return true;
         }
      }
      return false;
   }

   private float getNametagTextWidth(Text text, float size) {
      if (text == null) {
         return 0.0F;
      }
      if ((this.showRpPrefixes.isChecked() || this.showRankGlyphs.isChecked()) && mc.textRenderer != null && hasResourcePackGlyphs(text)) {
         return (float) mc.textRenderer.getWidth(text) * (size / 9.0F);
      }
      return this.getRasterTextWidth(text, size);
   }

   private void drawNametagText(Text text, float x, float y, float size) {
      if (text == null) {
         return;
      }

      DrawContext context = HudRenderContext.getDrawContext();
      if ((this.showRpPrefixes.isChecked() || this.showRankGlyphs.isChecked()) && context != null && mc.textRenderer != null && hasResourcePackGlyphs(text)) {
         float scale = (size / 9.0F) * GuiScale.computeFloat3();
         float guiX = GuiScale.computeFloat4(x);
         float guiY = GuiScale.computeFloat4(y) + (size * GuiScale.computeFloat3() - 9.0F * scale) / 2.0F;

         context.getMatrices().pushMatrix();
         context.getMatrices().translate(guiX, guiY);
         context.getMatrices().scale(scale, scale);
         context.drawText(mc.textRenderer, text, 0, 0, 0xFFFFFFFF, false);
         context.getMatrices().popMatrix();
         return;
      }

      this.drawRasterText(text, x, y, size);
   }

   private float getRasterTextWidth(Text text, float size) {
      float width = 0.0F;
      for (FormattedText.DataRecord part : FormattedText.getArrayList2(text, ThemeColors.intVal5)) {
         width += Render2D.computeFloat2("noto_sans", part.text(), size);
      }
      return width;
   }

   private float drawRasterText(Text text, float x, float y, float size) {
      float width = 0.0F;
      for (FormattedText.DataRecord part : FormattedText.getArrayList2(text, ThemeColors.intVal5)) {
         int color = part.color() & 0xFFFFFF | ThemeColors.intVal5 & 0xFF000000;
         Render2D.processData65(new RasterTextDrawData("noto_sans", part.text(), x + width, y, size, color));
         width += Render2D.computeFloat2("noto_sans", part.text(), size);
      }
      return width;
   }

   private void processData5(
      AbstractClientPlayerEntity abstractClientPlayerEntity, boolean local6881_2, float local6881_3, float local6881_4, float local6881_5, float local6881_6
   ) {
      String local6881_7 = abstractClientPlayerEntity.getGameProfile().name();
      String local6881_8 = EntityUtils.formatHealth(abstractClientPlayerEntity);
      Text nameTagText = this.getNameTagText(abstractClientPlayerEntity);
      String local6881_12 = String.valueOf(Math.round(local6881_5));
      float local6881_13 = this.getNametagTextWidth(nameTagText, 6.0F);
      float local6881_14 = 3.0F;
      boolean local6881_16 = this.matchesCriteria8(abstractClientPlayerEntity);
      float local6881_17 = local6881_16 ? FontText.computeFloat(FontFace.SEMI_BOLD, "AIRSTUCK", 6.0F) + local6881_14 : 0.0F;

      boolean isPulseUser = PulseModModule.getInstance().isPulseActive() && (
         abstractClientPlayerEntity == mc.player
         || (mc.getSession() != null && local6881_7.equalsIgnoreCase(mc.getSession().getUsername()))
         || (!PulseModModule.getInstance().getAccountLogin().isEmpty() && local6881_7.equalsIgnoreCase(PulseModModule.getInstance().getAccountLogin()))
      );
      float pulseBadgeWidth = isPulseUser ? (11.0F + local6881_14) : 0.0F;

      float local6881_18 = 2.0F
         + local6881_17
         + pulseBadgeWidth
         + local6881_13
         + local6881_14
         + FontText.computeFloat(FontFace.SEMI_BOLD, "[", 6.0F)
         + FontText.computeFloat(FontFace.SEMI_BOLD, local6881_8, 6.0F)
         + FontText.computeFloat(FontFace.SEMI_BOLD, "/", 6.0F)
         + FontText.computeFloat(FontFace.SEMI_BOLD, local6881_12, 6.0F)
         + FontText.computeFloat(FontFace.SEMI_BOLD, "]", 6.0F)
         + 2.0F;
      float local6881_19 = 12.0F;
      float local6881_20 = local6881_3 - local6881_18 / 2.0F;
      float local6881_21 = local6881_4 - local6881_19 - 3.0F + 6.0F - Math.min(6.0F, local6881_5 * 0.08F);
      float local6881_22 = local6881_21 + local6881_19 / 2.0F - 3.65F;
      HudRenderContext.processData3(local6881_3, local6881_4, local6881_6 * Math.max(0.7F, 1.0F - local6881_5 * 0.005F));
      int accentColor = local6881_16 ? intVal3 : (local6881_2 ? intVal6 : ThemeColors.intVal);
      int glowColor = local6881_16 ? 0x88FF3333 : (local6881_2 ? 0x8800FF77 : Colors.computeInt7(ThemeColors.intVal, 110));
      int glassTint = local6881_16 ? 0xE62A1416 : (local6881_2 ? 0xE614261B : 0xE60E121A);
      int outlineColor = Colors.computeInt7(accentColor, 175);

      Render2D.processData15(local6881_20, local6881_21, local6881_18, local6881_19, 4.0F, -1);
      Render2D.processData58(
         new SoftGlowDrawData(local6881_20, local6881_21, local6881_18, local6881_19, 4.0F, glowColor, 6.0F).withIntensity(0.9F)
      );
      Render2D.processData33(
         local6881_20,
         local6881_21,
         local6881_18,
         local6881_19,
         4.0F,
         4.0F,
         4.0F,
         4.0F,
         glassTint,
         0.95F,
         2.2F,
         accentColor,
         0.35F,
         false,
         0.72F,
         0.015F,
         1.0F,
         0.0F
      );
      Render2D.processData20(
         local6881_20, local6881_21, local6881_18, local6881_19, 4.0F, 4.0F, 4.0F, 4.0F, 0.9F, outlineColor
      );
      float local6881_23 = local6881_20 + 2.0F;

      if (local6881_16) {
         FontText.processData3(FontFace.SEMI_BOLD, "AIRSTUCK", local6881_23, local6881_22, 6.0F, intVal3);
         local6881_23 += FontText.computeFloat(FontFace.SEMI_BOLD, "AIRSTUCK", 6.0F) + local6881_14;
      }

      if (isPulseUser) {
         float badgeX = local6881_23;
         float badgeY = local6881_21 + 1.5F;
         Render2D.processData58(
            new SoftGlowDrawData(badgeX, badgeY, 11.0F, 9.0F, 2.0F, 0x886444E0, 4.0F).withIntensity(0.9F)
         );
         Render2D.processData51(badgeX, badgeY, 11.0F, 9.0F, 2.0F, 0x991E1336);
         Render2D.processData20(badgeX, badgeY, 11.0F, 9.0F, 2.0F, 2.0F, 2.0F, 2.0F, 0.8F, 0xCC7F5AF0);
         Render2D.processData89("assets/horus/textures/icons/pulse_logo.png", badgeX + 1.5F, badgeY + 0.5F, 8.0F, 8.0F, 0.0F, -1);
         local6881_23 += pulseBadgeWidth;
      }

      this.drawNametagText(nameTagText, local6881_23, local6881_22, 6.0F);
      local6881_23 += local6881_13 + local6881_14;
      FontText.processData3(FontFace.SEMI_BOLD, "[", local6881_23, local6881_22, 6.0F, intVal5);
      local6881_23 += FontText.computeFloat(FontFace.SEMI_BOLD, "[", 6.0F);
      FontText.processData3(FontFace.SEMI_BOLD, local6881_8, local6881_23, local6881_22, 6.0F, intVal3);
      local6881_23 += FontText.computeFloat(FontFace.SEMI_BOLD, local6881_8, 6.0F);
      FontText.processData3(FontFace.SEMI_BOLD, "/", local6881_23, local6881_22, 6.0F, intVal5);
      local6881_23 += FontText.computeFloat(FontFace.SEMI_BOLD, "/", 6.0F);
      FontText.processData3(FontFace.SEMI_BOLD, local6881_12, local6881_23, local6881_22, 6.0F, intVal6);
      local6881_23 += FontText.computeFloat(FontFace.SEMI_BOLD, local6881_12, 6.0F);
      FontText.processData3(FontFace.SEMI_BOLD, "]", local6881_23, local6881_22, 6.0F, intVal5);
      if (!this.checkState2()) {
         ArrayList<ItemStack> local6881_24 = new ArrayList<>();
         if (this.matchesCriteria2(abstractClientPlayerEntity.getMainHandStack()) || this.matchesCriteria7(abstractClientPlayerEntity.getMainHandStack())) {
            local6881_24.add(abstractClientPlayerEntity.getMainHandStack());
         }

         for (ItemStack itemStack : new ItemStack[]{
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.HEAD),
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST),
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.LEGS),
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.FEET)
         }) {
            if (this.matchesCriteria3(itemStack)) {
               local6881_24.add(itemStack);
            }
         }

         local6881_23 = local6881_3 - (local6881_24.size() * 8.0F + (local6881_24.size() - 1) * 2.0F) / 2.0F;

         for (ItemStack itemStack2 : local6881_24) {
            this.processData(itemStack2, local6881_23, local6881_21 - 10.0F - 1.5F + 1.0F);
            ItemRenderCache.processData(
               itemStack2, local6881_23, local6881_21 - 10.0F - 1.5F + 1.0F, 8.0F, ItemDrawOptions.noDecorations(1.0F).glint(GlintMode.OFF)
            );
            local6881_23 += 10.0F;
         }
      }

      if (this.checkState2()) {
         ArrayList<ItemStack> local6881_24 = new ArrayList<>();

         for (ItemStack itemStack3 : new ItemStack[]{
            abstractClientPlayerEntity.getMainHandStack(),
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.HEAD),
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST),
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.LEGS),
            abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.FEET),
            abstractClientPlayerEntity.getOffHandStack()
         }) {
            if (!itemStack3.isEmpty()) {
               local6881_24.add(itemStack3);
            }
         }

         if (!local6881_24.isEmpty()) {
            local6881_23 = local6881_20 + 2.0F;

            for (ItemStack itemStack4 : local6881_24) {
               this.processData(itemStack4, local6881_23, local6881_21 - 10.0F - 1.5F + 1.0F);
               ItemRenderCache.processData(
                  itemStack4, local6881_23, local6881_21 - 10.0F - 1.5F + 1.0F, 8.0F, ItemDrawOptions.decorated(1.0F).glint(GlintMode.OFF)
               );
               local6881_23 += 10.0F;
            }
         }
      }

      HudRenderContext.updateState();
   }

   public boolean matchesCriteria8(AbstractClientPlayerEntity abstractClientPlayerEntity) {
      if (abstractClientPlayerEntity == mc.player) {
         return AirStuckModule.getInstance().isFrozenInAir();
      } else {
         return abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) ? false : this.map2.getOrDefault(abstractClientPlayerEntity.getId(), 0) >= 20;
      }
   }

   private boolean checkState3() {
      return this.showPlayerDonationPotions.isChecked() && ServerItemType.checkState();
   }

   private boolean matchesCriteria9(ItemStack itemStack, Vec3d vec3d, int local6886_3, int local6886_4, int local6886_5, float local6886_6) {
      if (itemStack.isEmpty()) {
         return false;
      }

      Vec3d vec3d2 = WorldProjection.getVec3d2(vec3d.add(0.0, 0.25, 0.0), local6886_4, local6886_5);
      if (vec3d2 == null) {
         return false;
      }

      TagsModule.DataRecord local6886_8 = this.map5.get(local6886_3);
      if (local6886_8 == null || local6886_8.stack() != itemStack && !ItemStack.areItemsAndComponentsEqual(local6886_8.stack(), itemStack)) {
         local6886_8 = new TagsModule.DataRecord(
            itemStack,
            itemStack.getName().getString(),
            itemStack.getName()
               .copy()
               .styled(
                  style -> style.getColor() != null && (style.getColor().getRgb() & 16777215) != 16777215
                     ? style
                     : style.withColor(TextColor.fromRgb(ThemeColors.intVal5 & 16777215))
               )
         );
         this.map5.put(local6886_3, local6886_8);
      }

      String local6886_9 = local6886_8.label();
      float local6886_10 = (float)mc.gameRenderer.getCamera().getCameraPos().distanceTo(vec3d);
      float local6886_11 = this.computeFloat(local6886_10);
      float local6886_12 = 9.0F;
      float local6886_13 = 2.0F + FontText.computeFloat(FontFace.SEMI_BOLD, local6886_9, 4.0F) + 2.0F;
      float local6886_14 = (float)vec3d2.x * local6886_6;
      float local6886_15 = (float)vec3d2.y * local6886_6 - local6886_12 - 2.0F;
      float local6886_16 = local6886_14 - local6886_13 / 2.0F;
      float local6886_17 = local6886_15 + local6886_12 / 2.0F;
      HudRenderContext.processData3(local6886_14, (float)vec3d2.y * local6886_6, local6886_11);
      Render2D.processData51(
         local6886_16, local6886_15, local6886_13, local6886_12, 0.0F, Colors.computeInt7(ThemeColors.intVal, 133)
      );
      FontText.processData5(
         FontFace.SEMI_BOLD,
         local6886_8.styledName(),
         local6886_16 + 2.0F,
         local6886_15 + local6886_12 / 2.0F - 2.45F,
         4.0F,
         ThemeColors.intVal5
      );
      HudRenderContext.updateState();
      return true;
   }

   private boolean matchesCriteria10(ProjectileEntity projectileEntity, float local6887_2, int local6887_3, int local6887_4, float local6887_5) {
      return this.matchesCriteria9(this.getItemStack2(projectileEntity), projectileEntity.getLerpedPos(local6887_2), projectileEntity.getId(), local6887_3, local6887_4, local6887_5);
   }

   private record DataRecord(ItemStack stack, String label, Text styledName) {
   }
}
