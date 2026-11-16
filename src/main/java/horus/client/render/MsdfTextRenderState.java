package horus.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;

final class MsdfTextRenderState implements SimpleGuiElementRenderState {
   private final Matrix3x2f matrix;
   private final TextureSetup textureSetup2;
   private final ScreenRect rect;
   private final boolean flag;
   private final float floatVal;
   private final List<MsdfTextRenderState.DataRecord> list = new ArrayList<>(128);
   private float floatVal2 = Float.MAX_VALUE;
   private float floatVal3 = Float.MAX_VALUE;
   private float floatVal4 = -Float.MAX_VALUE;
   private float floatVal5 = -Float.MAX_VALUE;

   @Override
   public TextureSetup textureSetup() {
      return this.textureSetup2;
   }

   @Override
   public RenderPipeline pipeline() {
      return this.flag ? MsdfTextRenderer.pipeline3 : MsdfTextRenderer.pipeline2;
   }

   @Override
   public ScreenRect scissorArea() {
      return this.rect;
   }

   private void include(float local1866_1, float local1866_2) {
      this.floatVal2 = Math.min(this.floatVal2, local1866_1);
      this.floatVal3 = Math.min(this.floatVal3, local1866_2);
      this.floatVal4 = Math.max(this.floatVal4, local1866_1);
      this.floatVal5 = Math.max(this.floatVal5, local1866_2);
   }

   @Override
   public void setupVertices(VertexConsumer vertexConsumer) {
      if (this.flag) {
         for (MsdfTextRenderState.DataRecord local1867_3 : this.list) {
            this.shimmerVertex(
               vertexConsumer,
               local1867_3.x0(),
               local1867_3.y0(),
               local1867_3.u0(),
               local1867_3.v0(),
               local1867_3.colorTopLeft(),
               local1867_3.quadIndex0()
            );
            this.shimmerVertex(
               vertexConsumer,
               local1867_3.x1(),
               local1867_3.y1(),
               local1867_3.u0(),
               local1867_3.v1(),
               local1867_3.colorBottomLeft(),
               local1867_3.quadIndex0()
            );
            this.shimmerVertex(
               vertexConsumer,
               local1867_3.x2(),
               local1867_3.y2(),
               local1867_3.u1(),
               local1867_3.v1(),
               local1867_3.colorBottomRight(),
               local1867_3.quadIndex1()
            );
            this.shimmerVertex(
               vertexConsumer,
               local1867_3.x3(),
               local1867_3.y3(),
               local1867_3.u1(),
               local1867_3.v0(),
               local1867_3.colorTopRight(),
               local1867_3.quadIndex1()
            );
         }
      } else {
         for (MsdfTextRenderState.DataRecord local1867_3 : this.list) {
            this.vertex(vertexConsumer, local1867_3.x0(), local1867_3.y0(), local1867_3.u0(), local1867_3.v0(), local1867_3.colorTopLeft());
            this.vertex(vertexConsumer, local1867_3.x1(), local1867_3.y1(), local1867_3.u0(), local1867_3.v1(), local1867_3.colorBottomLeft());
            this.vertex(
               vertexConsumer,
               local1867_3.x2(),
               local1867_3.y2(),
               local1867_3.u1(),
               local1867_3.v1(),
               local1867_3.colorBottomRight()
            );
            this.vertex(
               vertexConsumer, local1867_3.x3(), local1867_3.y3(), local1867_3.u1(), local1867_3.v0(), local1867_3.colorTopRight()
            );
         }
      }
   }

