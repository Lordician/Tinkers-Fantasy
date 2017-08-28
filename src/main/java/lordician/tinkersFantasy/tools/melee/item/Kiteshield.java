package lordician.tinkersFantasy.tools.melee.item;

import java.util.List;

import lordician.tinkersFantasy.library.tools.ShieldCore;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.tools.TinkerTools;

public class Kiteshield extends ShieldCore {

	public Kiteshield(String name) {
		super(PartMaterialType.handle(TinkerTools.toughToolRod),
				PartMaterialType.head(TinkerTools.largePlate),
				PartMaterialType.head(TinkerTools.largePlate),
				PartMaterialType.extra(TinkerTools.panHead));
		addCategory(Category.WEAPON);
		this.setUnlocalizedName(name).setRegistryName(name);
	}

	@Override
	public float cooldownAttackspeedModifier() {
		return 0.2F;
	}

	@Override
	public float cooldownUnUsedModifier() {
		return 0.0F;
	}

	@Override
	public boolean canReflectProjectiles() {
		return false;
	}

	@Override
	public float durabilityModifier() {
		return 1.0F;
	}

	@Override
	public float attackAddition() {
		return 0.0F;
	}

	@Override
	public float damagePotential() {
		return 0.8F;
	}

	@Override
	public double attackSpeed() {
		return 0.7;
	}
	
	//Custom Overrides
	@Override
	public float useMovementSpeedModifier() {
		return 0.7F;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	protected ToolNBT buildTagData(List<Material> materials)
	{
		HandleMaterialStats edge = materials.get(0).getStatsOrUnknown(MaterialTypes.HANDLE);
		HeadMaterialStats boss_1 = materials.get(1).getStatsOrUnknown(MaterialTypes.HEAD);
		HeadMaterialStats boss_2 = materials.get(2).getStatsOrUnknown(MaterialTypes.HEAD);
		ExtraMaterialStats deco = materials.get(3).getStatsOrUnknown(MaterialTypes.EXTRA);
		
		ToolNBT data = new ToolNBT();
		data.head(boss_1, boss_2);
		data.handle(edge);
		data.extra(deco);
		
		data.attack += attackAddition();
		data.durability *= durabilityModifier();
		
		return data;
	}
}
