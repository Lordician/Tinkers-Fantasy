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
	ToolBuildGuiInfo bucklerBuildGUI;
	ToolBuildGuiInfo kiteshieldBuildGUI;
	
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
		//Registering Buckler Tool Model
		registerToolModel(TinkerFantasyMeleeWeapons.buckler);
		//Registering Kiteshield Tool Model
		registerToolModel(TinkerFantasyMeleeWeapons.kiteshield);
	}
	
	private void createToolGuis()
	{
		//Naginata Build GUI
		naginataBuildGUI = new ToolBuildGuiInfo(TinkerFantasyMeleeWeapons.naginata);
		//Buckler Build GUI
		bucklerBuildGUI = new ToolBuildGuiInfo(TinkerFantasyMeleeWeapons.buckler);
		//Kiteshield Build GUI
		kiteshieldBuildGUI = new ToolBuildGuiInfo(TinkerFantasyMeleeWeapons.kiteshield);
	}
	
	private void setToolGuis()
	{
		//Naginata Build GUI positions
		naginataBuildGUI.addSlotPosition(33 - 10 - 14, 42 + 10 + 12); //Handle_1
		naginataBuildGUI.addSlotPosition(33 - 8, 42 - 10 + 4); //Handle_2
		naginataBuildGUI.addSlotPosition(33 + 14, 42 - 10 - 2); //Guard
		naginataBuildGUI.addSlotPosition(33 + 10 - 10, 42 + 10 + 6); //Blade
		//Buckler Build GUI positions
		bucklerBuildGUI.addSlotPosition(33 - 20 - 1, 42 + 20); // Edge
		bucklerBuildGUI.addSlotPosition(33 + 20 - 5, 42 - 20 + 4); // Face
		bucklerBuildGUI.addSlotPosition(33 - 2 - 1, 42 + 2); // Boss
		//Kiteshield Build GUI positions
		kiteshieldBuildGUI.addSlotPosition(33 - 10 - 14, 42 + 10 + 12); //Handle_1
		kiteshieldBuildGUI.addSlotPosition(33 - 8, 42 - 10 + 4); //Handle_2
		kiteshieldBuildGUI.addSlotPosition(33 + 14, 42 - 10 - 2); //Guard
		kiteshieldBuildGUI.addSlotPosition(33 + 10 - 10, 42 + 10 + 6); //Blade
	}
	
	private void cleanToolGuis()
	{
		//CLearing Naginata Build GUI
		naginataBuildGUI.positions.clear();
		//CLearing Buckler Build GUI
		bucklerBuildGUI.positions.clear();
		//CLearing Kiteshield Build GUI
		kiteshieldBuildGUI.positions.clear();
	}
	
	private void registerToolGuis()
	{
		//Registering Naginata Build GUI
		TinkerRegistryClient.addToolBuilding(naginataBuildGUI);
		//Registering Buckler Build GUI
		TinkerRegistryClient.addToolBuilding(bucklerBuildGUI);
		//Registering Kiteshield Build GUI
		TinkerRegistryClient.addToolBuilding(kiteshieldBuildGUI);
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
