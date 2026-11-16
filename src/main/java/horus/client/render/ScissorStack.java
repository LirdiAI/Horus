package horus.client.render;

import horus.client.util.GuiScale;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.gui.ScreenRect;
import org.joml.Matrix3x2f;

public final class ScissorStack {
   private static final ScreenRect rect = new ScreenRect(0, 0, 0, 0);
   private static final Deque<ScreenRect> deque = new ArrayDeque<>();

   public static void processData(Matrix3x2f matrix3x2f, float local6584_1, float local6584_2, float local6584_3, float local6584_4) {
      if (matrix3x2f == null) {
         processData2(local6584_1, local6584_2, local6584_3, local6584_4);
      } else {
         ScreenRect screenRect = new ScreenRect(
               Math.round(local6584_1), Math.round(local6584_2), Math.max(0, Math.round(local6584_3)), Math.max(0, Math.round(local6584_4))
            )
            .transformEachVertex(matrix3x2f);
         processData3(screenRect);
      }
   }

   public static void processData2(float local6586_0, float local6586_1, float local6586_2, float local6586_3) {
      int local6586_4 = GuiScale.computeInt2(local6586_0);
      int local6586_5 = GuiScale.computeInt2(local6586_1);
      int local6586_6 = GuiScale.computeInt2(local6586_0 + local6586_2);
      int local6586_7 = GuiScale.computeInt2(local6586_1 + local6586_3);
      processData3(new ScreenRect(local6586_4, local6586_5, Math.max(0, local6586_6 - local6586_4), Math.max(0, local6586_7 - local6586_5)));
   }

   public static ScreenRect getScreenRect() {
      return deque.peek();
   }

   private ScissorStack() {
   }

   private static void processData3(ScreenRect screenRect) {
      ScreenRect screenRect2 = deque.peek();
      ScreenRect screenRect3 = screenRect2 == null ? screenRect : screenRect2.intersection(screenRect);
      deque.push(screenRect3 == null ? rect : screenRect3);
   }

   public static void updateState() {
      deque.clear();
   }

   public static void updateState2() {
      if (!deque.isEmpty()) {
         deque.pop();
      }
   }
}
