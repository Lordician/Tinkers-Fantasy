package lordician.tinkersFantasy.library.tools;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;

public abstract class TinkersFantasyTool extends TinkerToolCore {

	public TinkersFantasyTool(PartMaterialType... requiredComponents) {
		super(requiredComponents);
	}
	
	public abstract float durabilityModifier();
	
	public abstract float attackAddition();
	
	public float useMovementSpeedModifier() {
		return 0.0F;
	}
	
	@Override
	protected ToolNBT buildTagData(List<Material> materials)
	{
		ToolNBT data = buildDefaultTag(materials);
		data.attack += attackAddition();
		data.durability *= durabilityModifier();
		return data;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		if (useMovementSpeedModifier() != 0.0F)
			this.preventSlowDown(entityIn, useMovementSpeedModifier());
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

}
