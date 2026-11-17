package horus.client.ui;

import horus.client.HorusClient;
import horus.client.render.FontFace;
import horus.client.render.FontText;
import horus.client.render.Render2D;
import horus.client.storage.MenuStore;
import horus.client.storage.ServerMenuStore;
import horus.client.util.Lang;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

/**
 * Модальные окна главного меню в стиле шейдер-мокапа:
 * Одиночная игра (реальные миры), Сетевая (реальный список серверов),
 * Выход (подтверждение), Студия фона (обои/затемнение/виньетка).
 */
final class MainMenuModals {
   enum Type { SINGLEPLAYER, MULTIPLAYER, EXIT, STUDIO }

   // ---- состояние ----
   private Type type;
   private static Type pendingReopen; // какую модалку переоткрыть после выхода с ванильного экрана
   private boolean closing; // модалка в процессе плавного закрытия
   private float animRaw;   // 0..1 прогресс анимации
   private long frameMs = System.currentTimeMillis();
   private final MainMenuScreen menu;
   private final List<LevelSummary> worlds = new ArrayList<>();
   private boolean worldsLoading;
   private int selectedWorld = -1;
   private boolean addingWorld;
   private String newWorldName = "";
   private int newWorldMode = 0; // 0 выживание, 1 творческий, 2 хардкор

   private static final java.util.concurrent.ExecutorService SERVER_QUERIES =
         java.util.concurrent.Executors.newFixedThreadPool(5, runnable -> {
            Thread thread = new Thread(runnable, "Horus-Menu-Server-Query");
            thread.setDaemon(true);
            return thread;
         });
   private volatile net.minecraft.client.network.MultiplayerServerListPinger serverPinger;
   private long serverQueryStarted;
   private final ServerMenuIcons serverIcons = new ServerMenuIcons();
   private ServerList serverList;
   private final List<ServerInfo> servers = new ArrayList<>();
   private int selectedServer = -1;
   private String serverSearch = "";
   private boolean addingServer;
   private String newServerName = "";
   private String newServerIp = "";
   private int editingServer = -1; // индекс сервера в режиме настроек (-1 = нет)
   private String editServerName = "";
   private String editServerIp = "";
   private int editServerPolicy; // 0 = вкл, 1 = выкл, 2 = классика (спрашивать)


   private float modalX;
   private float modalY;
   private float modalW;
   private float modalH;
   private float listTop;
   private float listBottom;
   private float listScroll;
   private float contentBottom; // нижняя граница контента (для прокрутки)

   MainMenuModals(MainMenuScreen menu) {
      this.menu = menu;
   }

   boolean isOpen() {
      return this.type != null;
   }

   /** Модалка активна для ввода (полностью открыта и не закрывается). */
   boolean isActive() {
      return this.type != null && !this.closing;
   }

   Type currentType() {
      return this.type;
   }

   void open(Type type) {
      this.type = type;
      this.closing = false;
      this.animRaw = 0.0F;
      this.frameMs = System.currentTimeMillis();
      this.listScroll = 0.0F;
      this.addingWorld = false;
      this.addingServer = false;
      this.editingServer = -1;
      this.selectedWorld = -1;
      this.selectedServer = -1;
      this.serverSearch = "";
      this.newWorldName = "";
      this.newServerName = "";
      this.newServerIp = "";

      if (type == Type.SINGLEPLAYER) {
         this.loadWorlds();
      } else if (type == Type.MULTIPLAYER) {
         this.loadServers();
      }
   }

   void close() {
      if (this.type != null && !this.closing) {
         this.stopServerQueries();
         this.closing = true; // закрываем плавно, тип сбросится в конце анимации
      }
   }

   // ===== данные =====

   private void loadWorlds() {
      MinecraftClient mc = MinecraftClient.getInstance();
      this.worlds.clear();
      this.worldsLoading = true;
      LevelStorage storage = mc.getLevelStorage();

      Thread thread = new Thread(() -> {
         try {
            LevelStorage.LevelList levelList = storage.getLevelList();
            List<LevelSummary> summaries = storage.loadSummaries(levelList).join();
            MinecraftClient.getInstance().execute(() -> {
               this.worlds.clear();
               this.worlds.addAll(summaries);
               this.worlds.sort((a, b) -> Long.compare(b.getLastPlayed(), a.getLastPlayed()));
               this.worldsLoading = false;
            });
         } catch (Exception exception) {
            MinecraftClient.getInstance().execute(() -> this.worldsLoading = false);
         }
      }, "Horus-Menu-Worlds");
      thread.setDaemon(true);
      thread.start();
   }

   private void loadServers() {
      this.stopServerQueries();
      this.serverIcons.close();
      this.serverPinger = new net.minecraft.client.network.MultiplayerServerListPinger();
      this.serverQueryStarted = System.currentTimeMillis();
      MinecraftClient mc = MinecraftClient.getInstance();
      this.servers.clear();

      try {
         this.serverList = new ServerList(mc);
         this.serverList.loadFile();

         for (int i = 0; i < this.serverList.size(); i++) {
            this.servers.add(this.serverList.get(i));
         }
      } catch (Exception ignored) {
      }

      this.pingServers();
   }

   // Штатный status-запрос получает не только задержку, но и favicon сервера.
   private void pingServers() {
      net.minecraft.client.network.MultiplayerServerListPinger pinger = this.serverPinger;
      ServerList list = this.serverList;
      MinecraftClient mc = MinecraftClient.getInstance();
      for (ServerInfo info : this.servers) {
         if (info.address == null || info.address.isBlank()) {
            continue;
         }
         info.ping = 0L;
         SERVER_QUERIES.execute(() -> {
            if (this.serverPinger != pinger) {
               return;
            }
            try {
               pinger.add(info, () -> mc.execute(() -> {
                  if (this.serverPinger == pinger && list != null) {
                     list.saveFile();
                  }
               }), () -> {}, net.minecraft.network.NetworkingBackend.remote(false));
            } catch (Exception exception) {
               mc.execute(() -> info.ping = -1L);
            } finally {
               // DNS может завершиться уже после закрытия окна.
               mc.execute(() -> {
                  if (this.serverPinger != pinger) {
                     pinger.cancel();
                  }
               });
            }
         });
      }
   }

   void tick() {
      if (this.serverPinger != null) {
         this.serverPinger.tick();
         if (System.currentTimeMillis() - this.serverQueryStarted > 10000L) {
            this.stopServerQueries();
            for (ServerInfo info : this.servers) {
               if (info.ping == 0L) {
                  info.ping = -1L;
               }
            }
         }
      }
   }

   private void stopServerQueries() {
      net.minecraft.client.network.MultiplayerServerListPinger pinger = this.serverPinger;
      this.serverPinger = null;
      if (pinger != null) {
         pinger.cancel();
      }
   }

   void removed() {
      this.stopServerQueries();
      this.serverIcons.close();
   }

