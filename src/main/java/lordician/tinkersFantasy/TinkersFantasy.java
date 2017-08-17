package lordician.tinkersFantasy;

import lordician.tinkersFantasy.common.CommonProxy;
import lordician.tinkersFantasy.common.item.ItemBaseMagicTools;
import lordician.tinkersFantasy.network.MessageExtendedReachAttack;
import lordician.tinkersFantasy.tools.melee.TinkerFantasyMeleeWeapons;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModInfo.MODID,
name = ModInfo.NAME,
version = ModInfo.VERSION,
dependencies = ModInfo.DEPENDS)
public class TinkersFantasy {
	
	public static Logger logger = LogManager.getLogger(ModInfo.MODID);
	public static final Random random = new Random();
	
	public static SimpleNetworkWrapper network;
	
	TinkerFantasyMeleeWeapons meleeWeapons;
	
	@SidedProxy(serverSide = "lordician.tinkersFantasy.common.CommonProxy", clientSide = "lordician.tinkersFantasy.common.ClientProxy")
	public static CommonProxy proxy;
	
	@Mod.Instance(ModInfo.MODID)
	public static TinkersFantasy instance;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		System.out.println(ModInfo.NAME + " is loading!");
		System.out.println("Called method: preInit");
		
		meleeWeapons = new TinkerFantasyMeleeWeapons();
		
		TinkersFantasy.proxy.preInit(event);
		meleeWeapons.preInit(event);
		
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("Called method: init");
		MinecraftForge.EVENT_BUS.register(instance);
		registerNetworkChannel();
		TinkersFantasy.proxy.init(event);
		meleeWeapons.init(event);
		ItemBaseMagicTools.init();
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		System.out.println("Called method: postInit");
		TinkersFantasy.proxy.postInit(event);
		meleeWeapons.postInit(event);
	}
	
	private void registerNetworkChannel() {
		network = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MODID);
		int packetId = 0;
		// register messages from client to server
		network.registerMessage(MessageExtendedReachAttack.Handler.class, MessageExtendedReachAttack.class, packetId++, Side.SERVER);
	}
}
