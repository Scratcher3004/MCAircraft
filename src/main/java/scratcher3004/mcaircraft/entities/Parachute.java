package scratcher3004.mcaircraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class Parachute extends Entity {
    private boolean canBePickedUp = false;

    public Parachute(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pAmount > 0) {
            canBePickedUp = false;
            kill();
            return true;
        }

        return false;
    }

    @Override
    public void tick() {
        if ((getPassengers().isEmpty() || onGround || isInWater()) && !isRemoved() && !level.isClientSide)
        {
            canBePickedUp = true;
            kill();
            return;
        }

        setDeltaMovement(0, -3.5f * 0.05d, 0);
        super.tick();
        this.move(MoverType.SELF, this.getDeltaMovement());

        for (Entity e: getPassengers()) {
            e.resetFallDistance();
        }

        resetFallDistance();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (level instanceof ServerLevel && canBePickedUp) {
            spawnAtLocation(new ItemStack(Items.SADDLE));
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }
}
