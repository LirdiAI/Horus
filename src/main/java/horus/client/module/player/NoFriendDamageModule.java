package horus.client.module.player;

import horus.client.event.EventListener;
import horus.client.event.PacketEvent;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.storage.FriendStore;
import horus.mixin.PlayerInteractEntityC2SPacketMixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(
   name = "NoFriendDamage",
   description = "Защита друзей: удары руками по друзьям не проходят — серверу не отправляется урон.",
   category = ModuleCategory.PLAYER
)
public final class NoFriendDamageModule extends Module {
   private static NoFriendDamageModule noFriendDamageModule;

   private final EventListener<PacketEvent> packetListener = packetEvent -> {
      if (packetEvent.isOutgoing() && packetEvent.getPacket() instanceof PlayerInteractEntityC2SPacket interactPacket) {
         boolean[] isAttack = new boolean[1];
         interactPacket.handle(new PlayerInteractEntityC2SPacket.Handler() {
            @Override
            public void attack() {
               isAttack[0] = true;
            }

            @Override
            public void interact(Hand hand) {
            }

            @Override
            public void interactAt(Hand hand, Vec3d pos) {
            }
         });

         if (!isAttack[0]) {
            return;
         }

         Entity entity = mc.world != null ? mc.world.getEntityById(((PlayerInteractEntityC2SPacketMixin)interactPacket).entityId()) : null;
         if (entity instanceof PlayerEntity player && FriendStore.isFriend(player.getGameProfile().name())) {
            packetEvent.cancel();
         }
      }
   };

   public static NoFriendDamageModule getInstance() {
      if (noFriendDamageModule == null) {
         noFriendDamageModule = new NoFriendDamageModule();
      }

      return noFriendDamageModule;
   }

   private NoFriendDamageModule() {
   }
}
