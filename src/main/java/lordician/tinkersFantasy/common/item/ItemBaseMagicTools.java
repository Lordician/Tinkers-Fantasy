package lordician.tinkersFantasy.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemBaseMagicTools {

	public static ItemMagicWand magicWand;
	
	public static void init() {
		magicWand = ItemBaseMagicTools.register(new ItemMagicWand("teststaff").setCreativeTab(CreativeTabs.COMBAT));
	}
	
	
	private static <T extends Item> T register(T item) {
		
		GameRegistry.register(item);
		
		if (item instanceof ItemModelProvider) {
			((ItemModelProvider)item).registerItemModel(item);
		}
		
		return item;
	}
	
}
