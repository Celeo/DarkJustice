package com.darktidegames.celeo;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.darktidegames.empyrean.C;

/**
 * Listener for the DarkJustice plugin
 * 
 * @author Celeo
 */
public class DarkJusticeListener implements Listener
{

	private final DarkJustice plugin;
	public Map<String, Long> throttles = new HashMap<String, Long>();
	public Map<String, Integer> timesThrottled = new HashMap<String, Integer>();
	public Map<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();

	/**
	 * Default constructor
	 * 
	 * @param instance
	 *            DarkJustice
	 */
	public DarkJusticeListener(DarkJustice plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Compass passthrough logging for Moderators
	 * 
	 * @param event
	 *            PlayerInteractEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCompassPassThrough(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (player.hasPermission("sudo.mod")
				&& !player.hasPermission("sudo.admin"))
		{
			if (player.getItemInHand().getType().equals(Material.COMPASS))
			{
				if (event.getAction() == Action.RIGHT_CLICK_AIR
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				{
					String message = String.valueOf(player.getName()
							+ " used compass pass through"
							+ formatLocation(player.getLocation()));
					plugin.getMyConfig().logCompassInfo(message);
				}
			}
		}
		if (player.getGameMode().equals(GameMode.CREATIVE))
		{
			Block block = event.getClickedBlock();
			if (block == null || block.getTypeId() == 0)
				return;
			if (block.getType().equals(Material.CHEST)
					|| block.getType().equals(Material.ENDER_CHEST))
				plugin.getMyConfig().logCreativeSevere(player.getName()
						+ " interacted with a chest"
						+ formatLocation(block.getLocation()));
		}
	}

	// @EventHandler
	public void onPlayerShareIP(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if (player.hasPermission("sudo.mod"))
			return;
		String raw = event.getMessage();
		if (!raw.contains("\\."))
			return;
		String[] args = raw.split(".");
		if (args.length < 4)
			return;
		int count = 0;
		for (int i = 0; i < args.length; i++)
		{
			if (C.isInt(args[i]))
				count++;
		}
		if (count > 3)
		{
			event.setCancelled(true);
			event.setMessage("I just got banned for sharing an IP address!");
			plugin.getLogger().warning(player.getName() + " chatted message `"
					+ raw + "`, which contained " + count
					+ " numbers, seperated by periods!");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ban "
					+ player.getName() + " Spamming");
		}
	}

	/**
	 * Returns the formatted String of the passed location.<br>
	 * Includes the <b>world</b>, <b>X value</b>, <b>Y value</b>, and <b>Z
	 * value</b> of the location.
	 * 
	 * @param location
	 * @return
	 */
	public String formatLocation(Location location)
	{
		return " in world " + location.getWorld().getName() + " at "
				+ formatDouble(location.getX()) + " "
				+ formatDouble(location.getY()) + " "
				+ formatDouble(location.getZ());
	}

	/**
	 * Formats the passed double to a String with the double to 2 decimal places
	 * 
	 * @param doubleToFormat
	 * @return
	 */
	public String formatDouble(double doubleToFormat)
	{
		return new DecimalFormat("#.##").format(doubleToFormat);
	}

	/**
	 * Logging Creative players' item drops
	 * 
	 * @param event
	 *            PlayerDropItemEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCreativePlayerDropItem(PlayerDropItemEvent event)
	{
		if (event.getPlayer().hasPermission("darkjustice.gm.bypass"))
			return;
		Player player = event.getPlayer();
		if (player.getGameMode().equals(GameMode.CREATIVE))
			event.setCancelled(true);
	}

	/**
	 * Block Creative players' item pickups
	 * 
	 * @param event
	 *            PlayerPickupItemEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCreativePlayerItemPickup(PlayerPickupItemEvent event)
	{
		if (event.getPlayer().hasPermission("darkjustice.gm.bypass"))
			return;
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			event.setCancelled(true);
	}

	/**
	 * Disable a player in Creative from accessing a chest
	 * 
	 * @param event
	 *            PlayerInteractEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCreativePlayerChestAccess(PlayerInteractEvent event)
	{
		if (event.getPlayer().hasPermission("darkjustice.gm.bypass"))
			return;
		if (event.getClickedBlock() != null
				&& event.getClickedBlock().getType().equals(Material.CHEST)
				&& event.getPlayer().getGameMode().equals(GameMode.CREATIVE)
				&& event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			event.setCancelled(true);
	}

	/**
	 * Watching for players changing gamemodes
	 * 
	 * @param event
	 *            PlayerGameModeChangeEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerGameModeSwitch(PlayerGameModeChangeEvent event)
	{
		Player player = event.getPlayer();
		String name = player.getName();
		plugin.getMyConfig().logCreativeInfo(name + " is now in "
				+ event.getNewGameMode().name() + " mode");
		switch (event.getNewGameMode())
		{
		default:
		case ADVENTURE:
			break;
		case SURVIVAL:
			checkInventory(player);
			break;
		case CREATIVE:
			inventories.put(player.getName(), player.getInventory().getContents());
			break;
		}
	}

	private void checkInventory(Player player)
	{
		if (!inventories.containsKey(player.getName()))
			throw new NullPointerException();
		ItemStack[] items = inventories.get(player.getName());
		inventories.remove(player.getName());
		for (int i = 0; i != 36; i++)
		{
			if (player.getInventory().getItem(i) == null && items[i] == null)
				continue;
			if (player.getInventory().getItem(i) == null && items[i] != null)
			{
				failInventory(player, i, player.getInventory().getItem(i), items[i]);
				continue;
			}
			if (player.getInventory().getItem(i) != null && items[i] == null)
			{
				failInventory(player, i, player.getInventory().getItem(i), items[i]);
				continue;
			}
			if (player.getInventory().getItem(i).getTypeId() == items[i].getTypeId()
					&& player.getInventory().getItem(i).getAmount() == items[i].getAmount())
				continue;
			failInventory(player, i, player.getInventory().getItem(i), items[i]);
		}
		/*
		 * TODO item[i] array out of bounds. Use (i - offset).
		 */
		// for (int i = 100; i != 104; i ++)
		// {
		// if (player.getInventory().getItem(i) == null && items[i] == null)
		// continue;
		// if (player.getInventory().getItem(i) == null && items[i] != null)
		// {
		// failInventory(player, i, player.getInventory().getItem(i), items[i]);
		// continue;
		// }
		// if (player.getInventory().getItem(i) != null && items[i] == null)
		// {
		// failInventory(player, i, player.getInventory().getItem(i), items[i]);
		// continue;
		// }
		// if (player.getInventory().getItem(i).getTypeId() ==
		// items[i].getTypeId()
		// && player.getInventory().getItem(i).getAmount() ==
		// items[i].getAmount())
		// continue;
		// failInventory(player, i, player.getInventory().getItem(i), items[i]);
		// }
	}

