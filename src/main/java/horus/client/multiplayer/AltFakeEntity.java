package horus.client.multiplayer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

/**
 * Минимальный фейк-Entity: нужен только чтобы собрать
 * PlayerInteractEntityC2SPacket.attack(entity, ...) с нужным id.
 */
public final class AltFakeEntity extends Entity {
   private final int fakeId;

   public AltFakeEntity(World world, int fakeId) {
      super(EntityType.PLAYER, world);
      this.fakeId = fakeId;
   }

   @Override
   public int getId() {
      return this.fakeId;
   }

   @Override
   protected void initDataTracker(DataTracker.Builder builder) {
   }

   @Override
   public boolean damage(ServerWorld serverWorld, DamageSource damageSource, float amount) {
      return false;
   }

   @Override
   protected void readCustomData(ReadView readView) {
   }

   @Override
   protected void writeCustomData(WriteView writeView) {
   }
}
