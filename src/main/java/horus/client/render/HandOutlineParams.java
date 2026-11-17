package horus.client.render;

/** Источник параметров шейдерной обводки предмета в руке (HandOutlineRenderer). */
public interface HandOutlineParams {
   boolean outlineEnabled();

   int outlineColor();

   float fillValue();

   float outlineValue();

   float highlightValue();
}
