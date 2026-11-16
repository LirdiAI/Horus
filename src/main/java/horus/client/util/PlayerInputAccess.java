package horus.client.util;

import horus.client.module.misc.MultiAccountModule;
import horus.client.multiplayer.AltBot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.PlayerInput;
import org.lwjgl.glfw.GLFW;

/**
 * Тело режима посадки за альт: тик движения + клики-удары.
 * Вынесено отдельно, чтобы не раздувать модуль.
 */
public final class PlayerInputAccess {
   private PlayerInputAccess() {
   }

   public static void handleDrive(MultiAccountModule module, AltBot bot, MinecraftClient mc) {
      PlayerInput input = mc.player.input.playerInput;
      float forward = (input.forward() ? 1.0F : 0.0F) + (input.backward() ? -1.0F : 0.0F);
      float sideways = (input.left() ? 1.0F : 0.0F) + (input.right() ? -1.0F : 0.0F);
      float yaw = mc.player.getYaw();
      float pitch = mc.player.getPitch();
      bot.driveMove(forward, sideways, yaw, pitch, input.jump(), input.sneak());

      long window = mc.getWindow().getHandle();
      boolean attackNow = GLFW.glfwGetMouseButton(window, 0) == 1;
      if (attackNow && !module.attackFlag) {
         bot.driveAttack(yaw);
      }

      module.attackFlag = attackNow;
   }
}
