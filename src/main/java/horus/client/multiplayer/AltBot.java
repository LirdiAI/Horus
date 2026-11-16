package horus.client.multiplayer;

import horus.client.ui.hud.NotificationHud;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.config.AcceptCodeOfConductC2SPacket;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerLoadedC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.s2c.config.CodeOfConductS2CPacket;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.config.ResetChatS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;

/**
 * Мультиаккаунт: второе подключение к текущему серверу внутри одного окна.
 * Оффлайн-режим (вход по нику) — как в EvoVisuals "день 23".
 */
public final class AltBot {
   public enum Status { CONNECTING, LOGIN, CONFIGURING, PLAYING, DEAD, CLOSED }

   public final String name;
   public final UUID uuid;
   public final String password;
   public final String modeCommand;
   private final java.net.InetSocketAddress resolvedAddress;
   private volatile Status status = Status.CONNECTING;
   private volatile String statusText = "Подключение...";
   private volatile ClientConnection connection;
   private volatile float health = -1.0F;
   private volatile int food = -1;
   private volatile int pingMs = -1;
   private volatile boolean needRespawn = false;
   private volatile boolean hasPosition = false;
   private volatile long lastWorldTimePacket = 0L;
   private volatile double x;
   private volatile double y;
   private volatile double z;
   private volatile float yaw;
   private volatile float pitch;
   private final MinecraftClient mc = MinecraftClient.getInstance();
   private final AtomicBoolean started = new AtomicBoolean(false);
   private volatile boolean authSent = false;
   private volatile long joinedAtMs = 0L;
   private final java.util.Deque<String> lastMessages = new java.util.concurrent.ConcurrentLinkedDeque<>();
   private final java.util.Map<Integer, double[]> trackedEntities = new java.util.concurrent.ConcurrentHashMap<>();
   private volatile int selfEntityId = -1;
   private volatile double driveVelY = 0.0;
   private volatile boolean driven = false;

   public AltBot(String name, String password, String modeCommand, java.net.InetSocketAddress resolvedAddress) {
      this.name = name;
      this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
      this.password = password == null ? "" : password;
      this.modeCommand = modeCommand == null ? "" : modeCommand.trim();
      this.resolvedAddress = resolvedAddress;
   }

   public void retry() {
      synchronized (this) {
         if (this.started.compareAndSet(true, false) || this.status == Status.CLOSED) {
            this.status = Status.CONNECTING;
            this.statusText = "Подключение...";
         }
      }

      this.start();
   }

   public static boolean isNickValid(String name) {
      return name.matches("[A-Za-z0-9_]{3,16}");
   }

   public String getStatusText() {
      return this.statusText;
   }

   public Status getStatus() {
      return this.status;
   }

   public float getHealth() {
      return this.health;
   }

   public int getFood() {
      return this.food;
   }

   public int getPing() {
      return this.pingMs;
   }

   public long getOnlineSeconds() {
      long joined = this.joinedAtMs;
      if (joined <= 0L) {
         return 0L;
      }

      return Math.max(0L, (System.currentTimeMillis() - joined) / 1000L);
   }

   public java.util.List<String> getLastMessages() {
      java.util.List<String> list = new java.util.ArrayList<>(this.lastMessages);
      return list;
   }

   public String getPassword() {
      return this.password;
   }

   public boolean isDriven() {
      return this.driven;
   }

   public void setDriven(boolean driven) {
      this.driven = driven;
      if (driven) {
         this.statusText = "Ты за альтом (в панели — встать)";
      } else {
         this.driveVelY = 0.0;
      }
   }

   // ===== трекинг сущностей вокруг альта =====

   void onEntitySpawn(net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket packet) {
      if (packet.getEntityId() == this.selfEntityId) {
         this.x = packet.getX();
         this.y = packet.getY();
         this.z = packet.getZ();
         this.hasPosition = true;
         this.driveVelY = 0.0;
         return;
      }

      this.trackedEntities.put(packet.getEntityId(), new double[]{packet.getX(), packet.getY(), packet.getZ()});
   }