   void addGlyph(
      float local1868_1,
      float local1868_2,
      float local1868_3,
      float local1868_4,
      float local1868_5,
      float local1868_6,
      float local1868_7,
      float local1868_8,
      int local1868_9,
      int local1868_10,
      int local1868_11,
      int local1868_12,
      float local1868_13,
      float local1868_14,
      float local1868_15,
      float local1868_16,
      float local1868_17
   ) {
      float local1868_18 = local1868_1;
      float local1868_19 = local1868_2;
      float local1868_20 = local1868_1;
      float local1868_21 = local1868_4;
      float local1868_22 = local1868_3;
      float local1868_23 = local1868_4;
      float local1868_24 = local1868_3;
      float local1868_25 = local1868_2;
      if (Math.abs(local1868_15) >= 0.001F) {
         float local1868_26 = (float)Math.toRadians(local1868_15);
         float local1868_27 = (float)Math.sin(local1868_26);
         float local1868_28 = (float)Math.cos(local1868_26);
         float[] floatArray = this.rotate(local1868_18, local1868_19, local1868_16, local1868_17, local1868_27, local1868_28);
         float[] floatArray2 = this.rotate(local1868_20, local1868_21, local1868_16, local1868_17, local1868_27, local1868_28);
         float[] floatArray3 = this.rotate(local1868_22, local1868_23, local1868_16, local1868_17, local1868_27, local1868_28);
         float[] floatArray4 = this.rotate(local1868_24, local1868_25, local1868_16, local1868_17, local1868_27, local1868_28);
         local1868_18 = floatArray[0];
         local1868_19 = floatArray[1];
         local1868_20 = floatArray2[0];
         local1868_21 = floatArray2[1];
         local1868_22 = floatArray3[0];
         local1868_23 = floatArray3[1];
         local1868_24 = floatArray4[0];
         local1868_25 = floatArray4[1];
      }

      this.list
         .add(
            new MsdfTextRenderState.DataRecord(
               local1868_18,
               local1868_19,
               local1868_20,
               local1868_21,
               local1868_22,
               local1868_23,
               local1868_24,
               local1868_25,
               local1868_5,
               local1868_6,
               local1868_7,
               local1868_8,
               local1868_9,
               local1868_10,
               local1868_11,
               local1868_12,
               local1868_13,
               local1868_14
            )
         );
      this.include(local1868_18, local1868_19);
      this.include(local1868_20, local1868_21);
      this.include(local1868_22, local1868_23);
      this.include(local1868_24, local1868_25);
   }

   private void vertex(VertexConsumer vertexConsumer, float local1869_2, float local1869_3, float local1869_4, float local1869_5, int local1869_6) {
      vertexConsumer.vertex(this.matrix, local1869_2, local1869_3).texture(local1869_4, local1869_5).color(local1869_6).lineWidth(this.floatVal);
   }

   private float[] rotate(float local1870_1, float local1870_2, float local1870_3, float local1870_4, float local1870_5, float local1870_6) {
      float local1870_7 = local1870_1 - local1870_3;
      float local1870_8 = local1870_2 - local1870_4;
      return new float[]{
         local1870_3 + local1870_7 * local1870_6 - local1870_8 * local1870_5, local1870_4 + local1870_7 * local1870_5 + local1870_8 * local1870_6
      };
   }

   @Override
   public ScreenRect bounds() {
      if (this.list.isEmpty()) {
         return new ScreenRect(0, 0, 1, 1);
      }

      int local1871_1 = (int)Math.floor(this.floatVal2);
      int local1871_2 = (int)Math.floor(this.floatVal3);
      int local1871_3 = Math.max(1, (int)Math.ceil(this.floatVal4 - this.floatVal2));
      int local1871_4 = Math.max(1, (int)Math.ceil(this.floatVal5 - this.floatVal3));
      ScreenRect screenRect = new ScreenRect(local1871_1, local1871_2, local1871_3, local1871_4).transformEachVertex(this.matrix);
      return this.rect == null ? screenRect : this.rect.intersection(screenRect);
   }

   private void shimmerVertex(
      VertexConsumer vertexConsumer, float local1872_2, float local1872_3, float local1872_4, float local1872_5, int local1872_6, float local1872_7
   ) {
      vertexConsumer.vertex(this.matrix, local1872_2, local1872_3).texture(local1872_4, local1872_5).color(local1872_6).lineWidth(local1872_7);
   }

   MsdfTextRenderState(Matrix3x2f matrix3x2f, TextureSetup textureSetup, ScreenRect screenRect, boolean local1873_4, float local1873_5) {
      this.matrix = new Matrix3x2f(matrix3x2f);
      this.textureSetup2 = textureSetup;
      this.rect = screenRect;
      this.flag = local1873_4;
      this.floatVal = local1873_5;
   }

   private record DataRecord(
      float x0,
      float y0,
      float x1,
      float y1,
      float x2,
      float y2,
      float x3,
      float y3,
      float u0,
      float v0,
      float u1,
      float v1,
      int colorTopLeft,
      int colorTopRight,
      int colorBottomRight,
      int colorBottomLeft,
      float quadIndex0,
      float quadIndex1
   ) {
   }
}
