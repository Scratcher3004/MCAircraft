package scratcher3004.mcaircraft;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import scratcher3004.mcaircraft.items.ParachuteItem;

public final class ModItems {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Mcaircraft.MODID);

    public static final RegistryObject<Item> Parachute = ITEMS.register("parachute", ParachuteItem::new);
}
