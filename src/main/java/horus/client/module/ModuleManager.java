package horus.client.module;

import horus.client.module.misc.AdminRecordModule;
import horus.client.module.misc.AdminShardModule;
import horus.client.module.combat.AimAssistModule;
import horus.client.module.movement.AirStuckModule;
import horus.client.module.render.ArmorBarModule;
import horus.client.module.render.ArrowsModule;
import horus.client.module.render.AspectRatioModule;
import horus.client.module.combat.AttackAuraModule;
import horus.client.module.combat.TargetStrafeModule;
import horus.client.module.movement.SpeedModule;
import horus.client.module.combat.CrystalAuraModule;
import horus.client.module.combat.AutoWebModule;


import horus.client.module.combat.AutoAnchorModule;
import horus.client.module.player.BorderBypassModule;
import horus.client.module.combat.AutoCartModule;
import horus.client.module.combat.AutoLungeModule;
import horus.client.module.combat.AutoSpearModule;
import horus.client.module.combat.AutoBuffModule;
import horus.client.module.misc.AuctionHelperModule;
import horus.client.module.misc.AutoAcceptModule;
import horus.client.module.misc.AutoAppleFarmModule;
import horus.client.module.misc.AutoBuyModule;
import horus.client.module.misc.AutoDeliverModule;
import horus.client.module.misc.AutoEatModule;
import horus.client.module.misc.AutoJoinerModule;
import horus.client.module.player.AutoExpModule;
import horus.client.module.misc.AutoLeaveModule;
import horus.client.module.misc.AutoMessageModule;
import horus.client.module.misc.AutoMineModule;
import horus.client.module.misc.AutoRegisterModule;
import horus.client.module.misc.AutoSellModule;
import horus.client.module.movement.AutoSprintModule;
import horus.client.module.combat.AutoSwapModule;
import horus.client.module.misc.AutoToolModule;
import horus.client.module.combat.AutoTotemModule;
import horus.client.module.misc.AutoVillagerModule;
import horus.client.module.misc.AutoWardenModule;
import horus.client.module.combat.BackTrackModule;
import horus.client.module.misc.BassBoostModule;
import horus.client.module.render.BlockEspModule;
import horus.client.module.render.BlockOverlayModule;
import horus.client.module.combat.BlockSlotModule;
import horus.client.module.render.BrightnessModule;
import horus.client.module.render.CameraFeatureModule;
import horus.client.module.render.ChamsModule;
import horus.client.module.misc.ChestStealerModule;
import horus.client.module.misc.ClanUpgraderModule;
import horus.client.module.player.ClickActionModule;
import horus.client.module.player.InvManagerModule;
import horus.client.module.player.TapeMouseModule;
import horus.client.module.render.CrosshairModule;
import horus.client.module.render.DynamicIslandModule;
import horus.client.module.movement.HighJumpModule;
import horus.client.module.render.InvTargetModule;
import horus.client.module.render.CustomWorldModule;
import horus.client.module.misc.DiscordRPCModule;
import horus.client.module.misc.MultiAccountModule;
import horus.client.module.misc.DuelHelperModule;
import horus.client.module.player.ElytraHelperModule;
import horus.client.module.movement.FlightModule;
import horus.client.module.movement.NoWebModule;
import horus.client.module.render.FreeCameraModule;
import horus.client.module.misc.GlobalsModule;
import horus.client.module.combat.HitBoxesModule;
import horus.client.module.misc.InfoReplaceModule;
import horus.client.module.render.InterfaceModule;
import horus.client.module.combat.ItemHelperModule;
import horus.client.module.player.ItemScrollerModule;
import horus.client.module.render.JumpCircleModule;
import horus.client.module.combat.MaceHelperModule;
import horus.client.module.render.MotionBlurModule;
import horus.client.module.misc.NameProtectModule;
import horus.client.module.combat.NoEntityTraceModule;
import horus.client.module.player.NoPlayerDelayModule;
import horus.client.module.player.NoFriendDamageModule;
import horus.client.module.player.NoPushModule;
import horus.client.module.render.NoRenderModule;
import horus.client.module.player.FrameSyncModule;
import horus.client.module.combat.NoServerPackModule;
import horus.client.module.movement.NoSlowModule;
import horus.client.module.render.ParticlesModule;
import horus.client.module.misc.PartyModule;
import horus.client.module.misc.PotionCombinerModule;
import horus.client.module.player.PotionThrowerModule;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import horus.client.module.render.ProjectilePredictionModule;
import ru.dreamix.protection.api.impl.profile.objects.RoleObject;
import horus.client.module.movement.ScreenWalkModule;
import horus.client.module.render.SeeInvisibleModule;
import horus.client.module.player.ServerHelperModule;
import horus.client.module.render.ShulkerPreviewModule;
import horus.client.module.render.SkeletonEspModule;
import horus.client.module.render.SmoothFeatureModule;
import horus.client.module.movement.SpiderModule;
import horus.client.module.misc.StreamerModModule;
import horus.client.module.render.PulseModModule;
import horus.client.module.render.StructureInfoModule;
import horus.client.module.render.SwordAnimationsModule;
import horus.client.module.render.CustomChatModule;
import horus.client.module.render.CustomScoreboardModule;
import horus.client.module.render.TagsModule;
import horus.client.module.render.TargetEspModule;
import horus.client.module.render.TorusModule;
import horus.client.module.render.TotemGhostModule;
import horus.client.module.combat.TriggerBotModule;
import horus.client.module.player.UseTrackerModule;
import horus.client.module.combat.VelocityModule;
import horus.client.module.render.ViewModelModule;
import horus.client.module.render.WardenEspModule;
import horus.client.module.render.ZoomModule;
import horus.client.module.misc.YTMODEModule;
import horus.client.module.misc.SafeModModule;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
   private static final List<Module> modules = new ArrayList<>();
   private static boolean initialized;

   public static Module findByName(String local866_0) {
      for (Module module : modules) {
         if (module.name.equalsIgnoreCase(local866_0)) {
            return module;
         }
      }

      return null;
   }

   public static void registerModule(Module module) {
      if (module != null && !modules.contains(module)) {
         if (!(module instanceof AutoMineModule)
               && !(module instanceof AutoBuyModule)
               && !(module instanceof AutoVillagerModule)
               && !(module instanceof AutoAppleFarmModule)
               && !(module instanceof AutoWardenModule)
            || ProfileAPI.get().getModel().getRole().priorityHigherOrEquals(RoleObject.BETA)) {
            if (!(module instanceof YTMODEModule) || ProfileAPI.get().getModel().getRole().priorityHigherOrEquals(RoleObject.YOUTUBE)) {
               if (!(module instanceof AdminShardModule) || ProfileAPI.get().getModel().getRole().priorityHigherOrEquals(RoleObject.ADMIN)) {
                  if (!(module instanceof AdminRecordModule) || ProfileAPI.get().getModel().getRole().priorityHigherOrEquals(RoleObject.ADMIN)) {
                     modules.add(module);
                  }
               }
            }
         }
      }
   }

   public static List<Module> getModules() {
      return modules;
   }

   private ModuleManager() {
   }

   public static void unregisterModule(Module module) {
      modules.remove(module);
   }

   public static List<Module> getModulesByCategory(ModuleCategory moduleCategory) {
      List<Module> local871_1 = new ArrayList<>();

      for (Module module : modules) {
         if (module.category == moduleCategory) {
            local871_1.add(module);
         }
      }

      return local871_1;
   }

   public static void initializeModules() {
      if (!initialized) {
         initialized = true;
         registerModule(AttackAuraModule.getInstance());
         registerModule(TargetStrafeModule.getInstance());
         registerModule(CrystalAuraModule.getInstance());
         registerModule(AutoAnchorModule.getInstance());
         registerModule(AutoCartModule.getInstance());


         registerModule(AutoWebModule.getInstance());
         registerModule(AutoSpearModule.getInstance());
         registerModule(AutoLungeModule.getInstance());
         registerModule(AutoBuffModule.getInstance());
         registerModule(MaceHelperModule.getInstance());
         registerModule(AutoSwapModule.getInstance());
         registerModule(AutoTotemModule.getInstance());
         registerModule(TriggerBotModule.getInstance());
         registerModule(ItemHelperModule.getInstance());
         registerModule(AimAssistModule.getInstance());
         registerModule(NoEntityTraceModule.getInstance());
         registerModule(NoServerPackModule.getInstance());
         registerModule(HitBoxesModule.getInstance());
         registerModule(BackTrackModule.getInstance());
         registerModule(AutoRegisterModule.getInstance());
         registerModule(AuctionHelperModule.getInstance());
         registerModule(AutoLeaveModule.getInstance());
         registerModule(AutoToolModule.getInstance());
         registerModule(AutoEatModule.getInstance());
         registerModule(AutoJoinerModule.getInstance());
         registerModule(AutoAcceptModule.getInstance());
         registerModule(AutoDeliverModule.getInstance());
         registerModule(ClanUpgraderModule.getInstance());
         if (ProfileAPI.get().getModel().getRole().priorityHigherOrEquals(RoleObject.BETA)) {
            registerModule(AutoMineModule.getInstance());
            registerModule(AutoBuyModule.getInstance());
            registerModule(AutoVillagerModule.getInstance());
            registerModule(AutoAppleFarmModule.getInstance());
            registerModule(AutoWardenModule.getInstance());
         }

         registerModule(AutoSellModule.getInstance());
         registerModule(PotionCombinerModule.getInstance());
         registerModule(ChestStealerModule.getInstance());
         registerModule(InfoReplaceModule.getInstance());
         registerModule(StreamerModModule.getInstance());
         registerModule(YTMODEModule.getInstance());
         registerModule(NameProtectModule.getInstance());
         registerModule(SafeModModule.getInstance());
         registerModule(GlobalsModule.getInstance());
         registerModule(PartyModule.getInstance());
         registerModule(DiscordRPCModule.getInstance());
         registerModule(MultiAccountModule.getInstance());
         registerModule(DuelHelperModule.getInstance());
         registerModule(AutoMessageModule.getInstance());
         registerModule(AirStuckModule.getInstance());
         registerModule(AutoSprintModule.getInstance());
         registerModule(FlightModule.getInstance());
         registerModule(NoWebModule.getInstance());
         registerModule(SpeedModule.getInstance());
         registerModule(ScreenWalkModule.getInstance());
         registerModule(VelocityModule.getInstance());
         registerModule(NoSlowModule.getInstance());
         registerModule(SpiderModule.getInstance());
         registerModule(ItemScrollerModule.getInstance());
         registerModule(NoPushModule.getInstance());
         registerModule(BorderBypassModule.getInstance());
         registerModule(BlockSlotModule.getInstance());
         registerModule(NoRenderModule.getInstance());
         registerModule(FrameSyncModule.getInstance());
         ProjectilePredictionModule.getInstance();
         registerModule(TagsModule.getInstance());
          CustomChatModule.getInstance(); // Always active subsystem, not a toggleable module
         registerModule(CustomScoreboardModule.getInstance());
         registerModule(TargetEspModule.getInstance());
         registerModule(ArmorBarModule.getInstance());
         registerModule(ShulkerPreviewModule.getInstance());
         registerModule(SkeletonEspModule.getInstance());
         registerModule(ChamsModule.getInstance());
         registerModule(BlockEspModule.getInstance());
         registerModule(SeeInvisibleModule.getInstance());
         registerModule(TorusModule.getInstance());
         TotemGhostModule.getInstance();
         registerModule(CrosshairModule.getInstance());
         registerModule(DynamicIslandModule.getInstance());
         registerModule(HighJumpModule.getInstance());
         registerModule(InvTargetModule.getInstance());
         registerModule(ArrowsModule.getInstance());
         registerModule(BassBoostModule.getInstance());
         registerModule(ClickActionModule.getInstance());
         registerModule(TapeMouseModule.getInstance());
         registerModule(InvManagerModule.getInstance());
         registerModule(PulseModModule.getInstance());
         registerModule(AutoExpModule.getInstance());
         registerModule(ElytraHelperModule.getInstance());
         registerModule(NoPlayerDelayModule.getInstance());
         registerModule(NoFriendDamageModule.getInstance());
         registerModule(FreeCameraModule.getInstance());
         registerModule(CameraFeatureModule.getInstance());
         registerModule(ZoomModule.getInstance());
         registerModule(SwordAnimationsModule.getInstance());
         registerModule(ViewModelModule.getInstance());
         registerModule(InterfaceModule.getInstance());
         SmoothFeatureModule.getInstance();
         registerModule(AspectRatioModule.getInstance());
         registerModule(StructureInfoModule.getInstance());
         registerModule(WardenEspModule.getInstance());
         registerModule(JumpCircleModule.getInstance());
         registerModule(BlockOverlayModule.getInstance());
         registerModule(ParticlesModule.getInstance());
         registerModule(MotionBlurModule.getInstance());
         registerModule(CustomWorldModule.getInstance());
         registerModule(BrightnessModule.getInstance());
         registerModule(ServerHelperModule.getInstance());
         registerModule(PotionThrowerModule.getInstance());
         registerModule(UseTrackerModule.getInstance());
      }
   }
}
