package horus.client.module.misc;

import horus.client.HorusClient;
import com.horus.lib.discord.DiscordNative$Handlers;
import com.horus.lib.discord.DiscordNative$Presence;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import ru.dreamix.protection.api.impl.profile.ProfileAPI;
import horus.client.util.DiscordRpcLibrary;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.MinecraftClient;

@ModuleInfo(name = "DiscordRPC", description = "Показывает клиент в статусе Discord.", category = ModuleCategory.MISC)
public final class DiscordRPCModule extends Module {
   private static DiscordRPCModule discordRPCModule;
   private final DiscordNative$Handlers discordHandlers = new DiscordNative$Handlers();
   private volatile Thread thread2;
   private volatile String strVal = "";
   private long timeMillis;

   /** Картинка статуса: ключ ассета "horus" из Rich Presence -> Art Assets
    *  или прямая ссылка на гиф (https://...), если портал сжал гиф в статику. */
   private static final String LARGE_IMAGE = "horus";

   private void updateState() {
      try {
         DiscordRpcLibrary.discordRpcLibrary.Discord_Initialize("1527791397556588664", this.discordHandlers, true, "");
         this.updateState2();

         while (this.thread2 == Thread.currentThread()) {
            DiscordRpcLibrary.discordRpcLibrary.Discord_RunCallbacks();
            this.updateState2();
            Thread.sleep(5000L);
         }
      } catch (InterruptedException exception) {
      } catch (Throwable throwable) {
         HorusClient.logger().warn("Discord RPC недоступен: {}", throwable.toString());
      } finally {
         try {
            DiscordRpcLibrary.discordRpcLibrary.Discord_ClearPresence();
            DiscordRpcLibrary.discordRpcLibrary.Discord_Shutdown();
         } catch (Throwable throwable2) {
         }
      }
   }

   public static DiscordRPCModule getInstance() {
      if (discordRPCModule == null) {
         discordRPCModule = new DiscordRPCModule();
      }

      return discordRPCModule;
   }

   private void updateState2() {
      DiscordNative$Presence presence = new DiscordNative$Presence();

      String playerName = "";
      try {
         if (ProfileAPI.get() != null && ProfileAPI.get().getModel() != null) {
            playerName = ProfileAPI.get().getModel().getName();
         }
      } catch (Exception ignored) {
      }

      boolean inGame = mc.player != null && mc.world != null;

      int enabledCount = 0;
      int totalCount = 0;
      try {
         List<horus.client.module.Module> allModules = horus.client.module.ModuleManager.getModules();
         totalCount = allModules.size();
         for (horus.client.module.Module module : allModules) {
            if (module.isEnabled()) {
               enabledCount++;
            }
         }
      } catch (Exception ignored) {
      }

      String serverIp = "В главном меню";
      String details = "Главное меню";

      if (inGame) {
         try {
            if (mc.getCurrentServerEntry() != null && !mc.getCurrentServerEntry().address.isEmpty()) {
               serverIp = mc.getCurrentServerEntry().address;
            } else if (mc.isIntegratedServerRunning()) {
               serverIp = "Одиночная игра";
            }
         } catch (Exception ignored) {
         }

         details = "Модули: " + enabledCount + "/" + totalCount;

         if (!playerName.isEmpty()) {
            details = playerName + " | Модули: " + enabledCount + "/" + totalCount;
         }
      }

      presence.details = details;
      presence.state = serverIp;
      presence.startTimestamp = this.timeMillis;
      presence.largeImageKey = LARGE_IMAGE;
      presence.largeImageText = "Horus Client";

      if (!this.strVal.isEmpty()) {
         presence.smallImageKey = this.strVal;
         presence.smallImageText = playerName;
      }

      presence.button_label_1 = "Телеграм";
      presence.button_url_1 = "https://t.me/horuscl";
      presence.button_label_2 = "Дискорд";
      presence.button_url_2 = "https://discord.gg/hrosuclient";
      DiscordRpcLibrary.discordRpcLibrary.Discord_UpdatePresence(presence);
   }

   @Override
   protected void onEnable() {
      if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux") && this.thread2 == null) {
         this.timeMillis = System.currentTimeMillis() / 1000L;
         Thread thread = new Thread(this::updateState, "horus-discord-rpc");
         thread.setDaemon(true);
         this.thread2 = thread;
         thread.start();
      }
   }

   private DiscordRPCModule() {
      this.discordHandlers.ready = user -> {
         if (user.avatar != null && !user.avatar.isEmpty()) {
            this.strVal = "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar + ".png";
         }

         this.updateState2();
      };
   }

   @Override
   protected void onDisable() {
      Thread thread = this.thread2;
      this.thread2 = null;
      if (thread != null) {
         thread.interrupt();
      }
   }
}
