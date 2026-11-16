package horus.client;



import horus.client.storage.AccountStore;

import horus.client.command.AutowardenCommand;

import horus.client.command.BlockespCommand;



import horus.client.ui.ClickGuiScreen;

import horus.client.command.ClientCommand;

import horus.client.event.ClientTickEvent;

import horus.client.command.CommandDispatcher;

import horus.client.command.ConfigCommand;

import horus.client.cosmetic.CosmeticFeatureRenderer;

import horus.client.ui.MainMenuScreen;

import horus.client.event.Event;

import horus.client.event.EventBus;

import horus.client.command.FriendCommand;

import horus.client.command.HelpCommand;

import horus.client.ui.hud.HudLayout;

import horus.client.command.IrcCommand;

import horus.client.command.MacroCommand;

import horus.client.storage.MacroStore;

import horus.client.module.ModuleManager;

import horus.client.command.PartyCommand;

import ru.dreamix.protection.api.impl.profile.ProfileAPI;

import horus.client.command.RctCommand;

import horus.client.command.WaypointCommand;

import horus.client.util.FrameTimer;

import horus.client.util.HwidManager;

import horus.client.util.WaypointManager;

import horus.client.command.ResourcePackCommand;

import horus.client.ui.UiWarmup;

import horus.client.render.ThemePaletteBuffer;

import horus.client.render.ItemRenderCache;

import horus.client.render.PipelineWarmup;

import horus.client.storage.ConfigStore;

import horus.client.util.IrcClient;

import horus.client.cosmetic.CosmeticRenderDispatcher;

import horus.client.cosmetic.CosmeticWarmup;

import horus.client.render.Render2D;

import ru.dreamix.protection.api.impl.profile.objects.RoleObject;

import horus.client.command.StaffCommand;



import java.util.ArrayList;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.option.KeyBinding;

import net.minecraft.client.option.KeyBinding.Category;

import net.minecraft.client.render.entity.PlayerEntityRenderer;

import net.minecraft.client.util.InputUtil.Type;

import net.minecraft.resource.ResourceType;

import net.minecraft.resource.SynchronousResourceReloader;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



public class HorusClient implements ModInitializer, ClientModInitializer {

   public static final String TEXT_HORUS = "horus";

   private static final HorusClient horusClient = new HorusClient();

   private static final Logger LOGGER = LoggerFactory.getLogger("horus");

   private long loadTimeMillis;

   private boolean loaded;

   private int autosaveTicks;

   private CommandDispatcher commandDispatcher;

   private KeyBinding openGuiKey;

   private final FrameTimer frameTimer = new FrameTimer();



   public boolean loaded() {

      return this.loaded;

   }



   public static Identifier id(String local6600_0) {

      return Identifier.of("horus", local6600_0);

   }



   public KeyBinding openGuiKey() {

      return this.openGuiKey;

   }



   private void registerOpenKey() {

      this.openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.horus.clickgui", Type.KEYSYM, 344, Category.GAMEPLAY));

      ClientTickEvents.END_CLIENT_TICK.register((EndTick)minecraftClient -> {

         while (this.openGuiKey.wasPressed()) {

            if (horus.client.module.misc.SafeModModule.getInstance().isLockGui()) {

               continue;

            }

            if (minecraftClient.currentScreen == null) {

               minecraftClient.setScreen(ClickGuiScreen.instance());

            }

         }

      });

      if (Boolean.getBoolean("horus.openGui")

         || System.getenv("HORUS_OPEN_GUI") != null

         || Boolean.getBoolean("horus.openGui")

         || System.getenv("HORUS_OPEN_GUI") != null) {

         ClientTickEvents.END_CLIENT_TICK.register((EndTick)minecraftClient2 -> {

            if (minecraftClient2.currentScreen instanceof MainMenuScreen) {

               minecraftClient2.setScreen(ClickGuiScreen.instance());

            }

         });

      }

   }



   public CommandDispatcher commandDispatcher() {

      return this.commandDispatcher;

   }



