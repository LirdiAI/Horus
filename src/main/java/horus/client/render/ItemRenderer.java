package horus.client.render;

import horus.client.HorusClient;
import horus.mixin.DrawContextMixin;
import horus.mixin.ItemRenderStateMixin;
import horus.mixin.ItemRenderState_LayerRenderStateMixin;
import horus.client.util.GuiScale;
import horus.client.render.GuiLayerSerialAccess;
import horus.client.render.ItemDrawData;
import horus.client.render.CachedItemGeometry;
import horus.client.render.ScissorStack;
import horus.client.render.ItemGeometryQuad;
import horus.client.util.TextureBinding;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;

public final class ItemRenderer implements AutoCloseable {
   private static volatile ItemRenderer itemRenderer;
   private static final VertexFormat vertexFormat = VertexFormat.builder()
      .add("Position", VertexFormatElement.POSITION)
      .add("UV0", VertexFormatElement.UV0)
      .add("Color", VertexFormatElement.COLOR)
      .add("LineWidth", VertexFormatElement.LINE_WIDTH)
      .build();
   public static final RenderPipeline pipeline2 = RenderPipeline.builder()
      .withLocation(getIdentifier("pipeline/item"))
      .withVertexShader(getIdentifier("core/item"))
      .withFragmentShader(getIdentifier("core/item"))
      .withSampler("Sampler0")
      .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
      .withUniform("Projection", UniformType.UNIFORM_BUFFER)
      .withUniform("ItemParamsArray", UniformType.UNIFORM_BUFFER)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withCull(false)
      .withVertexFormat(vertexFormat, DrawMode.QUADS)
      .build();
   private final Map<Identifier, ItemRenderer.DataRecord6> map = new HashMap<>();
   private final Map<ItemRenderer.DataRecord, CachedItemGeometry> map2 = new LinkedHashMap<ItemRenderer.DataRecord, CachedItemGeometry>(
      256, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<ItemRenderer.DataRecord, CachedItemGeometry> local2517_1) {
         return this.size() > 512;
      }
   };
   private final Map<ItemRenderer.CachedItemKey, CachedItemGeometry> map3 = new LinkedHashMap<ItemRenderer.CachedItemKey, CachedItemGeometry>(
      256, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<ItemRenderer.CachedItemKey, CachedItemGeometry> local2543_1) {
         return this.size() > 512;
      }
   };
   private ByteBuffer byteBuffer2;
   private final Map<ItemRenderer.DataRecord2, GuiItemRenderState> map4 = new LinkedHashMap<>(32);
   private final Map<ItemGuiElementRenderState, Integer> map5 = new IdentityHashMap<>();
   private Integer integer;
   private final float[] floatValArray = new float[4096];
   private int intVal;
   private DrawContext drawContext2;
   private GpuBuffer gpuBuffer2;
   private boolean flag;

   private static Identifier getIdentifier(String local2409_0) {
      return Identifier.of("horus", local2409_0);
   }

   private CachedItemGeometry getCachedItemGeometry(MinecraftClient minecraftClient, ItemDrawData itemDrawData) {
      ItemRenderer.DataRecord local2410_3 = ItemRenderer.DataRecord.of(itemDrawData.stack(), itemDrawData.seed());
      CachedItemGeometry cachedItemGeometry = this.map2.get(local2410_3);
      if (cachedItemGeometry != null) {
         return cachedItemGeometry;
      }

      KeyedItemRenderState itemRenderState = new KeyedItemRenderState();
      minecraftClient.getItemModelManager()
         .clearAndUpdate(itemRenderState, itemDrawData.stack(), ItemDisplayContext.GUI, minecraftClient.world, minecraftClient.player, itemDrawData.seed());
      ItemRenderer.CachedItemKey local2410_6 = new ItemRenderer.CachedItemKey(itemRenderState.getModelKey(), itemDrawData.seed());
      CachedItemGeometry cachedItemGeometry2 = this.map3.get(local2410_6);
      if (cachedItemGeometry2 == null) {
         cachedItemGeometry2 = this.getCachedItemGeometry2(itemRenderState);
         this.map3.put(local2410_6, cachedItemGeometry2);
      }

      if (!itemRenderState.isAnimated()) {
         this.map2.put(local2410_3, cachedItemGeometry2);
      }

      return cachedItemGeometry2;
   }

   private boolean matchesCriteria(LayerRenderState layerRenderState) {
      return layerRenderState != null && ((ItemRenderState_LayerRenderStateMixin)layerRenderState).getSpecialRenderer() != null;
   }

   public void processData(ItemDrawData itemDrawData) {
      this.renderElement2(this.drawContext2, itemDrawData);
   }

   public void processData2(ItemGuiElementRenderState itemGuiElementRenderState) {
      if (this.integer != null) {
         this.map5.put(itemGuiElementRenderState, this.integer);
      }
   }

   private static int computeInt(int local2415_0, float local2415_1) {
      local2415_0 = computeInt6(local2415_0);
      int local2415_2 = local2415_0 >>> 24 & 0xFF;
      int local2415_3 = computeInt3(Math.round((local2415_0 >>> 16 & 0xFF) * local2415_1), 0, 255);
      int local2415_4 = computeInt3(Math.round((local2415_0 >>> 8 & 0xFF) * local2415_1), 0, 255);
      int local2415_5 = computeInt3(Math.round((local2415_0 & 0xFF) * local2415_1), 0, 255);
      return local2415_2 << 24 | local2415_3 << 16 | local2415_4 << 8 | local2415_5;
   }

   private ByteBuffer getByteBuffer(boolean local2416_1) {
      if (this.byteBuffer2 == null) {
         this.byteBuffer2 = MemoryUtil.memCalloc(65536);
      }

      ByteBuffer byteBuffer = this.byteBuffer2;
      byteBuffer.clear();
      float local2416_3 = (float)(System.nanoTime() % 30000000000L) / 1.0E9F;

      for (int index = 0; index < this.intVal; index++) {
         int local2416_5 = index * 4 * 4;
         byteBuffer.putFloat(local2416_5, this.floatValArray[index]);
         byteBuffer.putFloat(local2416_5 + 4, local2416_3);
         byteBuffer.putFloat(local2416_5 + 8, 0.0F);
         byteBuffer.putFloat(local2416_5 + 12, 0.0F);
      }

      int local2416_4 = this.intVal * 4 * 4;
      if (local2416_1) {
         if (local2416_4 < 65536) {
            MemoryUtil.memSet(MemoryUtil.memAddress(byteBuffer) + local2416_4, 0, 65536 - local2416_4);
         }

         byteBuffer.position(0).limit(65536);
      } else {
         byteBuffer.position(0).limit(Math.max(16, local2416_4));
      }

      return byteBuffer;
   }

   private GpuBuffer getGpuBuffer() {
      if (this.gpuBuffer2 != null && !this.gpuBuffer2.isClosed() && this.gpuBuffer2.size() >= 65536L) {
         return this.gpuBuffer2;
      }

      this.updateState2();

      try {
         this.gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_item_params", 136, 65536L);
         return this.gpuBuffer2;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   public boolean matchesCriteria2(RenderPipeline renderPipeline) {
      return renderPipeline == pipeline2;
   }

   private void processData3(
      GuiRenderState guiRenderState,
      int local2419_2,
      Matrix3x2f matrix3x2f,
      ItemRenderer.DataRecord3 local2419_4,
      TextureBinding textureBinding,
      ItemGeometryQuad itemGeometryQuad,
      float local2419_7,
      float local2419_8,
      float local2419_9,
      int local2419_10,
      float local2419_11
   ) {
      ItemRenderer.DataRecord2 local2419_12 = new ItemRenderer.DataRecord2(
         guiRenderState, local2419_2, textureBinding.id(), local2419_4, ScissorStack.getScreenRect()
      );
      GuiItemRenderState itemRenderState = this.map4.get(local2419_12);
      if (itemRenderState == null) {
         itemRenderState = new GuiItemRenderState(matrix3x2f, textureBinding, ScissorStack.getScreenRect());
         itemRenderState.add(itemGeometryQuad, local2419_7, local2419_8, local2419_9, local2419_10, local2419_11);
         this.map4.put(local2419_12, itemRenderState);
         guiRenderState.addSimpleElement(itemRenderState);
      } else {
         itemRenderState.add(itemGeometryQuad, local2419_7, local2419_8, local2419_9, local2419_10, local2419_11);
      }
   }

   private static int computeInt2(BakedQuad bakedQuad, int[] intArray) {
      int local2420_2 = -1;
      if (bakedQuad.hasTint()) {
         int local2420_3 = bakedQuad.tintIndex();
         if (local2420_3 >= 0 && local2420_3 < intArray.length) {
            local2420_2 = computeInt6(intArray[local2420_3]);
         }
      }

      return local2420_2;
   }

   private static float computeFloat(
      float local2422_0, float local2422_1, float local2422_2, float local2422_3, float local2422_4, float local2422_5, float local2422_6, float local2422_7
   ) {
      return Math.abs(
            computeFloat3(local2422_0, local2422_1, local2422_2, local2422_3)
               + computeFloat3(local2422_2, local2422_3, local2422_4, local2422_5)
               + computeFloat3(local2422_4, local2422_5, local2422_6, local2422_7)
               + computeFloat3(local2422_6, local2422_7, local2422_0, local2422_1)
         )
         * 0.5F;
   }

   private void renderItemGeometry(DrawContext drawContext, ItemDrawData itemDrawData) {
      float local2423_3 = itemDrawData.size() / 16.0F;
      if (!(local2423_3 <= 0.0F)) {
         drawContext.getMatrices().pushMatrix();

         try {
            GuiScale.processData(drawContext.getMatrices());
            drawContext.getMatrices().translate(itemDrawData.x(), itemDrawData.y());
            drawContext.getMatrices().scale(local2423_3);
            this.integer = computeInt5(itemDrawData.options().color(), itemDrawData.options().alpha());
            drawContext.drawItem(itemDrawData.stack(), 0, 0, itemDrawData.seed());
         } finally {
            this.integer = null;
            drawContext.getMatrices().popMatrix();
         }
      }
   }

   public void renderElement(DrawContext drawContext) {
      if (this.drawContext2 != drawContext) {
         this.map4.clear();
      }

      this.drawContext2 = drawContext;
   }

   private void processData4(List<ItemRenderer.DataRecord4> local2425_1, ItemRenderer.SubContext5 local2425_2, LayerRenderState layerRenderState) {
      if (layerRenderState != null) {
         List<BakedQuad> currentQuads = layerRenderState.getQuads();
         if (!currentQuads.isEmpty()) {
            ItemRenderState_LayerRenderStateMixin itemRenderState_LayerRenderStateMixin = (ItemRenderState_LayerRenderStateMixin)layerRenderState;
            Glint foilType = itemRenderState_LayerRenderStateMixin.getFoilType();
            boolean local2425_7 = foilType != Glint.NONE;
            int[] tintLayers = itemRenderState_LayerRenderStateMixin.getTintLayers();
            Matrix4f matrix4f = this.getMatrix4f(itemRenderState_LayerRenderStateMixin);
            boolean usesBlockLight = itemRenderState_LayerRenderStateMixin.getUsesBlockLight();
            if (usesBlockLight) {
               local2425_2.updateState7();
            }

            for (BakedQuad bakedQuad : currentQuads) {
               ItemRenderer.DataRecord4 local2425_13 = this.getNested0399(
                  bakedQuad, matrix4f, computeInt2(bakedQuad, tintLayers), local2425_7, usesBlockLight
               );
               if (!(local2425_13.area() < 2.0E-6F)) {
                  local2425_2.processData6(local2425_13.x0(), local2425_13.y0(), local2425_13.z0());
                  local2425_2.processData6(local2425_13.x1(), local2425_13.y1(), local2425_13.z1());
                  local2425_2.processData6(local2425_13.x2(), local2425_13.y2(), local2425_13.z2());
                  local2425_2.processData6(local2425_13.x3(), local2425_13.y3(), local2425_13.z3());
                  local2425_1.add(local2425_13);
               }
            }
         }
      }
   }

   @Override
   public void close() {
      this.map4.clear();
      this.map5.clear();
      this.intVal = 0;
      this.map2.clear();
      this.map3.clear();
      this.map.clear();
      this.drawContext2 = null;
      this.updateState2();
      if (this.byteBuffer2 != null) {
         MemoryUtil.memFree(this.byteBuffer2);
         this.byteBuffer2 = null;
      }
   }

   private Matrix4f getMatrix4f(ItemRenderState_LayerRenderStateMixin itemRenderState_LayerRenderStateMixin) {
      net.minecraft.client.util.math.MatrixStack.Entry entry = new net.minecraft.client.util.math.MatrixStack.Entry();
      Transformation itemTransform = itemRenderState_LayerRenderStateMixin.getItemTransform();
      if (itemTransform == null) {
         itemTransform = Transformation.IDENTITY;
      }

      itemTransform.apply(false, entry);
      return new Matrix4f(entry.getPositionMatrix());
   }

   private ItemGeometryQuad getItemGeometryQuad(ItemRenderer.DataRecord4 local2428_1, ItemRenderer.DataRecord7 local2428_2) {
      return new ItemGeometryQuad(
         local2428_1.atlas(),
         local2428_2.x(local2428_1.x0()),
         local2428_2.y(local2428_1.y0()),
         local2428_1.u0(),
         local2428_1.v0(),
         local2428_2.x(local2428_1.x1()),
         local2428_2.y(local2428_1.y1()),
         local2428_1.u1(),
         local2428_1.v1(),
         local2428_2.x(local2428_1.x2()),
         local2428_2.y(local2428_1.y2()),
         local2428_1.u2(),
         local2428_1.v2(),
         local2428_2.x(local2428_1.x3()),
         local2428_2.y(local2428_1.y3()),
         local2428_1.u3(),
         local2428_1.v3(),
         local2428_1.tint(),
         local2428_1.foil()
      );
   }

   private ItemRenderer.DataRecord4 getNested0399(BakedQuad bakedQuad, Matrix4f matrix4f, int local2429_3, boolean local2429_4, boolean local2429_5) {
      Vector3fc vector3fc = bakedQuad.position0();
      Vector3fc vector3fc2 = bakedQuad.position1();
      Vector3fc vector3fc3 = bakedQuad.position2();
      Vector3fc vector3fc4 = bakedQuad.position3();
      Vector3f vector3f = getVector3f(vector3fc, matrix4f);
      Vector3f vector3f2 = getVector3f(vector3fc2, matrix4f);
      Vector3f vector3f3 = getVector3f(vector3fc3, matrix4f);
      Vector3f vector3f4 = getVector3f(vector3fc4, matrix4f);
      long local2429_14 = bakedQuad.packedUV0();
      long local2429_16 = bakedQuad.packedUV1();
      long local2429_18 = bakedQuad.packedUV2();
      long local2429_20 = bakedQuad.packedUV3();
      float local2429_22 = (vector3f.z + vector3f2.z + vector3f3.z + vector3f4.z) * 0.25F;
      int local2429_23 = local2429_5 ? computeInt(local2429_3, computeFloat2(bakedQuad.face())) : local2429_3;
      return new ItemRenderer.DataRecord4(
         bakedQuad.sprite().getAtlasId(),
         vector3f.x,
         vector3f.y,
         Vector2f.getX(local2429_14),
         Vector2f.getY(local2429_14),
         vector3f2.x,
         vector3f2.y,
         Vector2f.getX(local2429_16),
         Vector2f.getY(local2429_16),
         vector3f3.x,
         vector3f3.y,
         Vector2f.getX(local2429_18),
         Vector2f.getY(local2429_18),
         vector3f4.x,
         vector3f4.y,
         Vector2f.getX(local2429_20),
         Vector2f.getY(local2429_20),
         local2429_23,
         local2429_4,
         local2429_22,
         computeFloat(vector3f.x, vector3f.y, vector3f2.x, vector3f2.y, vector3f3.x, vector3f3.y, vector3f4.x, vector3f4.y),
         vector3f.z,
         vector3f2.z,
         vector3f3.z,
         vector3f4.z
      );
   }

   public void updateState() {
      this.map5.clear();
   }

   public static ItemRenderer getItemRenderer() {
      ItemRenderer currentItemRenderer = itemRenderer;
      if (currentItemRenderer == null) {
         synchronized (ItemRenderer.class) {
            currentItemRenderer = itemRenderer;
            if (currentItemRenderer == null) {
               currentItemRenderer = new ItemRenderer();
               itemRenderer = currentItemRenderer;
            }
         }
      }

      return currentItemRenderer;
   }

   private static int computeInt3(int local2432_0, int local2432_1, int local2432_2) {
      return Math.max(local2432_1, Math.min(local2432_2, local2432_0));
   }

   private void updateState2() {
      if (this.gpuBuffer2 != null) {
         this.gpuBuffer2.close();
         this.gpuBuffer2 = null;
      }
   }

   public static void updateState3() {
      ItemRenderer currentItemRenderer = itemRenderer;
      if (currentItemRenderer != null) {
         currentItemRenderer.close();
         itemRenderer = null;
      }
   }

   public void updateState4() {
      this.intVal = 0;
      this.flag = false;
   }

   private static int computeInt4(int local2436_0, int local2436_1, float local2436_2) {
      local2436_0 = computeInt6(local2436_0);
      local2436_1 = computeInt6(local2436_1);
      int local2436_3 = Math.round((local2436_0 >>> 24 & 0xFF) * ((local2436_1 >>> 24 & 0xFF) / 255.0F) * local2436_2);
      int local2436_4 = (local2436_0 >>> 16 & 0xFF) * (local2436_1 >>> 16 & 0xFF) / 255;
      int local2436_5 = (local2436_0 >>> 8 & 0xFF) * (local2436_1 >>> 8 & 0xFF) / 255;
      int local2436_6 = (local2436_0 & 0xFF) * (local2436_1 & 0xFF) / 255;
      return computeInt3(local2436_3, 0, 255) << 24 | local2436_4 << 16 | local2436_5 << 8 | local2436_6;
   }

   public void clearItemCache() {
      this.drawContext2 = null;
      this.map4.clear();
   }

   private static float computeFloat2(Direction direction) {
      if (direction == null) {
         return 0.84F;
      }

      return switch (direction) {
         case UP -> 0.98F;
         case DOWN -> 0.72F;
         case NORTH -> 0.9F;
         case SOUTH -> 0.84F;
         case EAST, WEST -> 0.78F;
      };
   }

   private CachedItemGeometry getCachedItemGeometry2(ItemRenderState itemRenderState) {
      ItemRenderStateMixin itemRenderStateMixin = (ItemRenderStateMixin)itemRenderState;
      LayerRenderState[] layers = itemRenderStateMixin.getLayers();
      int local2439_4 = Math.min(itemRenderStateMixin.getActiveLayerCount(), layers.length);

      for (int index = 0; index < local2439_4; index++) {
         if (this.matchesCriteria(layers[index])) {
            return new CachedItemGeometry(List.of(), itemRenderState.isAnimated(), true);
         }
      }

      List<ItemRenderer.DataRecord4> local2439_5 = new ArrayList<>(Math.max(8, local2439_4 * 8));
      ItemRenderer.SubContext5 local2439_6 = new ItemRenderer.SubContext5();

      for (int index2 = 0; index2 < local2439_4; index2++) {
         this.processData4(local2439_5, local2439_6, layers[index2]);
      }

      local2439_5.sort(Comparator.comparingDouble(ItemRenderer.DataRecord4::depth));
      ItemRenderer.DataRecord7 local2439_7 = ItemRenderer.DataRecord7.of(local2439_6);
      List<ItemGeometryQuad> local2439_8 = new ArrayList<>(local2439_5.size());

      for (ItemRenderer.DataRecord4 local2439_10 : local2439_5) {
         local2439_8.add(this.getItemGeometryQuad(local2439_10, local2439_7));
      }

      return new CachedItemGeometry(List.copyOf(local2439_8), itemRenderState.isAnimated(), false);
   }

   private GpuBuffer getGpuBuffer2() {
      if (!this.flag && this.gpuBuffer2 != null) {
         return this.gpuBuffer2;
      }

      this.updateState5();
      if (!this.flag && this.gpuBuffer2 != null) {
         return this.gpuBuffer2;
      }

      this.updateState2();
      ByteBuffer byteBuffer = this.getByteBuffer(true);
      this.gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "horus_item_params", 128, byteBuffer);
      this.flag = false;
      return this.gpuBuffer2;
   }

   private static int computeInt5(int local2441_0, float local2441_1) {
      local2441_0 = computeInt6(local2441_0);
      float local2441_2 = (local2441_0 >>> 24 & 0xFF) / 255.0F * local2441_1;
      return Math.round(255.0F * local2441_2) << 24
         | Math.round((local2441_0 >>> 16 & 0xFF) * local2441_2) << 16
         | Math.round((local2441_0 >>> 8 & 0xFF) * local2441_2) << 8
         | Math.round((local2441_0 & 0xFF) * local2441_2);
   }

   private static int computeInt6(int local2442_0) {
      return (local2442_0 & 0xFF000000) == 0 && (local2442_0 & 16777215) != 0 ? local2442_0 | 0xFF000000 : local2442_0;
   }

   int computeInt7(float local2443_1) {
      int local2443_2 = this.intVal;
      if (local2443_2 == 4096) {
         return -1;
      }

      this.floatValArray[local2443_2] = local2443_1;
      this.intVal++;
      this.flag = true;
      return local2443_2;
   }

   public void updateState5() {
      if (this.intVal != 0 && this.flag) {
         GpuBuffer gpuBuffer = this.getGpuBuffer();
         if (gpuBuffer != null) {
            try {
               ByteBuffer byteBuffer = this.getByteBuffer(false);
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(0L, byteBuffer.remaining()), byteBuffer);
               this.flag = false;
            } catch (RuntimeException exception) {
               this.flag = true;
            }
         }
      }
   }

   private static float computeFloat3(float local2445_0, float local2445_1, float local2445_2, float local2445_3) {
      return local2445_0 * local2445_3 - local2445_2 * local2445_1;
   }

   private void renderElement2(DrawContext drawContext, ItemDrawData itemDrawData) {
      if (drawContext != null && itemDrawData != null && itemDrawData.visible()) {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         if (minecraftClient != null && minecraftClient.getItemModelManager() != null) {
            try {
               GuiRenderState guiRenderState = ((DrawContextMixin)drawContext).getGuiRenderState();
               int currentLayerSerial = ((GuiLayerSerialAccess)guiRenderState).getLayerSerial();
               Matrix3x2f matrix3x2f = GuiScale.renderElement(drawContext);
               ItemRenderer.DataRecord3 local2446_7 = ItemRenderer.DataRecord3.of(matrix3x2f);
               CachedItemGeometry cachedItemGeometry = this.getCachedItemGeometry(minecraftClient, itemDrawData);
               if (cachedItemGeometry.specialRenderer()) {
                  this.renderItemGeometry(drawContext, itemDrawData);
                  return;
               }

               for (ItemGeometryQuad itemGeometryQuad : cachedItemGeometry.quads()) {
                  TextureBinding textureBinding = this.getTextureBinding(itemGeometryQuad.atlas());
                  if (textureBinding != null) {
                     float local2446_12 = itemDrawData.options().glintMode().shouldRender(itemDrawData.stack(), itemGeometryQuad.foil())
                        ? itemDrawData.options().glintStrength()
                        : 0.0F;
                     int local2446_13 = computeInt4(itemDrawData.options().color(), itemGeometryQuad.tint(), itemDrawData.options().alpha());
                     this.processData3(
                        guiRenderState,
                        currentLayerSerial,
                        matrix3x2f,
                        local2446_7,
                        textureBinding,
                        itemGeometryQuad,
                        itemDrawData.x(),
                        itemDrawData.y(),
                        itemDrawData.size(),
                        local2446_13,
                        local2446_12
                     );
                  }
               }
            } catch (RuntimeException exception) {
               HorusClient.logger().warn("[RenderItem] Failed to submit item: {}", itemDrawData.stack().getName().getString(), exception);
            }
         }
      }
   }

   private static Vector3f getVector3f(Vector3fc vector3fc, Matrix4f matrix4f) {
      return new Vector3f(vector3fc).mulPosition(matrix4f);
   }

   private ItemRenderer() {
   }

   public int computeInt8(ItemGuiElementRenderState itemGuiElementRenderState) {
      return this.map5.getOrDefault(itemGuiElementRenderState, -1);
   }

   public void processData5(RenderPass renderPass) {
      if (renderPass != null && this.intVal != 0) {
         GpuBuffer gpuBuffer = this.getGpuBuffer2();
         if (gpuBuffer != null) {
            renderPass.setUniform("ItemParamsArray", gpuBuffer);
         }
      }
   }

   private TextureBinding getTextureBinding(Identifier identifier) {
      if (identifier == null) {
         return null;
      }

      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient == null) {
         return null;
      }

      TextureManager textureManager = minecraftClient.getTextureManager();
      if (textureManager == null) {
         return null;
      }

      AbstractTexture currentTexture = textureManager.getTexture(identifier);
      if (currentTexture != null && currentTexture.getGlTextureView() != null) {
         ItemRenderer.DataRecord6 local2451_5 = this.map.get(identifier);
         if (local2451_5 != null && local2451_5.texture() == currentTexture) {
            return local2451_5.value();
         }

         TextureSetup textureSetup = TextureSetup.of(currentTexture.getGlTextureView(), RenderSystem.getSamplerCache().get(FilterMode.NEAREST));
         TextureBinding textureBinding = new TextureBinding(
            identifier, textureSetup, TextureSetup.of(currentTexture.getGlTextureView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR))
         );
         this.map.put(identifier, new ItemRenderer.DataRecord6(currentTexture, textureBinding));
         return textureBinding;
      } else {
         return null;
      }
   }

   public void updateState6() {
      this.map2.clear();
      this.map3.clear();
      this.map.clear();
   }

   private record DataRecord(Item item, ComponentMap components, int count, int seed) {
      static ItemRenderer.DataRecord of(ItemStack itemStack, int local2460_1) {
         return new ItemRenderer.DataRecord(itemStack.getItem(), itemStack.getImmutableComponents(), itemStack.getCount(), local2460_1);
      }
   }

   private record DataRecord2(GuiRenderState state, int layerSerial, Identifier texture, ItemRenderer.DataRecord3 pose, ScreenRect scissor) {
   }

   private record DataRecord3(float m00, float m01, float m10, float m11, float m20, float m21) {
      static ItemRenderer.DataRecord3 of(Matrix3x2f matrix3x2f) {
         return new ItemRenderer.DataRecord3(
            matrix3x2f.m00(), matrix3x2f.m01(), matrix3x2f.m10(), matrix3x2f.m11(), matrix3x2f.m20(), matrix3x2f.m21()
         );
      }
   }

   private record DataRecord4(
      Identifier atlas,
      float x0,
      float y0,
      float u0,
      float v0,
      float x1,
      float y1,
      float u1,
      float v1,
      float x2,
      float y2,
      float u2,
      float v2,
      float x3,
      float y3,
      float u3,
      float v3,
      int tint,
      boolean foil,
      float depth,
      float area,
      float z0,
      float z1,
      float z2,
      float z3
   ) {
   }

   private static final class SubContext5 {
      private float floatVal = Float.MAX_VALUE;
      private float floatVal2 = Float.MAX_VALUE;
      private float floatVal3 = Float.MAX_VALUE;
      private float floatVal4 = -Float.MAX_VALUE;
      private float floatVal5 = -Float.MAX_VALUE;
      private float floatVal6 = -Float.MAX_VALUE;
      private boolean flag2;

      private void updateState7() {
         this.flag2 = true;
      }

      private boolean checkState() {
         return this.flag2 || this.floatVal6 - this.floatVal3 > 0.02F;
      }

      private void processData6(float local2515_1, float local2515_2, float local2515_3) {
         this.floatVal = Math.min(this.floatVal, local2515_1);
         this.floatVal2 = Math.min(this.floatVal2, local2515_2);
         this.floatVal3 = Math.min(this.floatVal3, local2515_3);
         this.floatVal4 = Math.max(this.floatVal4, local2515_1);
         this.floatVal5 = Math.max(this.floatVal5, local2515_2);
         this.floatVal6 = Math.max(this.floatVal6, local2515_3);
      }

      private boolean checkState2() {
         return this.floatVal <= this.floatVal4 && this.floatVal2 <= this.floatVal5;
      }
   }

   private record DataRecord6(AbstractTexture texture, TextureBinding value) {
   }

   private record DataRecord7(float minX, float maxY, float scale, float offsetX, float offsetY) {
      private float x(float local2526_1) {
         return this.offsetX + (local2526_1 - this.minX) * this.scale;
      }

      private float y(float local2528_1) {
         return this.offsetY + (this.maxY - local2528_1) * this.scale;
      }

      private static ItemRenderer.DataRecord7 of(ItemRenderer.SubContext5 local2536_0) {
         if (!local2536_0.checkState2()) {
            return new ItemRenderer.DataRecord7(0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
         } else {
            float local2536_1 = Math.max(1.0E-4F, local2536_0.floatVal4 - local2536_0.floatVal);
            float local2536_2 = Math.max(1.0E-4F, local2536_0.floatVal5 - local2536_0.floatVal2);
            float local2536_3 = Math.max(local2536_1, local2536_2);
            if (local2536_0.checkState()) {
               float local2536_4 = Math.min(1.0F / local2536_3, 1.08F);
               return new ItemRenderer.DataRecord7(
                  (local2536_0.floatVal + local2536_0.floatVal4) * 0.5F, (local2536_0.floatVal2 + local2536_0.floatVal5) * 0.5F, local2536_4, 0.5F, 0.5F
               );
            } else {
               float local2536_4 = 1.0F / local2536_3;
               return new ItemRenderer.DataRecord7(
                  local2536_0.floatVal,
                  local2536_0.floatVal5,
                  local2536_4,
                  (1.0F - local2536_1 * local2536_4) * 0.5F,
                  (1.0F - local2536_2 * local2536_4) * 0.5F
               );
            }
         }
      }
   }

   private record CachedItemKey(Object identity, int seed) {
   }
}