   void onEntityMove(net.minecraft.network.packet.s2c.play.EntityS2CPacket packet) {
      int entityId = ((horus.mixin.EntityS2CPacketAccessor)packet).horus$getId();
      double[] pos = this.trackedEntities.get(entityId);
      if (pos == null) {
         return;
      }

      if (packet.isPositionChanged()) {
         pos[0] += packet.getDeltaX() / 4096.0;
         pos[1] += packet.getDeltaY() / 4096.0;
         pos[2] += packet.getDeltaZ() / 4096.0;
      }
   }

   void onEntityPositionSync(net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket packet) {
      if (packet.id() == this.selfEntityId) {
         this.x = packet.values().position().x;
         this.y = packet.values().position().y;
         this.z = packet.values().position().z;
         this.hasPosition = true;
         this.driveVelY = 0.0;
         return;
      }

      this.trackedEntities.put(packet.id(), new double[]{packet.values().position().x, packet.values().position().y, packet.values().position().z});
   }

   void onEntitiesDestroy(net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket packet) {
      for (int id : packet.getEntityIds()) {
         this.trackedEntities.remove(id);
      }
   }

   /** Ближайшая сущность перед альтом (для удара). */
   public int findAttackTarget(float yaw) {
      double rad = Math.toRadians(yaw);
      double dirX = -Math.sin(rad);
      double dirZ = Math.cos(rad);
      int best = -1;
      double bestScore = Double.MAX_VALUE;

      for (java.util.Map.Entry<Integer, double[]> entry : this.trackedEntities.entrySet()) {
         if (entry.getKey() == this.selfEntityId) {
            continue;
         }

         double dx = entry.getValue()[0] - this.x;
         double dy = entry.getValue()[1] - this.y;
         double dz = entry.getValue()[2] - this.z;
         double distSq = dx * dx + dy * dy + dz * dz;
         if (distSq > 12.25) {
            continue;
         }

         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (horizDist < 0.01) {
            horizDist = 0.01;
         }

         double dot = (dx / horizDist) * dirX + (dz / horizDist) * dirZ;
         if (dot < 0.55) {
            continue;
         }

         if (distSq < bestScore) {
            bestScore = distSq;
            best = entry.getKey();
         }
      }

      return best;
   }

   public boolean driveAttack(float yaw) {
      int target = this.findAttackTarget(yaw);
      if (target == -1) {
         return false;
      }

      ClientConnection conn = this.connection;
      if (conn == null) {
         return false;
      }

      net.minecraft.entity.Entity fake = new AltFakeEntity(mc.world != null ? mc.world : null, target);
      conn.send(net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.attack(fake, false));
      conn.send(new net.minecraft.network.packet.c2s.play.HandSwingC2SPacket(net.minecraft.util.Hand.MAIN_HAND));
      return true;
   }

   /** Управление движением альта из режима посадки. */
   public void driveMove(float forward, float sideways, float yaw, float pitch, boolean jumpHeld, boolean sneak) {
      if (!this.hasPosition || !this.driven) {
         return;
      }

      double speed = sneak ? 1.3 / 20.0 : 4.317 / 20.0;
      double rad = Math.toRadians(yaw);
      double dirX = -Math.sin(rad);
      double dirZ = Math.cos(rad);
      double sideX = -dirZ;
      double sideZ = dirX;
      double dx = dirX * forward + sideX * sideways;
      double dz = dirZ * forward + sideZ * sideways;
      double len = Math.sqrt(dx * dx + dz * dz);
      if (len > 1.0) {
         dx /= len;
         dz /= len;
      }

      this.x += dx * speed;
      this.z += dz * speed;

      if (jumpHeld && this.driveVelY == 0.0) {
         this.driveVelY = 0.42;
      }

      this.driveVelY = this.driveVelY * 0.98 - 0.08;
      if (this.driveVelY < -3.92) {
         this.driveVelY = -3.92;
      }

      double prevY = this.y;
      this.y += this.driveVelY;
      boolean onGround = this.y <= prevY + 0.0001 && this.driveVelY < 0 && this.driveVelY > -0.25 && jumpHeld == false;
      ClientConnection conn = this.connection;
      if (conn != null) {
         conn.send(new PlayerMoveC2SPacket.Full(this.x, this.y, this.z, yaw, pitch, onGround, false));
         conn.send(ClientTickEndC2SPacket.INSTANCE);
      }
   }

   public String getModeLabel() {
      if (this.modeCommand.isEmpty()) {
         return "лобби";
      }

      return this.modeCommand;
   }

