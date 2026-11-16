package horus.client.module.movement;

import net.minecraft.util.math.Vec3d;

public interface SimulatedMovementAccess {
   Vec3d getVec3d();

   void updateState();
}
