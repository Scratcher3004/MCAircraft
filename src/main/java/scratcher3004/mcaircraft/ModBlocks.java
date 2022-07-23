package scratcher3004.mcaircraft;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import scratcher3004.mcaircraft.blocks.ToyBlock;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Mcaircraft.MODID);

    // Toys
    public static final RegistryObject<Block> ToyAircraft = BLOCKS.register("aw", ToyBlock::new);
}