   private void registerTicks() {

      ClientTickEvents.START_CLIENT_TICK.register((StartTick)minecraftClient -> {

         if (EventBus.getInstance().hasListeners(ClientTickEvent.class)) {

            EventBus.getInstance().post(new ClientTickEvent(Event.Stage.PRE));

         }

      });

      ClientTickEvents.END_CLIENT_TICK.register((EndTick)minecraftClient2 -> {

         if (minecraftClient2.world != null && minecraftClient2.player != null) {

            HwidManager.ensureBound();

         }



         if (EventBus.getInstance().hasListeners(ClientTickEvent.class)) {

            EventBus.getInstance().post(new ClientTickEvent(Event.Stage.POST));

         }



         if (this.loaded && ++this.autosaveTicks >= 1200) {

            this.autosaveTicks = 0;

            ConfigStore.autosave();

         }

      });

   }



   public static Logger logger() {

      return LOGGER;

   }



   @Override

   public String toString() {

      return "Horus(loadTimeMs="

         + this.loadTimeMs()

         + ", loaded="

         + this.loaded()

         + ", autosaveTicks="

         + this.autosaveTicks()

         + ", commandDispatcher="

         + this.commandDispatcher()

         + ", openGuiKey="

         + this.openGuiKey()

         + ", framelibrary="

         + this.framelibrary()

         + ")";

   }



   public int autosaveTicks() {

      return this.autosaveTicks;

   }



   private void registerCosmetics() {

      CosmeticWarmup.updateState3();

      CosmeticWarmup.updateState();

      LivingEntityFeatureRendererRegistrationCallback.EVENT

         .register((LivingEntityFeatureRendererRegistrationCallback)(local6603_0, local6603_1, registrationHelper, context) -> {

            if (local6603_1 instanceof PlayerEntityRenderer<?> local6603_4) {

               registrationHelper.register(new CosmeticFeatureRenderer(local6603_4));

            }

         });

      CosmeticRenderDispatcher.updateState();

   }



   @Override

   public boolean equals(Object local6612_1) {

      if (local6612_1 == this) {

         return true;

      } else if (!(local6612_1 instanceof HorusClient currentHorusClient)) {

         return false;

      } else {

         if (!currentHorusClient.canEqual(this)) {

            return false;

         }



         if (this.loadTimeMs() != currentHorusClient.loadTimeMs()) {

            return false;

         }



         if (this.loaded() != currentHorusClient.loaded()) {

            return false;

         }



         if (this.autosaveTicks() != currentHorusClient.autosaveTicks()) {

            return false;

         }



         Object local6612_3 = this.commandDispatcher();

         Object local6612_4 = currentHorusClient.commandDispatcher();

         if (local6612_3 == null ? local6612_4 == null : local6612_3.equals(local6612_4)) {

            Object local6612_5 = this.openGuiKey();

            Object local6612_6 = currentHorusClient.openGuiKey();

            if (local6612_5 == null ? local6612_6 == null : local6612_5.equals(local6612_6)) {

               Object local6612_7 = this.framelibrary();

               Object local6612_8 = currentHorusClient.framelibrary();

               return local6612_7 == null ? local6612_8 == null : local6612_7.equals(local6612_8);

            } else {

               return false;

            }

         } else {

            return false;

         }

      }

   }



   public HorusClient commandDispatcher(CommandDispatcher newCommandDispatcher) {

      this.commandDispatcher = newCommandDispatcher;

      return this;

   }



   public void unload() {

      horus.client.web.WebBridgeServer.stop();

      ConfigStore.saveActiveConfig();

      ItemRenderCache.updateState6();

      Render2D.updateState4();

      this.loaded = false;

      LOGGER.info("shutdown");

   }