   // настройка мира через ванильный EditWorldScreen (переименовать/удалить/бэкап/папка)
   private void editWorld(LevelSummary summary) {
      MinecraftClient mc = MinecraftClient.getInstance();
      final String worldName = summary.getName();
      pendingReopen = Type.SINGLEPLAYER;

      Thread thread = new Thread(() -> {
         try {
            LevelStorage.Session session = mc.getLevelStorage().createSession(worldName);
            MinecraftClient.getInstance().execute(() -> {
               try {
                  mc.setScreen(EditWorldScreen.create(mc, session, done -> mc.setScreen(new MainMenuScreen(false))));
               } catch (Exception exception) {
                  HorusClient.logger().warn("[MainMenu] Failed to open world settings", exception);
                  mc.setScreen(new MainMenuScreen(false));
               }
            });
         } catch (Exception exception) {
            HorusClient.logger().warn("[MainMenu] Failed to open world session: {}", worldName, exception);
            MinecraftClient.getInstance().execute(() -> mc.setScreen(new MainMenuScreen(false)));
         }
      }, "Horus-World-Edit");
      thread.setDaemon(true);
      thread.start();
   }

   static Type consumePendingReopen() {
      Type type = pendingReopen;
      pendingReopen = null;
      return type;
   }

   private void startEditServer(int index) {
      if (index < 0 || index >= this.servers.size()) {
         return;
      }

      ServerInfo info = this.servers.get(index);
      this.editingServer = index;
      this.editServerName = info.name == null ? "" : info.name;
      this.editServerIp = info.address == null ? "" : info.address;

      ServerInfo.ResourcePackPolicy policy = info.getResourcePackPolicy();
      this.editServerPolicy = policy == ServerInfo.ResourcePackPolicy.ENABLED ? 0
              : policy == ServerInfo.ResourcePackPolicy.DISABLED ? 1 : 2;
   }

   private void saveEditServer() {
      if (this.editingServer < 0 || this.editingServer >= this.servers.size()) {
         return;
      }

      ServerInfo info = this.servers.get(this.editingServer);
      String name = this.editServerName.trim();
      String ip = this.editServerIp.trim();

      if (!ip.isEmpty()) {
         info.address = ip;
      }

      if (!name.isEmpty()) {
         info.name = name;
      }

      info.setResourcePackPolicy(this.editServerPolicy == 0 ? ServerInfo.ResourcePackPolicy.ENABLED
              : this.editServerPolicy == 1 ? ServerInfo.ResourcePackPolicy.DISABLED : ServerInfo.ResourcePackPolicy.PROMPT);

      try {
         if (this.serverList != null) {
            this.serverList.saveFile();
         }
      } catch (Exception ignored) {
      }

      this.editingServer = -1;
      this.loadServers();
   }

   private void joinServer(ServerInfo info) {
      MinecraftClient mc = MinecraftClient.getInstance();
      this.close();
      // после отмены подключения возвращаемся в главное меню Horus
      // и сразу открываем список серверов заново
      pendingReopen = Type.MULTIPLAYER;
      this.menu.transitionTo(() -> ConnectScreen.connect(
              this.menu,
              mc,
              ServerAddress.parse(info.address),
              info,
              false,
              null
      ));
   }

   private void playWorld(LevelSummary summary) {
      MinecraftClient mc = MinecraftClient.getInstance();
      this.close();
      this.menu.transitionTo(() -> mc.createIntegratedServerLoader().start(summary.getName(), () -> mc.setScreen(new MainMenuScreen(false))));
   }

   // ===== рендер =====

   void render(float screenW, float screenH, float scale, float mx, float my, boolean interactive) {
      // плавное открытие/закрытие: затемнение + подлёт снизу + затухание
      long now = System.currentTimeMillis();
      float dt = Math.min(60.0F, Math.max(0.0F, now - this.frameMs)) / 1000.0F;
      this.frameMs = now;

      if (this.closing) {
         this.animRaw = Math.max(0.0F, this.animRaw - dt * 4.5F);
         if (this.animRaw <= 0.001F) {
            this.type = null;
            this.closing = false;
            return;
         }
      } else {
         this.animRaw = Math.min(1.0F, this.animRaw + dt * 3.2F);
      }

      float ease = UiTheme.computeFloat2(this.animRaw);
      float alpha = Math.max(0.02F, ease);
      float slideY = (1.0F - ease) * 18.0F * scale;
      boolean ui = interactive && !this.closing && ease > 0.5F;

      Render2D.processData46(alpha);

      // затемнение фона
      Render2D.processData51(0.0F, 0.0F, screenW, screenH, 0.0F, 0x99050609);

      float[] size = this.modalSize(screenW, screenH, scale);
      this.modalW = size[0];
      this.modalH = size[1];
      this.modalX = (screenW - this.modalW) / 2.0F;
      this.modalY = (screenH - this.modalH) / 2.0F + slideY;

      Render2D.processData51(this.modalX, this.modalY, this.modalW, this.modalH, 10.0F * scale, 0xEB111218);
      Render2D.processData86(this.modalX, this.modalY, this.modalW, this.modalH, 10.0F * scale, 0.9F * scale, 0x1FFFFFFF);

      // заголовок
      String title;
      String subtitle;
      switch (this.type) {
         case SINGLEPLAYER -> {
            title = Lang.tr("Одиночная игра", "Singleplayer");
            subtitle = Lang.tr("Выберите локальный мир или создайте новый", "Pick a local world or create a new one");
         }
         case MULTIPLAYER -> {
            horus.client.proxy.ProxyManager proxyManager = horus.client.proxy.ProxyManager.getInstance();
            title = Lang.tr("Сетевая игра", "Multiplayer");
            subtitle = Lang.tr("Прокси: ", "Proxy: ") + (proxyManager.isEnabled() ? Lang.tr("включён", "on") : Lang.tr("выключен", "off"));
         }
         case EXIT -> {
            title = Lang.tr("Выход из Horus", "Exit Horus");
            subtitle = Lang.tr("Настройки и аккаунты сохранены", "Settings and accounts are saved");
         }
         default -> {
            title = Lang.tr("Оформление меню", "Menu Appearance");
            subtitle = Lang.tr("Своё фото или GIF и глянец кнопок", "Custom photo or GIF and button gloss");
         }
      }

      FontText.processData3(FontFace.SEMI, title, this.modalX + 22.0F * scale, this.modalY + 14.0F * scale, 9.0F * scale, 0xFFF4F4F8);
      FontText.processData3(FontFace.SEMI, subtitle, this.modalX + 22.0F * scale, this.modalY + 27.0F * scale, 6.5F * scale, 0xFF8C8FA3);

      // кнопка ×
      float closeSize = 16.0F * scale;
      float closeX = this.modalX + this.modalW - closeSize - 10.0F * scale;
      float closeY = this.modalY + 12.0F * scale;
      boolean closeHover = mx >= closeX && mx <= closeX + closeSize && my >= closeY && my <= closeY + closeSize;
      FontText.processData3(FontFace.SEMI, "×", closeX + closeSize / 2.0F - 2.5F * scale, closeY + 3.0F * scale, 8.0F * scale, closeHover ? 0xFFFFFFFF : 0xFF8C8FA3);
      if (ui) {
         this.menu.addModalArea(closeX, closeY, closeSize, closeSize, this::close);
      }

      // разделитель
      Render2D.processData51(this.modalX, this.modalY + 40.0F * scale, this.modalW, 1.0F, 0.0F, 0x14FFFFFF);

      // контент
      this.listTop = this.modalY + 48.0F * scale;
      float footerH = this.type == Type.EXIT ? 0.0F : 40.0F * scale;
      this.listBottom = this.modalY + this.modalH - footerH;

      switch (this.type) {
         case SINGLEPLAYER -> this.renderWorlds(mx, my, scale, ui);
         case MULTIPLAYER -> this.renderServers(mx, my, scale, ui);
         case EXIT -> this.renderExit(mx, my, scale, ui);
         case STUDIO -> this.renderStudio(mx, my, scale, ui);
      }

      if (this.type != Type.EXIT) {
         Render2D.processData51(this.modalX, this.listBottom, this.modalW, 1.0F, 0.0F, 0x14FFFFFF);
         this.renderFooter(mx, my, scale, ui);
      }

      Render2D.updateState();
   }

