package horus.client.module.render;

import horus.client.setting.BooleanSetting;
import horus.client.util.Colors;
import horus.client.util.DepthSnapshot;
import horus.client.event.EventListener;
import horus.client.setting.LegacyNumberSetting;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.util.RaycastUtils;
import horus.client.setting.ColorSetting;
import horus.client.render.ThemeColors;
import horus.client.RotationAngles;
import horus.client.setting.SectionSetting;
import horus.client.event.WorldRenderEvent;

import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;

@ModuleInfo(name = "BlockOverlay", description = "Выделяет грань блока под прицелом, видно сквозь блоки.", category = ModuleCategory.RENDER)
public final class BlockOverlayModule extends Module {
   private static BlockOverlayModule blockOverlayModule;
   public BooleanSetting fill;
   public LegacyNumberSetting fillOpacity;
   public LegacyNumberSetting lineWidth;
   public BooleanSetting animation;
   public LegacyNumberSetting speed;
   public BooleanSetting customColor;
   public ColorSetting colorSetting;
   private Box box2;
   private float floatVal;
   private long timeMillis;
   private final EventListener<WorldRenderEvent> worldRenderListener = worldRenderEvent -> {
      if (mc.player != null && mc.world != null) {
         float local5973_2 = Math.max(1.0F, Math.min((float)(System.currentTimeMillis() - this.timeMillis), 100.0F)) / 16.666666F;
         this.timeMillis = System.currentTimeMillis();
         BlockHitResult blockHitResult = mc.world
            .raycast(
               new RaycastContext(
                  mc.player.getCameraPosVec(worldRenderEvent.computeFloat()),
                  mc.player
                     .getCameraPosVec(worldRenderEvent.computeFloat())
                     .add(
                        new RotationAngles(RotationAngles.getCameraYaw(), RotationAngles.getCameraPitch())
                           .getDirectionVector()
                           .multiply(mc.player.getBlockInteractionRange())
                     ),
                  ShapeType.OUTLINE,
                  FluidHandling.NONE,
                  mc.player
               )
            );
         Box box = blockHitResult.getType() == Type.BLOCK && !mc.world.getBlockState(blockHitResult.getBlockPos()).isAir()
            ? (
               mc.world.getBlockState(blockHitResult.getBlockPos()).getOutlineShape(mc.world, blockHitResult.getBlockPos()).isEmpty()
                  ? new Box(blockHitResult.getBlockPos()).expand(0.002)
                  : mc.world
                     .getBlockState(blockHitResult.getBlockPos())
                     .getOutlineShape(mc.world, blockHitResult.getBlockPos())
                     .getBoundingBox()
                     .offset(blockHitResult.getBlockPos())
                     .expand(0.002)
            )
            : null;
         if (box != null && this.matchesCriteria(mc.player.getCameraPosVec(worldRenderEvent.computeFloat()), blockHitResult.getPos())) {
            box = null;
         }

         if (box != null) {
            box = this.getBox(box, blockHitResult.getSide());
         }

         if (box != null) {
            this.box2 = this.box2 != null && !(this.floatVal < 0.003921569F) && this.animation.isChecked()
               ? new Box(
                  MathHelper.lerp(Math.min(1.0F, this.speed.getValue() * local5973_2), this.box2.minX, box.minX),
                  MathHelper.lerp(Math.min(1.0F, this.speed.getValue() * local5973_2), this.box2.minY, box.minY),
                  MathHelper.lerp(Math.min(1.0F, this.speed.getValue() * local5973_2), this.box2.minZ, box.minZ),
                  MathHelper.lerp(Math.min(1.0F, this.speed.getValue() * local5973_2), this.box2.maxX, box.maxX),
                  MathHelper.lerp(Math.min(1.0F, this.speed.getValue() * local5973_2), this.box2.maxY, box.maxY),
                  MathHelper.lerp(Math.min(1.0F, this.speed.getValue() * local5973_2), this.box2.maxZ, box.maxZ)
               )
               : box;
         }

         this.floatVal = MathHelper.clamp(this.floatVal + (box == null ? -0.22F : 0.22F) * local5973_2, 0.0F, 1.0F);
         if (this.box2 != null && !(this.floatVal < 0.003921569F)) {
            DepthSnapshot.updateState2();
            worldRenderEvent.getMatrixStack().push();
            worldRenderEvent.getMatrixStack()
               .translate(
                  this.box2.minX - mc.gameRenderer.getCamera().getCameraPos().x,
                  this.box2.minY - mc.gameRenderer.getCamera().getCameraPos().y,
                  this.box2.minZ - mc.gameRenderer.getCamera().getCameraPos().z
               );
            Matrix4f positionMatrix = worldRenderEvent.getMatrixStack().peek().getPositionMatrix();
            if (this.fill.isChecked() && this.fillOpacity.getValue() > 0.001F) {
               VertexConsumer vertexConsumer = mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayers.debugQuads());
               float[] floatArray = new float[]{0.0F, (float)(this.box2.maxX - this.box2.minX)};
               float[] floatArray2 = new float[]{0.0F, (float)(this.box2.maxY - this.box2.minY)};
               float[] floatArray3 = new float[]{0.0F, (float)(this.box2.maxZ - this.box2.minZ)};
               int[][] intArrayArray = new int[][]{{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
               int local5973_11 = floatArray[1] < 0.01F ? 0 : (floatArray2[1] < 0.01F ? 1 : (floatArray3[1] < 0.01F ? 2 : -1));

               for (int index = 0; index < intArrayArray.length; index++) {
                  if (local5973_11 < 0 || index == local5973_11 * 2) {
                     for (int local5973_16 : intArrayArray[index]) {
                        vertexConsumer.vertex(positionMatrix, floatArray[local5973_16 >> 2], floatArray2[local5973_16 >> 1 & 1], floatArray3[local5973_16 & 1])
                           .color(
                              Colors.computeInt6(
                                 this.customColor.isChecked() ? this.colorSetting.getValue() : ThemeColors.intVal9,
                                 this.fillOpacity.getValue() * this.floatVal
                              )
                           );
                     }
                  }
               }
            }

            VertexRendering.drawOutline(
               worldRenderEvent.getMatrixStack(),
               mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayers.lines()),
               VoxelShapes.cuboid(
                  new Box(
                     0.0,
                     0.0,
                     0.0,
                     this.box2.maxX - this.box2.minX,
                     this.box2.maxY - this.box2.minY,
                     this.box2.maxZ - this.box2.minZ
                  )
               ),
               0.0,
               0.0,
               0.0,
               Colors.computeInt6(this.customColor.isChecked() ? this.colorSetting.getValue() : ThemeColors.intVal9, this.floatVal),
               this.lineWidth.getValue()
            );
            mc.getBufferBuilders().getEntityVertexConsumers().draw();
            worldRenderEvent.getMatrixStack().pop();
         }
      }
   };

