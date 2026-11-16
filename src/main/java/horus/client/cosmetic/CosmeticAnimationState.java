package horus.client.cosmetic;

import horus.client.cosmetic.BedrockBone;
import horus.client.cosmetic.BedrockModel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public final class CosmeticAnimationState {
   final int intVal;
   final String strVal;
   final int intVal2;
   public final float floatVal;
   public final float floatVal2;
   public final float floatVal3;
   public final float floatVal4;
   public final float floatVal5;
   public final float floatVal6;
   public final float floatVal7;
   public final BedrockModel bedrockModel;
   public final Map<String, CosmeticAnimationState.ChannelState> map;
   public final Map<String, float[]> map2 = new HashMap<>();
   private final Map<String, BedrockBone> map3 = new HashMap<>();
   private final String[] strValArray = new String[16];
   private final boolean[] flagArray = new boolean[16];
   public int intVal3;

   private String formatText(String... stringArray) {
      for (String local7724_3 : this.map.keySet()) {
         String local7724_4 = formatText6(local7724_3);

         for (String local7724_8 : stringArray) {
            if (local7724_4.contains(local7724_8)) {
               return local7724_3;
            }
         }
      }

      return null;
   }

   private String formatText2(String stringArray) {
      for (String local7725_3 : this.map.keySet()) {
         if (formatText6(local7725_3).equals(stringArray)) {
            return local7725_3;
         }
      }

      return this.formatText(stringArray);
   }

   String formatText3(boolean local7726_1, boolean local7726_2, boolean local7726_3, boolean local7726_4) {
      if (this.map.isEmpty()) {
         return null;
      }

      if (local7726_1) {
         String local7726_5 = this.formatText("elytra", "flying", "fly");
         if (local7726_5 != null) {
            return local7726_5;
         }
      }

      if (local7726_2) {
         if (local7726_4) {
            String local7726_5 = this.formatText4("moving", "water");
            if (local7726_5 != null) {
               return local7726_5;
            }
         }

         String local7726_5 = this.formatText("swimming", "swim");
         if (local7726_5 != null) {
            return local7726_5;
         }

         local7726_5 = this.formatText4("idle", "water");
         if (local7726_5 != null) {
            return local7726_5;
         }

         local7726_5 = this.formatText4("main", "water");
         if (local7726_5 != null) {
            return local7726_5;
         }
      }

      if (local7726_3) {
         if (local7726_4) {
            String local7726_5 = this.formatText4("moving", "sneak");
            if (local7726_5 != null) {
               return local7726_5;
            }
         }

         String local7726_5 = this.formatText4("idle", "sneak");
         if (local7726_5 != null) {
            return local7726_5;
         }

         local7726_5 = this.formatText4("main", "sneak");
         if (local7726_5 != null) {
            return local7726_5;
         }

         local7726_5 = this.formatText("sneak", "crouch");
         if (local7726_5 != null) {
            return local7726_5;
         }
      }

      if (local7726_4) {
         String local7726_5 = this.formatText("walking", "walk", "moving", "move", "run");
         if (local7726_5 != null && !formatText6(local7726_5).contains("water") && !formatText6(local7726_5).contains("sneak")) {
            return local7726_5;
         }
      }

      String local7726_5 = this.formatText2("idle");
      if (local7726_5 != null && !formatText6(local7726_5).contains("water") && !formatText6(local7726_5).contains("sneak")) {
         return local7726_5;
      }

      String local7726_6 = this.formatText2("main");
      if (local7726_6 != null && !formatText6(local7726_6).contains("water") && !formatText6(local7726_6).contains("sneak")) {
         return local7726_6;
      }

      for (String local7726_8 : this.map.keySet()) {
         String local7726_9 = formatText6(local7726_8);
         if (!local7726_9.contains("gui") && !local7726_9.contains("preview")) {
            return local7726_8;
         }
      }

      return this.map.keySet().iterator().next();
   }

   private void processData(BedrockBone bedrockBone) {
      float[] floatArray = this.map2.get(bedrockBone.strVal);
      if (floatArray != null) {
         bedrockBone.processData5(floatArray[0]);
         bedrockBone.processData7(floatArray[1]);
         bedrockBone.processData8(floatArray[2]);
         bedrockBone.processData2(floatArray[3]);
         bedrockBone.processData13(floatArray[4]);
         bedrockBone.processData4(floatArray[5]);
         bedrockBone.processData9(floatArray[6]);
         bedrockBone.processData12(floatArray[7]);
         bedrockBone.processData6(floatArray[8]);
      }

      for (BedrockBone bedrockBone2 : bedrockBone.list) {
         this.processData(bedrockBone2);
      }
   }

   private String formatText4(String... stringArray) {
      for (String local7728_3 : this.map.keySet()) {
         String local7728_4 = formatText6(local7728_3);
         boolean local7728_5 = true;

         for (String local7728_9 : stringArray) {
            local7728_5 &= local7728_4.contains(local7728_9);
         }

         if (local7728_5) {
            return local7728_3;
         }
      }

      return null;
   }

   public String formatText5(boolean local7729_1, boolean local7729_2, boolean local7729_3, boolean local7729_4) {
      int local7729_5 = (local7729_1 ? 1 : 0) | (local7729_2 ? 2 : 0) | (local7729_3 ? 4 : 0) | (local7729_4 ? 8 : 0);
      if (!this.flagArray[local7729_5]) {
         this.strValArray[local7729_5] = this.formatText3(local7729_1, local7729_2, local7729_3, local7729_4);
         this.flagArray[local7729_5] = true;
      }

      return this.strValArray[local7729_5];
   }

   private static String formatText6(String local7730_0) {
      return local7730_0.toLowerCase(Locale.ROOT);
   }

   private void processData2(BedrockBone bedrockBone) {
      this.map2
         .put(
            bedrockBone.strVal,
            new float[]{
               bedrockBone.computeFloat7(),
               bedrockBone.computeFloat8(),
               bedrockBone.computeFloat4(),
               bedrockBone.computeFloat5(),
               bedrockBone.computeFloat(),
               bedrockBone.computeFloat10(),
               bedrockBone.computeFloat11(),
               bedrockBone.computeFloat3(),
               bedrockBone.computeFloat9()
            }
         );
      this.map3.putIfAbsent(bedrockBone.strVal, bedrockBone);
      this.intVal3 = this.intVal3 + bedrockBone.list2.size();

      for (BedrockBone bedrockBone2 : bedrockBone.list) {
         this.processData2(bedrockBone2);
      }
   }

   public CosmeticAnimationState(
      int local7732_1,
      String local7732_2,
      int local7732_3,
      float local7732_4,
      float local7732_5,
      float local7732_6,
      float local7732_7,
      float local7732_8,
      float local7732_9,
      float local7732_10,
      BedrockModel initialBedrockModel,
      Map<String, CosmeticAnimationState.ChannelState> local7732_12
   ) {
      this.intVal = local7732_1;
      this.strVal = local7732_2;
      this.intVal2 = local7732_3;
      this.floatVal = local7732_4;
      this.floatVal2 = local7732_5;
      this.floatVal3 = local7732_6;
      this.floatVal4 = local7732_7;
      this.floatVal5 = local7732_8;
      this.floatVal6 = local7732_9;
      this.floatVal7 = local7732_10;
      this.bedrockModel = initialBedrockModel;
      this.map = local7732_12;

      for (BedrockBone bedrockBone : initialBedrockModel.list) {
         this.processData2(bedrockBone);
      }
   }

   public void updateState() {
      for (BedrockBone bedrockBone : this.bedrockModel.list) {
         this.processData(bedrockBone);
      }
   }

   public BedrockBone getBedrockBone(String local7734_1) {
      return this.map3.get(local7734_1);
   }

   public static final class BoneState {
      final NavigableMap<Float, float[]> map4 = new TreeMap<>();
      float[] floatValArray;
      private float[] floatValArray2;
      private float[][] floatValArray3;

      public boolean checkState() {
         return this.floatValArray == null && this.map4.isEmpty();
      }

      float[] computeFloat(float local7736_1, float[] floatArray) {
         return this.computeFloat2(local7736_1, floatArray, new float[3]);
      }

      public float[] computeFloat2(float local7737_1, float[] floatArray, float[] floatArray2) {
         if (this.floatValArray != null) {
            return this.floatValArray;
         }

         if (this.map4.isEmpty()) {
            return floatArray;
         }

         if (this.map4.size() == 1) {
            return this.map4.firstEntry().getValue();
         }

         if (this.floatValArray2 == null || this.floatValArray2.length != this.map4.size()) {
            float[] floatArray3 = new float[this.map4.size()];
            float[][] floatArrayArray = new float[this.map4.size()][];
            int local7737_6 = 0;

            for (Entry<Float, float[]> local7737_8 : this.map4.entrySet()) {
               floatArray3[local7737_6] = local7737_8.getKey();
               floatArrayArray[local7737_6++] = local7737_8.getValue();
            }

            this.floatValArray3 = floatArrayArray;
            this.floatValArray2 = floatArray3;
         }

         float[] floatArray4 = this.floatValArray2;
         int local7737_5 = floatArray4.length;
         if (local7737_1 <= floatArray4[0]) {
            return this.floatValArray3[0];
         }

         if (local7737_1 >= floatArray4[local7737_5 - 1]) {
            return this.floatValArray3[local7737_5 - 1];
         }

         int local7737_6 = 0;
         int local7737_7 = local7737_5 - 1;

         while (local7737_7 - local7737_6 > 1) {
            int local7737_8 = local7737_6 + local7737_7 >>> 1;
            if (floatArray4[local7737_8] <= local7737_1) {
               local7737_6 = local7737_8;
            } else {
               local7737_7 = local7737_8;
            }
         }

         if (floatArray4[local7737_6] == local7737_1) {
            return this.floatValArray3[local7737_6];
         }

         float local7737_8 = (local7737_1 - floatArray4[local7737_6]) / (floatArray4[local7737_7] - floatArray4[local7737_6]);
         local7737_8 = Math.max(0.0F, Math.min(1.0F, local7737_8));
         float[] floatArray5 = this.floatValArray3[local7737_6];
         float[] floatArray6 = this.floatValArray3[local7737_7];
         floatArray2[0] = floatArray5[0] + local7737_8 * (floatArray6[0] - floatArray5[0]);
         floatArray2[1] = floatArray5[1] + local7737_8 * (floatArray6[1] - floatArray5[1]);
         floatArray2[2] = floatArray5[2] + local7737_8 * (floatArray6[2] - floatArray5[2]);
         return floatArray2;
      }
   }

   public static final class ChannelState {
      final String strVal2;
      public final boolean flag;
      public final float floatVal8;
      public final Map<String, CosmeticAnimationState.KeyframeState> map5;

      ChannelState(String local7739_1, boolean local7739_2, float local7739_3, Map<String, CosmeticAnimationState.KeyframeState> local7739_4) {
         this.strVal2 = local7739_1;
         this.flag = local7739_2;
         this.floatVal8 = Math.max(0.001F, local7739_3);
         this.map5 = local7739_4;
      }
   }

   public static final class KeyframeState {
      public final CosmeticAnimationState.BoneState nested0533;
      public final CosmeticAnimationState.BoneState nested05332;
      public final CosmeticAnimationState.BoneState nested05333;

      KeyframeState(CosmeticAnimationState.BoneState local7740_1, CosmeticAnimationState.BoneState local7740_2, CosmeticAnimationState.BoneState local7740_3) {
         this.nested0533 = local7740_1;
         this.nested05332 = local7740_2;
         this.nested05333 = local7740_3;
      }
   }
}
