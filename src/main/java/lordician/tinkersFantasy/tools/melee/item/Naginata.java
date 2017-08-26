package lordician.tinkersFantasy.tools.melee.item;

import java.util.List;
import javax.annotation.Nonnull;

import lordician.tinkersFantasy.common.item.IExtendedReach;
import lordician.tinkersFantasy.library.tools.TinkersFantasySwordCore;
import lordician.tinkersFantasy.tools.melee.TinkerFantasyMeleeWeapons;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Naginata extends TinkersFantasySwordCore implements IExtendedReach
{
	public static double DEFAULT_ATTACKSPEED = 0.9D;
	
	public Naginata(String name)
	{
		super(	PartMaterialType.handle(TinkerTools.toolRod), //Handle_1
				PartMaterialType.handle(TinkerTools.toolRod), //Handle_2
				PartMaterialType.extra(TinkerTools.handGuard), //Guard
				PartMaterialType.head(TinkerTools.swordBlade)); //Blade
		this.addCategory(Category.WEAPON);
		this.setUnlocalizedName(name).setRegistryName(name);
	}
	
	public float durabilityModifier() {
		return 1.2F;
	}
	
	public float attackAddition() {
		return 2.0F;
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
		
		data.attack += attackAddition();
		data.durability *= durabilityModifier();
		
		return data;
	}

	@Override
	public float damagePotential()
	{
		return 0.95f;
	}
	
	@Override
	public float damageCutoff()
	{
		return 16.0f;
	}

	@Override
	public double attackSpeed()
	{
		return Naginata.DEFAULT_ATTACKSPEED;
	}
	
	@Override
	public float knockback()
	{
		return 0.6f;
	}
	
	@Override
	public float getRepairModifierForPart(int index)
	{
		return durabilityModifier();
	}
	
	//Extended Reach Implementation!
	@Override
	public float getReach()
	{
		return 7.0f;
	}
	
	//Sweep Attack Implementation!
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		
		if (ToolHelper.isBroken(itemStackIn))
		{
			return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);
		}
		playerIn.setActiveHand(hand);
		
		boolean flag1 = playerIn.fallDistance > 0.0F && !playerIn.onGround && !playerIn.isOnLadder() && !playerIn.isInWater() && !playerIn.isPotionActive(MobEffects.BLINDNESS) && !playerIn.isRiding(); //We accept riding with the Naginata Sweep Alt-Attack
		if (!flag1 && this.readyForSpecialAttack(playerIn) && !playerIn.isSprinting())
		{
			double reach = (double)this.getReach();
			List<EntityLivingBase> hitEnemies = playerIn.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, playerIn.getEntityBoundingBox().expand(reach, reach, reach));
			for (EntityLivingBase entitylivingbase : hitEnemies)
			{
				double distanceTo = playerIn.getDistanceToEntity(entitylivingbase);
				//calculations needed to check if the target is in front of the player.
				float entityHitYaw = (float) ((Math.atan2(entitylivingbase.posZ - playerIn.posZ, entitylivingbase.posX - playerIn.posX) * (180 / Math.PI) - 90) % 360);
				float entityAttackingYaw = playerIn.rotationYaw % 360;
				if (entityHitYaw < 0) {
				    entityHitYaw += 360;
				}
				if (entityAttackingYaw < 0) {
				    entityAttackingYaw += 360;
				}
				float entityRelativeYaw = entityHitYaw - entityAttackingYaw;
				
				float xzDistance = (float) Math.sqrt((entitylivingbase.posZ - playerIn.posZ) * (entitylivingbase.posZ - playerIn.posZ) + (entitylivingbase.posX - playerIn.posX) * (entitylivingbase.posX - playerIn.posX));
				float entityHitPitch = (float) ((Math.atan2((entitylivingbase.posY - playerIn.posY), xzDistance) * (180 / Math.PI)) % 360);
				float entityAttackingPitch = -playerIn.rotationPitch % 360;
				if (entityHitPitch < 0) {
				    entityHitPitch += 360;
				}
				if (entityAttackingPitch < 0) {
				    entityAttackingPitch += 360;
				}
				float entityRelativePitch = entityHitPitch - entityAttackingPitch;
				float arc = 90.0f;
				boolean yawCheck = (entityRelativeYaw <= arc / 2 && entityRelativeYaw >= -arc / 2) || (entityRelativeYaw >= 360 - arc / 2 || entityRelativeYaw <= -360 + arc / 2);
				boolean pitchCheck = (entityRelativePitch <= arc / 2 && entityRelativePitch >= -arc / 2) || (entityRelativePitch >= 360 - arc / 2 || entityRelativePitch <= -360 + arc / 2);

				if (entitylivingbase != playerIn && !playerIn.isOnSameTeam(entitylivingbase)
						&& distanceTo <= reach && yawCheck && pitchCheck && playerIn.canEntityBeSeen(entitylivingbase))
				{
					entitylivingbase.knockBack(playerIn, 0.4F,
							(double) MathHelper.sin(playerIn.rotationYaw * 0.017453292F),
							(double) (-MathHelper.cos(playerIn.rotationYaw * 0.017453292F)));
					ToolHelper.attackEntity(playerIn.getHeldItem(hand), this, playerIn, entitylivingbase);
				}
			}
			
			playerIn.getEntityWorld().playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, playerIn.getSoundCategory(), 1.0f, 1.0f);
			if (!playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).hasModifier(TinkerFantasyMeleeWeapons.cooldown_debuff))
			{
				playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).applyModifier(TinkerFantasyMeleeWeapons.cooldown_debuff);
				
			}
			playerIn.swingArm(hand);
			playerIn.spawnSweepParticles();
			playerIn.addExhaustion(0.2F);
			playerIn.resetCooldown();
			return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
		}
		return ActionResult.newResult(EnumActionResult.PASS, itemStackIn);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		// has to be done in onUpdate because onTickUsing is too early and gets overwritten. bleh.
		if (entityIn instanceof EntityPlayer)
		{
			if (((EntityPlayer) entityIn).getCooledAttackStrength(0.5F) > 0.9F)
			{
				//Cooldown regen'd, remove naginata attackspeed debuff
				if (((EntityLivingBase) entityIn).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).hasModifier(TinkerFantasyMeleeWeapons.cooldown_debuff))
				{
					((EntityLivingBase) entityIn).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).removeModifier(TinkerFantasyMeleeWeapons.cooldown_debuff);
				}
			}
		}
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}
	
	@Override
	public boolean dealDamage(ItemStack stack, EntityLivingBase player, Entity entity, float damage) {
		//We want to 'nerf' the Naginata on short range to make shorter range weapons more interesting on their range and this weapon powerful at long ranges.
		Vec3d playerPos = player.getPositionVector();
		Vec3d entityPos = entity.getPositionVector();
		float distance = (float)playerPos.subtract(entityPos).lengthSquared();
		float minDistance = 5.0F;
		//This weapon needs to be at least at sword reach (or above, which means 5+) to do full damage
		
		distance -= minDistance * minDistance;
		float damageModifier = 1.0F;
		if (distance != 0.0F) {
			damageModifier = 1.0F - (-distance / (minDistance * minDistance));
		}
		float damageAddition = damage * 0.2F;
		damageAddition -= damageAddition * damageModifier;
		System.out.println("PRE: " + damage);
		System.out.println("ADD: " + damageAddition);
		System.out.println("MOD: " + damageModifier);
		damage -= damageAddition;
		
		System.out.println("FINAL: " + damage);
		return super.dealDamage(stack, player, entity, damage);
	}

	@Override
	public float useMovementSpeedModifier() {
		return 0.5F;
	}
	
	
	
}
