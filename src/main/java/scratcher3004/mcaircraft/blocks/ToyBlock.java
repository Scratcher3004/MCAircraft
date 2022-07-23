package scratcher3004.mcaircraft.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class ToyBlock extends Block {
    public ToyBlock() {
        super(Properties.of(Material.WOOL).noCollission().instabreak().dynamicShape());
    }
}