   private float[] modalSize(float screenW, float screenH, float scale) {
      return switch (this.type) {
         case SINGLEPLAYER -> new float[]{380.0F * scale, Math.min(screenH - 40.0F, 330.0F * scale)};
         case MULTIPLAYER -> new float[]{396.0F * scale, Math.min(screenH - 40.0F, 330.0F * scale)};
         case EXIT -> new float[]{240.0F * scale, 150.0F * scale};
         case STUDIO -> new float[]{320.0F * scale, 252.0F * scale};
      };
   }

   private void renderWorlds(float mx, float my, float scale, boolean interactive) {
      if (this.addingWorld) {
         this.renderAddWorld(mx, my, scale, interactive);
         return;
      }

      float rowH = 42.0F * scale;
      float gap = 6.0F * scale;
      float y = this.listTop + 8.0F * scale - this.listScroll;

      if (this.worldsLoading) {
         FontText.processData3(FontFace.SEMI, Lang.tr("Загрузка миров...", "Loading worlds..."), this.modalX + 22.0F * scale, this.listTop + 12.0F * scale, 7.0F * scale, 0xFF8C8FA3);
         this.contentBottom = this.listTop;
         return;
      }

      if (this.worlds.isEmpty()) {
         FontText.processData3(FontFace.SEMI, Lang.tr("Миров нет. Создай новый!", "No worlds. Create one!"), this.modalX + 22.0F * scale, this.listTop + 12.0F * scale, 7.0F * scale, 0xFF8C8FA3);
         this.contentBottom = this.listTop;
         return;
      }

      // полная высота списка — чтобы прокрутка показывала все строки
      this.contentBottom = this.listTop + 8.0F * scale + this.worlds.size() * (rowH + gap);

      for (int i = 0; i < this.worlds.size(); i++) {
         LevelSummary summary = this.worlds.get(i);
         if (y < this.listTop) {
            y += rowH + gap;
            continue;
         }

         if (y + rowH > this.listBottom) {
            break;
         }

         boolean selected = i == this.selectedWorld;
         boolean hover = mx >= this.modalX + 14.0F * scale && mx <= this.modalX + this.modalW - 14.0F * scale && my >= y && my <= y + rowH;

         Render2D.processData51(this.modalX + 14.0F * scale, y, this.modalW - 28.0F * scale, rowH, 7.0F * scale,
                 selected ? 0x269B6DFF : (hover ? 0x0DFFFFFF : 0x06FFFFFF));
         if (selected) {
            Render2D.processData86(this.modalX + 14.0F * scale, y, this.modalW - 28.0F * scale, rowH, 7.0F * scale, 0.8F * scale, 0xFF9B6DFF);
         }

         // иконка мира (цветной квадрат)
         float iconSize = 26.0F * scale;
         int iconColor = summary.isHardcore() ? 0xFFFB7185 : (summary.getGameMode().isSurvivalLike() ? 0xFF9B6DFF : 0xFF38BDF8);
         Render2D.processData51(this.modalX + 22.0F * scale, y + (rowH - iconSize) / 2.0F, iconSize, iconSize, 5.0F * scale, UiTheme.computeInt(iconColor, 0.35F));
         Render2D.processData86(this.modalX + 22.0F * scale, y + (rowH - iconSize) / 2.0F, iconSize, iconSize, 5.0F * scale, 0.7F * scale, 0x26FFFFFF);

         String name = summary.getDisplayName();
         if (name.length() > 28) {
            name = name.substring(0, 28) + "…";
         }

         String mode = summary.isHardcore() ? Lang.tr("Хардкор", "Hardcore") : summary.getGameMode().isCreative() ? Lang.tr("Творческий", "Creative") : Lang.tr("Выживание", "Survival");
         String sub = mode + " · " + MainMenuScreen.formatLastPlayed(summary.getLastPlayed());

         FontText.processData3(FontFace.SEMI, name, this.modalX + 56.0F * scale, y + 9.0F * scale, 7.5F * scale, 0xFFF4F4F8);
         FontText.processData3(FontFace.SEMI, sub, this.modalX + 56.0F * scale, y + 21.0F * scale, 6.0F * scale, 0xFF8C8FA3);

         // кнопка Настройки мира (ванильный EditWorldScreen: переименовать/удалить/бэкап)
         float playW = 34.0F * scale;
         float playX = this.modalX + this.modalW - playW - 20.0F * scale;
         float gearW = 17.0F * scale;
         float gearX = playX - gearW - 4.0F * scale;
         float gearY = y + (rowH - 17.0F * scale) / 2.0F;
         boolean gearHover = mx >= gearX && mx <= gearX + gearW && my >= gearY && my <= gearY + 17.0F * scale;
         Render2D.processData51(gearX, gearY, gearW, 17.0F * scale, 4.0F * scale, gearHover ? 0x24FFFFFF : 0x14FFFFFF);
         Render2D.processData86(gearX, gearY, gearW, 17.0F * scale, 4.0F * scale, 0.7F * scale, gearHover ? 0x66FFFFFF : 0x1AFFFFFF);
         FontText.processData3(FontFace.SEMI, "⚙", gearX + gearW / 2.0F - 2.2F * scale, gearY + 4.5F * scale, 6.0F * scale, gearHover ? 0xFFFFFFFF : 0xFF8C8FA3);

         float playX2 = playX;
         float playY2 = y + (rowH - 17.0F * scale) / 2.0F;
         boolean playHover = mx >= playX2 && mx <= playX2 + playW && my >= playY2 && my <= playY2 + 17.0F * scale;
         Render2D.processData51(playX2, playY2, playW, 17.0F * scale, 4.0F * scale, playHover ? 0xFF9B6DFF : 0x339B6DFF);
         FontText.processData3(FontFace.SEMI, "▶", playX2 + playW / 2.0F - 2.0F * scale, playY2 + 5.0F * scale, 6.0F * scale, 0xFFFFFFFF);

         if (interactive) {
            LevelSummary summaryFinal = summary;
            int worldIndex = i;
            this.menu.addModalArea(this.modalX + 14.0F * scale, y, this.modalW - 28.0F * scale, rowH, () -> this.selectedWorld = worldIndex);
            this.menu.addModalArea(gearX, gearY, gearW, 17.0F * scale, () -> this.editWorld(summaryFinal));
            this.menu.addModalArea(playX2, playY2, playW, 17.0F * scale, () -> this.playWorld(summaryFinal));
         }

         y += rowH + gap;
      }
   }

