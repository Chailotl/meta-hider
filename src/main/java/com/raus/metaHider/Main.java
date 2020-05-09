package com.raus.metaHider;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class Main extends JavaPlugin implements Listener
{
	private static boolean hideStackSize;
	private static boolean hideDisplayName;
	private static boolean hideLore;
	private static boolean hideUnbreakable;
	private static boolean hideEnchantments;
	private static boolean hideDurability;
	private static boolean hidePotionData;
	private static boolean hideHealth;
	private static boolean hideStatusEffects;

	@Override
	public void onEnable()
	{
		// Register command
		getCommand("metahider").setExecutor(new ReloadCommand());

		// Listeners
		getServer().getPluginManager().registerEvents(new MountListener(), this);

		// First time load
		reload();

		// Packet modification
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT)
		{
			@Override
			public void onPacketSending(PacketEvent event)
			{
				// Get
				PacketContainer packet = event.getPacket();
				ItemStack item = packet.getItemModifier().read(0);
				ItemMeta meta = item.getItemMeta();

				// Ignore
				if (item.getType().equals(Material.AIR))
				{
					return;
				}

				// Modify
				if (hideStackSize) { item.setAmount(1); }
				if (hideDisplayName) { meta.setDisplayName(null); }
				if (hideLore) { meta.setLore(null); }
				if (hideUnbreakable) { meta.setUnbreakable(false); }

				if (hideEnchantments && meta.hasEnchants())
				{
					for (Enchantment ench : meta.getEnchants().keySet())
					{
						meta.removeEnchant(ench);
					}
					meta.addEnchant(Enchantment.VANISHING_CURSE, 1, false);
				}

				if (hideDurability && meta instanceof Damageable)
				{
					Damageable dmg = (Damageable) meta;
					dmg.setDamage(0);
					meta = (ItemMeta) dmg;
				}

				if (hidePotionData && meta instanceof PotionMeta)
				{
					PotionMeta pot = (PotionMeta) meta;
					pot.clearCustomEffects();
					pot.setBasePotionData(new PotionData(pot.getBasePotionData().getType()));
					meta = pot;
				}

				// Send
				item.setItemMeta(meta);
				packet.getItemModifier().write(0, item);
			}
		});

		manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA)
		{
			@Override
			public void onPacketSending(PacketEvent event)
			{
				if (!hideHealth) { return; }

				// Get
				PacketContainer packet = event.getPacket();
				Player ply = event.getPlayer();
				Entity entity = packet.getEntityModifier(event).read(0);
				List<WrappedWatchableObject> modifier = packet.getWatchableCollectionModifier().read(0);

				// Ignore
				if (!(entity instanceof LivingEntity))
				{
					// We want living entities
					return;
				}

				// Modify
				for (WrappedWatchableObject obj : modifier)
				{
					/*
					 * 8  - health
					 * 12 - absorption
					 * 14 - absorption (player)
					 */
					if (obj.getIndex() == 8)
					{
						// Players needs to know their health, as well as the health of bosses, mounts, and wolves (their tail counts)
						if (entity.equals(ply)
								|| entity.getPassengers().contains(ply)
								|| entity instanceof Wolf
								|| entity instanceof EnderDragon
								|| entity instanceof Wither)
						{
							obj.setValue((float) ((LivingEntity) entity).getHealth());
						}
						else
						{
							Float value = (Float) obj.getValue();
							if (value > 0)
							{
								double health = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
								obj.setValue((float) health);
							}
						}
					}
					else if (obj.getIndex() == 12)
					{
						// Players need to know their absorption
						if (entity.equals(ply))
						{
							obj.setValue((int) ((LivingEntity) entity).getAbsorptionAmount());
						}
						else
						{
							obj.setValue(0);
						}
					}
					else if (obj.getIndex() == 14 && entity instanceof Player)
					{
						// Players need to know their absorption
						if (entity.equals(ply))
						{
							obj.setValue((float) ((Player) entity).getAbsorptionAmount());
						}
						else
						{
							obj.setValue(0f);
						}
					}
				}
				// https://wiki.vg/Entity_metadata#Living
			}
		});

		manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EFFECT)
		{
			@Override
			public void onPacketSending(PacketEvent event)
			{
				if (!hideStatusEffects) { return; }

				// Get
				PacketContainer packet = event.getPacket();
				int entityID = packet.getIntegers().read(0);

				// Ignore
				if (event.getPlayer().getEntityId() == entityID)
				{
					// Player needs to know their own potion effects
					return;
				}

				// Modify
				packet.getBytes().write(1, (byte) 0); // amplifier
				packet.getIntegers().write(0, 0); // type
				packet.getIntegers().write(1, 0); // duration
				// https://wiki.vg/Protocol#Entity_Effect
			}
		});
	}

	@Override
	public void onDisable()
	{

	}

	public void reload()
	{
		// Prepare
		reloadConfig();
		hideStackSize = getConfig().getBoolean("hideStackSize");
		hideDisplayName = getConfig().getBoolean("hideDisplayName");
		hideLore = getConfig().getBoolean("hideLore");
		hideUnbreakable = getConfig().getBoolean("hideUnbreakable");
		hideEnchantments = getConfig().getBoolean("hideEnchantments");
		hideDurability = getConfig().getBoolean("hideDurability");
		hidePotionData = getConfig().getBoolean("hidePotionData");
		hideHealth = getConfig().getBoolean("hideHealth");
		hideStatusEffects = getConfig().getBoolean("hideStatusEffects");
	}
}