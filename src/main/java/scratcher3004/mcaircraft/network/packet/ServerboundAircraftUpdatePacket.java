package scratcher3004.mcaircraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import scratcher3004.mcaircraft.entities.Aircraft;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ServerboundAircraftUpdatePacket {
    public final ActionType type;

    public ServerboundAircraftUpdatePacket(ActionType type) {
        this.type = type;
    }

    public ServerboundAircraftUpdatePacket(FriendlyByteBuf buffer) {
        this(ActionType.values()[buffer.readInt()]);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(type.ordinal());
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            Entity vhc = ctx.get().getSender().getRootVehicle();

            if (!(vhc instanceof Aircraft ac)) {
                return;
            }

            switch (type) {
                case ATTACK -> {
                    ac.fire();
                }
                case HOLD_ATTACK -> {
                    ac.fire();
                    // TODO Replace with hold
                    var i = 1 + 1;
                }
                case FREECAM_ON -> {
                    ac.setFreecam(true);
                }
                case FREECAM_OFF -> {
                    ac.setFreecam(false);
                }
            }

            success.set(true);
        });

        ctx.get().setPacketHandled(true);
        return success.get();
    }

    public enum ActionType {
        ATTACK,
        HOLD_ATTACK,
        FREECAM_ON,
        FREECAM_OFF
    }
}
