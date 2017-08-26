package lordician.tinkersFantasy.library.tools;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;

public abstract class TinkersFantasySwordCore extends TinkersFantasyTool {

	public static final ImmutableSet<Material> effective_materials =
		      ImmutableSet.of(Material.WEB,
		                      Material.VINE,
		                      Material.CORAL,
		                      Material.GOURD,
		                      Material.LEAVES);
	
	public TinkersFantasySwordCore(PartMaterialType... requiredComponents) {
		super(requiredComponents);
		setHarvestLevel("sword", 0);
	}
	
	@Override
	public boolean isEffective(IBlockState state) {
		return effective_materials.contains(state.getMaterial());
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		if(state.getBlock() == Blocks.WEB) {
			return super.getStrVsBlock(stack, state) * 7.5f;
		}
		return super.getStrVsBlock(stack, state);
	}
	
	@Override
	public float miningSpeedModifier() {
		return 0.5f; // slooow, because it's a swooooord
	}
}