   private Box getBox(Box box, Direction direction) {
      return switch (direction) {
         case DOWN -> new Box(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
         case UP -> new Box(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
         case NORTH -> new Box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
         case SOUTH -> new Box(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
         case WEST -> new Box(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
         case EAST -> new Box(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
      };
   }

   private boolean matchesCriteria(Vec3d vec3d, Vec3d vec3d2) {
      return RaycastUtils.getEntityHitResult2(
            mc.player,
            vec3d,
            vec3d2,
            mc.player.getBoundingBox().stretch(vec3d2.subtract(vec3d)).expand(1.0, 1.0, 1.0),
            entity -> !entity.isSpectator() && entity.canHit(),
            vec3d.distanceTo(vec3d2)
         )
         != null;
   }

   private BlockOverlayModule() {
      this.addSetting(new SectionSetting("Бокс"));
      this.fill = this.addSetting(new BooleanSetting("Заливка", "Заливать грань цветом.", true));
      this.fillOpacity = this.addSetting(new LegacyNumberSetting("Прозрачность заливки", "Насколько плотно залиты грани.", 0.25, 0.0, 1.0, 0.05));
      this.lineWidth = this.addSetting(new LegacyNumberSetting("Толщина линии", "Толщина линий оутлайна.", 2.0, 0.5, 6.0, 0.1));
      this.addSetting(new SectionSetting("Анимация"));
      this.animation = this.addSetting(new BooleanSetting("Анимация", "Бокс плавно переезжает по x, y, z на новый блок вместо мгновенного прыжка.", true));
      this.speed = this.addSetting(new LegacyNumberSetting("Скорость", "Как быстро бокс догоняет новый блок.", 0.35, 0.05, 1.0, 0.05));
      this.addSetting(new SectionSetting("Цвет"));
      this.customColor = this.addSetting(new BooleanSetting("Свой цвет", "Использовать свой цвет вместо ACCENT.", false));
      this.colorSetting = this.addSetting(new ColorSetting("Цвет", "Цвет бокса."));
      this.fillOpacity.withVisibility(() -> this.fill.isChecked());
      this.speed.withVisibility(() -> this.animation.isChecked());
      this.colorSetting.withVisibility(() -> this.customColor.isChecked());
   }

   @Override
   protected void onDisable() {
      this.box2 = null;
      this.floatVal = 0.0F;
   }

   public static BlockOverlayModule getInstance() {
      if (blockOverlayModule == null) {
         blockOverlayModule = new BlockOverlayModule();
      }

      return blockOverlayModule;
   }
}
