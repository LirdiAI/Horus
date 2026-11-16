package horus.client.module.movement;

import horus.client.util.PlayerUtils;

import horus.mixin.EntityMixin2;
import horus.mixin.LivingEntityMixin;
import horus.client.MinecraftAccess;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.shape.VoxelShape;

public class SimulatedPlayerInput implements SimulatedMovementAccess, MinecraftAccess {
   public final PlayerEntity playerEntity2;
   public final SimulatedPlayerInput.SimulatedPlayerInputState nested0387;
   public Vec3d vec3d7;
   public Vec3d vec3d8;
   public Box box2;
   public float floatVal;
   public float floatVal2;
   public boolean flag;
   public float floatVal3;
   public int intVal;
   public boolean flag2;
   public boolean flag3;
   public boolean flag4;
   public boolean flag5;
   public boolean flag6;
   public boolean flag7;
   public boolean flag8;
   public boolean flag9;
   private final Object2DoubleMap<TagKey<Fluid>> map;
   private final HashSet<TagKey<Fluid>> set2;
   private int intVal2 = 0;
   private boolean flag10 = false;

   private Vec3d getVec3d2(Vec3d vec3d) {
      Box box = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).offset(this.vec3d7);
      List<VoxelShape> local2188_3 = Collections.emptyList();
      Vec3d vec3d2;
      if (vec3d.lengthSquared() == 0.0) {
         vec3d2 = vec3d;
      } else {
         vec3d2 = Entity.adjustMovementForCollisions(this.playerEntity2, vec3d, box, this.playerEntity2.getEntityWorld(), local2188_3);
      }

      boolean local2188_5 = vec3d.x != vec3d2.x;
      boolean local2188_6 = vec3d.y != vec3d2.y;
      boolean local2188_7 = vec3d.z != vec3d2.z;
      boolean local2188_8 = this.flag4 || local2188_6 && vec3d.y < 0.0;
      if (this.playerEntity2.getStepHeight() > 0.0F && local2188_8 && (local2188_5 || local2188_7)) {
         Vec3d vec3d3 = Entity.adjustMovementForCollisions(
            this.playerEntity2,
            new Vec3d(vec3d.x, this.playerEntity2.getStepHeight(), vec3d.z),
            box,
            this.playerEntity2.getEntityWorld(),
            local2188_3
         );
         Vec3d vec3d4 = Entity.adjustMovementForCollisions(
            this.playerEntity2,
            new Vec3d(0.0, this.playerEntity2.getStepHeight(), 0.0),
            box.stretch(vec3d.x, 0.0, vec3d.z),
            this.playerEntity2.getEntityWorld(),
            local2188_3
         );
         Vec3d vec3d5 = Entity.adjustMovementForCollisions(
               this.playerEntity2, new Vec3d(vec3d.x, 0.0, vec3d.z), box.offset(vec3d4), this.playerEntity2.getEntityWorld(), local2188_3
            )
            .add(vec3d4);
         if (vec3d4.y < this.playerEntity2.getStepHeight() && vec3d5.horizontalLengthSquared() > vec3d3.horizontalLengthSquared()) {
            vec3d3 = vec3d5;
         }

         if (vec3d3.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
            return vec3d3.add(
               Entity.adjustMovementForCollisions(
                  this.playerEntity2,
                  new Vec3d(0.0, -vec3d3.y + vec3d.y, 0.0),
                  box.offset(vec3d3),
                  this.playerEntity2.getEntityWorld(),
                  local2188_3
               )
            );
         }
      }

