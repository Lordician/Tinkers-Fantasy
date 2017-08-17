package lordician.tinkersFantasy.tools.melee.item;

import java.util.List;

import lordician.tinkersFantasy.common.item.IExtendedReach;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.tools.TinkerTools;

public class Naginata extends SwordCore implements IExtendedReach
{
	public static final float DURABILITY_MODIFIER = 1.2f;
	public static final float ATTACK_ADDITION = 1.0f;
	
	public Naginata(String name)
	{
		super(	PartMaterialType.handle(TinkerTools.toolRod), //Handle_1
				PartMaterialType.handle(TinkerTools.toolRod), //Handle_2
				PartMaterialType.extra(TinkerTools.handGuard), //Guard
				PartMaterialType.head(TinkerTools.swordBlade)); //Blade
		this.addCategory(Category.WEAPON);
		this.setUnlocalizedName(name).setRegistryName(name);
	}

	@Override
	protected ToolNBT buildTagData(List<Material> materials)
	{
		HandleMaterialStats handle0 = materials.get(0).getStatsOrUnknown(MaterialTypes.HANDLE);
		HandleMaterialStats handle1 = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);
		ExtraMaterialStats guard = materials.get(2).getStatsOrUnknown(MaterialTypes.EXTRA);
		HeadMaterialStats blade = materials.get(3).getStatsOrUnknown(MaterialTypes.HEAD);
		
		ToolNBT data = new ToolNBT();
		data.head(blade);
		data.handle(handle0, handle1);
		data.extra(guard);
		
		data.attack += ATTACK_ADDITION;
		data.durability *= DURABILITY_MODIFIER;
		
		return data;
	}

	@Override
	public float damagePotential()
	{
		return 0.8f;
	}
	
	@Override
	public float damageCutoff()
	{
		return 15.0f;
	}

	@Override
	public double attackSpeed()
	{
		return 1.4d;
	}
	
	@Override
	public float knockback()
	{
		return 0.6f;
	}
	
	@Override
	public float getRepairModifierForPart(int index)
	{
		return DURABILITY_MODIFIER;
	}
	
	@Override
	public float getReach()
	{
		return 7.0f;
	}
}
