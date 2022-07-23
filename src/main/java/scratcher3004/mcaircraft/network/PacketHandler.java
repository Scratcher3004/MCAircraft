package scratcher3004.mcaircraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import scratcher3004.mcaircraft.Mcaircraft;
import scratcher3004.mcaircraft.network.packet.ServerboundAircraftUpdatePacket;

public final class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static PacketHandler ph;

    private PacketHandler() {
    }

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Mcaircraft.MODID,
        "aircraft"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void init() {
        int index = 0;

        INSTANCE.messageBuilder(ServerboundAircraftUpdatePacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(ServerboundAircraftUpdatePacket::encode).decoder(ServerboundAircraftUpdatePacket::new)
            .consumer(ServerboundAircraftUpdatePacket::handle).add();

        System.out.println(index + " Packets registered.");
    }
}
