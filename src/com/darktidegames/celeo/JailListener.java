package com.darktidegames.celeo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class JailListener implements Listener
{

	private final DarkJustice plugin;

	public JailListener(DarkJustice plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void jailedTalk(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if (event.isCancelled())
			return;
		if (plugin.myConfig.isJailed(player.getName()))
		{
			event.setCancelled(true);
			player.sendMessage("§cYou are jailed");
		}
	}

	@EventHandler
	public void jailedCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		if (event.isCancelled())
			return;
		if (plugin.myConfig.isJailed(player.getName()))
		{
			event.setCancelled(true);
			player.sendMessage("§cYou are jailed");
		}
	}

	@EventHandler
	public void jailedJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.myConfig.isJailed(player.getName()))
		{
			player.teleport(plugin.myConfig.jailLocation);
			player.sendMessage("§cYou are jailed");
		}
	}

	@EventHandler
	public void jailedTeleport(PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.myConfig.isJailed(player.getName()))
		{
			event.setTo(plugin.myConfig.jailLocation);
			player.sendMessage("§cYou are jailed");
		}
	}

	@EventHandler
	public void jailedBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.myConfig.isJailed(player.getName()))
		{
			event.setCancelled(true);
			player.sendMessage("§cYou are jailed");
		}
	}

	@EventHandler
	public void jailedPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.myConfig.isJailed(player.getName()))
		{
			event.setCancelled(true);
			player.sendMessage("§cYou are jailed");
		}
	}

}