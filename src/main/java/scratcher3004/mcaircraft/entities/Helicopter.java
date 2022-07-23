package scratcher3004.mcaircraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.phys.*;
import scratcher3004.mcaircraft.ModEntityTypes;
import scratcher3004.mcaircraft.util.MathUtil;

import javax.annotation.Nullable;

public class Helicopter extends Aircraft {
    private static final String RotorSpeedName = "RotorSpeed";
    private static final String MotorStrengthName = "MotorStrength";

    private static final EntityDataAccessor<Float> RotorSpeedParam = SynchedEntityData.defineId(Helicopter.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MotorStrengthParam = SynchedEntityData.defineId(Helicopter.class, EntityDataSerializers.FLOAT);

    protected float maxPassengers = 1;

    private boolean firePressed = false;
    private int fireCooldown = 0;

    public Helicopter(EntityType<? extends Aircraft> entityType, Level world) {
        super(entityType, world);
        toyScale = 5;
        entityData.set(MotorStrengthParam, 1.2f);
    }

    public float getSpeed() {
        return entityData.get(RotorSpeedParam);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RotorSpeedParam, 0f);
        entityData.define(MotorStrengthParam, 1.2f);
    }

    private void accelerate() {
        float str = 2;
        float strength2 = entityData.get(RotorSpeedParam) * entityData.get(MotorStrengthParam) * str;
        float strength = strength2 / 20 / str;
        setDeltaMovement(getDeltaMovement().add(calculateUpVector(getXRot(), getYRot()).multiply(strength, strength, strength)));
        Vec3 motion = MathUtil.rotateBy(getDeltaMovement(), new Vec3(getXRot(), getYRot(), 0));

        if (motion.y > strength2) {
            setDeltaMovement(calculateUpVector(getXRot(), getYRot()).multiply(strength2, strength2, strength2));
        }

        if (Math.abs(motion.x) < 0.05f && Math.abs(motion.z) < 0.05f && Math.abs(entityData.get(RotorSpeedParam) - 0.5f) < 0.005f) {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat(MotorStrengthName, entityData.get(MotorStrengthParam));
        compound.putFloat(RotorSpeedName, entityData.get(RotorSpeedParam));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.getFloat(MotorStrengthName) > 0)
            entityData.set(MotorStrengthParam, compound.getFloat(MotorStrengthName));
        entityData.set(RotorSpeedParam, compound.getFloat(RotorSpeedName));
    }

