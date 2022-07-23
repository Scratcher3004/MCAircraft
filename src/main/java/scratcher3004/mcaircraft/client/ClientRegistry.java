package scratcher3004.mcaircraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import scratcher3004.mcaircraft.Mcaircraft;
import scratcher3004.mcaircraft.ModEntityTypes;
import scratcher3004.mcaircraft.entities.Aircraft;
import scratcher3004.mcaircraft.entities.renderer.AircraftRenderer;
import scratcher3004.mcaircraft.entities.Helicopter;
import scratcher3004.mcaircraft.entities.renderer.BulletRenderer;
import scratcher3004.mcaircraft.entities.renderer.ParachuteRenderer;
import scratcher3004.mcaircraft.entities.model.ParachuteModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Mcaircraft.MODID, value = Dist.CLIENT)
public final class ClientRegistry {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        KeyMappings.init();
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LOGGER.info("Registering Renderers");
        event.registerEntityRenderer(ModEntityTypes.AIRCRAFT.get(), AircraftRenderer<Aircraft>::new);
        event.registerEntityRenderer(ModEntityTypes.HELICOPTER.get(), AircraftRenderer<Helicopter>::new);
        event.registerEntityRenderer(ModEntityTypes.PARACHUTE.get(), ParachuteRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BULLET.get(), BulletRenderer::new);
    }

    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        LOGGER.info("Registering Layers");
        event.registerLayerDefinition(ParachuteModel.LAYER_LOCATION, ParachuteModel::createBodyLayer);
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
