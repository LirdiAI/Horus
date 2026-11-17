package horus.client.render;

/** Источник параметров шейдерной обводки игроков (PlayerOutlineRenderer). */
public interface PlayerOutlineParams {
   boolean outlineEnabled();

   int outlineColor();

   float fillValue();

   float outlineValue();

   float highlightValue();
}