   public void load() {

      String launcherUser = horus.client.util.LauncherSession.getLauncherUsername();

      horus.client.util.SupabaseProfileManager.initAndSync(launcherUser);

      this.loadTimeMillis = System.currentTimeMillis();

      System.setProperty("sun.font.layout.ffm", "false");

      Render2D.updateState5();

      ItemRenderCache.updateState();

      ThemePaletteBuffer.updateState();

      UiWarmup.updateState5();

      HudLayout.updateState2();

      ModuleManager.initializeModules();

      EventBus.getInstance().subscribe(AccountStore.getAccountStore());

      EventBus.getInstance().subscribe(WaypointManager.getInstance());

      EventBus.getInstance().subscribe(MacroStore.getInstance());

      EventBus.getInstance().subscribe(IrcClient.getInstance());

      this.registerCosmetics();

      this.initCommands();

      this.registerOpenKey();

      this.registerTicks();

      ConfigStore.loadLastConfig();

      horus.client.web.WebBridgeServer.start();

      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)minecraftClient -> this.unload());

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {

         if (this.loaded) {

            ConfigStore.saveActiveConfig();

         }

      }));

      ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(id("render_warmup"), (SynchronousResourceReloader)resourceManager -> {

         MinecraftClient minecraftClient2 = MinecraftClient.getInstance();

         if (minecraftClient2 != null) {

            minecraftClient2.execute(() -> {

               Render2D.updateState2();

               PipelineWarmup.resetWarmup();

               UiWarmup.updateState3();

            });

         }

      });

      this.loaded = true;

   }



   public HorusClient loaded(boolean newLoaded) {

      this.loaded = newLoaded;

      return this;

   }



   public HorusClient loadTimeMs(long newLoadTimeMillis) {

      this.loadTimeMillis = newLoadTimeMillis;

      return this;

   }



   public HorusClient autosaveTicks(int newAutosaveTicks) {

      this.autosaveTicks = newAutosaveTicks;

      return this;

   }



   public void onInitializeClient() {

      horusClient.load();

   }



   public FrameTimer framelibrary() {

      return this.frameTimer;

   }



   public HorusClient openGuiKey(KeyBinding keyBinding) {

      this.openGuiKey = keyBinding;

      return this;

   }



   @Override

   public int hashCode() {

      int local6627_1 = 59;

      int local6627_2 = 1;

      long local6627_3 = this.loadTimeMs();

      local6627_2 = local6627_2 * 59 + (int)(local6627_3 >>> 32 ^ local6627_3);

      local6627_2 = local6627_2 * 59 + (this.loaded() ? 79 : 97);

      local6627_2 = local6627_2 * 59 + this.autosaveTicks();

      Object local6627_5 = this.commandDispatcher();

      local6627_2 = local6627_2 * 59 + (local6627_5 == null ? 43 : local6627_5.hashCode());

      Object local6627_6 = this.openGuiKey();

      local6627_2 = local6627_2 * 59 + (local6627_6 == null ? 43 : local6627_6.hashCode());

      Object local6627_7 = this.framelibrary();

      return local6627_2 * 59 + (local6627_7 == null ? 43 : local6627_7.hashCode());

   }



   public static HorusClient inst() {

      return horusClient;

   }



   private void initCommands() {

      ArrayList<ClientCommand> local6629_1 = new ArrayList<>();

      local6629_1.add(new ResourcePackCommand());

      local6629_1.add(new ConfigCommand());

      local6629_1.add(new horus.client.command.BindCommand());

      local6629_1.add(new FriendCommand());

      local6629_1.add(new WaypointCommand("gps"));

      local6629_1.add(new WaypointCommand("way"));

      local6629_1.add(new MacroCommand());

      local6629_1.add(new StaffCommand());

      local6629_1.add(new BlockespCommand());

      local6629_1.add(new AutowardenCommand());

      local6629_1.add(new RctCommand());

      local6629_1.add(new IrcCommand());

      local6629_1.add(new PartyCommand());

      local6629_1.add(new horus.client.command.SafeModCommand("safemod"));

      local6629_1.add(new horus.client.command.SafeModCommand("safe"));

      local6629_1.add(new horus.client.command.CloudCommand("cloud"));

      local6629_1.add(new horus.client.command.CloudCommand("share"));

      local6629_1.add(new HelpCommand(local6629_1));

      this.commandDispatcher = new CommandDispatcher(local6629_1, ".");

   }



   public void onInitialize() {

      LOGGER.info("Horus initialized");

   }



   public long loadTimeMs() {

      return this.loadTimeMillis;

   }



   protected boolean canEqual(Object local6635_1) {

      return local6635_1 instanceof HorusClient;

   }

}

