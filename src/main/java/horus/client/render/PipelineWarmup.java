package horus.client.render;

import horus.client.render.ShapePipelines;

import horus.client.render.ArcDividerRenderer;
import horus.client.render.BatchedBlurRenderer;
import horus.client.render.BlobRenderer;
import horus.client.module.render.BlockEspModule;
import horus.client.render.CircleRenderer;
import horus.client.render.CornerBracketRenderer;
import horus.client.module.render.CustomWorldModule;
import horus.client.render.DitherRenderer;
import horus.client.render.EffectIconRenderer;
import horus.client.render.GlassRenderer;
import horus.client.render.GlowCompositeRenderer;
import horus.client.render.HandOutlineRenderer;
import horus.client.render.ImageRenderer;
import horus.client.render.ItemRenderer;
import horus.client.render.JumpCircleRenderer;
import horus.client.render.LineRenderer;
import horus.client.render.MenuGridRenderer;
import horus.client.module.render.MotionBlurModule;
import horus.client.render.MsdfTextRenderer;
import horus.client.render.Outline360Renderer;
import horus.client.render.OutlineDefaultRenderer;
import horus.client.render.OutlineGlassRenderer;
import horus.client.module.render.ParticlesModule;
import horus.client.render.PickerRenderer;
import horus.client.render.PlayerOutlineRenderer;
import horus.client.module.render.ProjectilePredictionModule;
import horus.client.render.RainScreenRenderer;
import horus.client.render.RectDefaultRenderer;
import horus.client.render.RectHalfIconRenderer;
import horus.client.render.RippleRenderer;
import horus.client.render.SaturationRenderer;
import horus.client.render.ShapeRenderer;
import horus.client.render.ShimmerRenderer;
import horus.client.module.render.SkeletonEspModule;
import horus.client.render.SoftGlowRenderer;
import horus.client.module.render.TargetEspModule;
import horus.client.render.TargetScanFeatureRenderer;
import horus.client.render.TextAtlasRenderer;
import horus.client.module.render.TorusModule;
import horus.client.module.render.TotemGhostModule;
import horus.client.render.WarpTextRenderer;
import horus.client.render.WetWorldRenderer;
import horus.client.render.WorldRainRenderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PipelineWarmup {
   private static final Class<?>[] PIPELINE_OWNERS = new Class[]{
      BatchedBlurRenderer.class,
      GlassRenderer.class,
      ShapeRenderer.class,
      GlowCompositeRenderer.class,
      SoftGlowRenderer.class,
      OutlineGlassRenderer.class,
      CircleRenderer.class,
      CornerBracketRenderer.class,
      BlobRenderer.class,
      ArcDividerRenderer.class,
      PickerRenderer.class,
      RectDefaultRenderer.class,
      RectHalfIconRenderer.class,
      OutlineDefaultRenderer.class,
      Outline360Renderer.class,
      ImageRenderer.class,
      LineRenderer.class,
      RippleRenderer.class,
      ShimmerRenderer.class,
      MenuGridRenderer.class,
      WarpTextRenderer.class,
      DitherRenderer.class,
      EffectIconRenderer.class,
      MsdfTextRenderer.class,
      TextAtlasRenderer.class,
      ItemRenderer.class,
      ShapePipelines.class,
      PlayerOutlineRenderer.class,
      HandOutlineRenderer.class,
      JumpCircleRenderer.class,
      SaturationRenderer.class,
      MotionBlurModule.class,
      ParticlesModule.class,
      SkeletonEspModule.class,
      BlockEspModule.class,
      TargetEspModule.class,
      TorusModule.class,
      ProjectilePredictionModule.class,
      CustomWorldModule.class,
      TotemGhostModule.class,
      TargetScanFeatureRenderer.class,
      WetWorldRenderer.class,
      RainScreenRenderer.class,
      WorldRainRenderer.class
   };
   private static final Logger LOGGER = LoggerFactory.getLogger("horus-pipelines");
   private static ArrayList<RenderPipeline> pipelines;
   private static int nextPipeline;

   public static boolean warmNextPipeline() {
      // The loading overlay draws before ShaderLoader has applied its new sources.
      if (MinecraftClient.getInstance().getOverlay() != null) {
         return false;
      }
      if (pipelines == null) {
         pipelines = collectPipelines();
      }

      if (nextPipeline >= pipelines.size()) {
         return false;
      }

      RenderPipeline renderPipeline = pipelines.get(nextPipeline++);

      try {
         RenderSystem.getDevice().precompilePipeline(renderPipeline);
      } catch (Throwable throwable) {
         LOGGER.error("pipeline {} failed to compile", renderPipeline.getLocation(), throwable);
      }

      return true;
   }

   public static void resetWarmup() {
      pipelines = null;
      nextPipeline = 0;
   }

   private PipelineWarmup() {
   }

   private static ArrayList<RenderPipeline> collectPipelines() {
      ArrayList<RenderPipeline> local3949_0 = new ArrayList<>(64);

      for (Class<?> local3949_4 : PIPELINE_OWNERS) {
         try {
            for (Field field : local3949_4.getDeclaredFields()) {
               if (Modifier.isStatic(field.getModifiers()) && RenderPipeline.class.isAssignableFrom(field.getType())) {
                  field.setAccessible(true);
                  RenderPipeline renderPipeline = (RenderPipeline)field.get(null);
                  if (renderPipeline != null && !local3949_0.contains(renderPipeline)) {
                     local3949_0.add(renderPipeline);
                  }
               }
            }
         } catch (Throwable throwable) {
         }
      }

      return local3949_0;
   }
}
