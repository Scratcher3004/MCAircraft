package scratcher3004.mcaircraft.util;

import net.minecraft.world.phys.Vec3;
import static scratcher3004.mcaircraft.util.MathUtil.*;

public class CoordinateSystem {
    private Vec3 forward;
    private Vec3 right;
    private Vec3 up;

    public CoordinateSystem(Vec3 inForward, Vec3 inRight, Vec3 inUp) {
        forward = inForward;
        right = inRight;
        up = inUp;
    }

    public Vec3 transformPoint(Vec3 originalVector) {
        return vectorAddition(vectorAddition(vectorMultiplication(forward, originalVector.x),
                vectorMultiplication(up, originalVector.y)), vectorMultiplication(right, originalVector.z));
    }
}
