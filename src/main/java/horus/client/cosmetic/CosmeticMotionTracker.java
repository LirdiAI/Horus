package horus.client.cosmetic;

import horus.client.cosmetic.CosmeticCategory;
import horus.client.cosmetic.CosmeticEntry;
import horus.client.cosmetic.CosmeticModel;
import horus.client.cosmetic.CosmeticPlacement;
import horus.client.cosmetic.CosmeticStore;
import horus.client.module.misc.GlobalsModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class CosmeticMotionTracker {
   private static final CosmeticMotionTracker cosmeticMotionTracker = new CosmeticMotionTracker();
   private final HashMap<UUID, CosmeticMotionTracker.MotionHistory> map = new HashMap<>();
   private final HashSet<UUID> set = new HashSet<>();
   private Object object;

   private boolean matchesCriteria(CosmeticMotionTracker.MotionHistory local5517_1, double local5517_2) {
      local5517_1.doubleVal9 = Math.min(0.85, local5517_1.doubleVal9 + 0.08);
      local5517_1.doubleVal2 = Math.max(local5517_2, local5517_1.doubleVal2 - local5517_1.doubleVal9);
      if (local5517_1.doubleVal2 > local5517_2 + 0.001) {
         return true;
      }

      local5517_1.doubleVal2 = local5517_2;
      local5517_1.doubleVal9 = 0.0;
      return false;
   }

   private static Vec3d getVec3d(Vec3d vec3d, float local5519_1, boolean local5519_2) {
      double local5519_3 = Math.toRadians(local5519_1);
      return local5519_2
         ? new Vec3d(vec3d.x + Math.cos(local5519_3 - (Math.PI / 2)) * 1.25, vec3d.y, vec3d.z + Math.sin(local5519_3 - (Math.PI / 2)) * 1.25)
         : new Vec3d(vec3d.x + Math.sin(local5519_3) * 1.75, vec3d.y, vec3d.z - Math.cos(local5519_3) * 1.75);
   }

   public static CosmeticMotionTracker getCosmeticMotionTracker() {
      return cosmeticMotionTracker;
   }

   private double computeDouble(MinecraftClient minecraftClient, Vec3d vec3d, Vec3d vec3d2) {
      HitResult hitResult = minecraftClient.world
         .raycast(new RaycastContext(vec3d, vec3d2, ShapeType.COLLIDER, FluidHandling.NONE, minecraftClient.player));
      if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() != Type.MISS) {
         BlockPos blockPos = blockHitResult.getBlockPos();
         BlockPos blockPos2 = blockPos.up();
         if (minecraftClient.world.getBlockState(blockPos).getCollisionShape(minecraftClient.world, blockPos).isEmpty()) {
            return Double.NaN;
         } else {
            return minecraftClient.world.getBlockState(blockPos2).getCollisionShape(minecraftClient.world, blockPos2).isEmpty()
                  && minecraftClient.world.getBlockState(blockPos2.up()).getCollisionShape(minecraftClient.world, blockPos2.up()).isEmpty()
               ? hitResult.getPos().y
               : Double.NaN;
         }
      } else {
         return Double.NaN;
      }
   }

   private Vec3d getVec3d2(MinecraftClient minecraftClient, double local5522_2, double local5522_4, double local5522_6) {
      for (int index = 0; index <= 4; index++) {
         for (int index2 = -index; index2 <= index; index2++) {
            for (int index3 = -index; index3 <= index; index3++) {
               if (index <= 0 || Math.abs(index2) == index || Math.abs(index3) == index) {
                  double local5522_11 = index == 0 ? local5522_2 : Math.floor(local5522_2) + index2 + 0.5;
                  double local5522_13 = index == 0 ? local5522_4 : Math.floor(local5522_4) + index3 + 0.5;
                  double local5522_15 = this.computeDouble3(minecraftClient, local5522_11, local5522_13, local5522_6);
                  if (!Double.isNaN(local5522_15)) {
                     return new Vec3d(local5522_11, local5522_15, local5522_13);
                  }
               }
            }
         }
      }

      return null;
   }

   public void processData(MinecraftClient minecraftClient) {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         if (this.object != minecraftClient.world) {
            this.map.clear();
            this.object = minecraftClient.world;
         }

         HashSet<UUID> local5523_2 = this.set;
         local5523_2.clear();

         for (AbstractClientPlayerEntity abstractClientPlayerEntity : minecraftClient.world.getPlayers()) {
            CosmeticEntry currentCosmeticEntry = abstractClientPlayerEntity == minecraftClient.player
               ? CosmeticStore.getCosmeticEntry(CosmeticCategory.PET)
               : (
                  !abstractClientPlayerEntity.isSpectator() && !abstractClientPlayerEntity.isInvisible() && GlobalsModule.getInstance().checkState()
                     ? GlobalsModule.getInstance().getCosmeticEntry(abstractClientPlayerEntity.getGameProfile().name(), CosmeticCategory.PET)
                     : null
               );
            if (currentCosmeticEntry != null && currentCosmeticEntry.placement() != null && currentCosmeticEntry.placement().isPet()) {
               local5523_2.add(abstractClientPlayerEntity.getUuid());
               CosmeticMotionTracker.MotionHistory local5523_6 = this.map.get(abstractClientPlayerEntity.getUuid());
               if (local5523_6 == null || local5523_6.cosmeticEntry.id() != currentCosmeticEntry.id()) {
                  Vec3d vec3d = getVec3d(abstractClientPlayerEntity.getEntityPos(), abstractClientPlayerEntity.bodyYaw, currentCosmeticEntry.placement().flying());
                  double local5523_8 = currentCosmeticEntry.placement().flying()
                     ? abstractClientPlayerEntity.getY() + 1.3
                     : this.computeDouble2(minecraftClient, vec3d.x, vec3d.z, abstractClientPlayerEntity.getY());
                  local5523_6 = new CosmeticMotionTracker.MotionHistory(
                     currentCosmeticEntry,
                     abstractClientPlayerEntity.getUuid().getLeastSignificantBits() ^ currentCosmeticEntry.id(),
                     vec3d.x,
                     local5523_8,
                     vec3d.z,
                     abstractClientPlayerEntity.bodyYaw
                  );
                  this.map.put(abstractClientPlayerEntity.getUuid(), local5523_6);
               }

               this.processData2(minecraftClient, abstractClientPlayerEntity, local5523_6);
            }
         }

         this.map.keySet().retainAll(local5523_2);
      } else {
         this.map.clear();
         this.object = null;
      }
   }

   private double computeDouble2(MinecraftClient minecraftClient, double local5524_2, double local5524_4, double local5524_6) {
      double local5524_8 = this.computeDouble3(minecraftClient, local5524_2, local5524_4, local5524_6);
      return Double.isNaN(local5524_8) ? local5524_6 : local5524_8;
   }

   private boolean matchesCriteria2(
      MinecraftClient minecraftClient, CosmeticMotionTracker.MotionHistory local5526_2, Vec3d vec3d, double local5526_4, boolean local5526_6
   ) {
      double local5526_7 = vec3d.x - local5526_2.doubleVal;
      double local5526_9 = vec3d.z - local5526_2.doubleVal3;
      double local5526_11 = Math.sqrt(local5526_7 * local5526_7 + local5526_9 * local5526_9);
      boolean local5526_13 = false;
      if (!local5526_6) {
         double local5526_14 = this.computeDouble4(minecraftClient, local5526_2.doubleVal, local5526_2.doubleVal3, local5526_2.doubleVal2);
         if (!Double.isNaN(local5526_14) && local5526_14 < local5526_2.doubleVal2 - 0.02) {
            local5526_13 = this.matchesCriteria(local5526_2, local5526_14);
         } else if (!Double.isNaN(local5526_14)) {
            local5526_2.doubleVal2 = local5526_14;
            local5526_2.doubleVal9 = 0.0;
         }
      }

      if (local5526_11 > 15.0) {
         if (local5526_6) {
            local5526_2.processData3(vec3d.x, vec3d.y, vec3d.z);
         } else {
            Vec3d vec3d2 = this.getVec3d2(minecraftClient, vec3d.x, vec3d.z, vec3d.y);
            if (vec3d2 != null) {
               local5526_2.processData3(vec3d2.x, vec3d2.y, vec3d2.z);
            }
         }

         return false;
      } else {
         boolean local5526_14 = false;
         if (local5526_11 > 0.12) {
            double local5526_15 = Math.min(local5526_4, local5526_11);
            double local5526_17 = local5526_7 / local5526_11;
            double local5526_19 = local5526_9 / local5526_11;
            if (local5526_6) {
               local5526_2.doubleVal += local5526_17 * local5526_15;
               local5526_2.doubleVal3 += local5526_19 * local5526_15;
               local5526_2.floatVal = local5526_2.floatVal
                  + MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(-local5526_7, local5526_9)) - local5526_2.floatVal) * 0.28F;
               local5526_14 = true;
            } else {
               local5526_14 = this.matchesCriteria3(
                  minecraftClient, local5526_2, local5526_2.doubleVal + local5526_17 * local5526_15, local5526_2.doubleVal3 + local5526_19 * local5526_15
               );
               if (!local5526_14) {
                  local5526_14 = this.matchesCriteria3(
                     minecraftClient,
                     local5526_2,
                     local5526_2.doubleVal - local5526_19 * local5526_15 * local5526_2.doubleVal10,
                     local5526_2.doubleVal3 + local5526_17 * local5526_15 * local5526_2.doubleVal10
                  );
               }

               if (!local5526_14) {
                  local5526_2.doubleVal10 = -local5526_2.doubleVal10;
                  local5526_14 = this.matchesCriteria3(
                     minecraftClient,
                     local5526_2,
                     local5526_2.doubleVal - local5526_19 * local5526_15 * local5526_2.doubleVal10,
                     local5526_2.doubleVal3 + local5526_17 * local5526_15 * local5526_2.doubleVal10
                  );
               }
            }
         }

         if (local5526_6) {
            local5526_2.doubleVal9 = 0.0;
            local5526_2.doubleVal2 = local5526_2.doubleVal2 + MathHelper.clamp(vec3d.y - local5526_2.doubleVal2, -0.32, 0.32);
         }

         if (!local5526_14 && !local5526_13 && local5526_11 > 0.12) {
            local5526_2.intVal = Math.min(local5526_2.intVal, 5);
         }

         return local5526_14 || local5526_13;
      }
   }

   public void renderElement(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, MinecraftClient minecraftClient) {
      if (minecraftClient.player != null && minecraftClient.world != null && matrixStack != null && vertexConsumerProvider != null) {
         float tickProgress = minecraftClient.getRenderTickCounter().getTickProgress(false);
         Vec3d cameraPos = minecraftClient.gameRenderer.getCamera().getCameraPos();

         for (CosmeticMotionTracker.MotionHistory local5527_7 : this.map.values()) {
            double local5527_8 = MathHelper.lerp(tickProgress, local5527_7.doubleVal4, local5527_7.doubleVal) - cameraPos.x;
            double local5527_10 = MathHelper.lerp(tickProgress, local5527_7.doubleVal5, local5527_7.doubleVal2) - cameraPos.y;
            double local5527_12 = MathHelper.lerp(tickProgress, local5527_7.doubleVal6, local5527_7.doubleVal3) - cameraPos.z;
            CosmeticModel cosmeticModel = CosmeticModel.getCosmeticModel(local5527_7.cosmeticEntry);
            if (cosmeticModel != null) {
               CosmeticPlacement cosmeticPlacement = local5527_7.cosmeticEntry.placement();
               float local5527_16 = local5527_7.floatVal2 + MathHelper.wrapDegrees(local5527_7.floatVal - local5527_7.floatVal2) * tickProgress;
               float local5527_17 = Math.max(0.05F, cosmeticPlacement.scale());
               matrixStack.push();
               matrixStack.translate(local5527_8, local5527_10, local5527_12);
               matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - local5527_16));
               matrixStack.translate(cosmeticPlacement.x(), cosmeticPlacement.y(), cosmeticPlacement.z());
               matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cosmeticPlacement.rotationX()));
               matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cosmeticPlacement.rotationY()));
               matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(cosmeticPlacement.rotationZ()));
               matrixStack.scale(local5527_17, local5527_17, local5527_17);
               cosmeticModel.renderElement6(
                  matrixStack,
                  vertexConsumerProvider,
                  15728880,
                  (local5527_7.intVal2 + tickProgress) / 20.0F,
                  MathHelper.lerp(tickProgress, local5527_7.floatVal5, local5527_7.floatVal4)
               );
               matrixStack.pop();
            }
         }
      }
   }

   private double computeDouble3(MinecraftClient minecraftClient, double local5528_2, double local5528_4, double local5528_6) {
      return this.computeDouble(minecraftClient, new Vec3d(local5528_2, local5528_6 + 4.0, local5528_4), new Vec3d(local5528_2, local5528_6 - 12.0, local5528_4));
   }

   private static double getValue(Vec3d vec3d, Vec3d vec3d2) {
      double local5529_2 = vec3d.x - vec3d2.x;
      double local5529_4 = vec3d.z - vec3d2.z;
      return local5529_2 * local5529_2 + local5529_4 * local5529_4;
   }

   private boolean matchesCriteria3(MinecraftClient minecraftClient, CosmeticMotionTracker.MotionHistory local5530_2, double local5530_3, double local5530_5) {
      double local5530_7 = this.computeDouble3(minecraftClient, local5530_3, local5530_5, local5530_2.doubleVal2);
      if (Double.isNaN(local5530_7) || local5530_7 < local5530_2.doubleVal2 - 1.25) {
         local5530_7 = this.computeDouble4(minecraftClient, local5530_3, local5530_5, local5530_2.doubleVal2);
      }

      if (!Double.isNaN(local5530_7) && !(local5530_7 > local5530_2.doubleVal2 + 1.25)) {
         double local5530_9 = local5530_3 - local5530_2.doubleVal;
         double local5530_11 = local5530_5 - local5530_2.doubleVal3;
         local5530_2.doubleVal = local5530_3;
         local5530_2.doubleVal3 = local5530_5;
         if (local5530_7 < local5530_2.doubleVal2 - 1.25) {
            this.matchesCriteria(local5530_2, local5530_7);
         } else {
            local5530_2.doubleVal2 = local5530_7;
            local5530_2.doubleVal9 = 0.0;
         }

         local5530_2.floatVal = local5530_2.floatVal
            + MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(-local5530_9, local5530_11)) - local5530_2.floatVal) * 0.28F;
         return true;
      } else {
         return false;
      }
   }

   private double computeDouble4(MinecraftClient minecraftClient, double local5531_2, double local5531_4, double local5531_6) {
      return this.computeDouble(minecraftClient, new Vec3d(local5531_2, local5531_6 + 0.35, local5531_4), new Vec3d(local5531_2, local5531_6 - 64.0, local5531_4));
   }

   private Vec3d getVec3d3(CosmeticMotionTracker.MotionHistory local5532_1, Vec3d vec3d, double local5532_3, double local5532_5) {
      if (--local5532_1.intVal <= 0
         || getValue(new Vec3d(local5532_1.doubleVal7, vec3d.y, local5532_1.doubleVal8), vec3d) > local5532_5 * local5532_5) {
         double local5532_7 = local5532_1.random.nextDouble() * Math.PI * 2.0;
         double local5532_9 = local5532_3 + local5532_1.random.nextDouble() * (local5532_5 - local5532_3);
         local5532_1.doubleVal7 = vec3d.x + Math.cos(local5532_7) * local5532_9;
         local5532_1.doubleVal8 = vec3d.z + Math.sin(local5532_7) * local5532_9;
         local5532_1.intVal = 45 + local5532_1.random.nextInt(90);
      }

      return new Vec3d(local5532_1.doubleVal7, vec3d.y, local5532_1.doubleVal8);
   }

   private void processData2(MinecraftClient minecraftClient, AbstractClientPlayerEntity abstractClientPlayerEntity, CosmeticMotionTracker.MotionHistory local5533_3) {
      boolean local5533_4 = local5533_3.cosmeticEntry.placement().flying();
      Vec3d vec3d = abstractClientPlayerEntity.getEntityPos();
      boolean local5533_6 = local5533_3.vec3d4 != null && getValue(vec3d, local5533_3.vec3d4) > 4.0E-4;
      local5533_3.vec3d4 = vec3d;
      if (local5533_6) {
         local5533_3.floatVal3 = abstractClientPlayerEntity.bodyYaw;
      }

      Vec3d vec3d2 = getVec3d(vec3d, local5533_3.floatVal3, local5533_4);
      if (local5533_4) {
         vec3d2 = new Vec3d(vec3d2.x, vec3d.y + 1.3, vec3d2.z);
      }

      local5533_3.doubleVal4 = local5533_3.doubleVal;
      local5533_3.doubleVal5 = local5533_3.doubleVal2;
      local5533_3.doubleVal6 = local5533_3.doubleVal3;
      local5533_3.floatVal2 = local5533_3.floatVal;
      local5533_3.floatVal5 = local5533_3.floatVal4;
      local5533_3.intVal2++;
      double local5533_8 = getValue(new Vec3d(local5533_3.doubleVal, local5533_3.doubleVal2, local5533_3.doubleVal3), vec3d);
      double local5533_10 = local5533_4 ? 4.0 : 7.0;
      double local5533_12 = local5533_4 ? 3.5 : 6.0;
      if (local5533_8 > local5533_10 * local5533_10) {
         local5533_3.flag = true;
      } else if (local5533_8 < local5533_12 * local5533_12) {
         local5533_3.flag = false;
      }

      Vec3d vec3d3 = local5533_3.flag ? vec3d2 : this.getVec3d3(local5533_3, vec3d, local5533_4 ? 3.0 : 1.4, local5533_4 ? 4.0 : 6.5);
      if (local5533_4) {
         vec3d3 = new Vec3d(vec3d3.x, vec3d.y + 1.3, vec3d3.z);
      }

      boolean local5533_15 = this.matchesCriteria2(minecraftClient, local5533_3, vec3d3, local5533_3.flag ? 0.22 : 0.075, local5533_4);
      local5533_3.floatVal4 = local5533_3.floatVal4 + ((local5533_15 ? 1.0F : 0.0F) - local5533_3.floatVal4) * 0.25F;
   }

   private static final class MotionHistory {
      private final CosmeticEntry cosmeticEntry;
      private final Random random;
      private double doubleVal;
      private double doubleVal2;
      private double doubleVal3;
      private double doubleVal4;
      private double doubleVal5;
      private double doubleVal6;
      private double doubleVal7;
      private double doubleVal8;
      private double doubleVal9;
      private double doubleVal10;
      private float floatVal;
      private float floatVal2;
      private float floatVal3;
      private float floatVal4;
      private float floatVal5;
      private int intVal;
      private int intVal2;
      private int intVal3;
      private final int intVal4;
      private double doubleVal11;
      private double doubleVal12;
      private double doubleVal13;
      private double doubleVal14;
      private boolean flag;
      private boolean flag2;
      private Vec3d vec3d4;

      private void processData3(double local5534_1, double local5534_3, double local5534_5) {
         this.doubleVal = this.doubleVal4 = this.doubleVal7 = local5534_1;
         this.doubleVal2 = this.doubleVal5 = local5534_3;
         this.doubleVal3 = this.doubleVal6 = this.doubleVal8 = local5534_5;
         this.doubleVal9 = 0.0;
      }

      private void updateState() {
         this.doubleVal4 = this.doubleVal;
         this.doubleVal5 = this.doubleVal2;
         this.doubleVal6 = this.doubleVal3;
         this.floatVal2 = this.floatVal;
         this.floatVal5 = this.floatVal4;
      }

      private MotionHistory(CosmeticEntry initialCosmeticEntry, long local5536_2, double local5536_4, double local5536_6, double local5536_8, float local5536_10) {
         this.cosmeticEntry = initialCosmeticEntry;
         this.random = new Random(local5536_2);
         this.intVal4 = (int)(local5536_2 & 2147483647L) % 4;
         this.doubleVal = this.doubleVal4 = this.doubleVal7 = local5536_4;
         this.doubleVal2 = this.doubleVal5 = local5536_6;
         this.doubleVal3 = this.doubleVal6 = this.doubleVal8 = local5536_8;
         this.floatVal = this.floatVal2 = this.floatVal3 = local5536_10;
         this.doubleVal10 = this.random.nextBoolean() ? 1.0 : -1.0;
      }
   }
}
