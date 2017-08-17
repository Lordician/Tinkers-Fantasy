package lordician.tinkersFantasy.tools.melee;

import lordician.tinkersFantasy.common.ClientProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import slimeknights.tconstruct.library.tools.ToolCore;

public class MeleeClientProxy extends ClientProxy {

	ToolBuildGuiInfo naginataBuildGUI;
	
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		registerToolModels();
	}
	
	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		createToolGuis();
		setToolGuis();
		registerToolGuis();
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
	
	private void registerToolModels()
	{
		//Registering Naginata Tool Model
		registerToolModel(TinkerFantasyMeleeWeapons.naginata);
	}
	
	private void createToolGuis()
	{
		//Naginata Build GUI
		naginataBuildGUI = new ToolBuildGuiInfo(TinkerFantasyMeleeWeapons.naginata);
	}
	
	private void setToolGuis()
	{
		//Naginata Build GUI positions
		naginataBuildGUI.addSlotPosition(33 - 10 - 14, 42 + 10 + 12); //Handle_1
		naginataBuildGUI.addSlotPosition(33 - 8, 42 - 10 + 4); //Handle_2
		naginataBuildGUI.addSlotPosition(33 + 14, 42 - 10 - 2); //Guard
		naginataBuildGUI.addSlotPosition(33 + 10 - 10, 42 + 10 + 6); //Blade
	}
	
	private void cleanToolGuis()
	{
		//CLearing Naginata Build GUI
		naginataBuildGUI.positions.clear();
	}
	
	private void registerToolGuis()
	{
		//Registering Naginata Build GUI
		TinkerRegistryClient.addToolBuilding(naginataBuildGUI);
	}
	
	public void reloadGuis()
	{
		cleanToolGuis();
		setToolGuis();
		registerToolGuis();
	}
	
	public void registerToolModel(ToolCore tc)
	{
		ModelRegisterUtil.registerToolModel(tc);
	}
}
