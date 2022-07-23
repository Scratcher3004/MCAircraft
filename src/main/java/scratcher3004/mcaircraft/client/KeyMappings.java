package scratcher3004.mcaircraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import scratcher3004.mcaircraft.Mcaircraft;

public final class KeyMappings {
    public static KeyMapping FreeLook;

    public static void init() {
        FreeLook = registerKey("free_look", KeyMapping.CATEGORY_GAMEPLAY, InputConstants.KEY_LCONTROL);
    }

    private static KeyMapping registerKey(String name, String category, int keycode) {
        KeyMapping key = new KeyMapping("key." + Mcaircraft.MODID + "." + name, keycode, category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
