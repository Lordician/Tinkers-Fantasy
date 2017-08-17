package lordician.tinkersFantasy.common;


import lordician.tinkersFantasy.TinkersFantasy;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
	}
	
	public void init(FMLInitializationEvent event)
	{
		registerEventListeners();
	}
	
	public void postInit(FMLPostInitializationEvent event) {
	}
	
	public void registerItemRenderer(Item item, int meta, String id) {
	}
	
	private void registerEventListeners()
	{
		// DEBUG
		TinkersFantasy.logger.info("Registering event listeners");
		MinecraftForge.EVENT_BUS.register(new ExtendedReachEventHandler());

		// some events, especially tick, are handled on FML bus
		MinecraftForge.EVENT_BUS.register(new ExtendedReachEventHandler());
	}
}
