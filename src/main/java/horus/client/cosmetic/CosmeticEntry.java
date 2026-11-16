package horus.client.cosmetic;

import net.minecraft.util.Identifier;

public record CosmeticEntry(
   int id,
   CosmeticCategory category,
   String name,
   boolean beta,
   String modelResource,
   String textureResource,
   String previewResource,
   CosmeticPlacement placement
) {
   public Identifier textureId() {
      return this.textureResource == null ? null : Identifier.of("horus", "cosmetics/" + this.textureResource);
   }

   public Identifier previewId() {
      return Identifier.of("horus", "cosmetics/" + this.previewResource);
   }
}