   public static String formatOnline(long seconds) {
      long min = seconds / 60L;
      long sec = seconds % 60L;
      return min > 0L ? min + "м " + sec + "с" : sec + "с";
   }

   public boolean isOnServer() {
      Status s = this.status;
      return s == Status.PLAYING || s == Status.DEAD;
   }

   public boolean isAlive() {
      return this.status != Status.CLOSED;
   }

   public void start() {
      if (!this.started.compareAndSet(false, true)) {
         return;
      }

      Thread thread = new Thread(() -> this.run(), "Horus-Alt-" + this.name);
      thread.setDaemon(true);
      thread.start();
   }

   private void run() {
      if (this.resolvedAddress == null) {
         this.close("Нет данных о сервере");
         return;
      }

      java.net.InetSocketAddress socketAddress = this.resolvedAddress;
      horus.client.HorusClient.logger().info("[MultiAccount] Alt {} -> {}:{}", this.name, socketAddress.getHostString(), socketAddress.getPort());
      MinecraftClient.getInstance().execute(() -> {
         this.statusText = "Соединение...";
      });
      ClientConnection conn = new ClientConnection(NetworkSide.CLIENTBOUND);
      this.connection = conn;

      try {
         ClientConnection.connect(socketAddress, net.minecraft.network.NetworkingBackend.remote(false), conn);
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} TCP connected, channel={}", this.name, conn.isOpen());
         this.status = Status.LOGIN;
         this.statusText = "Авторизация...";
         // как в ванилле: generic connect + сразу шлём Hello (ник) сами
         conn.connect(socketAddress.getHostName(), socketAddress.getPort(), LoginStates.C2S, LoginStates.S2C, new AltLoginListener(), false);
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} handshake submitted, sending hello", this.name);
         conn.send(new LoginHelloC2SPacket(this.name, this.uuid));
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} hello sent (nick+uuid)", this.name);
      } catch (Exception exception) {
         horus.client.HorusClient.logger().error("[MultiAccount] Alt {} connect failed", this.name, exception);
         this.close("Нет связи с " + socketAddress.getHostString());
      }
   }

   public void tick(MinecraftClient client) {
      ClientConnection conn = this.connection;
      if (conn == null) {
         return;
      }

      if (!conn.isOpen()) {
         if (this.status != Status.CLOSED) {
            horus.client.HorusClient.logger().info("[MultiAccount] Alt {} channel closed while status={}", this.name, this.status);
            this.close("Соединение потеряно");
         }

         return;
      }

      if (this.status == Status.PLAYING || this.status == Status.DEAD) {
         // Позиция: следуем за главным игроком (или стоим на месте)
         if (this.followPosition(client)) {
            PlayerMoveC2SPacket.Full full = new PlayerMoveC2SPacket.Full(this.x, this.y, this.z, this.yaw, this.pitch, true, false);
            conn.send(full);
            conn.send(ClientTickEndC2SPacket.INSTANCE);
         }

         if (this.needRespawn) {
            this.needRespawn = false;
            conn.send(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
            this.statusText = "Возрождение...";
         }
      }

      conn.tick();
   }

   private boolean followPosition(MinecraftClient client) {
      if (this.hasPosition && client.player != null) {
         double dx = client.player.getX() - this.x;
         double dz = client.player.getZ() - this.z;
         if (dx * dx + dz * dz > 4.0) {
            this.x = client.player.getX() + Math.cos(this.x * 0.37) * 1.5;
            this.z = client.player.getZ() + Math.sin(this.z * 0.37) * 1.5;
            this.y = client.player.getY();
         }
      }

      return this.hasPosition;
   }

   public void disconnect() {
      ClientConnection conn = this.connection;
      this.status = Status.CLOSED;
      if (conn != null && conn.isOpen()) {
         try {
            conn.disconnect(Text.literal("Отключён игроком"));
         } catch (Exception ignored) {
         }
      }

      this.connection = null;
   }

   public void close(String reason) {
      if (this.status == Status.CLOSED) {
         return;
      }

      ClientConnection conn = this.connection;
      if (conn != null && conn.isOpen()) {
         try {
            conn.disconnect(Text.literal(reason));
         } catch (Exception ignored) {
         }
      }

      this.status = Status.CLOSED;
      this.statusText = "Отключён: " + reason;
      MinecraftClient.getInstance().execute(() -> {
         NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal(this.name + " отключён: " + reason), false);
      });
   }

   public void sendChat(String message) {
      ClientConnection conn = this.connection;
      if (conn != null && this.status == Status.PLAYING) {
         ChatMessageC2SPacket packet = new ChatMessageC2SPacket(
            message,
            java.time.Instant.now(),
            0L,
            null,
            new LastSeenMessageList.Acknowledgment(0, new java.util.BitSet(20), LastSeenMessageList.Acknowledgment.NO_CHECKSUM)
         );
         conn.send(packet);
      }
   }

   // ===== обработчики пакетов (вызываются с нетти-потока) =====

   void onGameJoin(net.minecraft.network.packet.s2c.play.GameJoinS2CPacket packet) {
      this.selfEntityId = packet.playerEntityId();
      horus.client.HorusClient.logger().info("[MultiAccount] Alt {} joined server (selfEntityId={})", this.name, this.selfEntityId);
      this.status = Status.PLAYING;
      this.statusText = this.password.isEmpty() ? "На сервере" : "Авторизация на сервере...";
      this.health = -1.0F;
      this.joinedAtMs = System.currentTimeMillis();
      ClientConnection conn = this.connection;
      if (conn != null) {
         conn.send(new PlayerLoadedC2SPacket());
      }

      MinecraftClient.getInstance().execute(() -> {
         NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal(this.name + " зашёл на сервер"), true);
      });

      // авто-вход / регистрация на оффлайн-сервере (у многих серверов свой /login /register)
      if (!this.password.isEmpty() && !this.authSent) {
         this.authSent = true;
         Thread authThread = new Thread(() -> {
            try {
               Thread.sleep(2500L);
               this.sendCommand("register " + this.password + " " + this.password);
               Thread.sleep(1500L);
               this.sendCommand("login " + this.password);
               if (!this.modeCommand.isEmpty()) {
                  Thread.sleep(3000L);
                  this.sendCommand(this.modeCommand);
                  this.statusText = "Вход в режим...";
                  Thread.sleep(4000L);
                  if (this.status == Status.PLAYING || this.status == Status.DEAD) {
                     this.statusText = "На сервере";
                  }
               }
            } catch (InterruptedException ignored) {
            }
         }, "Horus-Alt-Auth-" + this.name);
         authThread.setDaemon(true);
         authThread.start();
      }
   }

   private void sendCommand(String command) {
      ClientConnection conn = this.connection;
      if (conn != null && this.status == Status.PLAYING) {
         conn.send(new net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket(command));
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} sent command /{}", this.name, command.split(" ")[0]);
      }
   }

   void onPositionLook(PlayerPositionLookS2CPacket packet) {
      this.x = packet.change().position().x;
      this.y = packet.change().position().y;
      this.z = packet.change().position().z;
      this.yaw = packet.change().yaw();
      this.pitch = packet.change().pitch();
      this.hasPosition = true;
      ClientConnection conn = this.connection;
      if (conn != null) {
         conn.send(new TeleportConfirmC2SPacket(packet.teleportId()));
      }
   }

   void onHealth(HealthUpdateS2CPacket packet) {
      this.health = packet.getHealth();
      this.food = packet.getFood();
      if (packet.getHealth() <= 0.0F && this.status == Status.PLAYING) {
         this.status = Status.DEAD;
         this.needRespawn = true;
         this.statusText = "Мёртв";
      } else if (packet.getHealth() > 0.0F && this.status == Status.DEAD) {
         this.status = Status.PLAYING;
         this.statusText = "На сервере";
      }
   }

   void onRespawn() {
      if (this.status == Status.DEAD) {
         this.status = Status.PLAYING;
         this.statusText = "На сервере";
         this.hasPosition = false;
         this.needRespawn = false;
      }
   }

   void onDeath(DeathMessageS2CPacket packet) {
      this.needRespawn = true;
   }

   void onWorldTime() {
      // оценка пинга по интервалу пакетов времени (сглаженная)
      long now = System.currentTimeMillis();
      long last = this.lastWorldTimePacket;
      this.lastWorldTimePacket = now;
      if (last > 0L) {
         int delta = (int)Math.min(1000, now - last);
         if (delta > 0) {
            this.pingMs = this.pingMs < 0 ? delta : (this.pingMs * 3 + delta) / 4;
         }
      }
   }

   void onKeepAlive(KeepAliveS2CPacket packet) {
      ClientConnection conn = this.connection;
      if (conn != null) {
         conn.send(new KeepAliveC2SPacket(packet.getId()));
         long latency = System.currentTimeMillis() - packet.getId();
         if (latency > 0L && latency < 10000L) {
            this.pingMs = (int)latency;
         }
      }
   }

   void onCommonPing(CommonPingS2CPacket packet) {
      ClientConnection conn = this.connection;
      if (conn != null) {
         conn.send(new CommonPongC2SPacket(packet.getParameter()));
      }
   }

   void onDisconnectPacket(DisconnectS2CPacket packet) {
      this.close(packet.reason().getString());
   }

   void onGameMessage(net.minecraft.network.packet.s2c.play.GameMessageS2CPacket packet) {
      String msg = packet.content().getString();
      this.lastMessages.addLast(msg);
      while (this.lastMessages.size() > 6) {
         this.lastMessages.pollFirst();
      }

      String low = msg.toLowerCase(java.util.Locale.ROOT);

      // авто-капча отключена по запросу: ответ вводится вручную через панель
      if (this.password.isEmpty()) {
         return;
      }

      // реагируем на ответы серверных авторизаций
      if (low.contains("зарегистр") && (low.contains("уже") || low.contains("уже"))) {
         this.sendCommand("login " + this.password);
      } else if (low.contains("успеш") && (low.contains("регистр") || low.contains("вошл") || low.contains("вход"))) {
         this.statusText = "На сервере";
      } else if (low.contains("неверн") && low.contains("пароль")) {
         this.statusText = "Неверный пароль!";
      } else if (low.contains("/register") || low.contains("/reg")) {
         // сервер просит регистрацию — уже шлём автоматически
      }
   }

   void onTransfer() {
      this.close("Сервер запросил перенос — отклонено");
   }

   void onResourcePack(ResourcePackSendS2CPacket packet) {
      ClientConnection conn = this.connection;
      if (conn != null) {
         conn.send(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
         conn.send(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
      }
   }

   void onCookieRequest(net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket packet) {
      ClientConnection conn = this.connection;
      if (conn != null) {
         conn.send(new net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket(packet.key(), null));
      }
   }

   void onDisconnected(String reason) {
      horus.client.HorusClient.logger().info("[MultiAccount] Alt {} disconnected: {}", this.name, reason);
      if (this.status != Status.CLOSED) {
         this.status = Status.CLOSED;
         this.statusText = "Отключён: " + reason;
         MinecraftClient.getInstance().execute(() -> {
            NotificationHud.notifyWithText("★", "Мультиаккаунт", Text.literal(this.name + " отключён: " + reason), false);
         });
      }
   }

   // ===== состояния входа =====

   private abstract class AltBaseListener implements ClientCommonPacketListener {
      @Override
      public net.minecraft.network.NetworkSide getSide() {
         return net.minecraft.network.NetworkSide.CLIENTBOUND;
      }

      @Override
      public boolean isConnectionOpen() {
         return AltBot.this.isAlive() && AltBot.this.connection != null && AltBot.this.connection.isOpen();
      }

      @Override
      public void onDisconnected(net.minecraft.network.DisconnectionInfo info) {
         AltBot.this.onDisconnected(info.reason().getString());
      }

      @Override
      public void onKeepAlive(KeepAliveS2CPacket packet) {
         AltBot.this.onKeepAlive(packet);
      }

      @Override
      public void onPing(CommonPingS2CPacket packet) {
         AltBot.this.onCommonPing(packet);
      }

      @Override
      public void onDisconnect(DisconnectS2CPacket packet) {
         AltBot.this.onDisconnectPacket(packet);
      }

      @Override
      public void onResourcePackSend(net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket packet) {
         AltBot.this.onResourcePack(packet);
      }

      @Override
      public void onResourcePackRemove(net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket packet) {
      }

      @Override
      public void onSynchronizeTags(net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket packet) {
      }

      @Override
      public void onStoreCookie(net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket packet) {
      }

      @Override
      public void onServerTransfer(net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket packet) {
         AltBot.this.onTransfer();
      }

      @Override
      public void onCustomReportDetails(net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket packet) {
      }

      @Override
      public void onServerLinks(net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket packet) {
      }

      @Override
      public void onClearDialog(net.minecraft.network.packet.s2c.common.ClearDialogS2CPacket packet) {
      }

      @Override
      public void onShowDialog(net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket packet) {
      }

      @Override
      public void onCustomPayload(net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket packet) {
      }

      @Override
      public void onCookieRequest(net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket packet) {
         AltBot.this.onCookieRequest(packet);
      }
   }

   private final class AltLoginListener extends AltBaseListener implements ClientLoginPacketListener {
      @Override
      public NetworkPhase getPhase() {
         return NetworkPhase.LOGIN;
      }

      @Override
      public void onHello(LoginHelloS2CPacket packet) {
         // onHello приходит только когда сервер просит шифрование = онлайн-режим
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} server requested encryption (online-mode)", AltBot.this.name);
         AltBot.this.close("Сервер в онлайн-режиме — вход по нику невозможен");
      }

      @Override
      public void onSuccess(LoginSuccessS2CPacket packet) {
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} login success, entering configuration", AltBot.this.name);
         ClientConnection conn = AltBot.this.connection;
         if (conn == null) {
            return;
         }

         AltBot.this.status = Status.CONFIGURING;
         AltBot.this.statusText = "Настройка...";
         conn.transitionInbound(ConfigurationStates.S2C, new AltConfigListener());
         conn.transitionOutbound(ConfigurationStates.C2S);
         conn.send(new ClientOptionsC2SPacket(SyncedClientOptions.createDefault()));
         conn.send(new CustomPayloadC2SPacket(new BrandCustomPayload("horus")));
      }

      @Override
      public void onCompression(LoginCompressionS2CPacket packet) {
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} compression {}", AltBot.this.name, packet.getCompressionThreshold());
         ClientConnection conn = AltBot.this.connection;
         if (conn != null) {
            conn.setCompressionThreshold(packet.getCompressionThreshold(), true);
         }
      }

      @Override
      public void onDisconnect(LoginDisconnectS2CPacket packet) {
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} login disconnect: {}", AltBot.this.name, packet.reason().getString());
         AltBot.this.close(packet.reason().getString());
      }

      @Override
      public void onQueryRequest(net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket packet) {
         ClientConnection conn = AltBot.this.connection;
         if (conn != null) {
            conn.send(new net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket(packet.queryId(), null));
         }
      }

      @Override
      public void onCookieRequest(net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket packet) {
         ClientConnection conn = AltBot.this.connection;
         if (conn != null) {
            conn.send(new net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket(packet.key(), null));
         }
      }
   }

   private final class AltConfigListener extends AltBaseListener implements ClientConfigurationPacketListener {
      @Override
      public void onSelectKnownPacks(SelectKnownPacksS2CPacket packet) {
         ClientConnection conn = AltBot.this.connection;
         if (conn != null) {
            conn.send(new SelectKnownPacksC2SPacket(List.of()));
         }
      }

      @Override
      public void onCodeOfConduct(CodeOfConductS2CPacket packet) {
         ClientConnection conn = AltBot.this.connection;
         if (conn != null) {
            conn.send(AcceptCodeOfConductC2SPacket.INSTANCE);
         }
      }

      @Override
      public void onReady(ReadyS2CPacket packet) {
         horus.client.HorusClient.logger().info("[MultiAccount] Alt {} config ready -> switching to play", AltBot.this.name);
         ClientConnection conn = AltBot.this.connection;
         if (conn == null) {
            return;
         }

         DynamicRegistryManager registryManager = ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager();
         conn.transitionInbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(registryManager)), new AltPlayListener(AltBot.this));
         conn.send(ReadyC2SPacket.INSTANCE);
         conn.transitionOutbound(PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(registryManager), () -> false));
      }

      @Override
      public void onDynamicRegistries(DynamicRegistriesS2CPacket packet) {
      }

      @Override
      public void onFeatures(FeaturesS2CPacket packet) {
      }

      @Override
      public void onResetChat(ResetChatS2CPacket packet) {
      }

      @Override
      public void onShowDialog(net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket packet) {
      }

      @Override
      public void onCookieRequest(net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket packet) {
         ClientConnection conn = AltBot.this.connection;
         if (conn != null) {
            conn.send(new net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket(packet.key(), null));
         }
      }
   }
}
