package scratcher3004.mcaircraft.util;

import net.minecraft.world.phys.Vec3;

public class TransformationMatrix {
    public Vec3 x;
    public Vec3 y;
    public Vec3 z;

    public Vec3 Transform(Vec3 vector) {
        return MathUtil.vectorAddition(MathUtil.vectorAddition(MathUtil.vectorMultiplication(z, vector.x),
                MathUtil.vectorMultiplication(y, vector.y)), MathUtil.vectorMultiplication(x, vector.z));
    }
}