   private void renderAddWorld(float mx, float my, float scale, boolean interactive) {
      float x = this.modalX + 22.0F * scale;
      float y = this.listTop + 10.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Создание нового мира", "Create new world"), x, y, 7.5F * scale, 0xFFF4F4F8);
      y += 14.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Название мира", "World name"), x, y, 6.0F * scale, 0xFF8C8FA3);
      y += 8.0F * scale;
      this.menu.renderModalInput("worldName", this.newWorldName, x, y, this.modalW - 44.0F * scale, 20.0F * scale, scale);
      y += 28.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Режим игры", "Game mode"), x, y, 6.0F * scale, 0xFF8C8FA3);
      y += 8.0F * scale;
      String[] modes = {Lang.tr("Выживание", "Survival"), Lang.tr("Творческий", "Creative"), Lang.tr("Хардкор", "Hardcore")};
      float modeW = (this.modalW - 44.0F * scale - 8.0F * scale) / 3.0F;

      for (int i = 0; i < modes.length; i++) {
         float mx0 = x + i * (modeW + 4.0F * scale);
         boolean sel = i == this.newWorldMode;
         boolean hov = mx >= mx0 && mx <= mx0 + modeW && my >= y && my <= y + 18.0F * scale;
         Render2D.processData51(mx0, y, modeW, 18.0F * scale, 5.0F * scale, sel ? 0x409B6DFF : (hov ? 0x0DFFFFFF : 0x0DFFFFFF));
         Render2D.processData86(mx0, y, modeW, 18.0F * scale, 5.0F * scale, 0.7F * scale, sel ? 0xFF9B6DFF : 0x1AFFFFFF);
         FontText.processData3(FontFace.SEMI, modes[i], mx0 + modeW / 2.0F - this.estimate(modes[i], 5.5F * scale) / 2.0F, y + 5.5F * scale, 5.5F * scale, sel ? 0xFFFFFFFF : 0xFF8C8FA3);

         if (interactive) {
            int modeIndex = i;
            this.menu.addModalArea(mx0, y, modeW, 18.0F * scale, () -> this.newWorldMode = modeIndex);
         }
      }

      if (interactive) {
         this.menu.addModalArea(x, y - 36.0F * scale, this.modalW - 44.0F * scale, 20.0F * scale, () -> this.menu.setModalFocus("worldName"));
      }
   }

   private void renderServers(float mx, float my, float scale, boolean interactive) {
      if (this.editingServer >= 0) {
         this.renderEditServer(mx, my, scale, interactive);
         return;
      }

      if (this.addingServer) {
         this.renderAddServer(mx, my, scale, interactive);
         return;
      }

      float searchX = this.modalX + 22.0F * scale;
      float searchY = this.listTop + 2.0F * scale;
      float searchW = this.modalW - 44.0F * scale;
      this.menu.renderModalInput("serverSearch", this.serverSearch, searchX, searchY, searchW, 20.0F * scale, scale);
      if (interactive) {
         this.menu.addModalArea(searchX, searchY, searchW, 20.0F * scale, () -> this.menu.setModalFocus("serverSearch"));
      }

      float rowH = 40.0F * scale;
      float gap = 6.0F * scale;
      float rowsTop = this.listTop + 28.0F * scale;
      float y = rowsTop - this.listScroll;
      String query = this.serverSearch.trim().toLowerCase(Locale.ROOT);
      List<ServerInfo> visible = new ArrayList<>();
      for (ServerInfo info : this.servers) {
         if (query.isEmpty() || (info.name != null && info.name.toLowerCase(Locale.ROOT).contains(query))
               || (info.address != null && info.address.toLowerCase(Locale.ROOT).contains(query))) {
            visible.add(info);
         }
      }
      // Stable sort preserves the saved Minecraft order within each group.
      visible.sort(Comparator.comparing((ServerInfo info) -> !ServerMenuStore.isFavorite(info.address)));
      if (visible.isEmpty()) {
         String empty = this.servers.isEmpty() ? Lang.tr("Серверов нет. Добавь свой!", "No servers. Add one!")
               : Lang.tr("Ничего не найдено", "No matching servers");
         FontText.processData3(FontFace.SEMI, empty, searchX, rowsTop + 4.0F * scale, 7.0F * scale, 0xFF8C8FA3);
         this.contentBottom = this.listBottom;
         this.listScroll = 0.0F;
         return;
      }

      this.contentBottom = rowsTop + visible.size() * (rowH + gap);
      this.listScroll = Math.min(this.listScroll, Math.max(0.0F, this.contentBottom - this.listBottom));
      y = rowsTop - this.listScroll;

      for (ServerInfo info : visible) {
         int i = this.servers.indexOf(info);
         if (y < rowsTop) {
            y += rowH + gap;
            continue;
         }

         if (y + rowH > this.listBottom) {
            break;
         }

         boolean selected = i == this.selectedServer;
         boolean hover = mx >= this.modalX + 14.0F * scale && mx <= this.modalX + this.modalW - 14.0F * scale && my >= y && my <= y + rowH;

         Render2D.processData51(this.modalX + 14.0F * scale, y, this.modalW - 28.0F * scale, rowH, 7.0F * scale,
                 selected ? 0x269B6DFF : (hover ? 0x0DFFFFFF : 0x06FFFFFF));
         if (selected) {
            Render2D.processData86(this.modalX + 14.0F * scale, y, this.modalW - 28.0F * scale, rowH, 7.0F * scale, 0.8F * scale, 0xFF9B6DFF);
         }

         String name = info.name == null ? "" : info.name;
         if (name.length() > 22) {
            name = name.substring(0, 22) + "…";
         }

         String address = info.address == null ? "" : info.address;
         if (address.length() > 30) {
            address = address.substring(0, 30) + "…";
         }

         float iconSize = 28.0F * scale;
         Render2D.processData89(this.serverIcons.texture(info), this.modalX + 22.0F * scale,
               y + (rowH - iconSize) / 2.0F, iconSize, iconSize, 4.0F * scale, 0xFFFFFFFF);
         float starX = this.modalX + 54.0F * scale;
         boolean favorite = ServerMenuStore.isFavorite(info.address);
         boolean starHover = mx >= starX && mx <= starX + 14.0F * scale && my >= y + 6.0F * scale && my <= y + 24.0F * scale;
         FontText.processData3(FontFace.SEMI, favorite ? "★" : "☆", starX, y + 10.0F * scale, 9.0F * scale,
               favorite ? 0xFFFBBF24 : (starHover ? 0xFFFFFFFF : 0xFF8C8FA3));
         FontText.processData3(FontFace.SEMI, name, this.modalX + 71.0F * scale, y + 8.0F * scale, 7.5F * scale, 0xFFF4F4F8);
         FontText.processData3(FontFace.SEMI, address, this.modalX + 71.0F * scale, y + 21.0F * scale, 6.0F * scale, 0xFF8C8FA3);

         if (info.players != null) {
            String players = info.players.online() + " online";
            FontText.processData3(FontFace.SEMI, players, this.modalX + this.modalW - 62.0F * scale - this.estimate(players, 5.5F * scale), y + 21.0F * scale, 5.5F * scale, 0xFF636875);
         }

         // пинг сервера справа сверху
         String pingText;
         int pingColor;
         if (info.ping > 0L) {
            pingText = info.ping + " мс";
            pingColor = info.ping < 60L ? 0xFF34D399 : info.ping < 140L ? 0xFFFBBF24 : 0xFFF87171;
         } else if (info.ping < 0L) {
            pingText = Lang.tr("Нет ответа", "No response");
            pingColor = 0xFF8C8FA3;
         } else {
            pingText = Lang.tr("Проверяем…", "Checking…");
            pingColor = 0xFF636875;
         }

         FontText.processData3(FontFace.SEMI, pingText, this.modalX + this.modalW - 80.0F * scale - this.estimate(pingText, 5.5F * scale), y + 8.0F * scale, 5.5F * scale, pingColor);

         float playW = 34.0F * scale;
         float playX = this.modalX + this.modalW - playW - 20.0F * scale;

         // кнопка Настройки сервера (имя, IP, ресурс-пак)
         float gearW = 17.0F * scale;
         float gearX = playX - gearW - 4.0F * scale;
         float gearY = y + (rowH - 17.0F * scale) / 2.0F;
         boolean gearHover = mx >= gearX && mx <= gearX + gearW && my >= gearY && my <= gearY + 17.0F * scale;
         Render2D.processData51(gearX, gearY, gearW, 17.0F * scale, 4.0F * scale, gearHover ? 0x24FFFFFF : 0x14FFFFFF);
         Render2D.processData86(gearX, gearY, gearW, 17.0F * scale, 4.0F * scale, 0.7F * scale, gearHover ? 0x66FFFFFF : 0x1AFFFFFF);
         FontText.processData3(FontFace.SEMI, "⚙", gearX + gearW / 2.0F - 2.2F * scale, gearY + 4.5F * scale, 6.0F * scale, gearHover ? 0xFFFFFFFF : 0xFF8C8FA3);

         float playY = y + (rowH - 17.0F * scale) / 2.0F;
         boolean playHover = mx >= playX && mx <= playX + playW && my >= playY && my <= playY + 17.0F * scale;
         Render2D.processData51(playX, playY, playW, 17.0F * scale, 4.0F * scale, playHover ? 0xFF9B6DFF : 0x339B6DFF);
         FontText.processData3(FontFace.SEMI, "▶", playX + playW / 2.0F - 2.0F * scale, playY + 5.0F * scale, 6.0F * scale, 0xFFFFFFFF);

         if (interactive) {
            ServerInfo infoFinal = info;
            int serverIndex = i;
            this.menu.addModalArea(this.modalX + 14.0F * scale, y, this.modalW - 60.0F * scale, rowH, () -> this.selectedServer = serverIndex);
            this.menu.addModalArea(starX, y + 6.0F * scale, 14.0F * scale, 18.0F * scale, () -> {
               ServerMenuStore.toggleFavorite(infoFinal.address);
               this.listScroll = 0.0F;
            });
            this.menu.addModalArea(gearX, gearY, gearW, 17.0F * scale, () -> this.startEditServer(serverIndex));
            this.menu.addModalArea(playX, playY, playW, 17.0F * scale, () -> this.joinServer(infoFinal));
         }

         y += rowH + gap;
      }
   }

