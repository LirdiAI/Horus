package horus.client.render;

public interface GuiLayerSerialAccess {
   int getLayerSerial();

   boolean checkState();

   void bumpLayerSerial();
}
