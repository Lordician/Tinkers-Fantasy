package lordician.tinkersFantasy.library.tools;

import javax.annotation.Nonnull;

import lordician.tinkersFantasy.TinkersFantasy;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
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
import slimeknights.tconstruct.common.TinkerNetwork;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.common.entity.EntityArrow;
import slimeknights.tconstruct.tools.common.network.EntityMovementChangePacket;

public abstract class ShieldCore extends TinkersFantasyTool {

	public ShieldCore(PartMaterialType... requiredComponents) {
		super(requiredComponents);
		this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
		      @Override
		      @SideOnly(Side.CLIENT)
		      public float apply(@Nonnull ItemStack stack, World worldIn, EntityLivingBase entityIn) {
		        return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
		      }
		});
	}
	
	public abstract float cooldownAttackspeedModifier();
	public abstract float cooldownUnUsedModifier();
	
	public abstract boolean canReflectProjectiles();
	
	//How much the user has to look in the direction of the incoming attack. (1 is spot on, everything below is less spot on.)
	public double blockDirectFactor() {
		return 0.1D;
	}
	
	public boolean canBlockFullHeight() {
		return false;
	}
	
	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (!ToolHelper.isBroken(itemStackIn) && !playerIn.getCooldownTracker().hasCooldown(itemStackIn.getItem())) {
			playerIn.setActiveHand(hand);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
	}
	
	//Now for the blocking part
	@SubscribeEvent(priority = EventPriority.LOW)
	public void reducedDamageBlocked(LivingHurtEvent event) {
		//We don't block unblockable, magic or explosion damage
		//Projectiles are handled in the LivingAttackEvent
		if (event.getSource().isUnblockable() ||
				event.getSource().isMagicDamage() ||
				event.getSource().isExplosion() ||
				event.getSource().isProjectile() ||
				event.isCanceled()) {
			return;
		}
		EntityLivingBase entity = event.getEntityLiving();
		if (shouldBlockDamage(entity)) {
			//Entity is blocking, so do some blocking stuff.
			onBlockedDamage(event);
		}
		else {
			//Entity is not blocking, so we stop the blocking code here
			return;
		}
		
		//this.disableShield(true, entity, shield);
	}
	
	@SubscribeEvent
	public void projectileBlock(LivingAttackEvent event) {
		onUserAttacked(event);
	}
	
	protected void onBlockedDamage(LivingHurtEvent event) {
		//Custom block effects can be done using this function.
		EntityLivingBase entity = event.getEntityLiving();
		Entity attacker = event.getSource().getEntity();
		
		ItemStack shield = entity.getActiveItemStack();
		
		int toolDamage = event.getAmount() < 2f ? 1 : Math.round(event.getAmount() / 2f);
		
		if (attacker != null) {
			//ToolHelper.attackEntity(shield, this, entity, attacker);
			toolDamage *= 3 / 2;
			//if (attacker instanceof EntityLivingBase) {
			//	EntityLivingBase attackerLivingBase = (EntityLivingBase) attacker;
			//	attackerLivingBase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,50,3));
			//	attackerLivingBase.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE,50,4));
			//}
		}
		
		event.setAmount(event.getAmount() * 0.7f);
		ToolHelper.damageTool(shield, toolDamage, entity);
		
	}
	
	protected void onUserAttacked(LivingAttackEvent event) {
		EntityLivingBase entityIn = event.getEntityLiving();
		DamageSource source = event.getSource();
		if (!shouldBlockDamage(entityIn) || !isSourceBlockable(entityIn, source) || !source.isProjectile())
		{
			//Damage is unblockable or not a projectile. So we stop
			return;
		}
		ItemStack shield = entityIn.getActiveItemStack();
		float damage = event.getAmount();
		int toolDamage = damage < 2f ? 1 : Math.round(damage / 2f);
		
	    Entity projectile = event.getSource().getSourceOfDamage();
	    //Vec3d motion = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ);
	    Vec3d look = entityIn.getLookVec();

	    // this gives a factor of how much we're looking at the incoming arrow
	    //double strength = -look.dotProduct(motion.normalize());
	    // we're looking away. oh no.
	    //if(strength < 0.1)
	    //{
	    //	return;
	    //}
	    event.setCanceled(true);
	    
	    if (canReflectProjectiles()) {
	    	//Oh look, Tinker's Construct BattleSign code!
	        // and return it to the sender
	        // calc speed of the projectile
	        double speed = projectile.motionX * projectile.motionX + projectile.motionY * projectile.motionY + projectile.motionZ * projectile.motionZ;
	        speed = Math.sqrt(speed);
	        speed += 0.2f; // we add a bit speed

	        // and redirect it to where the player is looking
	        projectile.motionX = look.xCoord * speed;
	        projectile.motionY = look.yCoord * speed;
	        projectile.motionZ = look.zCoord * speed;

	        projectile.rotationYaw = (float) (Math.atan2(projectile.motionX, projectile.motionZ) * 180.0D / Math.PI);
	        projectile.rotationPitch = (float) (Math.atan2(projectile.motionY, speed) * 180.0D / Math.PI);

	        // notify clients from change, otherwise people will get veeeery confused
	        TinkerNetwork.sendToAll(new EntityMovementChangePacket(projectile));

	        // special treatement for arrows
	        if(projectile instanceof EntityArrow) {
	          ((EntityArrow) projectile).shootingEntity = entityIn;

	          // the inverse is done when the event is cancelled in arrows etc.
	          // we reverse it so it has no effect. yay
	          projectile.motionX /= -0.10000000149011612D;
	          projectile.motionY /= -0.10000000149011612D;
	          projectile.motionZ /= -0.10000000149011612D;
	        }
	        ToolHelper.damageTool(shield, toolDamage, entityIn);
	    }
	}
	
	protected boolean shouldBlockDamage(EntityLivingBase entityIn) {
		//Needs to be blocking with shield
		if (!entityIn.isActiveItemStackBlocking() || entityIn.getActiveItemStack().getItem() != this) {
			return false;
		}
		
		//check if tool is broken or not
		//We return that one and do not check for cooldown because active blocking can only be done if the tool is off cooldown anyway.
		return !ToolHelper.isBroken(entityIn.getActiveItemStack());
	}
	
	//Oh look, EntityLivingBase (Vanilla Minecraft) canBlockDamageSource code, but slightly edited so we don't check if the entity is actively blocking!
	protected boolean isSourceBlockable(EntityLivingBase entityIn, DamageSource damageSourceIn) {
		if (!damageSourceIn.isUnblockable() && damageSourceIn.getSourceOfDamage() != null && !damageSourceIn.isExplosion() && !damageSourceIn.isMagicDamage()) {
			//Okay, so the source is blockable and there is a source of damage
			Vec3d look = entityIn.getLook(1.0F);
			Vec3d damageLocation = damageSourceIn.getDamageLocation();
			Vec3d damageDirection = null;

			if (damageSourceIn.isProjectile()) {
				Entity projectile = damageSourceIn.getSourceOfDamage();
				if (projectile != null) {
					damageDirection = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ);
				}
			}
			else if (damageLocation != null) {
				
				damageDirection = entityIn.getPositionVector().subtract(damageLocation);
			}
			
			if (damageDirection != null) {
				if (canBlockFullHeight()) {
					damageDirection = new Vec3d(damageDirection.xCoord, 0.0D, damageDirection.zCoord);
				}
				double strength = -look.dotProduct(damageDirection.normalize());
				
				if (strength > blockDirectFactor()) {
					return true;
				}
			}
		}
		return false;
	}
	
	//Taken from EntityPlayer to ensure compatibility (and change cooldown if needed)
	public void disableShield(boolean alwaysDisable, EntityLivingBase entityIn, ItemStack stack) {
        float f = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(entityIn) * 0.05F;

        if (alwaysDisable) {
            f += 0.75F;
        }

        if (TinkersFantasy.random.nextFloat() < f) {
        	if (entityIn instanceof EntityPlayer) {
    			EntityPlayer player = (EntityPlayer) entityIn;
    			
    			int cooldownTime = this.getShieldCooldownTime(entityIn, stack);
    			player.getCooldownTracker().setCooldown(this, cooldownTime);
    		}
        }
    }
	
	public int getShieldCooldownTime(EntityLivingBase entityIn, ItemStack stack) {
		float entityAttackSpeed = (float)entityIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue();
		float toolAttackSpeed = ToolHelper.getActualAttackSpeed(stack);
		float totalAttackSpeed = toolAttackSpeed * entityAttackSpeed * cooldownAttackspeedModifier();
		int cooldownTime = (int)(1 / totalAttackSpeed * 20); //Attackspeed tick amount is 20
		
		return cooldownTime;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		if (entityLiving.isHandActive() && cooldownUnUsedModifier() > 0.0F) {
			if (entityLiving instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entityLiving;
				int cooldownTime = (int)(getShieldCooldownTime(entityLiving, stack) * cooldownUnUsedModifier());
				player.getCooldownTracker().setCooldown(this, cooldownTime);
				
			}
		}
		return super.onItemUseFinish(stack, worldIn, entityLiving);
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		if (entityLiving.isHandActive() && cooldownUnUsedModifier() > 0.0F) {
			if (entityLiving instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entityLiving;
				int cooldownTime = (int)(getShieldCooldownTime(entityLiving, stack) * cooldownUnUsedModifier());
				player.getCooldownTracker().setCooldown(this, cooldownTime);
				
			}
		}
		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
	}

}
