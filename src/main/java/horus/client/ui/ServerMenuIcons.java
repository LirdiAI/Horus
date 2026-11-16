package horus.client.ui;

import horus.client.HorusClient;
import horus.client.render.ImageRenderer;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

/** Owns the dynamic textures for one menu's server list; accessed on the client thread. */
final class ServerMenuIcons implements AutoCloseable {
   private static final String FALLBACK = "minecraft:textures/misc/unknown_server.png";
   private static final AtomicLong NEXT_ID = new AtomicLong();
   private final Map<ServerInfo, Entry> entries = new IdentityHashMap<>();

   String texture(ServerInfo server) {
      byte[] bytes = server.getFavicon();
      Entry previous = this.entries.get(server);
      if (previous != null && Arrays.equals(previous.bytes(), bytes)) {
         return previous.id() == null ? FALLBACK : previous.id().toString();
      }
      this.release(previous);
      Identifier id = null;
      if (bytes != null) {
         try (NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes))) {
            if (image.getWidth() != 64 || image.getHeight() != 64) {
               throw new IllegalArgumentException("Server icon must be 64x64");
            }
            Identifier textureId = Identifier.of("horus", "menu/server_icon/" + NEXT_ID.incrementAndGet());
            // Copy ownership to the texture; the decoded source is closed by try-with-resources.
            NativeImage copy = new NativeImage(64, 64, false);
            copy.copyFrom(image);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(textureId::toString, copy);
            MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
            id = textureId;
         } catch (Exception exception) {
            HorusClient.logger().warn("[MainMenu] Invalid server icon for {}", server.address, exception);
         }
      }
      this.entries.put(server, new Entry(bytes == null ? null : bytes.clone(), id));
      return id == null ? FALLBACK : id.toString();
   }

   private void release(Entry entry) {
      if (entry != null && entry.id() != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(entry.id());
         ImageRenderer.getImageRenderer().updateState5();
      }
   }

   @Override
   public void close() {
      this.entries.values().forEach(this::release);
      this.entries.clear();
   }

   private record Entry(byte[] bytes, Identifier id) {}
}
