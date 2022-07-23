package scratcher3004.mcaircraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import scratcher3004.mcaircraft.entities.Aircraft;
import scratcher3004.mcaircraft.entities.Bullet;
import scratcher3004.mcaircraft.entities.Helicopter;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import scratcher3004.mcaircraft.entities.Parachute;

public class ModEntityTypes
{
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, Mcaircraft.MODID);

    public static final RegistryObject<EntityType<Aircraft>> AIRCRAFT = ENTITY_TYPES.register("aircraft", ()
        -> EntityType.Builder.of(Aircraft::new, MobCategory.MISC).sized(0.5f, 0.5f)
            .build(new ResourceLocation(Mcaircraft.MODID, "aircraft").toString()));
    public static final RegistryObject<EntityType<Helicopter>> HELICOPTER = ENTITY_TYPES.register("helicopter", ()
        -> EntityType.Builder.of(Helicopter::new, MobCategory.MISC).sized(2f, 2f)
            .build(new ResourceLocation(Mcaircraft.MODID, "helicopter").toString()));

    public static final RegistryObject<EntityType<Parachute>> PARACHUTE = ENTITY_TYPES.register("parachute", ()
        -> EntityType.Builder.of(Parachute::new, MobCategory.MISC).sized(1.5f, 3f)
            .build(new ResourceLocation(Mcaircraft.MODID, "parachute").toString()));

    public static final RegistryObject<EntityType<Bullet>> BULLET = ENTITY_TYPES.register("bullet", ()
        -> EntityType.Builder.of(Bullet::new, MobCategory.MISC).sized(.2f, .2f)
        .build(new ResourceLocation(Mcaircraft.MODID, "bullet").toString()));
}
