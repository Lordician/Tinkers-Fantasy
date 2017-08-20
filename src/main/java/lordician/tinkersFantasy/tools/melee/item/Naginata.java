package lordician.tinkersFantasy.tools.melee.item;

import java.util.List;

import javax.annotation.Nonnull;

import lordician.tinkersFantasy.common.item.IExtendedReach;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;
import net.minecraft.util.math.MathHelper;

public class Naginata extends SwordCore implements IExtendedReach
{
	public static final float DURABILITY_MODIFIER = 1.2f;
	public static final float ATTACK_ADDITION = 1.0f;
	
	public static double defaultAttackSpeed = 1.4d;
	private double currAttackSpeed = defaultAttackSpeed;
	
	public static final AttributeModifier cooldown_debuff = new AttributeModifier("naginata_attackspeed_down", -Naginata.defaultAttackSpeed/1.5, 0);
	
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
		return Naginata.defaultAttackSpeed;
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
	
	//Extended Reach Implementation!
	@Override
	public float getReach()
	{
		return 7.0f;
	}
	
	//Sweep Attack Implementation!
	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.NONE;
	}
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 200;
	}
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		playerIn.setActiveHand(hand);
		
		return ActionResult.newResult(EnumActionResult.PASS, itemStackIn);
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft)
	{
		int time = this.getMaxItemUseDuration(stack) - timeLeft;
		
		double d0 = (double) (player.distanceWalkedModified - player.prevDistanceWalkedModified);
		boolean flag2 = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding();
		if (time > 1 && !ToolHelper.isBroken(stack) && this.readyForSpecialAttack(player) && !player.isSwingInProgress && !player.isSprinting() && !flag2 && d0 < (double) player.getAIMoveSpeed())
		{
			double reach = (double)this.getReach();
			List<EntityLivingBase> hitEnemies = player.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().expand(reach, reach, reach));
			for (EntityLivingBase entitylivingbase : hitEnemies)
			{
				double distanceTo = player.getDistanceToEntity(entitylivingbase);
				//calculations needed to check if the target is in front of the player.
				float entityHitYaw = (float) ((Math.atan2(entitylivingbase.posZ - player.posZ, entitylivingbase.posX - player.posX) * (180 / Math.PI) - 90) % 360);
	            float entityAttackingYaw = player.rotationYaw % 360;
	            if (entityHitYaw < 0) {
	                entityHitYaw += 360;
	            }
	            if (entityAttackingYaw < 0) {
	                entityAttackingYaw += 360;
	            }
	            float entityRelativeYaw = entityHitYaw - entityAttackingYaw;

	            float xzDistance = (float) Math.sqrt((entitylivingbase.posZ - player.posZ) * (entitylivingbase.posZ - player.posZ) + (entitylivingbase.posX - player.posX) * (entitylivingbase.posX - player.posX));
	            float entityHitPitch = (float) ((Math.atan2((entitylivingbase.posY - player.posY), xzDistance) * (180 / Math.PI)) % 360);
	            float entityAttackingPitch = -player.rotationPitch % 360;
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
				
				if (entitylivingbase != player && !player.isOnSameTeam(entitylivingbase)
						&& distanceTo <= reach && yawCheck && pitchCheck)
				{
					entitylivingbase.knockBack(player, 0.4F,
							(double) MathHelper.sin(player.rotationYaw * 0.017453292F),
							(double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
					ToolHelper.attackEntity(stack, this, player, entitylivingbase);
				}
			}
			
			player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0f, 1.0f);
			if (!player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).hasModifier(cooldown_debuff))
			{
				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).applyModifier(cooldown_debuff);
				
			}
			if (attackSpeed() > 0)
			{
				int speed = (int)Math.min(5, player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue());
				ToolHelper.swingItem(speed, player);
			}
			
			if (player instanceof EntityPlayer)
			{
				((EntityPlayer) player).spawnSweepParticles();
				((EntityPlayer) player).addExhaustion(0.2F);
				((EntityPlayer) player).resetCooldown();
			}
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		// has to be done in onUpdate because onTickUsing is too early and gets overwritten. bleh.
		preventSlowDown(entityIn, 0.5f);
		if (entityIn instanceof EntityPlayer)
		{
			if (((EntityPlayer) entityIn).getCooledAttackStrength(0.5F) > 0.9F)
			{
				//Cooldown regen'd, remove naginata attackspeed debuff
				if (((EntityLivingBase) entityIn).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).hasModifier(cooldown_debuff))
				((EntityLivingBase) entityIn).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).removeModifier(cooldown_debuff);
			}
		}
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}
	
	
	
}
