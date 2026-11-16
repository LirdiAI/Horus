package horus.client.cosmetic;

import horus.client.HorusClient;
import horus.client.cosmetic.CosmeticWarmup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class CosmeticModel {
   private static final Map<Integer, CosmeticModel> map = new ConcurrentHashMap<>();
   private static final String[] strValArray = new String[]{"north", "south", "east", "west", "up", "down"};
   private static final float[] floatValArray = new float[]{0.0F, 0.0F, 0.0F};
   private static final float[] floatValArray2 = new float[]{1.0F, 1.0F, 1.0F};
   private final ArrayList<CosmeticModel.TextureDefinition> list;
   private final ArrayList<CosmeticModel.BoneDefinition> list2;
   private final CosmeticModel.AnimationDefinition nested0412;
   private final CosmeticModel.AnimationDefinition nested04122;
   private final float[] floatValArray3;
   private final HashMap<String, Transformation> map2;
   private final float[] floatValArray4 = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
   private final Vector3fc[] vector3fcArray;
   private final boolean flag;
   private static final MatrixStack matrixStack3 = new MatrixStack();
   private static final Quaternionf quaternionf2 = new Quaternionf();
   private final CosmeticModel.BoneDefinition[] nested0411Array;
   private final CosmeticModel.KeyframeDefinition[][][] nested0414Array2;
   private final CosmeticModel.KeyframeDefinition[][][] nested0414Array3;
   private final float[] floatValArray5;
   private final float[][] floatValArray6 = new float[3][3];
   private final float[][] floatValArray7 = new float[3][3];
   private final Quaternionf quaternionf3;
   private float floatVal;
   private float floatVal2;
   private boolean flag2;
   private int intVal;
   private int intVal2;

   private void renderElement(MatrixStack matrixStack, VertexConsumer vertexConsumer, int local2867_3, float local2867_4, float local2867_5, int local2867_6) {
      this.processData3(local2867_4, local2867_5);
      CosmeticModel.AtlasDefinition local2867_7 = this.list.get(local2867_6).animation();
      if (local2867_7 != null) {
         int local2867_8 = local2867_7.frameAt((int)(local2867_4 * 20.0F));
         this.intVal = local2867_8 % local2867_7.columns();
         this.intVal2 = local2867_8 / local2867_7.columns();
      }

      for (CosmeticModel.BoneDefinition local2867_9 : this.list2) {
         this.renderElement7(local2867_9, matrixStack, vertexConsumer, local2867_3, local2867_7, local2867_6);
      }
   }

   private static float[] computeFloat(JsonArray jsonArray, int local2869_1) {
      float[] floatArray = new float[local2869_1];

      for (int currentIndex = 0; currentIndex < Math.min(local2869_1, jsonArray.size()); currentIndex++) {
         floatArray[currentIndex] = jsonArray.get(currentIndex).getAsFloat();
      }

      return floatArray;
   }

   private static void processData(CosmeticModel.BoneDefinition local2870_0, ArrayList<CosmeticModel.BoneDefinition> local2870_1) {
      local2870_1.add(local2870_0);

      for (CosmeticModel.BoneDefinition local2870_3 : local2870_0.children()) {
         processData(local2870_3, local2870_1);
      }
   }

   private static float[] computeFloat2(
      CosmeticModel.KeyframeDefinition[] nested0414Array, float local2871_1, float local2871_2, float[] floatArray, float[] floatArray2
   ) {
      if (nested0414Array.length == 0) {
         return floatArray;
      }

      if (nested0414Array.length == 1) {
         return nested0414Array[0].value();
      }

      CosmeticModel.KeyframeDefinition local2871_5 = nested0414Array[nested0414Array.length - 1];
      CosmeticModel.KeyframeDefinition local2871_6 = nested0414Array[0];
      float local2871_7 = local2871_5.time() - local2871_2;
      float local2871_8 = local2871_6.time();

      for (int currentIndex = 0; currentIndex < nested0414Array.length - 1; currentIndex++) {
         if (local2871_1 >= nested0414Array[currentIndex].time() && local2871_1 <= nested0414Array[currentIndex + 1].time()) {
            local2871_5 = nested0414Array[currentIndex];
            local2871_6 = nested0414Array[currentIndex + 1];
            local2871_7 = local2871_5.time();
            local2871_8 = local2871_6.time();
            break;
         }
      }

      if (local2871_1 > nested0414Array[nested0414Array.length - 1].time()) {
         local2871_5 = nested0414Array[nested0414Array.length - 1];
         local2871_6 = nested0414Array[0];
         local2871_7 = local2871_5.time();
         local2871_8 = local2871_6.time() + local2871_2;
      }

      if (local2871_5.step()) {
         return local2871_5.value();
      }

      float local2871_9 = local2871_1 < local2871_7 ? local2871_1 + local2871_2 : local2871_1;
      float local2871_10 = local2871_8 == local2871_7 ? 0.0F : MathHelper.clamp((local2871_9 - local2871_7) / (local2871_8 - local2871_7), 0.0F, 1.0F);
      floatArray2[0] = MathHelper.lerp(local2871_10, local2871_5.value()[0], local2871_6.value()[0]);
      floatArray2[1] = MathHelper.lerp(local2871_10, local2871_5.value()[1], local2871_6.value()[1]);
      floatArray2[2] = MathHelper.lerp(local2871_10, local2871_5.value()[2], local2871_6.value()[2]);
      return floatArray2;
   }

   public static boolean matchesCriteria(CosmeticEntry cosmeticEntry) {
      if (map.containsKey(cosmeticEntry.id())) {
         return true;
      }

      try {
         JsonObject asJsonObject = JsonParser.parseString(new String(CosmeticPreviewAtlas.getByte(cosmeticEntry.modelResource()), StandardCharsets.UTF_8))
            .getAsJsonObject();
         ArrayList<CosmeticModel.TextureDefinition> local2872_2 = new ArrayList<>();

         for (JsonElement jsonElement : asJsonObject.getAsJsonArray("textures")) {
            JsonObject asJsonObject2 = jsonElement.getAsJsonObject();
            String asString = asJsonObject2.get("file").getAsString();
            CosmeticModel.AtlasDefinition local2872_7 = null;
            if (asJsonObject2.has("animation")) {
               JsonObject asJsonObject3 = asJsonObject2.getAsJsonObject("animation");
               JsonArray asJsonArray = asJsonObject3.getAsJsonArray("frames");
               int[] intArray = new int[asJsonArray.size()];
               int[] intArray2 = new int[asJsonArray.size()];
               int local2872_12 = 0;

               for (int currentIndex = 0; currentIndex < asJsonArray.size(); currentIndex++) {
                  intArray[currentIndex] = asJsonArray.get(currentIndex).getAsJsonArray().get(0).getAsInt();
                  intArray2[currentIndex] = Math.max(1, asJsonArray.get(currentIndex).getAsJsonArray().get(1).getAsInt());
                  local2872_12 += intArray2[currentIndex];
               }

               local2872_7 = new CosmeticModel.AtlasDefinition(
                  asJsonObject3.get("columns").getAsInt(),
                  asJsonObject3.get("rows").getAsInt(),
                  asJsonObject3.get("imageWidth").getAsInt(),
                  asJsonObject3.get("imageHeight").getAsInt(),
                  intArray,
                  intArray2,
                  local2872_12
               );
            }

            local2872_2.add(
               new CosmeticModel.TextureDefinition(
                  Identifier.of("horus", "cosmetics/" + asString),
                  asString,
                  asJsonObject2.get("uvWidth").getAsFloat(),
                  asJsonObject2.get("uvHeight").getAsFloat(),
                  local2872_7
               )
            );
         }

         ArrayList<CosmeticModel.BoneDefinition> local2872_3 = new ArrayList<>();
         int[] intArray3 = new int[]{0};

         for (JsonElement jsonElement2 : asJsonObject.getAsJsonArray("bones")) {
            local2872_3.add(getNested0411(jsonElement2.getAsJsonObject(), floatValArray, local2872_2, intArray3));
         }

         JsonObject jsonObject = asJsonObject.has("animations") ? asJsonObject.getAsJsonObject("animations") : new JsonObject();
         HashMap<String, Transformation> local2872_6 = new HashMap<>();
         if (asJsonObject.has("display")) {
            for (Entry<String, JsonElement> local2872_8 : asJsonObject.getAsJsonObject("display").entrySet()) {
               float[] floatArray = computeFloat(local2872_8.getValue().getAsJsonArray(), 9);
               local2872_6.put(
                  local2872_8.getKey(),
                  new Transformation(
                     new Vector3f(floatArray[0], floatArray[1], floatArray[2]),
                     new Vector3f(floatArray[3], floatArray[4], floatArray[5]),
                     new Vector3f(floatArray[6], floatArray[7], floatArray[8])
                  )
               );
            }
         }

         CosmeticModel cosmeticModel = new CosmeticModel(
            local2872_2,
            local2872_3,
            jsonObject.has("idle") ? getNested0412(jsonObject.getAsJsonObject("idle")) : null,
            jsonObject.has("walk") ? getNested0412(jsonObject.getAsJsonObject("walk")) : null,
            asJsonObject.has("displayHead") ? computeFloat(asJsonObject.getAsJsonArray("displayHead"), 9) : null,
            local2872_6
         );

         for (int currentIndex2 = 0; currentIndex2 < local2872_2.size(); currentIndex2++) {
            if (!CosmeticPreviewAtlas.matchesCriteria2(
               local2872_2.get(currentIndex2).id(),
               local2872_2.get(currentIndex2).file(),
               currentIndex2 == local2872_2.size() - 1 ? () -> map.put(cosmeticEntry.id(), cosmeticModel) : null
            )) {
               return false;
            }
         }

         return !local2872_2.isEmpty();
      } catch (Exception exception) {
         HorusClient.logger().warn("[Cosmetics] model {} not loaded: {}", cosmeticEntry.modelResource(), exception.toString());
         return false;
      }
   }

   private static void renderElement2(MatrixStack matrixStack, float local2873_1, float local2873_2, float local2873_3) {
      if (local2873_3 != 0.0F) {
         matrixStack.multiply(quaternionf2.rotationZ(local2873_3 * (float) (Math.PI / 180.0)));
      }

      if (local2873_2 != 0.0F) {
         matrixStack.multiply(quaternionf2.rotationY(local2873_2 * (float) (Math.PI / 180.0)));
      }

      if (local2873_1 != 0.0F) {
         matrixStack.multiply(quaternionf2.rotationX(local2873_1 * (float) (Math.PI / 180.0)));
      }
   }

   public void renderElement3(MatrixStack matrixStack) {
      if (this.floatValArray3 != null) {
         matrixStack.translate(this.floatValArray3[3], this.floatValArray3[4], this.floatValArray3[5]);
         matrixStack.multiply(this.quaternionf3);
         matrixStack.scale(this.floatValArray3[6], this.floatValArray3[7], this.floatValArray3[8]);
      }

      matrixStack.translate(-0.5F, -0.5F, -0.5F);
   }

   public void renderElement4(MatrixStack matrixStack, ItemDisplayContext itemDisplayContext) {
      float local2876_3 = Math.max(1.0F, this.floatValArray4[3] - this.floatValArray4[0]);
      float local2876_4 = Math.max(1.0F, this.floatValArray4[4] - this.floatValArray4[1]);
      float local2876_5 = Math.min(18.0F / local2876_3, 22.0F / local2876_4);
      matrixStack.translate((itemDisplayContext.isLeftHand() ? -1.5F : 1.5F) / 16.0F, (itemDisplayContext.isFirstPerson() ? 1.5F : 0.0F) / 16.0F, -0.125F);
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(itemDisplayContext.isLeftHand() ? 90.0F : -90.0F));
      matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(itemDisplayContext.isLeftHand() ? -8.0F : 8.0F));
      matrixStack.scale(local2876_5, local2876_5, local2876_5);
      matrixStack.translate(
         -(this.floatValArray4[0] + this.floatValArray4[3]) / 32.0F,
         -(this.floatValArray4[1] + this.floatValArray4[4]) / 32.0F,
         -(this.floatValArray4[2] + this.floatValArray4[5]) / 32.0F
      );
   }

   private CosmeticModel(
      ArrayList<CosmeticModel.TextureDefinition> local2877_1,
      ArrayList<CosmeticModel.BoneDefinition> local2877_2,
      CosmeticModel.AnimationDefinition local2877_3,
      CosmeticModel.AnimationDefinition local2877_4,
      float[] floatArray,
      HashMap<String, Transformation> local2877_6
   ) {
      this.list = local2877_1;
      this.list2 = local2877_2;
      this.nested0412 = local2877_3;
      this.nested04122 = local2877_4;
      this.floatValArray3 = floatArray;
      this.map2 = local2877_6;
      ArrayList<CosmeticModel.BoneDefinition> local2877_7 = new ArrayList<>();

      for (CosmeticModel.BoneDefinition local2877_9 : local2877_2) {
         processData(local2877_9, local2877_7);
      }

      int local2877_8 = 0;

      for (CosmeticModel.BoneDefinition local2877_10 : local2877_7) {
         local2877_8 = Math.max(local2877_8, local2877_10.index() + 1);
      }

      this.nested0411Array = new CosmeticModel.BoneDefinition[local2877_8];
      this.nested0414Array2 = new CosmeticModel.KeyframeDefinition[local2877_8][][];
      this.nested0414Array3 = new CosmeticModel.KeyframeDefinition[local2877_8][][];

      for (CosmeticModel.BoneDefinition local2877_10 : local2877_7) {
         this.nested0411Array[local2877_10.index()] = local2877_10;
         this.nested0414Array2[local2877_10.index()] = local2877_3 == null ? null : local2877_3.tracksByBone().get(local2877_10.uuid());
         this.nested0414Array3[local2877_10.index()] = local2877_4 == null ? null : local2877_4.tracksByBone().get(local2877_10.uuid());
      }

      this.floatValArray5 = new float[local2877_8 * 9];
      this.quaternionf3 = floatArray == null
         ? null
         : new Quaternionf()
            .rotationXYZ(floatArray[0] * (float) (Math.PI / 180.0), floatArray[1] * (float) (Math.PI / 180.0), floatArray[2] * (float) (Math.PI / 180.0));

      for (CosmeticModel.BoneDefinition local2877_10 : local2877_2) {
         this.processData4(local2877_10);
      }

      if (this.floatValArray4[0] == Float.MAX_VALUE) {
         System.arraycopy(new float[]{0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F}, 0, this.floatValArray4, 0, 6);
      }

      this.vector3fcArray = new Vector3fc[8];

      for (int currentIndex = 0; currentIndex < 8; currentIndex++) {
         this.vector3fcArray[currentIndex] = new Vector3f(
            this.floatValArray4[(currentIndex & 1) == 0 ? 0 : 3] / 16.0F,
            this.floatValArray4[(currentIndex & 2) == 0 ? 1 : 4] / 16.0F,
            this.floatValArray4[(currentIndex & 4) == 0 ? 2 : 5] / 16.0F
         );
      }

      this.flag = local2877_1.stream().anyMatch(local2881_0 -> local2881_0.animation() != null);
   }

   public boolean checkState() {
      return this.flag;
   }

   public Transformation getTransformation(ItemDisplayContext itemDisplayContext) {
      return this.map2.getOrDefault(itemDisplayContext.asString(), Transformation.IDENTITY);
   }

   public void renderElement5(MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, int local2882_3, float local2882_4, float local2882_5) {
      for (int currentIndex = 0; currentIndex < this.list.size(); currentIndex++) {
         int currentIndex2 = currentIndex;
         orderedRenderCommandQueue.submitCustom(matrixStack, RenderLayers.entityCutoutNoCull(this.list.get(currentIndex).id()), (entry, vertexConsumer) -> {
            MatrixStack matrixStack2 = matrixStack3;
            matrixStack2.peek().getPositionMatrix().set(entry.getPositionMatrix());
            matrixStack2.peek().getNormalMatrix().set(entry.getNormalMatrix());
            this.renderElement(matrixStack2, vertexConsumer, local2882_3, local2882_4, local2882_5, currentIndex2);
         });
      }
   }

   private static CosmeticModel.AnimationDefinition getNested0412(JsonObject jsonObject) {
      HashMap<String, CosmeticModel.KeyframeDefinition[][]> local2883_1 = new HashMap<>();

      for (Entry<String, JsonElement> local2883_3 : jsonObject.getAsJsonObject("tracks").entrySet()) {
         ArrayList<CosmeticModel.KeyframeDefinition> local2883_4 = new ArrayList<>();
         ArrayList<CosmeticModel.KeyframeDefinition> local2883_5 = new ArrayList<>();
         ArrayList<CosmeticModel.KeyframeDefinition> local2883_6 = new ArrayList<>();

         for (JsonElement jsonElement : local2883_3.getValue().getAsJsonArray()) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            CosmeticModel.KeyframeDefinition local2883_10 = new CosmeticModel.KeyframeDefinition(
               asJsonObject.get("time").getAsFloat(), computeFloat(asJsonObject.getAsJsonArray("value"), 3), asJsonObject.get("step").getAsBoolean()
            );
            switch (asJsonObject.get("channel").getAsString()) {
               case "position":
                  local2883_4.add(local2883_10);
                  break;
               case "scale":
                  local2883_6.add(local2883_10);
                  break;
               default:
                  local2883_5.add(local2883_10);
            }
         }

         local2883_1.put(
            local2883_3.getKey(),
            new CosmeticModel.KeyframeDefinition[][]{
               local2883_4.toArray(CosmeticModel.KeyframeDefinition[]::new),
               local2883_5.toArray(CosmeticModel.KeyframeDefinition[]::new),
               local2883_6.toArray(CosmeticModel.KeyframeDefinition[]::new)
            }
         );
      }

      return new CosmeticModel.AnimationDefinition(jsonObject.get("length").getAsFloat(), local2883_1);
   }

   public void renderElement6(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int local2885_3, float local2885_4, float local2885_5) {
      for (int currentIndex = 0; currentIndex < this.list.size(); currentIndex++) {
         VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayers.entityCutoutNoCull(this.list.get(currentIndex).id()));
         this.renderElement(matrixStack, vertexConsumer, local2885_3, local2885_4, local2885_5, currentIndex);
      }
   }

   private static void processData2(CosmeticModel.KeyframeDefinition[] nested0414Array, float local2887_1, float local2887_2, float[] floatArray, float[] floatArray2) {
      float[] floatArray3 = computeFloat2(nested0414Array, local2887_1, local2887_2, floatArray, floatArray2);
      if (floatArray3 != floatArray2) {
         System.arraycopy(floatArray3, 0, floatArray2, 0, 3);
      }
   }

   private void renderElement7(
      CosmeticModel.BoneDefinition local2888_1,
      MatrixStack matrixStack,
      VertexConsumer vertexConsumer,
      int local2888_4,
      CosmeticModel.AtlasDefinition local2888_5,
      int local2888_6
   ) {
      float[] floatArray = this.floatValArray5;
      int local2888_8 = local2888_1.index() * 9;
      matrixStack.push();
      matrixStack.translate(floatArray[local2888_8], floatArray[local2888_8 + 1], floatArray[local2888_8 + 2]);
      renderElement2(matrixStack, floatArray[local2888_8 + 3], floatArray[local2888_8 + 4], floatArray[local2888_8 + 5]);
      matrixStack.scale(floatArray[local2888_8 + 6], floatArray[local2888_8 + 7], floatArray[local2888_8 + 8]);

      for (CosmeticModel.CubeDefinition local2888_10 : local2888_1.cubes()) {
         this.renderElement8(local2888_10, matrixStack, vertexConsumer, local2888_4, local2888_5, local2888_6);
      }

      for (CosmeticModel.BoneDefinition local2888_10 : local2888_1.children()) {
         this.renderElement7(local2888_10, matrixStack, vertexConsumer, local2888_4, local2888_5, local2888_6);
      }

      matrixStack.pop();
   }

   public static CosmeticModel getCosmeticModel(CosmeticEntry cosmeticEntry) {
      CosmeticModel cosmeticModel = map.get(cosmeticEntry.id());
      if (cosmeticModel == null) {
         CosmeticWarmup.processData(cosmeticEntry);
      }

      return cosmeticModel;
   }

   private void processData3(float local2891_1, float local2891_2) {
      if (!this.flag2 || this.floatVal != local2891_1 || this.floatVal2 != local2891_2) {
         float local2891_3 = this.nested0412 != null && !(this.nested0412.length() <= 0.0F) ? local2891_1 % this.nested0412.length() : 0.0F;
         float local2891_4 = this.nested04122 != null && !(this.nested04122.length() <= 0.0F) ? local2891_1 % this.nested04122.length() : 0.0F;
         float[] floatArray = this.floatValArray5;
         float[] floatArray2 = this.floatValArray6[0];
         float[] floatArray3 = this.floatValArray6[1];
         float[] floatArray4 = this.floatValArray6[2];
         float[] floatArray5 = this.floatValArray7[0];
         float[] floatArray6 = this.floatValArray7[1];
         float[] floatArray7 = this.floatValArray7[2];

         for (int currentIndex = 0; currentIndex < this.nested0411Array.length; currentIndex++) {
            CosmeticModel.BoneDefinition local2891_13 = this.nested0411Array[currentIndex];
            CosmeticModel.KeyframeDefinition[][] nested0414ArrayArray = this.nested0414Array2[currentIndex];
            CosmeticModel.KeyframeDefinition[][] nested0414ArrayArray2 = this.nested0414Array3[currentIndex];
            if (nested0414ArrayArray != null) {
               processData2(nested0414ArrayArray[0], local2891_3, this.nested0412.length(), floatValArray, floatArray2);
               processData2(nested0414ArrayArray[1], local2891_3, this.nested0412.length(), floatValArray, floatArray3);
               processData2(nested0414ArrayArray[2], local2891_3, this.nested0412.length(), floatValArray2, floatArray4);
            } else {
               System.arraycopy(floatValArray, 0, floatArray2, 0, 3);
               System.arraycopy(floatValArray, 0, floatArray3, 0, 3);
               System.arraycopy(floatValArray2, 0, floatArray4, 0, 3);
            }

            if (nested0414ArrayArray2 != null) {
               processData2(nested0414ArrayArray2[0], local2891_4, this.nested04122.length(), floatValArray, floatArray5);
               processData2(nested0414ArrayArray2[1], local2891_4, this.nested04122.length(), floatValArray, floatArray6);
               processData2(nested0414ArrayArray2[2], local2891_4, this.nested04122.length(), floatValArray2, floatArray7);
            } else {
               System.arraycopy(floatValArray, 0, floatArray5, 0, 3);
               System.arraycopy(floatValArray, 0, floatArray6, 0, 3);
               System.arraycopy(floatValArray2, 0, floatArray7, 0, 3);
            }

            int local2891_16 = currentIndex * 9;

            for (int currentIndex2 = 0; currentIndex2 < 3; currentIndex2++) {
               floatArray[local2891_16 + currentIndex2] = (
                     local2891_13.offset()[currentIndex2] + MathHelper.lerp(local2891_2, floatArray2[currentIndex2], floatArray5[currentIndex2])
                  )
                  / 16.0F;
               floatArray[local2891_16 + 3 + currentIndex2] = local2891_13.rotation()[currentIndex2]
                  + MathHelper.lerp(local2891_2, floatArray3[currentIndex2], floatArray6[currentIndex2]);
               floatArray[local2891_16 + 6 + currentIndex2] = MathHelper.lerp(local2891_2, floatArray4[currentIndex2], floatArray7[currentIndex2]);
            }
         }

         this.floatVal = local2891_1;
         this.floatVal2 = local2891_2;
         this.flag2 = true;
      }
   }

   private void processData4(CosmeticModel.BoneDefinition local2892_1) {
      for (CosmeticModel.CubeDefinition local2892_3 : local2892_1.cubes()) {
         for (int currentIndex = 0; currentIndex < 3; currentIndex++) {
            this.floatValArray4[currentIndex] = Math.min(this.floatValArray4[currentIndex], Math.min(local2892_3.from()[currentIndex], local2892_3.to()[currentIndex]));
            this.floatValArray4[currentIndex + 3] = Math.max(
               this.floatValArray4[currentIndex + 3], Math.max(local2892_3.from()[currentIndex], local2892_3.to()[currentIndex])
            );
         }
      }

      for (CosmeticModel.BoneDefinition local2892_3 : local2892_1.children()) {
         this.processData4(local2892_3);
      }
   }

   public Vector3fc[] getVector3fc() {
      return this.vector3fcArray;
   }

   private static CosmeticModel.BoneDefinition getNested0411(
      JsonObject jsonObject, float[] floatArray, ArrayList<CosmeticModel.TextureDefinition> local2894_2, int[] intArray
   ) {
      int local2894_4 = (int)(intArray[0]++);
      float[] floatArray2 = computeFloat(jsonObject.getAsJsonArray("origin"), 3);
      ArrayList<CosmeticModel.CubeDefinition> local2894_6 = new ArrayList<>();

      for (JsonElement jsonElement : jsonObject.getAsJsonArray("cubes")) {
         JsonObject asJsonObject = jsonElement.getAsJsonObject();
         float[] floatArray3 = computeFloat(asJsonObject.getAsJsonArray("from"), 3);
         float[] floatArray4 = computeFloat(asJsonObject.getAsJsonArray("to"), 3);
         float[] floatArray5 = computeFloat(asJsonObject.getAsJsonArray("origin"), 3);
         float[] floatArray6 = computeFloat(asJsonObject.getAsJsonArray("rotation"), 3);
         float local2894_14 = (Math.min(floatArray3[0], floatArray4[0]) - floatArray5[0]) / 16.0F;
         float local2894_15 = (Math.min(floatArray3[1], floatArray4[1]) - floatArray5[1]) / 16.0F;
         float local2894_16 = (Math.min(floatArray3[2], floatArray4[2]) - floatArray5[2]) / 16.0F;
         float local2894_17 = (Math.max(floatArray3[0], floatArray4[0]) - floatArray5[0]) / 16.0F;
         float local2894_18 = (Math.max(floatArray3[1], floatArray4[1]) - floatArray5[1]) / 16.0F;
         float local2894_19 = (Math.max(floatArray3[2], floatArray4[2]) - floatArray5[2]) / 16.0F;
         boolean local2894_20 = local2894_17 - local2894_14 < 5.0E-4F;
         boolean local2894_21 = local2894_18 - local2894_15 < 5.0E-4F;
         boolean local2894_22 = local2894_19 - local2894_16 < 5.0E-4F;
         boolean[] booleanArray = new boolean[local2894_2.size()];
         ArrayList<CosmeticModel.FaceUvDefinition> local2894_24 = new ArrayList<>();

         for (JsonElement jsonElement2 : asJsonObject.getAsJsonArray("faces")) {
            JsonObject asJsonObject2 = jsonElement2.getAsJsonObject();
            float[] floatArray7 = computeFloat(asJsonObject2.getAsJsonArray("uv"), 4);
            int local2894_29 = 0;

            while (local2894_29 < 5 && !strValArray[local2894_29].equals(asJsonObject2.get("dir").getAsString())) {
               local2894_29++;
            }

            int asInt = asJsonObject2.get("texture").getAsInt();
            boolean local2894_31 = asInt >= 0 && asInt < local2894_2.size();
            boolean local2894_32 = !local2894_31
               || (local2894_29 <= 1 ? local2894_20 || local2894_21 : (local2894_29 <= 3 ? local2894_21 || local2894_22 : local2894_20 || local2894_22));
            float local2894_33 = local2894_31 ? local2894_2.get(asInt).uvWidth() : 1.0F;
            float local2894_34 = local2894_31 ? local2894_2.get(asInt).uvHeight() : 1.0F;
            float local2894_36 = 0.0F;
            float local2894_37 = 0.0F;
            float local2894_38 = 0.0F;
            float[] floatArray8;
            switch (local2894_29) {
               case 0:
                  floatArray8 = new float[]{
                     local2894_17,
                     local2894_18,
                     local2894_16,
                     local2894_17,
                     local2894_15,
                     local2894_16,
                     local2894_14,
                     local2894_15,
                     local2894_16,
                     local2894_14,
                     local2894_18,
                     local2894_16
                  };
                  local2894_38 = -1.0F;
                  break;
               case 1:
                  floatArray8 = new float[]{
                     local2894_14,
                     local2894_18,
                     local2894_19,
                     local2894_14,
                     local2894_15,
                     local2894_19,
                     local2894_17,
                     local2894_15,
                     local2894_19,
                     local2894_17,
                     local2894_18,
                     local2894_19
                  };
                  local2894_38 = 1.0F;
                  break;
               case 2:
                  floatArray8 = new float[]{
                     local2894_17,
                     local2894_18,
                     local2894_19,
                     local2894_17,
                     local2894_15,
                     local2894_19,
                     local2894_17,
                     local2894_15,
                     local2894_16,
                     local2894_17,
                     local2894_18,
                     local2894_16
                  };
                  local2894_36 = 1.0F;
                  break;
               case 3:
                  floatArray8 = new float[]{
                     local2894_14,
                     local2894_18,
                     local2894_16,
                     local2894_14,
                     local2894_15,
                     local2894_16,
                     local2894_14,
                     local2894_15,
                     local2894_19,
                     local2894_14,
                     local2894_18,
                     local2894_19
                  };
                  local2894_36 = -1.0F;
                  break;
               case 4:
                  floatArray8 = new float[]{
                     local2894_14,
                     local2894_18,
                     local2894_16,
                     local2894_14,
                     local2894_18,
                     local2894_19,
                     local2894_17,
                     local2894_18,
                     local2894_19,
                     local2894_17,
                     local2894_18,
                     local2894_16
                  };
                  local2894_37 = 1.0F;
                  break;
               default:
                  floatArray8 = new float[]{
                     local2894_14,
                     local2894_15,
                     local2894_19,
                     local2894_14,
                     local2894_15,
                     local2894_16,
                     local2894_17,
                     local2894_15,
                     local2894_16,
                     local2894_17,
                     local2894_15,
                     local2894_19
                  };
                  local2894_37 = -1.0F;
            }

            if (!local2894_32) {
               booleanArray[asInt] = true;
            }

            local2894_24.add(
               new CosmeticModel.FaceUvDefinition(
                  local2894_29,
                  floatArray7[0] / local2894_33,
                  floatArray7[1] / local2894_34,
                  floatArray7[2] / local2894_33,
                  floatArray7[3] / local2894_34,
                  Math.floorMod(asJsonObject2.get("rotation").getAsInt() / 90, 4),
                  asInt,
                  local2894_32,
                  floatArray8,
                  local2894_36,
                  local2894_37,
                  local2894_38
               )
            );
         }

         Quaternionf quaternionf = floatArray6[0] == 0.0F && floatArray6[1] == 0.0F && floatArray6[2] == 0.0F
            ? null
            : new Quaternionf()
               .rotationZ(floatArray6[2] * (float) (Math.PI / 180.0))
               .rotateY(floatArray6[1] * (float) (Math.PI / 180.0))
               .rotateX(floatArray6[0] * (float) (Math.PI / 180.0));
         float[] floatArray9 = new float[]{
            (floatArray5[0] - floatArray2[0]) / 16.0F, (floatArray5[1] - floatArray2[1]) / 16.0F, (floatArray5[2] - floatArray2[2]) / 16.0F
         };
         local2894_6.add(
            new CosmeticModel.CubeDefinition(floatArray3, floatArray4, floatArray5, floatArray6, local2894_24, floatArray9, quaternionf, booleanArray)
         );
      }

      ArrayList<CosmeticModel.BoneDefinition> local2894_7 = new ArrayList<>();

      for (JsonElement jsonElement3 : jsonObject.getAsJsonArray("children")) {
         local2894_7.add(getNested0411(jsonElement3.getAsJsonObject(), floatArray2, local2894_2, intArray));
      }

      float[] floatArray10 = new float[]{floatArray2[0] - floatArray[0], floatArray2[1] - floatArray[1], floatArray2[2] - floatArray[2]};
      return new CosmeticModel.BoneDefinition(
         local2894_4,
         jsonObject.get("uuid").getAsString(),
         floatArray2,
         floatArray10,
         computeFloat(jsonObject.getAsJsonArray("rotation"), 3),
         local2894_6,
         local2894_7
      );
   }

   private void renderElement8(
      CosmeticModel.CubeDefinition local2895_1,
      MatrixStack matrixStack,
      VertexConsumer vertexConsumer,
      int local2895_4,
      CosmeticModel.AtlasDefinition local2895_5,
      int local2895_6
   ) {
      if (local2895_1.hasTexture()[local2895_6]) {
         matrixStack.push();
         matrixStack.translate(local2895_1.offset()[0], local2895_1.offset()[1], local2895_1.offset()[2]);
         if (local2895_1.rotationQuaternion() != null) {
            matrixStack.multiply(local2895_1.rotationQuaternion());
         }

         net.minecraft.client.util.math.MatrixStack.Entry entry = matrixStack.peek();

         for (CosmeticModel.FaceUvDefinition local2895_9 : local2895_1.faces()) {
            if (local2895_9.texture() == local2895_6 && !local2895_9.skip()) {
               float local2895_10 = local2895_9.u1();
               float local2895_11 = local2895_9.v1();
               float local2895_12 = local2895_9.u2();
               float local2895_13 = local2895_9.v2();
               if (local2895_5 != null) {
                  local2895_10 = (this.intVal + local2895_10) / local2895_5.columns();
                  local2895_12 = (this.intVal + local2895_12) / local2895_5.columns();
                  local2895_11 = (this.intVal2 + local2895_11) / local2895_5.rows();
                  local2895_13 = (this.intVal2 + local2895_13) / local2895_5.rows();
                  float local2895_14 = 0.5F / local2895_5.imageWidth() * Math.signum(local2895_12 - local2895_10);
                  float local2895_15 = 0.5F / local2895_5.imageHeight() * Math.signum(local2895_13 - local2895_11);
                  local2895_10 += local2895_14;
                  local2895_12 -= local2895_14;
                  local2895_11 += local2895_15;
                  local2895_13 -= local2895_15;
               }

               float[] floatArray = local2895_9.corners();

               for (int currentIndex = 0; currentIndex < 4; currentIndex++) {
                  int local2895_16 = (currentIndex + local2895_9.uvTurns()) % 4;
                  float local2895_17 = local2895_16 <= 1 ? local2895_10 : local2895_12;
                  float local2895_18 = local2895_16 != 0 && local2895_16 != 3 ? local2895_13 : local2895_11;
                  vertexConsumer.vertex(entry, floatArray[currentIndex * 3], floatArray[currentIndex * 3 + 1], floatArray[currentIndex * 3 + 2])
                     .color(255, 255, 255, 255)
                     .texture(local2895_17, local2895_18)
                     .overlay(OverlayTexture.DEFAULT_UV)
                     .light(local2895_4)
                     .normal(entry, local2895_9.nx(), local2895_9.ny(), local2895_9.nz());
               }
            }
         }

         matrixStack.pop();
      }
   }

   private record BoneDefinition(
      int index,
      String uuid,
      float[] origin,
      float[] offset,
      float[] rotation,
      ArrayList<CosmeticModel.CubeDefinition> cubes,
      ArrayList<CosmeticModel.BoneDefinition> children
   ) {
   }

   private record AnimationDefinition(float length, HashMap<String, CosmeticModel.KeyframeDefinition[][]> tracksByBone) {
   }

   private record CubeDefinition(
      float[] from,
      float[] to,
      float[] origin,
      float[] rotation,
      ArrayList<CosmeticModel.FaceUvDefinition> faces,
      float[] offset,
      Quaternionf rotationQuaternion,
      boolean[] hasTexture
   ) {
   }

   private record KeyframeDefinition(float time, float[] value, boolean step) {
   }

   private record FaceUvDefinition(
      int direction,
      float u1,
      float v1,
      float u2,
      float v2,
      int uvTurns,
      int texture,
      boolean skip,
      float[] corners,
      float nx,
      float ny,
      float nz
   ) {
   }

   private record TextureDefinition(Identifier id, String file, float uvWidth, float uvHeight, CosmeticModel.AtlasDefinition animation) {
   }

   private record AtlasDefinition(int columns, int rows, int imageWidth, int imageHeight, int[] frameIndexes, int[] frameTicks, int totalTicks) {
      int frameAt(int local2967_1) {
         int local2967_2 = this.totalTicks == 0 ? 0 : Math.floorMod(local2967_1, this.totalTicks);

         for (int currentIndex = 0; currentIndex < this.frameIndexes.length; currentIndex++) {
            if (local2967_2 < this.frameTicks[currentIndex]) {
               return this.frameIndexes[currentIndex];
            }

            local2967_2 -= this.frameTicks[currentIndex];
         }

         return this.frameIndexes.length == 0 ? 0 : this.frameIndexes[this.frameIndexes.length - 1];
      }
   }
}
