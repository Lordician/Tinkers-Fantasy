package lordician.tinkersFantasy.tools.melee.item;

import java.util.List;

import javax.annotation.Nonnull;

import lordician.tinkersFantasy.TinkersFantasy;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

public class Buckler extends TinkerToolCore
{
	
	public int cooldownTime()
	{
		return 100;
	}
	
	public Buckler(String name)
	{
		super(PartMaterialType.handle(TinkerTools.toolRod),
										PartMaterialType.head(TinkerTools.largePlate),
										PartMaterialType.extra(TinkerTools.panHead));
		addCategory(Category.WEAPON);
		this.setUnlocalizedName(name).setRegistryName(name);
		
		this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
		      @Override
		      @SideOnly(Side.CLIENT)
		      public float apply(@Nonnull ItemStack stack, World worldIn, EntityLivingBase entityIn) {
		        return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
		      }
		    });
	}

	@Override
	public float damagePotential()
	{
		// TODO Auto-generated method stub
		return 0.86f;
	}

	@Override
	public double attackSpeed()
	{
		// TODO Auto-generated method stub
		return 1.0d;
	}
	
	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BLOCK;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 20;
	}
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (!ToolHelper.isBroken(itemStackIn) && !playerIn.getCooldownTracker().hasCooldown(itemStackIn.getItem()))
		{
			playerIn.setActiveHand(hand);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		this.preventSlowDown(entityIn, 0.9F);
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}
	
	@Override
	protected ToolNBT buildTagData(List<Material> materials)
	{
		return buildDefaultTag(materials);
	}
	
	//Now for the blocking part
	@SubscribeEvent(priority = EventPriority.LOW)
	public void reducedDamageBlocked(LivingHurtEvent event)
	{
		//We don't block unblockable, magic or explosion damage
		//Projectiles are handled in the LivingAttackEvent
		if (event.getSource().isUnblockable() ||
				event.getSource().isMagicDamage() ||
				event.getSource().isExplosion() ||
				event.getSource().isProjectile() ||
				event.isCanceled())
		{
			return;
		}
		
		EntityLivingBase entity = event.getEntityLiving();
		Entity attacker = event.getSource().getEntity();
		
		ItemStack buckler = entity.getActiveItemStack();
		int damage = event.getAmount() < 2f ? 1 : Math.round(event.getAmount() / 2f);
		
		if (shouldBlockDamage(entity))
		{
			//Entity is blocking, so we parry.
			if (attacker != null)
			{
				attacker.attackEntityFrom(DamageSource.causeThornsDamage(entity), event.getAmount() / 2.0f);
				damage = damage * 3 / 2;
				if (attacker instanceof EntityLivingBase)
				{
					EntityLivingBase attackerLivingBase = (EntityLivingBase) attacker;
					attackerLivingBase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,50,3));
					attackerLivingBase.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE,50,4));
					
				}
			}
		}
		else
		{
			//Entity is not blocking, so we stop the blocking code here
			return;
		}
		
		//In contrary to the Battlesign, from where i took most of this code, the buckler does not reflect.
		ToolHelper.damageTool(buckler, damage, entity);
		this.disableShield(true, entity);
	}
	
	@SubscribeEvent
	public void projectileBlock(LivingAttackEvent event)
	{
		//First of we check the two situations we want to check
		if (event.getSource().isUnblockable() || event.getSource().getSourceOfDamage() == null)
		{
			//Damage is unblockable, without source or not a projectile. So we stop
			return;
		}
		
		EntityLivingBase entityIn = event.getEntityLiving();
		Entity attacker = event.getSource().getEntity();
		ItemStack buckler = entityIn.getActiveItemStack();
		int damage = (int) event.getAmount();
		
		if (event.getSource().isProjectile())
		{
			//Projectile, can only be blocked if we manually block so we check that next.
			if (!shouldBlockDamage(entityIn))
			{
				return;
			}
			//Entity is manually blocking a projectile!
			
			 // ensure the player is looking at the projectile (aka not getting shot into the back)
		    Entity projectile = event.getSource().getSourceOfDamage();
		    Vec3d motion = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ);
		    Vec3d look = entityIn.getLookVec();

		    // this gives a factor of how much we're looking at the incoming arrow
		    double strength = -look.dotProduct(motion.normalize());
		    // we're looking away. oh no.
		    if(strength < 0.1)
		    {
		    	return;
		    }
			
		}
		else if (!shouldBlockDamage(entityIn) && shouldAutoBlockDamage(entityIn))
		{
			//Not being attacked by projectiles, but can be auto-blocked.
			Vec3d damageDirection = entityIn.getPositionVector().subtract(event.getSource().getDamageLocation());
		    Vec3d look = entityIn.getLookVec();

		    // this gives a factor of how much we're looking at the incoming damage
		    double strength = -look.dotProduct(damageDirection.normalize());
		    // we're looking away. oh no.
		    if(strength < 0.1)
		    {
		    	return;
		    }
		    //entityIn.getEntityWorld().playSound(null, entityIn.posX, entityIn.posY, entityIn.posZ, SoundEvents.ITEM_SHIELD_BLOCK, entityIn.getSoundCategory(), 1.0F, 1.0F);
			damage = damage * 3 / 2;
			if (attacker instanceof EntityLivingBase)
			{
				EntityLivingBase attackerLiving = (EntityLivingBase) attacker;
				attackerLiving.knockBack(entityIn, 0.2F, entityIn.posX - attackerLiving.posX, entityIn.posZ - attackerLiving.posZ);
			}
		}
		else
		{
			return;
		}
	    // caught that bastard! block it!
	    event.setCanceled(true);
		
	    
	    ToolHelper.damageTool(buckler, damage, entityIn);
	    this.disableShield(true, entityIn);
	}
	
	protected boolean shouldBlockDamage(Entity entity)
	{
		//hit entity is a player?
		if (!(entity instanceof EntityLivingBase))
		{
			return false;
		}
		EntityLivingBase entityLiving = (EntityLivingBase) entity;
		
		//Needs to be blocking with buckler
		if (!entityLiving.isActiveItemStackBlocking() || entityLiving.getActiveItemStack().getItem() != this)
		{
			return false;
		}
		
		//check if tool is broken or not
		return !ToolHelper.isBroken(entityLiving.getActiveItemStack());
	}
	
	protected boolean shouldAutoBlockDamage(Entity entity)
	{
		//hit entity is a player?
		if (!(entity instanceof EntityLivingBase))
		{
			return false;
		}
		EntityLivingBase entityLiving = (EntityLivingBase) entity;
		
		ItemStack checkStack = entityLiving.getHeldItemMainhand();
		if (checkStack.getItem() != this)
		{
			checkStack = entityLiving.getHeldItemOffhand();
			if (checkStack.getItem() != this)
			{
				return false;
			}
		}
		
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entity;
			if (player.getCooldownTracker().hasCooldown(this))
			{
				return false;
			}
		}
		
		//check if tool is broken or not
		return !ToolHelper.isBroken(checkStack);
	}
	
	//Taken from EntityPlayer to ensure compatibility (and change cooldown if needed)
	public void disableShield(boolean alwaysDisable, EntityLivingBase entityIn)
    {
        float f = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(entityIn) * 0.05F;

        if (alwaysDisable)
        {
            f += 0.75F;
        }

        if (TinkersFantasy.random.nextFloat() < f)
        {
        	if (entityIn instanceof EntityPlayer)
    		{
    			EntityPlayer player = (EntityPlayer) entityIn;
    			
    			player.getCooldownTracker().setCooldown(this, (int)(this.cooldownTime()/player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue()));
    		}
            entityIn.world.setEntityState(entityIn, (byte)29);
        }
    }
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
	{
		if (entityLiving.isHandActive())
		{
			if (entityLiving instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entityLiving;
				player.getCooldownTracker().setCooldown(this, (int)((this.cooldownTime()/2)/player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue()));
				
			}
		}
		return super.onItemUseFinish(stack, worldIn, entityLiving);
	}
}