      return vec3d2;
   }

   private void processData(Vec3d vec3d) {
      if (this.flag8 && !this.playerEntity2.hasVehicle()) {
         double local2189_2 = this.getVec3d7().y;
         double local2189_4 = local2189_2 < -0.2 ? 0.085 : 0.06;
         BlockPos blockPos = new BlockPos(
            MathHelper.floor(this.vec3d7.x), MathHelper.floor(this.vec3d7.y + 1.0 - 0.1), MathHelper.floor(this.vec3d7.z)
         );
         if (local2189_2 <= 0.0 || this.nested0387.playerInput2.jump() || !this.playerEntity2.getEntityWorld().getBlockState(blockPos).getFluidState().isEmpty()) {
            this.vec3d8 = this.vec3d8.add(0.0, (local2189_2 - this.vec3d8.y) * local2189_4, 0.0);
         }
      }

      double local2189_2 = this.vec3d8.y;
      double local2189_4 = 0.08;
      boolean local2189_6 = this.vec3d8.y <= 0.0;
      if (this.vec3d8.y <= 0.0 && this.matchesCriteria4(StatusEffects.SLOW_FALLING)) {
         local2189_4 = 0.01;
         this.updateState4();
      }

      if (this.checkState6() && this.playerEntity2.shouldSwimInFluids()) {
         double local2189_7 = this.vec3d7.y;
         float local2189_9 = this.checkState2() ? 0.9F : 0.8F;
         float local2189_10 = 0.02F;
         float local2189_11 = (float)this.computeDouble2(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
         if (!this.flag4) {
            local2189_11 *= 0.5F;
         }

         if (local2189_11 > 0.0F) {
            local2189_9 += (0.54600006F - local2189_9) * local2189_11 / 3.0F;
            local2189_10 += (this.computeFloat5() - local2189_10) * local2189_11 / 3.0F;
         }

         if (this.matchesCriteria4(StatusEffects.DOLPHINS_GRACE)) {
            local2189_9 = 0.96F;
         }

         this.processData2(local2189_10, vec3d);
         this.processData3(this.vec3d8);
         Vec3d vec3d2 = this.vec3d8;
         if (this.flag5 && this.checkState3()) {
            vec3d2 = new Vec3d(vec3d2.x, 0.2, vec3d2.z);
         }

         this.vec3d8 = vec3d2.multiply(local2189_9, 0.8, local2189_9);
         Vec3d vec3d3 = this.playerEntity2.applyFluidMovingSpeed(local2189_4, local2189_6, this.vec3d8);
         this.vec3d8 = vec3d3;
         if (this.flag5 && this.matchesCriteria2(vec3d3.x, vec3d3.y + 0.6 - this.vec3d7.y + local2189_7, vec3d3.z)) {
            this.vec3d8 = new Vec3d(vec3d3.x, 0.3, vec3d3.z);
         }
      } else if (this.checkState() && this.playerEntity2.shouldSwimInFluids()) {
         double local2189_7 = this.vec3d7.y;
         this.processData2(0.02F, vec3d);
         this.processData3(this.vec3d8);
         if (this.computeDouble3(FluidTags.LAVA) <= this.computeDouble()) {
            this.vec3d8 = this.vec3d8.multiply(0.5, 0.8, 0.5);
            this.vec3d8 = this.playerEntity2.applyFluidMovingSpeed(local2189_4, local2189_6, this.vec3d8);
         } else {
            this.vec3d8 = this.vec3d8.multiply(0.5);
         }

         if (!this.playerEntity2.hasNoGravity()) {
            this.vec3d8 = this.vec3d8.add(0.0, -local2189_4 / 4.0, 0.0);
         }

         if (this.flag5 && this.matchesCriteria2(this.vec3d8.x, this.vec3d8.y + 0.6 - this.vec3d7.y + local2189_7, this.vec3d8.z)) {
            this.vec3d8 = new Vec3d(this.vec3d8.x, 0.3, this.vec3d8.z);
         }
      } else if (this.flag3) {
         Vec3d vec3d4 = this.vec3d8;
         if (vec3d4.y > -0.5) {
            this.floatVal3 = 1.0F;
         }

         Vec3d vec3d5 = this.getVec3d7();
         float local2189_11 = this.floatVal2 * (float) (Math.PI / 180.0);
         double local2189_12 = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
         double local2189_14 = this.vec3d8.horizontalLength();
         double local2189_16 = vec3d5.length();
         float local2189_18 = MathHelper.cos(local2189_11);
         local2189_18 = (float)(local2189_18 * (local2189_18 * Math.min(1.0, local2189_16 / 0.4)));
         vec3d4 = this.vec3d8.add(0.0, local2189_4 * (-1.0 + local2189_18 * 0.75), 0.0);
         if (vec3d4.y < 0.0 && local2189_12 > 0.0) {
            double local2189_7 = vec3d4.y * -0.1 * local2189_18;
            vec3d4 = vec3d4.add(vec3d5.x * local2189_7 / local2189_12, local2189_7, vec3d5.z * local2189_7 / local2189_12);
         }

         if (local2189_11 < 0.0F && local2189_12 > 0.0) {
            double local2189_7 = local2189_14 * -MathHelper.sin(local2189_11) * 0.04;
            vec3d4 = vec3d4.add(-vec3d5.x * local2189_7 / local2189_12, local2189_7 * 3.2, -vec3d5.z * local2189_7 / local2189_12);
         }

         if (local2189_12 > 0.0) {
            vec3d4 = vec3d4.add(
               (vec3d5.x / local2189_12 * local2189_14 - vec3d4.x) * 0.1, 0.0, (vec3d5.z / local2189_12 * local2189_14 - vec3d4.z) * 0.1
            );
         }

         this.vec3d8 = vec3d4.multiply(0.99, 0.98, 0.99);
         this.processData3(this.vec3d8);
      } else {
         BlockPos blockPos2 = this.getBlockPos();
         float slipperiness = this.playerEntity2.getEntityWorld().getBlockState(blockPos2).getBlock().getSlipperiness();
         float local2189_9 = this.flag4 ? slipperiness * 0.91F : 0.91F;
         Vec3d vec3d6 = this.getVec3d3(vec3d, slipperiness);
         double local2189_11 = vec3d6.y;
         if (this.matchesCriteria4(StatusEffects.LEVITATION)) {
            StatusEffectInstance statusEffectInstance = this.getStatusEffectInstance(StatusEffects.LEVITATION);
            if (statusEffectInstance != null) {
               local2189_11 += (0.05 * (statusEffectInstance.getAmplifier() + 1) - vec3d6.y) * 0.2;
            }
         } else if (this.playerEntity2.getEntityWorld().isClient() && !this.playerEntity2.getEntityWorld().isChunkLoaded(blockPos2)) {
            local2189_11 = this.vec3d7.y > this.playerEntity2.getEntityWorld().getBottomY() ? -0.1 : 0.0;
         } else if (!this.playerEntity2.hasNoGravity()) {
            local2189_11 -= local2189_4;
         }

         if (this.playerEntity2.hasNoDrag()) {
            this.vec3d8 = new Vec3d(vec3d6.x, local2189_11, vec3d6.z);
         } else {
            this.vec3d8 = new Vec3d(vec3d6.x * local2189_9, local2189_11 * 0.98F, vec3d6.z * local2189_9);
         }
      }

      if (this.playerEntity2.getAbilities().flying && !this.playerEntity2.hasVehicle()) {
         this.vec3d8 = new Vec3d(this.vec3d8.x, local2189_2 * 0.6, this.vec3d8.z);
         this.updateState4();
      }
   }

   public SimulatedPlayerInput(
      PlayerEntity playerEntity,
      SimulatedPlayerInput.SimulatedPlayerInputState local2190_2,
      Vec3d vec3d,
      Vec3d vec3d2,
      Box box,
      float local2190_6,
      float local2190_7,
      boolean local2190_8,
      float local2190_9,
      int local2190_10,
      boolean local2190_11,
      boolean local2190_12,
      boolean local2190_13,
      boolean local2190_14,
      boolean local2190_15,
      boolean local2190_16,
      boolean local2190_17,
      boolean local2190_18,
      Object2DoubleMap<TagKey<Fluid>> local2190_19,
      HashSet<TagKey<Fluid>> local2190_20
   ) {
      this.playerEntity2 = playerEntity;
      this.nested0387 = local2190_2;
      this.vec3d7 = vec3d;
      this.vec3d8 = vec3d2;
      this.box2 = box;
      this.floatVal = local2190_6;
      this.floatVal2 = local2190_7;
      this.flag = local2190_8;
      this.floatVal3 = local2190_9;
      this.intVal = local2190_10;
      this.flag2 = local2190_11;
      this.flag3 = local2190_12;
      this.flag4 = local2190_13;
      this.flag5 = local2190_14;
      this.flag6 = local2190_15;
      this.flag7 = local2190_16;
      this.flag8 = local2190_17;
      this.flag9 = local2190_18;
      this.map = local2190_19;
      this.set2 = local2190_20;
   }

   private void processData2(float local2191_1, Vec3d vec3d) {
      if (!(vec3d.lengthSquared() < 1.0E-7)) {
         Vec3d vec3d2 = (vec3d.lengthSquared() > 1.0 ? vec3d.normalize() : vec3d).multiply(local2191_1);
         this.vec3d8 = this.vec3d8
            .add(
               vec3d2.x * MathHelper.cos(this.floatVal * (float) (Math.PI / 180.0))
                  - vec3d2.z * MathHelper.sin(this.floatVal * (float) (Math.PI / 180.0)),
               vec3d2.y,
               vec3d2.z * MathHelper.cos(this.floatVal * (float) (Math.PI / 180.0))
                  + vec3d2.x * MathHelper.sin(this.floatVal * (float) (Math.PI / 180.0))
            );
      }
   }

   private StatusEffectInstance getStatusEffectInstance(RegistryEntry<StatusEffect> local2192_1) {
      StatusEffectInstance statusEffect = this.playerEntity2.getStatusEffect(local2192_1);
      return statusEffect != null && statusEffect.getDuration() >= this.intVal2 ? statusEffect : null;
   }

   private void processData3(Vec3d vec3d) {
      Vec3d vec3d2 = vec3d;
      vec3d2 = this.getVec3d4(vec3d2);
      Vec3d vec3d3 = this.getVec3d2(vec3d2);
      if (vec3d3.lengthSquared() > 1.0E-7) {
         this.vec3d7 = this.vec3d7.add(vec3d3);
         this.box2 = this.playerEntity2.getDimensions(this.playerEntity2.getPose()).getBoxAt(this.vec3d7);
      }

      boolean local2193_4 = !MathHelper.approximatelyEquals(vec3d.x, vec3d3.x);
      boolean local2193_5 = !MathHelper.approximatelyEquals(vec3d.z, vec3d3.z);
      this.flag5 = local2193_4 || local2193_5;
      this.flag6 = vec3d.y != vec3d3.y;
      this.flag4 = this.flag6 && vec3d.y < 0.0;
      if (!this.checkState6()) {
         this.updateState3();
      }

      if (this.flag4) {
         this.updateState4();
      } else if (vec3d.y < 0.0) {
         this.floatVal3 = this.floatVal3 - (float)vec3d.y;
      }

      Vec3d vec3d4 = this.vec3d8;
      if (this.flag5 || this.flag6) {
         this.vec3d8 = new Vec3d(local2193_4 ? 0.0 : vec3d4.x, this.flag4 ? 0.0 : vec3d4.y, local2193_5 ? 0.0 : vec3d4.z);
      }
   }

   private Vec3d getVec3d3(Vec3d vec3d, float local2194_2) {
      this.processData2(this.computeFloat4(local2194_2), vec3d);
      this.vec3d8 = this.getVec3d6(this.vec3d8);
      this.processData3(this.vec3d8);
      Vec3d vec3d2 = this.vec3d8;
      BlockPos blockPos = this.getBlockPos2(this.vec3d7);
      BlockState blockState = this.getBlockState2(blockPos);
      if ((this.flag5 || this.flag2)
         && (this.checkState3() || blockState != null && blockState.isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this.playerEntity2))) {
         vec3d2 = new Vec3d(vec3d2.x, 0.2, vec3d2.z);
      }

      return vec3d2;
   }

   private float computeFloat() {
      return 0.42F * this.computeFloat2() + this.computeFloat3();
   }

   public static SimulatedPlayerInput getSimulatedPlayerInput(int local2196_0) {
      SimulatedPlayerInput simulatedPlayerInput = getSimulatedPlayerInput4(SimulatedPlayerInput.SimulatedPlayerInputState.fromClientPlayer(mc.player.input.playerInput));

      for (int index = 0; index < local2196_0; index++) {
         simulatedPlayerInput.updateState();
      }

      return simulatedPlayerInput;
   }

   private Vec3d getVec3d4(Vec3d vec3d) {
      if (vec3d.y <= 0.0 && this.checkState5()) {
         double local2197_2 = vec3d.x;
         double local2197_4 = vec3d.z;

         double local2197_6;
         for (local2197_6 = 0.05;
            local2197_2 != 0.0 && this.playerEntity2.getEntityWorld().isSpaceEmpty(this.playerEntity2, this.box2.offset(local2197_2, -0.5, 0.0));
            local2197_2 += local2197_2 > 0.0 ? -local2197_6 : local2197_6
         ) {
            if (local2197_2 < local2197_6 && local2197_2 >= -local2197_6) {
               local2197_2 = 0.0;
               break;
            }
         }

         while (local2197_4 != 0.0 && this.playerEntity2.getEntityWorld().isSpaceEmpty(this.playerEntity2, this.box2.offset(0.0, -0.5, local2197_4))) {
            if (local2197_4 < local2197_6 && local2197_4 >= -local2197_6) {
               local2197_4 = 0.0;
               break;
            }

            local2197_4 += local2197_4 > 0.0 ? -local2197_6 : local2197_6;
         }

         while (
            local2197_2 != 0.0
               && local2197_4 != 0.0
               && this.playerEntity2.getEntityWorld().isSpaceEmpty(this.playerEntity2, this.box2.offset(local2197_2, -0.5, local2197_4))
         ) {
            local2197_2 = local2197_2 < local2197_6 && local2197_2 >= -local2197_6
               ? 0.0
               : (local2197_2 > 0.0 ? local2197_2 - local2197_6 : local2197_2 + local2197_6);
            if (local2197_4 < local2197_6 && local2197_4 >= -local2197_6) {
               local2197_4 = 0.0;
               break;
            }

            local2197_4 += local2197_4 > 0.0 ? -local2197_6 : local2197_6;
         }

         if (vec3d.x != local2197_2 || vec3d.z != local2197_4) {
            this.flag10 = true;
         }

         if (this.checkState7()) {
            vec3d = new Vec3d(local2197_2, vec3d.y, local2197_4);
         }
      }

      return vec3d;
   }

   private boolean matchesCriteria(TagKey<Fluid> local2198_1, double local2198_2) {
      if (this.checkState4()) {
         return false;
      }

      Box box = this.box2.contract(0.001);
      int local2198_5 = MathHelper.floor(box.minX);
      int local2198_6 = MathHelper.ceil(box.maxX);
      int local2198_7 = MathHelper.floor(box.minY);
      int local2198_8 = MathHelper.ceil(box.maxY);
      int local2198_9 = MathHelper.floor(box.minZ);
      int local2198_10 = MathHelper.ceil(box.maxZ);
      double local2198_11 = 0.0;
      boolean local2198_13 = true;
      boolean local2198_14 = false;
      Vec3d vec3d = Vec3d.ZERO;
      int local2198_16 = 0;
      Mutable mutable = new Mutable();

      for (int index = local2198_5; index < local2198_6; index++) {
         for (int index2 = local2198_7; index2 < local2198_8; index2++) {
            for (int index3 = local2198_9; index3 < local2198_10; index3++) {
               mutable.set(index, index2, index3);
               FluidState fluidState = this.playerEntity2.getEntityWorld().getFluidState(mutable);
               if (fluidState.isIn(local2198_1)) {
                  double local2198_22 = index2 + fluidState.getHeight(this.playerEntity2.getEntityWorld(), mutable);
                  if (local2198_22 >= box.minY) {
                     local2198_14 = true;
                     local2198_11 = Math.max(local2198_22 - box.minY, local2198_11);
                     if (local2198_13) {
                        Vec3d vec3d2 = fluidState.getVelocity(this.playerEntity2.getEntityWorld(), mutable);
                        if (local2198_11 < 0.4) {
                           vec3d2 = vec3d2.multiply(local2198_11);
                        }

                        vec3d = vec3d.add(vec3d2);
                        local2198_16++;
                     }
                  }
               }
            }
         }
      }

      if (vec3d.length() > 0.0) {
         if (local2198_16 > 0) {
            vec3d = vec3d.multiply(1.0 / local2198_16);
         }

         vec3d = vec3d.multiply(local2198_2);
         if (Math.abs(this.vec3d8.x) < 0.003 && Math.abs(this.vec3d8.z) < 0.003 && vec3d.length() < 0.0045) {
            vec3d = vec3d.normalize().multiply(0.0045);
         }

         this.vec3d8 = this.vec3d8.add(vec3d);
      }

      this.map.put(local2198_1, local2198_11);
      return local2198_14;
   }

   public boolean checkState() {
      return this.map.getDouble(FluidTags.LAVA) > 0.0;
   }

   private BlockPos getBlockPos() {
      return BlockPos.ofFloored(this.vec3d7.x, this.box2.minY - 0.5000001, this.vec3d7.z);
   }

   private void updateState2() {
      this.flag9 = this.set2.contains(FluidTags.WATER);
      this.set2.clear();
      double local2201_1 = this.computeDouble4() - 0.11111111F;
      if (!(
         this.playerEntity2.getVehicle() instanceof AbstractBoatEntity abstractBoatEntity
            && !abstractBoatEntity.isSubmergedInWater()
            && abstractBoatEntity.getBoundingBox().maxY >= local2201_1
            && abstractBoatEntity.getBoundingBox().minY <= local2201_1
      )) {
         BlockPos blockPos = BlockPos.ofFloored(this.vec3d7.x, local2201_1, this.vec3d7.z);
         FluidState fluidState = this.playerEntity2.getEntityWorld().getFluidState(blockPos);
         double local2201_5 = blockPos.getY() + fluidState.getHeight(this.playerEntity2.getEntityWorld(), blockPos);
         if (local2201_5 > local2201_1) {
            this.set2.addAll(fluidState.streamTags().toList());
         }
      }
   }

   public static SimulatedPlayerInput getSimulatedPlayerInput2(PlayerEntity playerEntity, int local2202_1) {
      SimulatedPlayerInput simulatedPlayerInput = getSimulatedPlayerInput3(playerEntity, SimulatedPlayerInput.SimulatedPlayerInputState.guessInput(playerEntity));

      for (int index = 0; index < local2202_1; index++) {
         simulatedPlayerInput.updateState();
      }

      return simulatedPlayerInput;
   }

   @Override
   public Vec3d getVec3d() {
      return this.playerEntity2.getEntityPos();
   }

   private boolean matchesCriteria2(double local2204_1, double local2204_3, double local2204_5) {
      return this.matchesCriteria5(this.box2.offset(local2204_1, local2204_3, local2204_5));
   }

   private boolean matchesCriteria3(BlockPos blockPos, BlockState blockState) {
      if (!blockState.get(TrapdoorBlock.OPEN)) {
         return false;
      }

      BlockState blockState2 = this.playerEntity2.getEntityWorld().getBlockState(blockPos.down());
      return blockState2.isOf(Blocks.LADDER) && blockState2.get(LadderBlock.FACING).equals(blockState.get(TrapdoorBlock.FACING));
   }

   private void processData4(TagKey<Fluid> local2206_1) {
      this.vec3d8 = this.vec3d8.add(0.0, 0.04F, 0.0);
   }

   private float computeFloat2() {
      float local2207_1 = 0.0F;
      Block block = this.getBlockState2(this.getBlockPos2(this.vec3d7)).getBlock();
      if (block != null) {
         local2207_1 = block.getJumpVelocityMultiplier();
      }

      float local2207_3 = 0.0F;
      Block block2 = this.getBlockState2(this.getBlockPos()).getBlock();
      if (block2 != null) {
         local2207_3 = block2.getJumpVelocityMultiplier();
      }

      return local2207_1 == 1.0F ? local2207_3 : local2207_1;
   }

   private Vec3d getVec3d5(float local2208_1, float local2208_2) {
      float local2208_3 = (float)(local2208_1 * Math.PI / 180.0);
      float local2208_4 = (float)(-local2208_2 * Math.PI / 180.0);
      float local2208_5 = MathHelper.cos(local2208_4);
      float local2208_6 = MathHelper.sin(local2208_4);
      float local2208_7 = MathHelper.cos(local2208_3);
      float local2208_8 = MathHelper.sin(local2208_3);
      return new Vec3d(local2208_6 * local2208_7, -local2208_8, local2208_5 * local2208_7);
   }

   public BlockState getBlockState2(BlockPos blockPos) {
      return this.playerEntity2.getEntityWorld().getBlockState(blockPos);
   }

   public BlockPos getBlockPos2(Vec3d vec3d) {
      return new BlockPos(MathHelper.floor(vec3d.x), MathHelper.floor(vec3d.y), MathHelper.floor(vec3d.z));
   }

   private double computeDouble() {
      return this.playerEntity2.getStandingEyeHeight() < 0.4 ? 0.0 : 0.4;
   }

   private float computeFloat3() {
      if (this.matchesCriteria4(StatusEffects.JUMP_BOOST)) {
         StatusEffectInstance statusEffectInstance = this.getStatusEffectInstance(StatusEffects.JUMP_BOOST);
         return 0.1F * (statusEffectInstance.getAmplifier() + 1);
      } else {
         return 0.0F;
      }
   }

   private void updateState3() {
      if (this.playerEntity2.getVehicle() instanceof AbstractBoatEntity abstractBoatEntity && !abstractBoatEntity.isSubmergedInWater()) {
         this.flag7 = false;
      } else {
         if (this.matchesCriteria(FluidTags.WATER, 0.014)) {
            this.updateState4();
            this.flag7 = true;
         } else {
            this.flag7 = false;
         }
      }
   }

   @Override
   public void updateState() {
      this.intVal2++;
      this.flag10 = false;
      if (!(this.vec3d7.y <= -70.0)) {
         this.nested0387.update();
         this.updateState3();
         this.updateState2();
         this.updateState5();
         if (this.intVal > 0) {
            this.intVal--;
         }

         this.flag2 = this.nested0387.playerInput2.jump();
         double local2214_1 = this.vec3d8.x;
         double local2214_3 = this.vec3d8.y;
         double local2214_5 = this.vec3d8.z;
         if (Math.abs(this.vec3d8.x) < 0.003) {
            local2214_1 = 0.0;
         }

         if (Math.abs(this.vec3d8.y) < 0.003) {
            local2214_3 = 0.0;
         }

         if (Math.abs(this.vec3d8.z) < 0.003) {
            local2214_5 = 0.0;
         }

         if (this.flag4) {
            this.flag3 = false;
         }

         this.vec3d8 = new Vec3d(local2214_1, local2214_3, local2214_5);
         if (this.flag2) {
            double local2214_7 = this.checkState() ? this.computeDouble3(FluidTags.LAVA) : this.computeDouble3(FluidTags.WATER);
            boolean local2214_9 = this.checkState6() && local2214_7 > 0.0;
            double local2214_10 = this.computeDouble();
            if (!local2214_9 || this.flag4 && !(local2214_7 > local2214_10)) {
               if (!this.checkState() || this.flag4 && !(local2214_7 > local2214_10)) {
                  if ((this.flag4 || local2214_9 && local2214_7 <= local2214_10) && this.intVal == 0) {
                     this.updateState6();
                     if (this.playerEntity2.equals(mc.player)) {
                        this.intVal = 10;
                     }
                  }
               } else {
                  this.processData4(FluidTags.LAVA);
               }
            } else {
               this.processData4(FluidTags.WATER);
            }
         }

         float local2214_7 = this.nested0387.floatVal5 * 0.98F;
         float local2214_8 = this.nested0387.floatVal4 * 0.98F;
         float local2214_9 = 0.0F;
         if (this.matchesCriteria4(StatusEffects.SLOW_FALLING) || this.matchesCriteria4(StatusEffects.LEVITATION)) {
            this.updateState4();
         }

         this.processData(new Vec3d(local2214_7, local2214_9, local2214_8));
      }
   }

   private void updateState4() {
      this.floatVal3 = 0.0F;
   }

   private void updateState5() {
      if (this.flag8) {
         this.flag8 = this.checkState2() && this.checkState6() && !this.playerEntity2.hasVehicle();
      } else {
         this.flag8 = this.checkState2()
            && this.checkState8()
            && !this.playerEntity2.hasVehicle()
            && this.playerEntity2.getEntityWorld().getFluidState(this.getBlockPos2(this.vec3d7)).isIn(FluidTags.WATER);
      }
   }

   private boolean checkState2() {
      return this.flag;
   }

   public double computeDouble2(RegistryEntry<EntityAttribute> local2218_1) {
      return this.playerEntity2.getAttributes().getValue(local2218_1);
   }

   private float computeFloat4(float local2219_1) {
      return this.flag4 ? this.computeFloat5() * (0.21600002F / (local2219_1 * local2219_1 * local2219_1)) : this.computeFloat6();
   }

   private float computeFloat5() {
      return 0.1F;
   }

   public boolean matchesCriteria4(RegistryEntry<StatusEffect> local2221_1) {
      StatusEffectInstance statusEffect = this.playerEntity2.getStatusEffect(local2221_1);
      return statusEffect != null && statusEffect.getDuration() >= this.intVal2;
   }

   public boolean checkState3() {
      BlockPos blockPos = this.getBlockPos2(this.vec3d7);
      BlockState blockState = this.getBlockState2(blockPos);
      return blockState.isIn(BlockTags.CLIMBABLE) ? true : blockState.getBlock() instanceof TrapdoorBlock && this.matchesCriteria3(blockPos, blockState);
   }

   private Vec3d getVec3d6(Vec3d vec3d) {
      if (!this.checkState3()) {
         return vec3d;
      }

      this.updateState4();
      double local2223_2 = MathHelper.clamp(vec3d.x, -0.15F, 0.15F);
      double local2223_4 = MathHelper.clamp(vec3d.z, -0.15F, 0.15F);
      double local2223_6 = Math.max(vec3d.y, -0.15F);
      if (local2223_6 < 0.0 && !this.getBlockState2(this.getBlockPos2(this.vec3d7)).isOf(Blocks.SCAFFOLDING) && this.playerEntity2.isHoldingOntoLadder()) {
         local2223_6 = 0.0;
      }

      return new Vec3d(local2223_2, local2223_6, local2223_4);
   }

   private double computeDouble3(TagKey<Fluid> local2224_1) {
      return this.map.getDouble(local2224_1);
   }

   public static SimulatedPlayerInput getSimulatedPlayerInput3(PlayerEntity playerEntity, SimulatedPlayerInput.SimulatedPlayerInputState local2225_1) {
      return new SimulatedPlayerInput(
         playerEntity,
         local2225_1,
         playerEntity.getEntityPos(),
         playerEntity.getEntityPos().subtract(new Vec3d(playerEntity.lastX, playerEntity.lastY, playerEntity.lastZ)),
         playerEntity.getBoundingBox(),
         playerEntity.getYaw(),
         playerEntity.getPitch(),
         playerEntity.isSprinting(),
         (float)playerEntity.fallDistance,
         ((LivingEntityMixin)playerEntity).getNoJumpDelay(),
         ((LivingEntityMixin)playerEntity).getJumping(),
         playerEntity.isGliding(),
         playerEntity.isOnGround(),
         playerEntity.horizontalCollision,
         playerEntity.verticalCollision,
         playerEntity.isTouchingWater(),
         playerEntity.isSwimming(),
         playerEntity.isSubmergedInWater(),
         new Object2DoubleArrayMap(((EntityMixin2)playerEntity).getFluidHeight()),
         new HashSet<>(((EntityMixin2)playerEntity).getFluidOnEyes())
      );
   }

   private boolean checkState4() {
      Box box = this.box2.expand(1.0);
      int local2226_2 = MathHelper.floor(box.minX);
      int local2226_3 = MathHelper.ceil(box.maxX);
      int local2226_4 = MathHelper.floor(box.minZ);
      int local2226_5 = MathHelper.ceil(box.maxZ);
      return !this.playerEntity2.getEntityWorld().isRegionLoaded(local2226_2, local2226_4, local2226_3, local2226_5);
   }

   public static SimulatedPlayerInput getSimulatedPlayerInput4(SimulatedPlayerInput.SimulatedPlayerInputState local2227_0) {
      ClientPlayerEntity playerEntity = mc.player;
      return new SimulatedPlayerInput(
         playerEntity,
         local2227_0,
         playerEntity.getEntityPos(),
         playerEntity.getVelocity(),
         playerEntity.getBoundingBox(),
         playerEntity.getYaw(),
         playerEntity.getPitch(),
         playerEntity.isSprinting(),
         (float)playerEntity.fallDistance,
         ((LivingEntityMixin)playerEntity).getNoJumpDelay(),
         ((LivingEntityMixin)playerEntity).getJumping(),
         playerEntity.isGliding(),
         playerEntity.isOnGround(),
         playerEntity.horizontalCollision,
         playerEntity.verticalCollision,
         playerEntity.isTouchingWater(),
         playerEntity.isSwimming(),
         playerEntity.isSubmergedInWater(),
         new Object2DoubleArrayMap(((EntityMixin2)playerEntity).getFluidHeight()),
         new HashSet<>(((EntityMixin2)playerEntity).getFluidOnEyes())
      );
   }

   private double computeDouble4() {
      return this.vec3d7.y + this.playerEntity2.getStandingEyeHeight();
   }

   private boolean checkState5() {
      return this.flag4
         || this.floatVal3 < 0.5 && !this.playerEntity2.getEntityWorld().isSpaceEmpty(this.playerEntity2, this.box2.offset(0.0, this.floatVal3 - 0.5, 0.0));
   }

   private Vec3d getVec3d7() {
      return this.getVec3d5(this.floatVal2, this.floatVal);
   }

   private float computeFloat6() {
      float local2231_1 = 0.02F;
      return this.nested0387.playerInput2.sprint() ? local2231_1 + 0.006F : local2231_1;
   }

   public void updateState6() {
      this.vec3d8 = this.vec3d8.add(0.0, this.computeFloat() - this.vec3d8.y, 0.0);
      if (this.checkState2()) {
         float local2232_1 = (float)Math.toRadians(this.floatVal);
         this.vec3d8 = this.vec3d8.add(-MathHelper.sin(local2232_1) * 0.2, 0.0, MathHelper.cos(local2232_1) * 0.2);
      }
   }

   public SimulatedPlayerInput clone() {
      return new SimulatedPlayerInput(
         this.playerEntity2,
         this.nested0387,
         this.vec3d7,
         this.vec3d8,
         this.box2,
         this.floatVal,
         this.floatVal2,
         this.flag,
         this.floatVal3,
         this.intVal,
         this.flag2,
         this.flag3,
         this.flag4,
         this.flag5,
         this.flag6,
         this.flag7,
         this.flag8,
         this.flag9,
         new Object2DoubleArrayMap(this.map),
         new HashSet<>(this.set2)
      );
   }

   private boolean checkState6() {
      return this.flag7;
   }

   protected boolean checkState7() {
      return this.nested0387.playerInput2.sneak() || this.nested0387.flag11;
   }

   public boolean checkState8() {
      return this.flag9 && this.checkState6();
   }

   private boolean matchesCriteria5(Box box) {
      return this.playerEntity2.getEntityWorld().isSpaceEmpty(this.playerEntity2, box) && !this.playerEntity2.getEntityWorld().containsFluid(box);
   }

   public static class SimulatedPlayerInputState extends Input {
      public boolean flag11 = false;
      public float floatVal4;
      public float floatVal5;
      public PlayerInput playerInput2;
      public static final double doubleVal = 0.121;

      public void update() {
         if (this.playerInput2.forward() != this.playerInput2.backward()) {
            this.floatVal4 = this.playerInput2.forward() ? 1.0F : -1.0F;
         } else {
            this.floatVal4 = 0.0F;
         }

         if (this.playerInput2.left() == this.playerInput2.right()) {
            this.floatVal5 = 0.0F;
         } else {
            this.floatVal5 = this.playerInput2.left() ? 1.0F : -1.0F;
         }

         if (this.playerInput2.sneak()) {
            this.floatVal5 *= 0.3F;
            this.floatVal4 *= 0.3F;
         }
      }

      public SimulatedPlayerInputState(PlayerInput playerInput) {
         this.playerInput2 = playerInput;
      }

      public static SimulatedPlayerInput.SimulatedPlayerInputState guessInput(PlayerEntity playerEntity) {
         Vec3d vec3d = playerEntity.getEntityPos().subtract(new Vec3d(playerEntity.lastX, playerEntity.lastY, playerEntity.lastZ));
         double local2240_2 = vec3d.horizontalLengthSquared();
         PlayerInput playerInput = new PlayerInput(false, false, false, false, !playerEntity.isOnGround(), playerEntity.isSneaking(), local2240_2 >= 0.014641);
         if (local2240_2 > 0.0025000000000000005) {
            double local2240_5 = PlayerUtils.computeDouble2(vec3d, playerEntity.getYaw());
            double local2240_7 = MathHelper.wrapDegrees(local2240_5);
            playerInput = PlayerUtils.getPlayerInput2(playerInput, local2240_7);
         }

         return new SimulatedPlayerInput.SimulatedPlayerInputState(playerInput);
      }

      public static SimulatedPlayerInput.SimulatedPlayerInputState fromClientPlayer(PlayerInput playerInput) {
         return new SimulatedPlayerInput.SimulatedPlayerInputState(playerInput);
      }

      @Override
      public String toString() {
         return "SimulatedPlayerInput(forwards={"
            + this.playerInput2.forward()
            + "}, backwards={"
            + this.playerInput2.backward()
            + "}, left={"
            + this.playerInput2.left()
            + "}, right={"
            + this.playerInput2.right()
            + "}, jumping={"
            + this.playerInput2.jump()
            + "}, sprinting="
            + this.playerInput2.sprint()
            + ", slowDown="
            + this.playerInput2.sneak()
            + ")";
      }
   }
}
