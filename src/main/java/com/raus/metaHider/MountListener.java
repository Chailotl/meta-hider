package com.raus.metaHider;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class MountListener implements Listener
{
	@EventHandler
	public void onMount(EntityMountEvent event)
	{
		updateMount(event.getEntity(), event.getMount());
	}

	@EventHandler
	public void onDismount(EntityDismountEvent event)
	{
		updateMount(event.getEntity(), event.getDismounted());
	}
	
	private void updateMount(Entity entity, Entity mount)
	{
		// Ignore if it isn't a player mounting a living entity
		if (!(entity instanceof Player) || !(mount instanceof LivingEntity))
		{
			return;
		}
		
		// Get stuff
		LivingEntity ent = (LivingEntity) mount;
		double health = ent.getHealth();
		
		// "Update" health
		ent.setHealth(1);
		ent.setHealth(health);
	}
}