package scratcher3004.mcaircraft.util;

import net.minecraft.world.phys.Vec3;

public class Vector3 {
    double x;
    double y;
    double z;

    public Vector3(double inX, double inY, double inZ) {
        x = inX;
        y = inY;
        z = inZ;
    }

    Vector3 plus(Vector3 other) {
        return new Vector3(other.x + x, other.y + y, other.z + z);
    }

    Vector3 minus(Vector3 other) {
        return new Vector3(other.x - x, other.y - y, other.z - z);
    }

    Vector3 negative() {
        return new Vector3(-x, -y, -z);
    }

    Vector3 multiply(Vector3 other) {
        return new Vector3(other.x * x, other.y * y, other.z * z);
    }

    Vector3 multiply(float other) {
        return new Vector3(other * x, other * y, other * z);
    }

    Vector3 div(Vector3 other) {
        return new Vector3(other.x / x, other.y / y, other.z / z);
    }

    Vector3 div(float other) {
        return new Vector3(x / other, y / other, z / other);
    }

    public Vec3 convert() {
        return new Vec3((float) x,(float) y, (float) z);
    }

    public static Vector3 convert(Vec3 vIn) {
        return new Vector3(vIn.x, vIn.y, vIn.z);
    }
}
