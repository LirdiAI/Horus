package horus.client.util;

public final class FastMath {
   private static final float[] SIN_TABLE = new float[65536];
   private static final float RAD_TO_INDEX = 65536.0F / (float)(Math.PI * 2.0);

   static {
      for (int i = 0; i < 65536; i++) {
         SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
      }
   }

   public static float sin(float rad) {
      return SIN_TABLE[(int)(rad * RAD_TO_INDEX) & 0xFFFF];
   }

   public static float cos(float rad) {
      return SIN_TABLE[(int)(rad * RAD_TO_INDEX + 16384.0F) & 0xFFFF];
   }

   public static float toRadians(float degrees) {
      return degrees * (float)(Math.PI / 180.0);
   }

   public static float toDegrees(float radians) {
      return radians * (float)(180.0 / Math.PI);
   }

   private FastMath() {
   }
}
