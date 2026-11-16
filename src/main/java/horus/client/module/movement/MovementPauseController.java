package horus.client.module.movement;

import horus.client.event.EventBus;
import horus.client.event.EventListener;
import horus.client.event.EventPriority;
import horus.client.MinecraftAccess;
import horus.client.event.MovementInputEvent;
import horus.client.event.PlayerTickEvent;

public class MovementPauseController implements MinecraftAccess {
   private static MovementPauseController movementPauseController;
   private int remainingTicks = -1;
   @EventPriority(Integer.MIN_VALUE)
   private final EventListener<PlayerTickEvent> playerTickListener = playerTickEvent -> {
      if (this.remainingTicks > 0) {
         this.remainingTicks--;
      } else if (this.remainingTicks == 0) {
         this.remainingTicks = -1;
      }
   };
   @EventPriority(Integer.MIN_VALUE)
   private final EventListener<MovementInputEvent> movementInputListener = movementInputEvent -> {
      if (this.remainingTicks >= 0) {
         movementInputEvent.setForward(0.0F);
         movementInputEvent.setSideways(0.0F);
         movementInputEvent.setJumping(false);
      }
   };

   public boolean isPaused() {
      return this.remainingTicks != -1;
   }

   private MovementPauseController() {
   }

   public void pauseForTicks(int newRemainingTicks) {
      this.remainingTicks = newRemainingTicks;
   }

   public static MovementPauseController getInstance() {
      if (movementPauseController == null) {
         movementPauseController = new MovementPauseController();
         EventBus.getInstance().subscribe(movementPauseController);
      }

      return movementPauseController;
   }

   public int getRemainingTicks() {
      return this.remainingTicks;
   }
}