   private void renderAddServer(float mx, float my, float scale, boolean interactive) {
      float x = this.modalX + 22.0F * scale;
      float y = this.listTop + 10.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Добавить сервер", "Add server"), x, y, 7.5F * scale, 0xFFF4F4F8);
      y += 14.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Название сервера", "Server name"), x, y, 6.0F * scale, 0xFF8C8FA3);
      y += 8.0F * scale;
      this.menu.renderModalInput("serverName", this.newServerName, x, y, this.modalW - 44.0F * scale, 20.0F * scale, scale);
      y += 28.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("IP-адрес сервера", "Server IP"), x, y, 6.0F * scale, 0xFF8C8FA3);
      y += 8.0F * scale;
      this.menu.renderModalInput("serverIp", this.newServerIp, x, y, this.modalW - 44.0F * scale, 20.0F * scale, scale);

      if (interactive) {
         this.menu.addModalArea(x, y - 36.0F * scale, this.modalW - 44.0F * scale, 20.0F * scale, () -> this.menu.setModalFocus("serverName"));
         this.menu.addModalArea(x, y, this.modalW - 44.0F * scale, 20.0F * scale, () -> this.menu.setModalFocus("serverIp"));
      }
   }

   private void renderEditServer(float mx, float my, float scale, boolean interactive) {
      float x = this.modalX + 22.0F * scale;
      float y = this.listTop + 10.0F * scale;
      float fieldW = this.modalW - 44.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Настройки сервера", "Server settings"), x, y, 7.5F * scale, 0xFFF4F4F8);
      y += 14.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Название сервера", "Server name"), x, y, 6.0F * scale, 0xFF8C8FA3);
      y += 8.0F * scale;
      this.menu.renderModalInput("editName", this.editServerName, x, y, fieldW, 20.0F * scale, scale);
      y += 28.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("IP-адрес сервера", "Server IP"), x, y, 6.0F * scale, 0xFF8C8FA3);
      y += 8.0F * scale;
      this.menu.renderModalInput("editIp", this.editServerIp, x, y, fieldW, 20.0F * scale, scale);
      y += 28.0F * scale;

      FontText.processData3(FontFace.SEMI, Lang.tr("Ресурс-пак сервера", "Server resource pack"), x, y, 6.0F * scale, 0xFF8C8FA3);
      y += 8.0F * scale;

      // РесурсПак: Вкл / Выкл / Классика (спрашивать)
      String[] policies = {Lang.tr("Вкл", "On"), Lang.tr("Выкл", "Off"), Lang.tr("Классика", "Classic")};
      float policyW = (fieldW - 8.0F * scale) / 3.0F;

      for (int i = 0; i < policies.length; i++) {
         float px0 = x + i * (policyW + 4.0F * scale);
         boolean sel = i == this.editServerPolicy;
         boolean hov = mx >= px0 && mx <= px0 + policyW && my >= y && my <= y + 18.0F * scale;
         Render2D.processData51(px0, y, policyW, 18.0F * scale, 5.0F * scale, sel ? 0x409B6DFF : (hov ? 0x14FFFFFF : 0x0AFFFFFF));
         Render2D.processData86(px0, y, policyW, 18.0F * scale, 5.0F * scale, 0.7F * scale, sel ? 0xFF9B6DFF : 0x1AFFFFFF);
         FontText.processData3(FontFace.SEMI, policies[i], px0 + policyW / 2.0F - this.estimate(policies[i], 5.5F * scale) / 2.0F, y + 5.5F * scale, 5.5F * scale, sel ? 0xFFFFFFFF : 0xFF8C8FA3);

         if (interactive) {
            int policyIndex = i;
            this.menu.addModalArea(px0, y, policyW, 18.0F * scale, () -> this.editServerPolicy = policyIndex);
         }
      }

      if (interactive) {
         this.menu.addModalArea(x, y - 64.0F * scale, fieldW, 20.0F * scale, () -> this.menu.setModalFocus("editName"));
         this.menu.addModalArea(x, y - 36.0F * scale, fieldW, 20.0F * scale, () -> this.menu.setModalFocus("editIp"));
      }
   }

   private void renderExit(float mx, float my, float scale, boolean interactive) {
      float cx = this.modalX + this.modalW / 2.0F;

      // красная иконка питания
      float iconSize = 26.0F * scale;
      Render2D.processData51(cx - iconSize / 2.0F, this.modalY + 26.0F * scale, iconSize, iconSize, 6.0F * scale, 0x26FF5C5C);
      Render2D.processData86(cx - iconSize / 2.0F, this.modalY + 26.0F * scale, iconSize, iconSize, 6.0F * scale, 0.8F * scale, 0x4DFF5C5C);
      String powerText = "⏻";
      FontText.processData3(FontFace.SEMI, powerText, cx - this.estimate(powerText, 9.0F * scale) / 2.0F, this.modalY + 33.0F * scale, 9.0F * scale, 0xFFFF8A8A);

      FontText.processData3(FontFace.SEMI, Lang.tr("Закрыть сессию игры?", "Close game session?"), cx - this.estimate(Lang.tr("Закрыть сессию игры?", "Close game session?"), 7.5F * scale) / 2.0F, this.modalY + 66.0F * scale, 7.5F * scale, 0xFFF4F4F8);

      float btnW = (this.modalW - 44.0F * scale - 8.0F * scale) / 2.0F;
      float btnY = this.modalY + 92.0F * scale;

      boolean hovStay = mx >= this.modalX + 22.0F * scale && mx <= this.modalX + 22.0F * scale + btnW && my >= btnY && my <= btnY + 22.0F * scale;
      Render2D.processData51(this.modalX + 22.0F * scale, btnY, btnW, 22.0F * scale, 6.0F * scale, hovStay ? 0x24FFFFFF : 0x14FFFFFF);
      FontText.processData3(FontFace.SEMI, Lang.tr("Остаться", "Stay"), this.modalX + 22.0F * scale + btnW / 2.0F - this.estimate(Lang.tr("Остаться", "Stay"), 6.0F * scale) / 2.0F, btnY + 7.0F * scale, 6.0F * scale, hovStay ? 0xFFFFFFFF : 0xFFC9CDD8);
      if (interactive) {
         this.menu.addModalArea(this.modalX + 22.0F * scale, btnY, btnW, 22.0F * scale, this::close);
      }

      float exitX = this.modalX + 22.0F * scale + btnW + 8.0F * scale;
      boolean hovExit = mx >= exitX && mx <= exitX + btnW && my >= btnY && my <= btnY + 22.0F * scale;
      Render2D.processData51(exitX, btnY, btnW, 22.0F * scale, 6.0F * scale, hovExit ? 0xFFFF5C5C : 0xCCFF5C5C);
      FontText.processData3(FontFace.SEMI, Lang.tr("Выйти", "Exit"), exitX + btnW / 2.0F - this.estimate(Lang.tr("Выйти", "Exit"), 6.0F * scale) / 2.0F, btnY + 7.0F * scale, 6.0F * scale, 0xFFFFFFFF);
      if (interactive) {
         this.menu.addModalArea(exitX, btnY, btnW, 22.0F * scale, () -> {
            this.close();
            this.menu.transitionTo(MinecraftClient.getInstance()::scheduleStop);
         });
      }
   }

   private void renderStudio(float mx, float my, float scale, boolean interactive) {
      float x = this.modalX + 22.0F * scale;
      float y = this.listTop + 6.0F * scale;
      float w = this.modalW - 44.0F * scale;
      this.studioButton(x, y, w, scale,
            this.menu.photoLoading() ? Lang.tr("Загрузка…", "Loading…") : Lang.tr("Выбрать фото / GIF", "Choose photo / GIF"),
            false, interactive && !this.menu.photoLoading(), this.menu::pickCustomPhoto, mx, my);
      y += 22.0F * scale;
      for (int i = 0; i < MenuStore.backgroundCount(); i++) {
         final int background = i;
         float bw = (w - 6.0F * scale) / 2.0F;
         this.studioButton(x + i * (bw + 6.0F * scale), y, bw, scale,
               i == 0 ? Lang.tr("Ночной фон", "Night background") : Lang.tr("Закат", "Sunset"),
               (MenuStore.customPhoto() == null || MenuStore.customPhoto().isBlank()) && MenuStore.backgroundIndex() == i,
               interactive, () -> { this.menu.clearCustomPhoto(); MenuStore.setBackgroundIndex(background); }, mx, my);
      }
      y += 22.0F * scale;
      String[] layouts = {Lang.tr("Классика", "Classic"), Lang.tr("Карточки", "Cards"), Lang.tr("Сетка", "Grid")};
      float lw = (w - 12.0F * scale) / 3.0F;
      for (int i = 0; i < layouts.length; i++) {
         final int layout = i;
         this.studioButton(x + i * (lw + 6.0F * scale), y, lw, scale, layouts[i],
               MenuStore.centerLayout() == i, interactive, () -> MenuStore.setCenterLayout(layout), mx, my);
      }
      y += 22.0F * scale;
      String[] liveNames = {Lang.tr("Статика", "Static"), Lang.tr("Дыхание", "Breathing"), Lang.tr("Частицы", "Particles"), Lang.tr("Дымка", "Aurora")};
      float liveW = (w - 18.0F * scale) / 4.0F;
      for (int i = 0; i < liveNames.length; i++) {
         final int mode = i;
         this.studioButton(x + i * (liveW + 6.0F * scale), y, liveW, scale, liveNames[i],
               MenuStore.liveMode() == i, interactive, () -> MenuStore.setLiveMode(mode), mx, my);
      }
      y += 22.0F * scale;
      this.studioButton(x, y, w, scale,
            Lang.tr("Глянец кнопок: ", "Button gloss: ") + (MenuStore.glossy() ? Lang.tr("ВКЛ", "ON") : Lang.tr("ВЫКЛ", "OFF")),
            MenuStore.glossy(), interactive, MenuStore::toggleGlossy, mx, my);
      y += 22.0F * scale;
      if (MenuStore.customPhoto() != null && !MenuStore.customPhoto().isBlank()) {
         this.studioButton(x, y, w, scale, Lang.tr("Убрать фото: ", "Remove photo: ") + shortName(MenuStore.photoName()),
               false, interactive, this.menu::clearCustomPhoto, mx, my);
         y += 22.0F * scale;
      }
      String status = MenuStore.saveFailed() ? Lang.tr("Не удалось сохранить настройки", "Could not save settings") : this.menu.photoStatus();
      if (status.isEmpty()) status = Lang.tr("Настройки сохраняются автоматически", "Settings saved automatically");
      FontText.processData3(FontFace.SEMI, status, x, y + 4.0F * scale, 5.5F * scale,
            MenuStore.saveFailed() ? 0xFFFF8A8A : 0xFFADB2C2);
   }

   private static String shortName(String name) {
      return name.length() > 28 ? name.substring(0, 25) + "…" : name;
   }

   private void studioButton(float x, float y, float w, float scale, String label,
                             boolean selected, boolean interactive, Runnable action, float mx, float my) {
      float h = 18.0F * scale;
      boolean hover = interactive && UiTheme.matchesCriteria(mx, my, x, y, w, h);
      Render2D.processData51(x, y, w, h, 4.0F * scale, selected ? 0x409B6DFF : hover ? 0x24FFFFFF : 0x14FFFFFF);
      Render2D.processData86(x, y, w, h, 4.0F * scale, 0.7F * scale, selected ? 0xFF9B6DFF : 0x30FFFFFF);
      FontText.processData3(FontFace.SEMI, label, x + 7.0F * scale, y + 6.0F * scale, 5.5F * scale, 0xFFF4F4F8);
      if (interactive) this.menu.addModalArea(x, y, w, h, action);
   }

   private void renderFooter(float mx, float my, float scale, boolean interactive) {
      float y = this.listBottom + 10.0F * scale;
      float btnH = 20.0F * scale;

      if (this.type == Type.SINGLEPLAYER) {
         if (this.addingWorld) {
            // Отмена | Создать
            float w = 86.0F * scale;
            float cancelX = this.modalX + 22.0F * scale;
            boolean hov = mx >= cancelX && mx <= cancelX + w && my >= y && my <= y + btnH;
            Render2D.processData51(cancelX, y, w, btnH, 5.0F * scale, hov ? 0x24FFFFFF : 0x14FFFFFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("Отмена", "Cancel"), cancelX + w / 2.0F - this.estimate(Lang.tr("Отмена", "Cancel"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, hov ? 0xFFFFFFFF : 0xFF8C8FA3);
            if (interactive) {
               this.menu.addModalArea(cancelX, y, w, btnH, () -> this.addingWorld = false);
            }

            float createX = this.modalX + this.modalW - 22.0F * scale - w * 1.6F;
            float createW = w * 1.6F;
            boolean hov2 = mx >= createX && mx <= createX + createW && my >= y && my <= y + btnH;
            Render2D.processData51(createX, y, createW, btnH, 5.0F * scale, hov2 ? 0xFFB794FF : 0xFF9B6DFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("Создать и играть", "Create & play"), createX + createW / 2.0F - this.estimate(Lang.tr("Создать и играть", "Create & play"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, 0xFFFFFFFF);
            if (interactive) {
               this.menu.addModalArea(createX, y, createW, btnH, this::createAndPlayWorld);
            }
         } else {
            // Создать мир | Играть в выбранном
            float w = 92.0F * scale;
            float createX = this.modalX + 22.0F * scale;
            boolean hov = mx >= createX && mx <= createX + w && my >= y && my <= y + btnH;
            Render2D.processData51(createX, y, w, btnH, 5.0F * scale, hov ? 0x24FFFFFF : 0x14FFFFFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("+ Создать мир", "+ Create world"), createX + w / 2.0F - this.estimate(Lang.tr("+ Создать мир", "+ Create world"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, hov ? 0xFFFFFFFF : 0xFF8C8FA3);
            if (interactive) {
               this.menu.addModalArea(createX, y, w, btnH, () -> this.addingWorld = true);
            }

            LevelSummary chosen = this.selectedWorld >= 0 && this.selectedWorld < this.worlds.size() ? this.worlds.get(this.selectedWorld) : null;
            float playW = 120.0F * scale;
            float playX = this.modalX + this.modalW - 22.0F * scale - playW;
            boolean hov2 = mx >= playX && mx <= playX + playW && my >= y && my <= y + btnH;
            boolean enabled = chosen != null;
            Render2D.processData51(playX, y, playW, btnH, 5.0F * scale, enabled ? (hov2 ? 0xFFB794FF : 0xFF9B6DFF) : 0x339B6DFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("▶ Играть", "▶ Play"), playX + playW / 2.0F - this.estimate(Lang.tr("▶ Играть", "▶ Play"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, enabled ? 0xFFFFFFFF : 0x66FFFFFF);
            if (interactive && enabled) {
               this.menu.addModalArea(playX, y, playW, btnH, () -> this.playWorld(chosen));
            }
         }
      } else if (this.type == Type.MULTIPLAYER) {
         if (this.editingServer >= 0) {
            // Настройки сервера: Отмена | Сохранить
            float w = 86.0F * scale;
            float cancelX = this.modalX + 22.0F * scale;
            boolean hov = mx >= cancelX && mx <= cancelX + w && my >= y && my <= y + btnH;
            Render2D.processData51(cancelX, y, w, btnH, 5.0F * scale, hov ? 0x24FFFFFF : 0x14FFFFFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("Отмена", "Cancel"), cancelX + w / 2.0F - this.estimate(Lang.tr("Отмена", "Cancel"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, hov ? 0xFFFFFFFF : 0xFF8C8FA3);
            if (interactive) {
               this.menu.addModalArea(cancelX, y, w, btnH, () -> this.editingServer = -1);
            }

            float saveX = this.modalX + this.modalW - 22.0F * scale - w * 1.6F;
            float saveW = w * 1.6F;
            boolean hov2 = mx >= saveX && mx <= saveX + saveW && my >= y && my <= y + btnH;
            Render2D.processData51(saveX, y, saveW, btnH, 5.0F * scale, hov2 ? 0xFFB794FF : 0xFF9B6DFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("Сохранить", "Save"), saveX + saveW / 2.0F - this.estimate(Lang.tr("Сохранить", "Save"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, 0xFFFFFFFF);
            if (interactive) {
               this.menu.addModalArea(saveX, y, saveW, btnH, this::saveEditServer);
            }
         } else if (this.addingServer) {
            float w = 86.0F * scale;
            float cancelX = this.modalX + 22.0F * scale;
            boolean hov = mx >= cancelX && mx <= cancelX + w && my >= y && my <= y + btnH;
            Render2D.processData51(cancelX, y, w, btnH, 5.0F * scale, hov ? 0x24FFFFFF : 0x14FFFFFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("Отмена", "Cancel"), cancelX + w / 2.0F - this.estimate(Lang.tr("Отмена", "Cancel"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, hov ? 0xFFFFFFFF : 0xFF8C8FA3);
            if (interactive) {
               this.menu.addModalArea(cancelX, y, w, btnH, () -> this.addingServer = false);
            }

            float saveX = this.modalX + this.modalW - 22.0F * scale - w * 1.6F;
            float saveW = w * 1.6F;
            boolean hov2 = mx >= saveX && mx <= saveX + saveW && my >= y && my <= y + btnH;
            Render2D.processData51(saveX, y, saveW, btnH, 5.0F * scale, hov2 ? 0xFFB794FF : 0xFF9B6DFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("Сохранить сервер", "Save server"), saveX + saveW / 2.0F - this.estimate(Lang.tr("Сохранить сервер", "Save server"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, 0xFFFFFFFF);
            if (interactive) {
               this.menu.addModalArea(saveX, y, saveW, btnH, this::saveNewServer);
            }
         } else {
            float w = 92.0F * scale;
            float addX = this.modalX + 22.0F * scale;
            boolean hov = mx >= addX && mx <= addX + w && my >= y && my <= y + btnH;
            Render2D.processData51(addX, y, w, btnH, 5.0F * scale, hov ? 0x24FFFFFF : 0x14FFFFFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("+ Добавить сервер", "+ Add server"), addX + w / 2.0F - this.estimate(Lang.tr("+ Добавить сервер", "+ Add server"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, hov ? 0xFFFFFFFF : 0xFF8C8FA3);
            if (interactive) {
               this.menu.addModalArea(addX, y, w, btnH, () -> this.addingServer = true);
            }

            ServerInfo chosen = this.selectedServer >= 0 && this.selectedServer < this.servers.size() ? this.servers.get(this.selectedServer) : null;
            float playW = 120.0F * scale;
            float playX = this.modalX + this.modalW - 22.0F * scale - playW;
            boolean hov2 = mx >= playX && mx <= playX + playW && my >= y && my <= y + btnH;
            boolean enabled = chosen != null;
            Render2D.processData51(playX, y, playW, btnH, 5.0F * scale, enabled ? (hov2 ? 0xFFB794FF : 0xFF9B6DFF) : 0x339B6DFF);
            FontText.processData3(FontFace.SEMI, Lang.tr("▶ Подключиться", "▶ Connect"), playX + playW / 2.0F - this.estimate(Lang.tr("▶ Подключиться", "▶ Connect"), 6.0F * scale) / 2.0F, y + 6.5F * scale, 6.0F * scale, enabled ? 0xFFFFFFFF : 0x66FFFFFF);
            if (interactive && enabled) {
               this.menu.addModalArea(playX, y, playW, btnH, () -> this.joinServer(chosen));
            }
         }
      }
   }

   // ===== действия =====

   private void createAndPlayWorld() {
      String name = this.newWorldName.trim();
      if (name.isEmpty()) {
         name = Lang.tr("Новый мир", "New World");
      }

      this.addingWorld = false;
      this.close();
      final String worldName = name;

      this.menu.transitionTo(() -> {
         MinecraftClient mc = MinecraftClient.getInstance();
         net.minecraft.client.gui.screen.world.CreateWorldScreen.show(mc, () -> mc.setScreen(new MainMenuScreen(false)));
      });
   }

   private void saveNewServer() {
      String name = this.newServerName.trim();
      String ip = this.newServerIp.trim();
      if (ip.isEmpty()) {
         return;
      }

      if (this.serverList == null) {
         this.loadServers();
      }

      try {
         ServerInfo info = new ServerInfo(name.isEmpty() ? ip : name, ip, ServerInfo.ServerType.OTHER);
         this.serverList.add(info, false);
         this.serverList.saveFile();
         this.addingServer = false;
         this.newServerName = "";
         this.newServerIp = "";
         this.loadServers();
      } catch (Exception ignored) {
      }
   }

   // ===== ввод =====

   boolean isTyping() {
      return this.type == Type.SINGLEPLAYER && this.addingWorld && this.menu.modalFocus() != null
              || this.type == Type.MULTIPLAYER && this.menu.modalFocus() != null
                    && (this.addingServer || this.editingServer >= 0 || "serverSearch".equals(this.menu.modalFocus()));
   }

   void charTyped(char c) {
      if (this.type == Type.SINGLEPLAYER && this.addingWorld) {
         if ("worldName".equals(this.menu.modalFocus()) && this.newWorldName.length() < 32) {
            this.newWorldName = this.newWorldName + c;
         }
      } else if (this.type == Type.MULTIPLAYER) {
         if (this.addingServer) {
            if ("serverName".equals(this.menu.modalFocus()) && this.newServerName.length() < 32) {
               this.newServerName = this.newServerName + c;
            } else if ("serverIp".equals(this.menu.modalFocus()) && this.newServerIp.length() < 64) {
               this.newServerIp = this.newServerIp + c;
            }
         } else if (this.editingServer >= 0) {
            if ("editName".equals(this.menu.modalFocus()) && this.editServerName.length() < 32) {
               this.editServerName = this.editServerName + c;
            } else if ("editIp".equals(this.menu.modalFocus()) && this.editServerIp.length() < 64) {
               this.editServerIp = this.editServerIp + c;
            }
         } else if ("serverSearch".equals(this.menu.modalFocus()) && this.serverSearch.length() < 64 && !Character.isISOControl(c)) {
            this.serverSearch += c;
            this.selectedServer = -1;
            this.listScroll = 0.0F;
         }
      }
   }

   boolean backspace() {
      if (this.type == Type.SINGLEPLAYER && this.addingWorld && "worldName".equals(this.menu.modalFocus()) && !this.newWorldName.isEmpty()) {
         this.newWorldName = this.newWorldName.substring(0, this.newWorldName.length() - 1);
         return true;
      }

      if (this.type == Type.MULTIPLAYER) {
         if (!this.addingServer && this.editingServer < 0 && "serverSearch".equals(this.menu.modalFocus()) && !this.serverSearch.isEmpty()) {
            this.serverSearch = this.serverSearch.substring(0, this.serverSearch.length() - 1);
            this.selectedServer = -1;
            this.listScroll = 0.0F;
            return true;
         }
         if (this.addingServer) {
            if ("serverName".equals(this.menu.modalFocus()) && !this.newServerName.isEmpty()) {
               this.newServerName = this.newServerName.substring(0, this.newServerName.length() - 1);
               return true;
            }

            if ("serverIp".equals(this.menu.modalFocus()) && !this.newServerIp.isEmpty()) {
               this.newServerIp = this.newServerIp.substring(0, this.newServerIp.length() - 1);
               return true;
            }
         } else if (this.editingServer >= 0) {
            if ("editName".equals(this.menu.modalFocus()) && !this.editServerName.isEmpty()) {
               this.editServerName = this.editServerName.substring(0, this.editServerName.length() - 1);
               return true;
            }

            if ("editIp".equals(this.menu.modalFocus()) && !this.editServerIp.isEmpty()) {
               this.editServerIp = this.editServerIp.substring(0, this.editServerIp.length() - 1);
               return true;
            }
         }
      }

      return false;
   }

   void scroll(float amount) {
      float max = Math.max(0.0F, this.contentBottom - this.listBottom);
      this.listScroll = Math.max(0.0F, Math.min(max, this.listScroll - amount * 16.0F));
   }

   boolean inside(float mx, float my) {
      return this.type != null && mx >= this.modalX && mx <= this.modalX + this.modalW && my >= this.modalY && my <= this.modalY + this.modalH;
   }

   private float estimate(String text, float size) {
      if (text == null || text.isEmpty()) {
         return 0.0F;
      }

      float units = 0.0F;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == ' ') {
            units += 0.30F;
         } else if ("iIl1.,!|:;'".indexOf(c) >= 0) {
            units += 0.28F;
         } else if ("mwMWЩШЖЮФ".indexOf(c) >= 0) {
            units += 0.78F;
         } else if (Character.isUpperCase(c)) {
            units += 0.62F;
         } else {
            units += 0.52F;
         }
      }

      return units * size;
   }
}
