package scratcher3004.mcaircraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import minecraftbyexample.usefultools.debugging.DebugBlockVoxelShapeHighlighter;
import minecraftbyexample.usefultools.debugging.DebugSpawnInhibitor;
import minecraftbyexample.usefultools.debugging.RegisterCommandEvent;
import minecraftbyexample.usefultools.debugging.commands.DebugTriggerWatcher;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.server.ServerStartingEvent;
import scratcher3004.mcaircraft.entities.Aircraft;
import scratcher3004.mcaircraft.entities.Helicopter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import scratcher3004.mcaircraft.network.PacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Mcaircraft.MODID)
public class Mcaircraft {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    @SuppressWarnings("SpellCheckingInspection")
    public static final String MODID = "mcaircraft";

    public Mcaircraft() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        ModEntityTypes.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");

        MinecraftForge.EVENT_BUS.register(RegisterCommandEvent.class);
        MinecraftForge.EVENT_BUS.register(DebugSpawnInhibitor.class);
        MinecraftForge.EVENT_BUS.register(DebugTriggerWatcher.class);

        event.enqueueWork(PacketHandler::init);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        MinecraftForge.EVENT_BUS.register(DebugBlockVoxelShapeHighlighter.class);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("mcaircraft", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        }
        @SubscribeEvent
        public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> entityRegistryEvent) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) { // handles drawing aircraft HUD
        if (!event.isCancelable() && event.getType() == RenderGameOverlayEvent.ElementType.LAYER) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null) // client should have player
                return;

            if (mc.player.getVehicle() != null && mc.player.getVehicle() instanceof Aircraft ac) {
                // shared hud
                PoseStack stack = event.getMatrixStack();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.setShaderTexture(0, new ResourceLocation(MODID, "textures/gui/hud.png"));

                if (ac.getControllingPassenger() == mc.player) { // pilot hud
                    mc.gui.blit(stack, 50, event.getWindow().getGuiScaledHeight() - 150, 128, 64, 128, 20);
                    mc.gui.blit(stack, 50, event.getWindow().getGuiScaledHeight() - 150, 0,
                            236, (int) (ac.getHealth(true) * 128f), 20);

                    if (ac instanceof Helicopter heli) { // heli hud
                        int posX = 50;
                        int posY = event.getWindow().getGuiScaledHeight() - 100;
                        mc.gui.blit(stack, posX, posY, 129, 1, 62, 62);
                        stack.pushPose();
                        float angle = heli.getSpeed() * 0.75f * 360;
                        stack.translate(posX + 31 - Math.sin(Math.toRadians(angle)) * 32d,
                                posY + 31 + Math.cos(Math.toRadians(angle)) * 32d, 0);
                        stack.mulPose(Quaternion.fromXYZ(0, 0, (float) Math.toRadians(angle - 180)));
                        mc.gui.blit(stack, -2, 5, 193, 5, 5, 30);
                        stack.popPose();
                    }
                }
                else { // copilot hud
                    // TODO
                }
            }
        }
    }
}
