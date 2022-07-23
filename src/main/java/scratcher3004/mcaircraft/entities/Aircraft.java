package scratcher3004.mcaircraft.entities;

import minecraftbyexample.usefultools.SetBlockStateFlag;
import minecraftbyexample.usefultools.UsefulFunctions;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import scratcher3004.mcaircraft.Mcaircraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Aircraft extends Entity {
    private int waterDmgTick = 0;

    protected ModelResourceLocation mrl = new ModelResourceLocation(new ResourceLocation(Mcaircraft.MODID, "aw"), "");
    protected ResourceLocation trl = new ResourceLocation(Mcaircraft.MODID, "textures/model/mbe81b_boomerang_texture.png");
    protected float toyScale = 10;
    protected boolean waterHurts = true;
    protected Entity lastAttack;
    protected boolean freecam = false;

    // Used on the server when reloading from disk, and on the client in response to a spawn packet from the server
    // a) On the server: after initial construction, readAdditional() will be called
    // b) On the client: after initial construction, readSpawnData() will be called
    public Aircraft(EntityType<? extends Aircraft> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(HEALTH, 100f);
    }

    // If you forget to override this method, the default vanilla method will be called.
    // This sends a vanilla spawn packet, which is then silently discarded when it reaches the client.
    //  Your entity will be present on the server and can cause effects, but the client will not have a copy of the entity
    //    and hence it will not render.
    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ------------------methods to setup the entity's state -----------------------------------------------
    // Each entity has three types of data:
    //  a) server-only member variables which are not synchronised to the client
    //  b) server member variables which are synchronised to the client through the initial spawn packet, but are not
    //      synchronised again; this is achieved using writeSpawnData() and readSpawnData()
    //  c) server member variables which are continually synchronised to the client through update packets; this is
    //     achieved using the DataManager and DataParameters


    // ----- member variables that do not need synchronisation, either because
    //         a) they're not used on the client; or
    //         b) they're generated by the client from other information

    // ---------- member variables that are regenerated from other information, hence don't need saving
    // record information about enchantments placed on the boomerang
    private Map<Enchantment, Integer> specialDamages = new HashMap<>();

    // ---------- member variables which are synchronised to the client at client spawn only

    // When the client entity is spawned (in response to a packet from the server), we need to send it three extra pieces of information
    // 1) The boomerang flight path information
    // 2) The number of ticks that the entity has spent in flight (i.e. if we saved a game when the boomerang was in the middle of its
    //     flight, then when we spawn after reload, this number of ticks will not be zero.)
    // 3) which hand was used to throw the boomerang
    // Why is this necessary?
    // Because when the entity is first created, or when it is loaded from disk (using NBT), the client does not receive all of the
    //   information that the server does.
    // Some information is synchronised by vanilla (DataManager variables, position, motion, yaw+pitch), but anything else
    //  must be transmitted by us.

    // How much momentum does the boomerang have left?
    private static final EntityDataAccessor<Float> MOMENTUM_DMP = SynchedEntityData.defineId(Aircraft.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(Aircraft.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_HEALTH = SynchedEntityData.defineId(Aircraft.class, EntityDataSerializers.FLOAT);

    //----------------- load/save the entity to/from NBT (used when saving & loading the game to disk)
    private final String MOMENTUM_NBT = "momentum";
    private final String HealthNBT = "health";
    private final String MaxHealthNBT = "maxHealth";

    //-------update of entity position & state, every tick ------------------------------

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MOMENTUM_DMP, 0f);
        this.entityData.define(HEALTH, 100f);
        this.entityData.define(MAX_HEALTH, 100f);
    }

    /**
     * Called to update the entity's position/logic.
     * We are using some unusual logic to create the particular flight path that we want:
     *
     * 1) If the boomerang is in flight, every tick we force the entity position to a defined position on the curve.
     * 2) If the boomerang is not in flight (eg has hit something and is falling to the ground), then we use vanilla
     *    mechanics i.e. set motion (velocity) and let gravity act on the entity
     */
    public void tick() {
        if (entityData.get(HEALTH) <= 0 && !isRemoved() && level instanceof ServerLevel) {
            onDestroyed();
            kill();
            return;
        }

        super.tick();  // base logic shared by all entities

        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();

        // when not in flight, gradually rotate the boomerang towards its "lying flat" position.  See boomerang_rotations.png
        /*if (Math.abs(getXRot()) > 1 ) {
            final float DEGREES_PER_TICK = 2.0F;
            this.rotationPitch -= (this.rotationPitch > 0) ? DEGREES_PER_TICK : -DEGREES_PER_TICK;
        } else {
            this.rotationPitch = 0;
        }*/

        Vec3 initialVelocity = this.getDeltaMovement();
        float bottomOfItem = this.getEyeHeight() - 0.11111111F;   // 0.111111F is from vanilla
        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > bottomOfItem) {
            // apply buoyancy if underwater, or gravity acceleration if out of water
            if (!waterHurts)
                this.applyFloatMotion();
            else {
                waterDmgTick++;
                if (waterDmgTick >= 20 && level instanceof ServerLevel) {
                    hurt(DamageSource.DROWN, random.nextFloat(5, 9));
                    waterDmgTick = 0;
                }
            }
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > bottomOfItem) {
            // check if we're in lava, bounce around randomly if we are
            this.setDeltaMovement( 0.2 * (this.random.nextFloat() - this.random.nextFloat()),
                    0.2,
                    0.2 * (this.random.nextFloat() - this.random.nextFloat()));
        } else if (!this.isNoGravity()) {
            // otherwise: apply gravity  acceleration
            final double ACCELERATION_DUE_TO_GRAVITY = getGravityScale(); // blocks per tick squared
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, ACCELERATION_DUE_TO_GRAVITY, 0.0));
        }

        if (!level.isClientSide) {
            HitResult hitresult = getHitResult(this, a -> isPickable());
            //HitResult hitresult = level.clip(new ClipContext(position().add(bb.minX, bb.minY, bb.minZ),
            //    position().add(bb.maxX, bb.maxY, bb.maxZ), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

            if (hitresult.getType() != HitResult.Type.MISS) {
                checkInsideBlocksCustom();

                HitResult.Type hitresult$type = hitresult.getType();
                if (hitresult$type == HitResult.Type.ENTITY) {
                    this.onHitEntity((EntityHitResult)hitresult);
                } else if (hitresult$type == HitResult.Type.BLOCK) {
                    this.onBlockHit((BlockHitResult) hitresult);
                }
            }
        }

        // check if we have collided with a block; if so, move out of the block
        // "noClip" is confusingly named.  It actually means "this entity is already colliding with a block before it has moved on this tick"
        //  If this is true, then we just push the entity out of the block in a suitable direction, and the move method doesn't check for
        //   collisions again on this tick.
        if (this.level.isClientSide) {  // not on client
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level.noCollision(this);
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        // move the item and adjust its speed
        // check for collisions with other objects (blocks, entities)
        final float THRESHOLD_HORIZONTAL_SPEED = 1E-5F;// below this speed, there is negligible horizontal speed
        if (!this.onGround
                || this.getDeltaMovement().horizontalDistanceSqr() > THRESHOLD_HORIZONTAL_SPEED
                || (this.tickCount + this.getId()) % 4 == 0) {  // check for movement at least every fourth tick
            Vec3 previousMotion = this.getDeltaMovement();
            this.move(MoverType.SELF, this.getDeltaMovement());  // move and check for collisions
            final float FRICTION_FACTOR = 0.98F;
            float horizontalfrictionFactor = FRICTION_FACTOR;

            if (this.onGround) {  // get the slipperiness of the block under the entity's "feet"
                BlockPos pos = new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ());
                horizontalfrictionFactor *= this.level.getBlockState(pos).getFriction(this.level, pos, this);
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(horizontalfrictionFactor, FRICTION_FACTOR, horizontalfrictionFactor));
            if (this.onGround) {
                final double BOUNCE_MULTIPLIER = -0.5;
                this.setDeltaMovement(this.getDeltaMovement().x, previousMotion.y * BOUNCE_MULTIPLIER, this.getDeltaMovement().z);
            }
        }

        // check if we're in lava, make burning sounds if we are
        boolean blockPositionHasChanged = Mth.floor(this.xOld) != Mth.floor(this.getX())
                || Mth.floor(this.yOld) != Mth.floor(this.getY())
                || Mth.floor(this.zOld) != Mth.floor(this.getZ());
        int tickUpdatePeriod = blockPositionHasChanged ? 2 : 40;
        if (this.tickCount % tickUpdatePeriod == 0) {
            if (this.level.getFluidState(this.blockPosition()).is(FluidTags.LAVA) && !this.fireImmune()) {
                this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }
        }

        // isAirBorne is poorly named.  it actually means "has the entity accelerated significantly?"
        //  if true, it prompts the server to send an immediate entity update to the client
        this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();  //.handleWaterMovement
        if (!this.level.isClientSide) {
            double accelerationSquared = this.getDeltaMovement().subtract(initialVelocity).lengthSqr();
            final double THRESHOLD_ACCELERATION_TO_TRIGGER_UPDATE = 0.1;

            if (accelerationSquared > THRESHOLD_ACCELERATION_TO_TRIGGER_UPDATE * THRESHOLD_ACCELERATION_TO_TRIGGER_UPDATE) {
                this.hasImpulse = true;
            }

            for (Entity e: getPassengers()) {
                e.resetFallDistance();
            }

            resetFallDistance();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(MOMENTUM_DMP, compound.getFloat(MOMENTUM_NBT));
        if (compound.getFloat(MaxHealthNBT) > 0)
            this.entityData.set(MAX_HEALTH, compound.getFloat(MaxHealthNBT));
        if (compound.getFloat(HealthNBT) > 0)
            this.entityData.set(HEALTH, compound.getFloat(HealthNBT));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat(MOMENTUM_NBT, this.entityData.get(MOMENTUM_DMP));
        compound.putFloat(MaxHealthNBT, this.entityData.get(MAX_HEALTH));
        compound.putFloat(HealthNBT, this.entityData.get(HEALTH));
    }

    /** called when underwater: apply upwards acceleration if currently moving slower than maximum upwards speed
     */
    private void applyFloatMotion() {
        Vec3 velocity = this.getDeltaMovement();
        final double SIDEWAYS_FRICTION_FACTOR = 0.99;
        final double MAXIMUM_UPWARDS_VELOCITY = 0.06;    // blocks per tick
        final double ACCELERATION_DUE_TO_BUOYANCY = 0.0005;  // blocks per tick squared
        double upwardsAcceleration = 0;
        if (velocity.y < MAXIMUM_UPWARDS_VELOCITY) {
            upwardsAcceleration = ACCELERATION_DUE_TO_BUOYANCY;
        }
        this.setDeltaMovement(velocity.x * SIDEWAYS_FRICTION_FACTOR,
                velocity.y + upwardsAcceleration,
                velocity.z * SIDEWAYS_FRICTION_FACTOR);
    }

    // Check to see if we collide with any entities
    protected EntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec, Vec3 motion) {
        AABB aabb = this.getBoundingBox().expandTowards(motion).inflate(1.0);
        // this aabb defines a worst case region that we might collide with entities in
        // i.e. if an entity's aabb doesn't intersect this region then the boomerang can't collide with it
        return ProjectileUtil.getEntityHitResult(this.level, this, startVec, endVec, aabb,
                this::canCollideWith);
    }

    // Called when the boomerang hits a block.
    // If the block is weak, smash it and keep flying
    // Otherwise, bounce off it and stop flying.
    //@Override
    private void onBlockHit(BlockHitResult rayTraceResult) {

        // is the block weak enough for the boomerang to smash through it?
        //   the boomerang is modelled as a wooden axe for the purposes of tool effectiveness
        BlockPos blockPos = rayTraceResult.getBlockPos();

        if (!canDestroyBlock(blockPos)) {  // block is too hard; make the boomerang bounce off and stop flying
            stopFlightDueToBlockImpact(rayTraceResult);
        } else { // smash block and keep flying
            harvestBlockWithItemDrops(level, blockPos);
        }
    }

    protected boolean canDestroyBlock(BlockPos pos) {
        Level world = this.getLevel();
        BlockState blockState = world.getBlockState(pos);
        float blockHardness = blockState.getDestroySpeed(world, pos);

        // typical destroy speeds:
        //  1.0F default, 2.0F wooden axe on proper material
        // typical hardness:
        // 1.5 for stone, 0.6 for grass, 2.0 for logs, 0.2 for leaves
        // Our momentum calculations are chosen so that a boomerang will break exactly one log (hardness 2.0)

        float remainingMomentum = 0.5f;
        return remainingMomentum >= blockHardness;
    }

    // ricochet off a solid block and stop flying.
    private void stopFlightDueToBlockImpact(BlockHitResult rayTraceResult) {
        //this.playSound(SoundEvents.METAL_HIT, 0.25F, 0.5F);
        // make the boomerang ricochet off the face
        Vec3 velocity = this.getDeltaMovement();
        final float RICOCHET_SPEED = 0.1f; // amount of speed left after ricochet
        switch (rayTraceResult.getDirection()) {
            case EAST: {
                if (velocity.x < 0) {
                    takeVelocityDamage(velocity.x);
                    velocity = new Vec3(-RICOCHET_SPEED * velocity.x, velocity.y, velocity.z);
                }
                break;
            }
            case WEST: {
                if (velocity.x > 0) {
                    takeVelocityDamage(velocity.x);
                    velocity = new Vec3(-RICOCHET_SPEED * velocity.x, velocity.y, velocity.z);
                }
                break;
            }
            case NORTH: {
                if (velocity.z > 0) {
                    takeVelocityDamage(velocity.z);
                    velocity = new Vec3(velocity.x, velocity.y, -RICOCHET_SPEED * velocity.z);
                }
                break;
            }
            case SOUTH: {
                if (velocity.z < 0) {
                    takeVelocityDamage(velocity.z);
                    velocity = new Vec3(velocity.x, velocity.y, -RICOCHET_SPEED * velocity.z);
                }
                break;
            }
            case UP:      // shouldn't happen, but if it does- just "graze" the surface without bouncing off
            case DOWN:
            default: {
                break;
            }
        }
        this.setDeltaMovement(velocity);
    }

    /**
     * Called when the boomerang in flight hits an entity
     */
    //@Override
    protected void onHitEntity(EntityHitResult rayTraceResult) {
        Entity target = rayTraceResult.getEntity();

        float speed = (float)this.getDeltaMovement().length() * 20;  // speed in blocks per second
        final float SPEED_FOR_MINIMUM_DAMAGE = 2.0f; // blocks per second
        final float SPEED_FOR_MAXIMUM_DAMAGE = 20.0f; // blocks per second
        final float DAMAGE_MULTIPLIER_FOR_MINIMUM_SPEED = 0.25F;
        final float DAMAGE_MULTIPLIER_FOR_MAXIMUM_SPEED = 2.0F;

        float speedMultiplierForDamage = (float)UsefulFunctions.interpolate_with_clipping(speed,
                SPEED_FOR_MINIMUM_DAMAGE, SPEED_FOR_MAXIMUM_DAMAGE,
                DAMAGE_MULTIPLIER_FOR_MINIMUM_SPEED, DAMAGE_MULTIPLIER_FOR_MAXIMUM_SPEED);
        int baseDamage = Mth.ceil(Math.max(speedMultiplierForDamage * 2, 0.0D));
        final float MAX_DAMAGE_BOOST_RATIO = 3.0F;  // at max power enchantment, add this much extra, eg 3 = add 300% extra damage
        float damageBoost = baseDamage * MAX_DAMAGE_BOOST_RATIO; //damageBoostLevel = 0.0 --> 1.0

        // special enchantments (eg BANE OF ARTHROPODS) cause extra damage to some creature types
        float specialDamageRatio = 0;
        if (!this.level.isClientSide && target instanceof LivingEntity) {
            LivingEntity targetLivingEntity = (LivingEntity)target;
            for (Map.Entry<Enchantment, Integer> enchantment : specialDamages.entrySet()) {
                if (enchantment.getKey() instanceof DamageEnchantment) {
                    DamageEnchantment damageEnchantment = (DamageEnchantment)(enchantment.getKey());
                    specialDamageRatio += damageEnchantment.getDamageBonus(enchantment.getValue(), targetLivingEntity.getMobType());
                } else {
                    LOGGER.warn("Expected a DamageEnchantment but got instead:" + enchantment.getKey());
                }
            }
        }
        float specialDamage = baseDamage * specialDamageRatio;
        float totalDamage = baseDamage + damageBoost + specialDamage;
        DamageSource damagesource;

        boolean isEnderMan = target.getType() == EntityType.ENDERMAN;  // endermen are immune to effects? copied from vanilla

        /* might be useful later: if (entityTookDamage) {
            if (isEnderMan) {
                return;
            }

            if (target instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)target;

                if (this.knockbackLevel > 0) {
                    final float VERTICAL_KNOCKBACK = 0.1F;
                    final float KNOCKBACK_VELOCITY_AT_MAX_ENCHANTMENT = 1.2F;   // blocks per tick velocity
                    Vec3 knockbackVelocity = this.getMotion().mul(1.0D, 0.0D, 1.0D).normalize()
                            .scale(this.knockbackLevel * KNOCKBACK_VELOCITY_AT_MAX_ENCHANTMENT);
                    if (knockbackVelocity.lengthSquared() > 0.0D) {
                        livingentity.addVelocity(knockbackVelocity.x, VERTICAL_KNOCKBACK, knockbackVelocity.z);
                    }
                }

                // special enchantments (eg BANE OF ARTHROPODS) apply an effect (eg slow) as well as extra damage
                if (!this.world.isRemote && thrower instanceof LivingEntity) {
                    for (Map.Entry<Enchantment, Integer> enchantment : specialDamages.entrySet()) {
                        if (enchantment.getKey() instanceof DamageEnchantment) {
                            DamageEnchantment damageEnchantment = (DamageEnchantment)(enchantment.getKey());
                            damageEnchantment.onEntityDamaged(thrower, target, enchantment.getValue());
                        } else {
                            LOGGER.warn("Expected a DamageEnchantment but got instead:" + enchantment.getKey());
                        }
                    }
                }
            }
        } else {
            target.forceFireTicks(fireTimer);  // undo any flame effect we added
        }*/
        //stopFlightDueToEntityImpact(rayTraceResult, target.isInvulnerable());
    }

    // destroy this block and spawn the relevant item drops
    //  copied from world.destroyBlock
    private void harvestBlockWithItemDrops(Level world, BlockPos blockPos) {
        BlockState blockstate = world.getBlockState(blockPos);
        if (blockstate.isAir()) return;

        FluidState ifluidstate = world.getFluidState(blockPos);
        final int EVENT_ID_BREAK_BLOCK_SOUND_AND_PARTICLES = 2001;
        //world.playEvent(EVENT_ID_BREAK_BLOCK_SOUND_AND_PARTICLES, blockPos, Block.getStateId(blockstate));
        BlockEntity tileentity = blockstate.hasBlockEntity() ? world.getBlockEntity(blockPos) : null;

        int flags = SetBlockStateFlag.get(SetBlockStateFlag.BLOCK_UPDATE, SetBlockStateFlag.SEND_TO_CLIENTS);
        world.setBlock(blockPos, ifluidstate.createLegacyBlock(), flags);
    }

    private final int INITIAL_NON_COLLISION_TICKS = 2;

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return pEntity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(pEntity) && !this.hasPassenger(pEntity);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource) || pSource.msgId.equals("mob")) {
            return false;
        } else if (pSource.msgId.equals("player")) {
            return false;
        } else if (level instanceof ServerLevel) {
            markHurt();
            this.entityData.set(HEALTH, entityData.get(HEALTH) - pAmount);
            lastAttack = pSource.getEntity();
            return true;
        }
        return false;
    }

    //@Override
    //public boolean canBeCollidedWith() {
    //    return true;
    //}

    @Override
    public boolean isPushable() {
        return true;
    }

    protected double getGravityScale(){
        return -0.04d;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    protected void onDestroyed() {
        if (!level.isClientSide) {
            level.explode(lastAttack, getX(), getY(), getZ(), 10, true, Explosion.BlockInteraction.BREAK);
        }
    }

    public void setFreecam(boolean useFreecam) {
        // Freecam gets disabled
        if (freecam && !useFreecam) {
            onFreecamDisable();
        }

        this.freecam = useFreecam;
    }

    public void takeVelocityDamage(double impact) {
        double baseForce = Math.pow(Math.abs(impact) * 10f, 1.5f);
        System.out.println("Ouch! " + baseForce);

        if (baseForce >= 2f)
            hurt(DamageSource.FLY_INTO_WALL, (float) baseForce);
    }

    // Hook for firing guns
    public void fire() {  }

    // Hook for disabling freecam
    protected void onFreecamDisable() {  }

    public float getHealth(boolean usePercent) {
        return entityData.get(HEALTH) * (usePercent ? 0.01f : 1);
    }

    public float getHealth() { return getHealth(false); }

    public float getToyScale() {
        return toyScale;
    }

    public ResourceLocation GetModelResource() { return mrl; }
    public ResourceLocation GetTextureResource() { return trl; }

    private static final Logger LOGGER = LogManager.getLogger();

    // From ProjectileUtil
    public HitResult getHitResult(Entity pProjectile, Predicate<Entity> pFilter) {
        Vec3 vec3 =
            pProjectile.getDeltaMovement().add(pProjectile.getDeltaMovement().normalize()
                .multiply(getBbWidth(), getBbHeight(), getBbWidth()));
        Level level = pProjectile.level;
        Vec3 vec31 = pProjectile.position();
        Vec3 vec32 = vec31.add(vec3);
        HitResult hitresult = level.clip(new ClipContext(vec31, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pProjectile));
        if (hitresult.getType() != HitResult.Type.MISS) {
            vec32 = hitresult.getLocation();
        }

        HitResult hitresult1 = ProjectileUtil.getEntityHitResult(level, pProjectile, vec31, vec32,
            pProjectile.getBoundingBox().expandTowards(pProjectile.getDeltaMovement()).inflate(1.0D), pFilter);
        if (hitresult1 != null) {
            hitresult = hitresult1;
        }

        return hitresult;
    }

    protected void checkInsideBlocksCustom() {
        AABB aabb = getBoundingBox().inflate(1f).move(position());
        delOverlappingBlocksIfPossible(aabb);
        delOverlappingBlocksIfPossible(aabb.move(getDeltaMovement()));
    }

    protected void delOverlappingBlocksIfPossible(AABB aabb) {
        for (int x = (int) Math.floor(aabb.minX); x <= Math.ceil(aabb.maxX); x++) {
            for (int y = (int) Math.floor(aabb.minY); y <= Math.ceil(aabb.maxY); y++) {
                for (int z = (int) Math.floor(aabb.minZ); z <= Math.ceil(aabb.maxZ); z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    BlockState bs = level.getBlockState(bp);

                    if (bs.isAir())
                        continue;

                    if (canDestroyBlock(bp))
                        harvestBlockWithItemDrops(level, bp);
                }
            }
        }
    }
}