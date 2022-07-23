package scratcher3004.mcaircraft.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class Bullet extends AbstractHurtingProjectile {
    private static final String DamageName = "damage";
    private static final EntityDataAccessor<Float> DamageParam = SynchedEntityData.defineId(Bullet.class,
        EntityDataSerializers.FLOAT);

    // Doesn't need to be synchronized, makes sure this bullet gets killed after 10 seconds. Doesn't really matter
    // when it disappears, it just shall be destroyed sometime.
    private int ticksAlive = 0;

    public Bullet(EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void fire(Entity shooter, Vec3 location, Vec3 direction, float damage, float velocity,
                     boolean applyShooterVelocity) {
        setOwner(shooter);
        shoot(direction.x, direction.y, direction.z, velocity, .5f);
        setPos(location);
        setDamage(damage);

        if (applyShooterVelocity)
            setDeltaMovement(getDeltaMovement().add(shooter.getDeltaMovement()));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DamageParam, 5f);
    }

    @Override
    public void tick() {
        super.tick();
        move(MoverType.SELF, getDeltaMovement());
        ticksAlive++;

        if (getDeltaMovement().length() < 1f)
            discard();
        else if (isInWater()) {
            for (int i = 0; i < 50; i++) {
                level.addParticle(ParticleTypes.DRIPPING_WATER, getX(), getY(), getZ(),
                    random.nextDouble() * 0.2, 3, random.nextDouble() * 0.2);
            }

            discard();
        } else if (ticksAlive > 200)
            discard();
        else if (isInLava()) {
            for (int i = 0; i < 50; i++) {
                level.addParticle(ParticleTypes.DRIPPING_LAVA, getX(), getY(), getZ(),
                    random.nextDouble() * 0.2, 3, random.nextDouble() * 0.2);
            }

            discard();
        }
    }

    @Override
    protected float getInertia() {
        return 1;
    }

    public void setDamage(float dmg) {
        entityData.set(DamageParam, dmg);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (pResult.getEntity() instanceof Bullet) // Bullets don't collide with bullets.
            return;

        Entity e = pResult.getEntity();
        Entity o = getOwner();

        if (e == o || e == o.getVehicle() || e == o.getRootVehicle()) {
            discard();
            return;
        }

        pResult.getEntity().hurt(new IndirectEntityDamageSource("gun", this, getOwner()).setProjectile(), 5);
        onHitBlock(null);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        level.explode(getOwner(), getX(), getY(), getZ(), .2f, Explosion.BlockInteraction.BREAK);
        discard();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains(DamageName))
            entityData.set(DamageParam, pCompound.getFloat(DamageName));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putFloat(DamageName, entityData.get(DamageParam));
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return e.getRootVehicle() != getOwner().getRootVehicle() && !(e instanceof Bullet);
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }
}
