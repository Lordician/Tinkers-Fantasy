package lordician.tinkersFantasy.tools.melee.item;

import lordician.tinkersFantasy.library.tools.ShieldCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.particle.Particles;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

public class Buckler extends ShieldCore {
	
	
	public Buckler(String name) {
		super(PartMaterialType.handle(TinkerTools.toolRod),
										PartMaterialType.head(TinkerTools.largePlate),
										PartMaterialType.extra(TinkerTools.panHead));
		addCategory(Category.WEAPON);
		this.setUnlocalizedName(name).setRegistryName(name);
	}

	//Mandatory Shield overrides
	@Override
	public float cooldownAttackspeedModifier() {
		// TODO Auto-generated method stub
		return 0.2F;
	}

	@Override
	public float cooldownUnUsedModifier() {
		// TODO Auto-generated method stub
		return 0.5F;
	}
	
	@Override
	public boolean canReflectProjectiles() {
		// TODO Auto-generated method stub
		return false;
	}
	
	//Mandatory Tool overrides
	@Override
	public float damagePotential()
	{
		return 0.8f;
	}

	@Override
	public double attackSpeed()
	{
		return 0.9d;
	}
	
	@Override
	public float durabilityModifier() {
		return 1.4F;
	}

	@Override
	public float attackAddition() {
		return 0.0F;
	}
	
	//Custom Overrides
	@Override
	public float useMovementSpeedModifier() {
		return 0.9F;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}
	
	@Override
	protected void onBlockedDamage(LivingHurtEvent event) {
		super.onBlockedDamage(event);
		//Custom buckler stuff
		
		Entity attacker = event.getSource().getEntity();
		EntityLivingBase entityIn = event.getEntityLiving();
		ItemStack shield = entityIn.getActiveItemStack();
		
		ToolHelper.attackEntity(shield, this, entityIn, attacker);
		if (attacker instanceof EntityLivingBase) {
			EntityLivingBase attackerLivingBase = (EntityLivingBase) attacker;
			attackerLivingBase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,50,3));
			attackerLivingBase.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE,50,4));
		}
		//We also want to see an effect after we bash people away.
		TinkerTools.proxy.spawnAttackParticle(Particles.FRYPAN_ATTACK, entityIn, 0.6d);
		
		disableShield(true, entityIn, shield);
	}
	
	@Override
	protected void onUserAttacked(LivingAttackEvent event) {
		super.onUserAttacked(event);
		EntityLivingBase entityIn = event.getEntityLiving();
		DamageSource damageSourceIn = event.getSource();
		//Custom buckler stuff
		if (isSourceBlockable(entityIn, damageSourceIn)) {
			//Damage is unblockable. So we stop.
			return;
		}
		
		ItemStack shield = entityIn.getHeldItemMainhand();
		LivingHurtEvent customHurtEvent = new LivingHurtEvent(entityIn, damageSourceIn, event.getAmount());
		
		boolean isProjectile = event.getSource().isProjectile();
		boolean shouldBlock = shouldBlockDamage(entityIn);
		boolean shouldAutoBlock = shouldAutoBlockDamage(entityIn);
		if (!isProjectile) {
			if (!shouldBlock && shouldAutoBlock) {
				System.out.println("AUTOBLOCK");
				if (shield.getItem() != this) {
					//Apparently, the shield was actually in the offhand!
					shield = entityIn.getHeldItemOffhand();
				}
				//Auto-blocking code, basically, short version of blocking code.
				//Cancel Damage
				event.setCanceled(true);
				//Play block sound
				//OR DO WE?!
				//Set entityState
				entityIn.world.setEntityState(entityIn, (byte)29);
				//Knockback
				Entity attacker = event.getSource().getEntity();
				if (attacker instanceof EntityLivingBase) {
					EntityLivingBase attackerLiving = (EntityLivingBase) attacker;
					attackerLiving.knockBack(entityIn, 0.5F * this.knockback(), entityIn.posX - attackerLiving.posX, entityIn.posZ - attackerLiving.posZ);
				}
				
				//Also, traits that have to be triggered (specifically those that have to do with blocking)
				if (entityIn instanceof EntityPlayer) {
					//Tinker's Construct hates NPC's using these tools... >_>
					EntityPlayer player = (EntityPlayer) entityIn;
					NBTTagList list = TagUtil.getTraitsTagList(shield);
					for (int i = 0; i < list.tagCount(); i++) {
						ITrait trait = TinkerRegistry.getTrait(list.getStringTagAt(i));
						if (trait != null) {
							trait.onBlock(shield, player, customHurtEvent);
						}
					}
				}
				//Oh and the tool has to be damaged
				int toolDamage = event.getAmount() < 2f ? 1 : Math.round(event.getAmount() / 2f);
				ToolHelper.damageTool(shield, toolDamage, entityIn);
			}
			else {
				//Cannot auto-block the melee attack. Stop, drop and roll (out autoblocks)!
				return;
			}
		}
		else if (shouldBlock) {
			//Just to do something here and make sure we got the right thing
			shield = entityIn.getActiveItemStack();
		}
		else {
			return;
		}
		//Because the buckler can only block 1 hit before disabling it, we disable it now.
		disableShield(true, entityIn, shield);
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
}