    @Override
    public void tick() {
        super.tick();

        int foundBlockAt = -1;

        for (int i = 0; i < 4; i++) {
            if (!(level.getBlockState(new BlockPos( getBlockX(), getBlockY() - i, getBlockZ())).getBlock() instanceof AirBlock)) {
                foundBlockAt = i;
                break;
            }
        }

        if (entityData.get(RotorSpeedParam) > 0.05f && foundBlockAt != -1) {
            //ParticleTick++;
            //if (ParticleTick >= 5 * (0.5f + (1 - entityData.get(RotorSpeedParam)))) {
            //    ParticleTick = 0;
                int iterations = 20;

                for (int l = 0; l < iterations; ++l) {
                    double angle = Math.toRadians((l / (double) iterations + random.nextDouble() / iterations) * 360d);
                    double x1 = getX() + Math.sin(angle);
                    double y = 1 + (int) getY() - foundBlockAt;
                    double z1 = getZ() + Math.cos(angle);
                    int i1 = random.nextInt(2) * 2 - 1;
                    double x2 = Math.sin(angle) * 1.000000001490116D * 0.2;
                    double z2 = Math.cos(angle) * 1.000000001490116D * 0.2;
                    level.addParticle(ParticleTypes.CLOUD, x1, y, z1, x2, 0.1d, z2);
                }
            //}
        }

        if (!this.getPassengers().isEmpty() && getControllingPassenger() instanceof Player pe) {
            float msc = normalize(pe.zza) * (1f / 20f) * 0.25f;
            boolean clampToPoint5 = false;

            if (entityData.get(RotorSpeedParam) > 0.5 && msc == 0f) {
                msc = -1 * (1 / 20f) * 0.25f;
                clampToPoint5 = true;
            }

            foundBlockAt = -1;

            for (int i = 0; i < 2; i++) {
                if (!(level.getBlockState(new BlockPos((int) getX(), (int) getY() - i, (int) getZ())).getBlock() instanceof AirBlock)) {
                    foundBlockAt = i;
                    break;
                }
            }

            if (foundBlockAt != -1 && Math.abs(getXRot()) > 30)
                hurt(DamageSource.FALL, 1);
            if (foundBlockAt != -1 && Math.abs(getXRot()) <= 30)
                setRot(getYRot(), getXRot() + (float)Mth.clamp(-getXRot(), -2.5, 2.5));

            if (!freecam) {
                if (foundBlockAt == -1 || Math.abs(getXRot()) > 30) {
                    float yawDifference = pe.getYRot() - getYRot();
                    float pitchDifference = pe.getXRot() - getXRot();

                    setRot(getYRot() + Math.min(4.5f, Math.max(-4.5f, yawDifference)),
                    /*if (entityData.get(RotorSpeedParam) <= 0.5f)
                        pitchDifference = Math.min(2.5f, Math.max(-2.5f, -rotationPitch));*/
                    getXRot() + Math.min(4.5f, Math.max(-4.5f, pitchDifference)));
                }

                pe.setXRot(getXRot());
                pe.setYRot(getYRot());
                pe.yHeadRot = getYRot();
            }

            entityData.set(RotorSpeedParam, Math.max(Math.min(entityData.get(RotorSpeedParam) + msc, 1f), clampToPoint5 ? 0.5f : 0f));
        } else if (entityData.get(RotorSpeedParam) > 0) {
            float targetSpeed = 0.45f;

            for (int i = 0; i < 3; i++) {
                if (!(level.getBlockState(new BlockPos((int) getX(), (int) getY() - i, (int) getZ())).getBlock() instanceof AirBlock)) {
                    targetSpeed = 0;
                    break;
                }
            }

            if (entityData.get(RotorSpeedParam) > targetSpeed) {
                entityData.set(RotorSpeedParam, Math.max(targetSpeed, Math.min(1, entityData.get(RotorSpeedParam) - 0.025f)));
            }
        }

        accelerate();

        if (!level.isClientSide) {
            fireCooldown--;

            if (firePressed && fireCooldown <= 0) {
                if (!(getControllingPassenger() instanceof Player p))
                    return;

                Vec3 vec3 = p.getEyePosition(0);
                Vec3 vec31 = p.getViewVector(0);
                Vec3 target = vec3.add(vec31.x * 100, vec31.y * 100, vec31.z * 100);
                HitResult hr = p.pick(100, 0, true);

                if (hr.getType() != HitResult.Type.MISS) {
                    target = hr.getLocation();
                }

                Bullet b = new Bullet(ModEntityTypes.BULLET.get(), level);
                Vec3 right = Vec3.directionFromRotation(getRotationVector().add(new Vec2(0, 90))).normalize();
                Vec3 firePos = position().add(right.multiply(7, 7, 7));
                b.fire(p, firePos, target.subtract(firePos), 5, 4, false);
                level.addFreshEntity(b);
                fireCooldown = 1; // 10 shots per second
            }

            firePressed = false;
        }
    }

    @Override
    public void fire() {
        firePressed = true;
    }

    private float normalize(float fIn) {
        if (fIn > 0.1f)
            return 1;
        if (fIn < -0.1f)
            return -1;
        return 0;
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return getPassengers().isEmpty() ? null : getPassengers().get(0);
    }

    @Override
    public void positionRider(Entity pPassenger) {
        super.positionRider(pPassenger); // TODO
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
        return new Vec3(getX() + 1, getY() + 1, getZ());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            if (this.getPassengers().size() >= maxPassengers){
                return InteractionResult.FAIL;
            }
            if (!this.level.isClientSide) {
                if (getPassengers().isEmpty()) { // player is going to be controlling passenger
                    player.setYRot(getYRot());
                    player.setXRot(getXRot());
                    player.yHeadRot = getYRot();
                }
                return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.FAIL;
            } else {
                return InteractionResult.SUCCESS;
            }
        }
    }

    @Override
    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
        return this.interact(pPlayer, pHand);
    }

    @Override
    protected double getGravityScale() {
        //float speed = entityData.get(RotorSpeedParam);
        //return speed >= (SpeedRequiredForHover - 0.01f) ? 0 : (-0.04f * (0.5 - speed));
        return -0.04f;
    }

    @Override
    protected void onFreecamDisable() {
        if (!(getControllingPassenger() instanceof Player pe))
            return;

        pe.setXRot(getXRot());
        pe.setYRot(getYRot());
        pe.yHeadRot = getYRot();
    }
}