	@SuppressWarnings("boxing")
	private void failInventory(Player player, int slot, ItemStack found, ItemStack expected)
	{
		plugin.myConfig.logCreativeSevere(String.format("Mismatch at %d for %s: Found %s, Exceptected %s", slot, player.getName(), found != null ? found.getTypeId()
				+ ":" + found.getAmount() : 0, expected != null ? expected.getTypeId()
				+ ":" + expected.getAmount() : 0));
	}

	/**
	 * Chat throttling
	 * 
	 * @param event
	 *            PlayerChatEvent
	 */
	@SuppressWarnings("boxing")
	// @EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTalkTooFast(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String playerName = event.getPlayer().getDisplayName();
		long time = System.currentTimeMillis();
		String toThrottle = "darkjustice.throttle";
		if (player.hasPermission("sudo.mod")
				|| player.hasPermission("sudo.admin"))
			return;
		for (String s : plugin.getMyConfig().throttleGroups)
		{
			if (s.equalsIgnoreCase(toThrottle))
			{
				if (!throttles.containsKey(playerName))
				{
					throttles.put(playerName, time);
					return;
				}
				if (throttles.get(playerName)
						+ plugin.getMyConfig().throttleTime >= time)
				{
					player.sendMessage(ChatColor.RED
							+ "You are talking too fast.");
					timesThrottled.put(playerName, (timesThrottled.containsKey(playerName) ? (timesThrottled.get(playerName) + 1) : 1));
					if (timesThrottled.get(playerName) >= plugin.getMyConfig().maxSpamMessages)
					{
						player.setBanned(true);
						player.kickPlayer("Banned from DarkTide for spamming!");
						plugin.getLogger().info(playerName
								+ " was banned for spamming by DarkJustice!");
						for (Player p : player.getServer().getOnlinePlayers())
							if (player.hasPermission(plugin.getMyConfig().modNode))
								p.sendMessage("§c"
										+ playerName
										+ " was banned for spamming by §6DarkJustice");
					}
					event.setCancelled(true);
				}
				else
					throttles.remove(playerName);
			}
		}
	}

	/**
	 * Logging of adminstration commands before they are processed.
	 * 
	 * @param event
	 *            PlayerCommandPreprocessEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		String[] args = event.getMessage().split(" ");
		String label = args[0];
		if (plugin.getMyConfig().logCommands.contains(label.toLowerCase().replace("/", "")))
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("sender", player.getName());
			params.put("time", String.valueOf(System.currentTimeMillis()));
			params.put("command", label);
			if (args.length == 2)
				params.put("target", args[1]);
			else if (args.length >= 3)
			{
				params.put("target", args[1]);
				String reason = "";
				for (int i = 2; i < args.length; i++)
					reason += args[i] + " ";
				params.put("reason", reason);
			}
			plugin.dbManager.logCommand(params);
		}
	}

	/**
	 * When a player joins, refresh their records.<br>
	 * If the player is not a Moderator or above, and their <br>
	 * <br>
	 * IP matches that of an already logged in player, kick them. This code is
	 * not placed in the PlayerLoginEvent due to the inability to access the
	 * player's IP at that point.
	 * 
	 * @param event
	 *            PlayerJoinEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		plugin.dbManager.updatePlayer(event.getPlayer().getName(), event.getPlayer().getAddress().toString().split(":")[0], String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * Logs Creative players' block placements
	 * 
	 * @param event
	 *            BlockPlaceEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCreativePlayerBlockPlace(BlockPlaceEvent event)
	{
		if (event.getPlayer().hasPermission("darkjustice.gm.bypass"))
			return;
		Player player = event.getPlayer();
		if (!player.getGameMode().equals(GameMode.CREATIVE))
			return;
		Block block = event.getBlock();
		plugin.getMyConfig().logCreativeInfo(player.getName()
				+ " placed block " + block.getType().name()
				+ formatLocation(block.getLocation()));
	}

	/**
	 * Logs Creative players' block breaks
	 * 
	 * @param event
	 *            BlockBreakEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCreativePlayerBlockBreak(BlockBreakEvent event)
	{
		if (event.getPlayer().hasPermission("darkjustice.gm.bypass"))
			return;
		Player player = event.getPlayer();
		if (!player.getGameMode().equals(GameMode.CREATIVE))
			return;
		Block block = event.getBlock();
		plugin.getMyConfig().logCreativeInfo(player.getName() + " broke block "
				+ block.getType().name() + formatLocation(block.getLocation()));
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreativeUsePotion(PlayerItemConsumeEvent event)
	{
		Player player = event.getPlayer();
		if (player.getGameMode().equals(GameMode.CREATIVE))
		{
			plugin.getMyConfig().logCreativeInfo(player.getName()
					+ " drank potion " + event.getItem().getTypeId() + ":"
					+ event.getItem().getDurability()
					+ formatLocation(player.getLocation()));
			event.setCancelled(true);
		}
	}

	public DarkJustice getPlugin()
	{
		return plugin;
	}

}