package horus.client.event;


public class CancellableEvent extends Event {
   private boolean flag;

   public boolean checkState() {
      return this.flag;
   }

   public void updateState() {
      this.flag = true;
   }

   public void cancel() {
      this.flag = true;
   }

   public void processData(boolean local7442_1) {
      this.flag = local7442_1;
   }
}
