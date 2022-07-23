package scratcher3004.mcaircraft.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import scratcher3004.mcaircraft.Mcaircraft;
import scratcher3004.mcaircraft.entities.Aircraft;
import scratcher3004.mcaircraft.network.PacketHandler;
import scratcher3004.mcaircraft.network.packet.ServerboundAircraftUpdatePacket;

@Mod.EventBusSubscriber(modid = Mcaircraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static boolean freecam = false;
    private static int i = 0;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        i--;
        if (Minecraft.getInstance().level == null) return;
        if (!(Minecraft.getInstance().player.getVehicle() instanceof Aircraft ac)) return;

        var click = Minecraft.getInstance().mouseHandler.isLeftPressed();

        // Prevents too many packets kick
        if (click && i <= 0) {
            PacketHandler.INSTANCE.sendToServer(new ServerboundAircraftUpdatePacket(ServerboundAircraftUpdatePacket.ActionType.HOLD_ATTACK));
            i = 1;
        }

        if (KeyMappings.FreeLook.isDown()) {
            if (freecam) return;
            freecam = true;
            PacketHandler.INSTANCE.sendToServer(new ServerboundAircraftUpdatePacket(ServerboundAircraftUpdatePacket.ActionType.FREECAM_ON));
            ac.setFreecam(true);
        } else {
            if (!freecam) return;
            freecam = false;
            PacketHandler.INSTANCE.sendToServer(new ServerboundAircraftUpdatePacket(ServerboundAircraftUpdatePacket.ActionType.FREECAM_OFF));
            ac.setFreecam(false);
        }
    }

    /** One-time button press */
    @SubscribeEvent
    public static void onClickInAircraft(InputEvent.ClickInputEvent event) {
        if (!(Minecraft.getInstance().player.getVehicle() instanceof Aircraft ac)) return;

        if (event.isAttack()) {
            // Left click
            PacketHandler.INSTANCE.sendToServer(new ServerboundAircraftUpdatePacket(ServerboundAircraftUpdatePacket.ActionType.ATTACK));
            event.setCanceled(true);
            event.setSwingHand(false);
        } else {
            // Right click
        }
    }
}
