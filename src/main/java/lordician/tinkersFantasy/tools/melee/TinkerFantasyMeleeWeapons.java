package lordician.tinkersFantasy.tools.melee;

import com.google.common.eventbus.Subscribe;

import lordician.tinkersFantasy.common.CommonProxy;
import lordician.tinkersFantasy.tools.melee.item.Naginata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.ToolCore;

public class TinkerFantasyMeleeWeapons
{
	@SidedProxy(clientSide = "lordician.tinkersFantasy.tools.melee.MeleeClientProxy", serverSide = "lordician.tinkersFantasy.common.CommonProxy")
	public static CommonProxy proxy;
	
	public static ToolCore naginata;
	
	@Subscribe
	public void preInit(FMLPreInitializationEvent event)
	{
		registerTools();
		
		proxy.preInit(event);
	}
	
	@Subscribe
	public void init(FMLInitializationEvent event)
	{
		registerToolBuilding();
		proxy.init(event);
	}
	
	@Subscribe
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}
	
	private void registerTools()
	{
		//Naginata registering
		naginata = new Naginata("naginata");
		registerTool(naginata);
		
	}
	
	private void registerToolBuilding()
	{
		//Naginata building registering (forge only)
		TinkerRegistry.registerToolForgeCrafting(naginata);
	}
	
	protected void registerTool(ToolCore tool)
	{
		TinkerRegistry.registerTool(tool);
		GameRegistry.register(tool);
		
	}
}
