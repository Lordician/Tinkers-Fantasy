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
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

public class Buckler extends TinkerToolCore
{
	public static final float DURABILITY_MODIFIER = 1.4f;
	public static final float ATTACK_ADDITION = 0.0f;
	
	public static final float COOLDOWN_MODIFIER = 0.2F;
	public static final float COOLDOWN_UNUSED_MODIFIER = 0.5F;
	
	
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
		return 0.8f;
	}

	@Override
	public double attackSpeed()
	{
		// TODO Auto-generated method stub
		return 0.9d;
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
		ToolNBT data = buildDefaultTag(materials);
		data.attack += Buckler.ATTACK_ADDITION;
		data.durability *= Buckler.DURABILITY_MODIFIER;
		return data;
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
		
		event.setAmount(event.getAmount() * 0.7f);
		
		if (shouldBlockDamage(entity))
		{
			//Entity is blocking, so we parry.
			if (attacker != null)
			{
				ToolHelper.attackEntity(buckler, this, entity, attacker);
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
		this.disableShield(true, entity, buckler);
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
		float damage = event.getAmount();
		int toolDamage = (int)damage;
		
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
			damage = 0.0F;
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
				attackerLiving.knockBack(entityIn, 0.5F * this.knockback(), entityIn.posX - attackerLiving.posX, entityIn.posZ - attackerLiving.posZ);
			}
			EnumHand shieldHand = entityIn.getHeldItemMainhand().getItem() == this ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			buckler = entityIn.getHeldItem(shieldHand);
			
			//onBlock traits need to be activated still, but Tinker's Construct hates NPC's wielding tools or something.
			if (entityIn instanceof EntityPlayer)
			{
				EntityPlayer playerIn = (EntityPlayer) entityIn;
				NBTTagList list = TagUtil.getTraitsTagList(buckler);
		        for(int i = 0; i < list.tagCount(); i++)
		        {
					ITrait trait = TinkerRegistry.getTrait(list.getStringTagAt(i));
					if(trait != null)
					{
						trait.onBlock(buckler, playerIn, new LivingHurtEvent(entityIn, event.getSource(), event.getAmount()));
					}
		        }
			}
			damage = 0.0F;
		}
		else
		{
			return;
		}
	    // caught that bastard! block it!
	    event.setCanceled(true);
		
	    if (damage != 0.0F)
	    {
	    	LivingHurtEvent newHurtEvent = new LivingHurtEvent(entityIn, event.getSource(), damage);
	    	MinecraftForge.EVENT_BUS.post(newHurtEvent);
	    }
	    
	    ToolHelper.damageTool(buckler, toolDamage, entityIn);
	    this.disableShield(true, entityIn, buckler);
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
	public void disableShield(boolean alwaysDisable, EntityLivingBase entityIn, ItemStack stack)
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
    			
    			int cooldownTime = this.getShieldCooldownTime(entityIn, stack);
    			player.getCooldownTracker().setCooldown(this, cooldownTime);
    		}
            entityIn.world.setEntityState(entityIn, (byte)29);
        }
    }
	
	public int getShieldCooldownTime(EntityLivingBase entityIn, ItemStack stack)
	{
		float entityAttackSpeed = (float)entityIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue();
		float toolAttackSpeed = ToolHelper.getActualAttackSpeed(stack);
		float totalAttackSpeed = toolAttackSpeed * entityAttackSpeed * Buckler.COOLDOWN_MODIFIER;
		int cooldownTime = (int)(1 / totalAttackSpeed * 20); //Attackspeed tick amount is 20
		
		return cooldownTime;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
	{
		if (entityLiving.isHandActive())
		{
			if (entityLiving instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entityLiving;
				int cooldownTime = (int)(this.getShieldCooldownTime(entityLiving, stack) * Buckler.COOLDOWN_UNUSED_MODIFIER);
				player.getCooldownTracker().setCooldown(this, cooldownTime);
				
			}
		}
		return super.onItemUseFinish(stack, worldIn, entityLiving);
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
	{
		if (entityLiving.isHandActive())
		{
			if (entityLiving instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entityLiving;
				int cooldownTime = (int)(this.getShieldCooldownTime(entityLiving, stack) * Buckler.COOLDOWN_UNUSED_MODIFIER);
				player.getCooldownTracker().setCooldown(this, cooldownTime);
				
			}
		}
		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
	}
	
	
}
