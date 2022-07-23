package minecraftbyexample.usefultools.debugging;

import minecraftbyexample.usefultools.debugging.commands.DebugTriggerWatcher;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * User: The Grey Ghost
 * Date: 24/12/2014
 *
 * Set up the debugging tools
 */
public class StartupCommon
{
  @SubscribeEvent
  public static void onCommonSetupEvent(FMLCommonSetupEvent event) {

  }

}
