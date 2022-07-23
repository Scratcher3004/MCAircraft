package scratcher3004.mcaircraft.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import scratcher3004.mcaircraft.ModEntityTypes;
import scratcher3004.mcaircraft.entities.Parachute;

public class ParachuteItem extends Item {
    public ParachuteItem() {
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        Parachute pc = new Parachute(ModEntityTypes.PARACHUTE.get(), pLevel);
        pc.setPos(pPlayer.position());

        if (!pLevel.addFreshEntity(pc)) {
            return InteractionResultHolder.pass(itemstack);
        }

        pPlayer.startRiding(pc);
        itemstack.shrink(1);
        return InteractionResultHolder.consume(itemstack);
    }
}
