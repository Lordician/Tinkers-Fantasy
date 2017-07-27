package lordician.tinkersFantasy;

import lordician.tinkersFantasy.common.CommonProxy;
import lordician.tinkersFantasy.common.item.ItemBaseMagicTools;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Random;

@Mod(modid = TinkersFantasy.MODID,
name = TinkersFantasy.MOD_NAME,
version = TinkersFantasy.VERSION)
public class TinkersFantasy {
	
	public static final String MODID = "tinkersfantasy";
	public static final String VERSION = "${version}";
	public static final String MOD_NAME = "Tinkers' Fantasy";
	
	public static final Random random = new Random();
	
	@SidedProxy(serverSide = "lordician.tinkersFantasy.common.CommonProxy", clientSide = "lordician.tinkersFantasy.common.ClientProxy")
	public static CommonProxy proxy;
	
	@Mod.Instance(MODID)
	public static TinkersFantasy instance;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		System.out.println(MOD_NAME + " is loading!");
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
