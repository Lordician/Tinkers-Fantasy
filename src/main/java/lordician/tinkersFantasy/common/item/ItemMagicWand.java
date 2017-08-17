package lordician.tinkersFantasy.common.item;

import lordician.tinkersFantasy.TinkersFantasy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemMagicWand extends Item implements ItemModelProvider {

	protected String name;
	
	public ItemMagicWand(String name) {
		this.name = name;
		this.setUnlocalizedName(name);
		this.setCreativeTab(CreativeTabs.COMBAT);
		
		this.maxStackSize = 1;
		this.setMaxDamage(300);
	}

	@Override
	public void registerItemModel(Item item) {
		
		TinkersFantasy.proxy.registerItemRenderer(this, 0, this.name);
		
	}
	
	@Override
	public ItemMagicWand setCreativeTab(CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}
	
}
