package com.darktidegames.celeo;

import java.util.Iterator;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ==========================<br>
 * <b>DarkJustice</b><br>
 * ==========================<br>
 * <br>
 * <br>
 * Plugin for keeping track of player's information.<br>
 * Logs commands like ban and kick by staff for documentation.
 * 
 * @author Celeo
 */
public class DarkJustice extends JavaPlugin
{

	public DatabaseManager dbManager = null;
	public Config myConfig = null;

	@Override
	public void onEnable()
	{
		myConfig = new Config(this);
		getDataFolder().mkdirs();
		getServer().getPluginManager().registerEvents(new DarkJusticeListener(this), this);
		getServer().getPluginManager().registerEvents(new JailListener(this), this);
		dbManager = new DatabaseManager(this);
		getCommand("darkjustice").setExecutor(this);
		getCommand("dj").setExecutor(this);
		getLogger().info("Enabled");
	}

	@Override
	public void onDisable()
	{
		dbManager.stop();
		myConfig.saveConfiguration();
		getLogger().info("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;
		String name = player.getName();
		if (args.length == 0)
		{
			player.sendMessage("§7/dj (creative | watch | find | reload | flush | defaults)");
			return true;
		}
		String param = args[0].toLowerCase();
		if (param.equals("creative"))
		{
			if (!hasPerms(player, myConfig.adminNode))
				return true;
			String players = "";
			for (Player p : getServer().getOnlinePlayers())
			{
				if (p.getGameMode().equals(GameMode.CREATIVE))
				{
					players += p.getName() + ", ";
				}
			}
			player.sendMessage("§7"
					+ (players == "" ? "No players are in creative." : "These players are in creative: "
							+ "§6" + players));
			return true;
		}
		else if (param.equals("-reload"))
		{
			if (!hasPerms(player, myConfig.adminNode))
				return true;
			myConfig.loadConfiguration(false);
			player.sendMessage("§7Settings loaded from file.");
			return true;
		}
		else if (param.equals("find"))
		{
			if (!hasPerms(player, myConfig.modNode))
				return true;
			if (args.length >= 2)
			{
				for (Player p : getServer().getOnlinePlayers())
					if (p.getAddress().toString().split(":")[0].contains(args[1]))
						player.sendMessage("§c" + p.getName()
								+ "§7 has the IP of §6"
								+ p.getAddress().toString().split(":")[0]);
			}
			else
			{
				player.sendMessage("§c/dj find [ip address]");
				player.sendMessage("§7You can use the entire IP address or search for part of it.");
			}
			return true;
		}
		else if (param.equals("-save"))
		{
			if (!hasPerms(player, myConfig.adminNode))
				return true;
			myConfig.saveConfiguration();
			player.sendMessage("§7Settings flushed to file.");
			return true;
		}
		else if (param.equals("-defaults"))
		{
			if (!hasPerms(player, myConfig.adminNode))
				return true;
			myConfig.setDefaults();
			player.sendMessage("§7Settings reset.");
			return true;
		}
		// else if (param.equals("oneip"))
		// {
		// if (!hasPerms(player, myConfig.adminNode))
		// return true;
		// List<String> addresses = new ArrayList<String>();
		// String ip = "";
		// String kicked = "";
		// for (Player onlinePlayer : getServer().getOnlinePlayers())
		// {
		// ip = onlinePlayer.getAddress().toString().split(":")[0];
		// if (addresses.contains(ip)
		// && !onlinePlayer.hasPermission("sudo.mod")
		// && !onlinePlayer.hasPermission("sudo.admin"))
		// {
		// kicked += onlinePlayer.getName() + " ";
		// onlinePlayer.kickPlayer("You are already logged in.");
		// }
		// else
		// addresses.add(ip);
		// }
		// if (kicked == "")
		// player.sendMessage("§7No players were kicked.");
		// else
		// player.sendMessage("§7Kicked players:" + kicked);
		// return true;
		// }
		else if (param.equals("stopserver"))
		{
			getLogger().severe(name + " is trying to stop the server!");
			if (!hasPerms(player, myConfig.modNode))
				return true;
			if (args.length == 2 && args[1].equals("-y"))
			{
				getLogger().severe(name + " is stopping the server!");
				player.sendMessage("§4Stopping server ...");
				getServer().shutdown();
				return true;
			}
			player.sendMessage("§4/dj stopserver -y | WARNING: STOPS THE SERVER!");
			return true;
		}
		// else if (param.equals("-d"))
		// {
		// if (!hasPerms(player, myConfig.adminNode))
		// return true;
		// if (args.length < 3)
		// {
		// player.sendMessage("§cdj -d [player/-player] [message]");
		// return true;
		// }
		// String message = "";
		// for (int i = 2; i < args.length; i++)
		// message += args[i] + " ";
		// message = message.replaceAll("&", "§");
		// if (args[1].startsWith("-"))
		// {
		// for (Player onlinePlayer : getServer().getOnlinePlayers())
		// if (!onlinePlayer.getName().equals(args[1].replace("-", "")))
		// onlinePlayer.sendMessage(message);
		// }
		// else
		// {
		// Player toSend = getServer().getPlayer(args[1]);
		// if (toSend == null)
		// player.sendMessage("§cCould not find the receiver.");
		// else
		// toSend.sendMessage(message);
		// }
		// player.sendMessage("§aMessage sent: " + message);
		// return true;
		// }
		else if (param.equals("warn"))
		{
			if (!hasPerms(player, myConfig.modNode))
				return true;
			if (args.length < 3)
			{
				player.sendMessage("§c/dj warn [player] [message]");
				return true;
			}
			String message = "";
			for (int i = 2; i < args.length; i++)
				message += args[i] + " ";
			message = message.replaceAll("&", "§");
			Player temp = getServer().getPlayer(args[1]);
			if (temp != null && temp.isOnline())
			{
				temp.sendMessage("§7[§4WARNING§7]: " + message);
				player.sendMessage("§7Sent " + message + " to " + temp);
			}
			else
				player.sendMessage("§cPlayer is not online or cannot be found!");
			dbManager.logWarning(player.getName(), message, args[1]);
		}
		else if (param.equals("watchlist"))
		{
			if (!hasPerms(player, myConfig.modNode))
				return true;
			if (args.length == 1)
			{
				player.sendMessage("§7Watchlist for §6everyone§7:");
				for (String line : myConfig.watchList)
					player.sendMessage("§7" + line);
			}
			else if (args.length == 2)
			{
				player.sendMessage("§7Watchlist for §6" + args[1] + "§7:");
				for (String line : myConfig.watchList)
				{
					if (line.startsWith(args[1].toLowerCase()))
						player.sendMessage("§7" + line);
				}
			}
			else if (args.length == 3)
			{
				if (args[1].equalsIgnoreCase("pardon"))
				{
					int count = 0;
					Iterator<String> i = myConfig.watchList.iterator();
					while (i.hasNext())
					{
						if (i.next().startsWith(args[2]))
						{
							count++;
							i.remove();
						}
					}
					player.sendMessage("§7Removed §6" + count + " §7entries");
				}
				else
					player.sendMessage("§/dj watchlist (pardon) <who>");
			}
			else if (args.length >= 4)
			{
				if (args[1].equalsIgnoreCase("add"))
				{
					String message = "";
					for (int i = 3; i < args.length; i++)
					{
						if (message.equals(""))
							message = args[i];
						else
							message += " " + args[i];
					}
					myConfig.watchList.add(args[2] + "|" + message);
					player.sendMessage(String.format("§7Added §6%s §7to player with name §4%s", message, args[2]));
				}
				else
					player.sendMessage("§/dj watchlist (add) <who> <reason>");
			}
			else
				player.sendMessage("§/dj watchlist (add|pardon) <who> <reason>");
			return true;
		}
		else if (param.equals("jail"))
		{
			if (!hasPerms(player, myConfig.modNode))
				return true;
			if (args.length < 2)
			{
				player.sendMessage("§/dj jail [list|add|remove] (who)");
				return true;
			}
			if (args[1].equalsIgnoreCase("list"))
			{
				String names = "";
				for (String inJail : myConfig.jailed)
				{
					if (names.equals(""))
						names = inJail;
					else
						names += ", " + inJail;
				}
				player.sendMessage("§7Players in jail: §c"
						+ (names.equals("") ? "§7-none-" : names));
			}
			else if (args[1].equalsIgnoreCase("add"))
			{
				if (args.length != 3)
				{
					player.sendMessage("§c/dj jail add [who]");
					return true;
				}
				if (myConfig.jailed.contains(args[2]))
					player.sendMessage("§cThat player is already in jail");
				else
				{
					myConfig.jailed.add(args[2]);
					player.sendMessage("§c" + args[2] + " §7put in jail");
					Player temp = getServer().getPlayer(args[2]);
					if (temp == null || !temp.isOnline())
						return true;
					temp.teleport(myConfig.jailLocation);
					temp.sendMessage("§cYou have been jailed");
				}
			}
			else if (args[1].equalsIgnoreCase("remove"))
			{
				if (args.length != 3)
				{
					player.sendMessage("§c/dj jail remove [who]");
					return true;
				}
				if (myConfig.jailed.contains(args[2]))
				{
					myConfig.jailed.remove(args[2]);
					player.sendMessage("§c" + args[2] + " §7freed from jail");
					Player temp = getServer().getPlayer(args[2]);
					if (temp == null || !temp.isOnline())
						return true;
					temp.teleport(myConfig.jailLocation);
					temp.sendMessage("§aYou have been freed from jailed.");
				}
				else
					player.sendMessage("§cThat player is not in jail");
			}
			else
				player.sendMessage("§/dj jail [list|add|remove] (who)");
			return true;
		}
		else
			player.sendMessage("§7/dj");
		return true;
	}

	private boolean hasPerms(Player player, String node)
	{
		if (!player.hasPermission(node))
		{
			player.sendMessage("§cYou do not have permission for this command.");
			return false;
		}
		return true;
	}

	public static boolean isInt(String input)
	{
		try
		{
			Integer.parseInt(input);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public List<String> getPreviousIPs(String name)
	{
		return dbManager.getPreviousIPs(name);
	}

	public Config getMyConfig()
	{
		return myConfig;
	}

	public boolean isJailed(String name)
	{
		return myConfig.jailed.contains(name);
	}

	public void jail(String name)
	{
		if (!isJailed(name))
			myConfig.jailed.add(name);
	}

	public boolean freeFromJail(String name)
	{
		return myConfig.jailed.remove(name);
	}

}