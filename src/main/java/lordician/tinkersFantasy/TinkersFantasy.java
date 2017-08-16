package lordician.tinkersFantasy;

import lordician.tinkersFantasy.common.CommonProxy;
import lordician.tinkersFantasy.common.item.ItemBaseMagicTools;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Random;

@Mod(modid = ModInfo.MODID,
name = ModInfo.NAME,
version = ModInfo.VERSION)
public class TinkersFantasy {
	
	public static final Random random = new Random();
	
	@SidedProxy(serverSide = "lordician.tinkersFantasy.common.CommonProxy", clientSide = "lordician.tinkersFantasy.common.ClientProxy")
	public static CommonProxy proxy;
	
	@Mod.Instance(ModInfo.MODID)
	public static TinkersFantasy instance;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		System.out.println(ModInfo.NAME + " is loading!");
		System.out.println("Called method: preInit");
		TinkersFantasy.proxy.preInit(event);
		ItemBaseMagicTools.init();
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("Called method: init");
		TinkersFantasy.proxy.init(event);
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		System.out.println("Called method: postInit");
		TinkersFantasy.proxy.postInit(event);
	}
	
}
