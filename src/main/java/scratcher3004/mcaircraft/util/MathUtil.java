package scratcher3004.mcaircraft.util;

import net.minecraft.world.phys.Vec3;
import static java.lang.Math.*;

public class MathUtil {
    public static final Vec3 RIGHT = new Vec3(1, 0, 0);
    public static final Vec3 UP = new Vec3(0, 1, 0);
    public static final Vec3 FORWARD = new Vec3(0, 0, 1);

    public static Vec3 rotateBy(Vec3 originalVector, Vec3 eulers) {
        CoordinateSystem sys = new CoordinateSystem(transformRotation(eulers.x, UP, FORWARD),
                transformRotation(eulers.y, RIGHT, FORWARD), transformRotation(eulers.z, RIGHT, UP));
        return sys.transformPoint(originalVector);
    }

    public static Vec3 vectorMultiplication(Vec3 vIn, double flt) {
        return new Vec3(vIn.x * flt, vIn.y * flt, vIn.z * flt);
    }

    public static Vec3 vectorAddition(Vec3 vIn1, Vec3 vIn2) {
        return new Vec3(vIn1.x + vIn2.x, vIn1.y + vIn2.y, vIn1.z + vIn2.z);
    }

    public static Vec3 transformRotation(double angle, Vec3 v1, Vec3 v2) {
        double angleRadians = toRadians(angle);
        double angle1 = cos(angleRadians);
        double angle2 = sin(angleRadians);
        return vectorAddition(vectorMultiplication(v1, angle1), vectorMultiplication(v2, angle2));
    }
}
