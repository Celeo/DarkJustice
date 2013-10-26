package com.darktidegames.celeo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Location;

import com.darktidegames.empyrean.C;

/**
 * @author Celeo
 */
public class Config
{

	public final DarkJustice plugin;
	public Logger compassLogger = Logger.getLogger("DarkJustice.Compass");
	public Logger creativeLogger = Logger.getLogger("DarkJustice.Creative");
	public List<String> logCommands = new ArrayList<String>();
	public List<String> throttleGroups = new ArrayList<String>();
	public Long throttleTime = Long.valueOf(500);
	public Integer maxSpamMessages = Integer.valueOf(50);
	public String watchGroup = "watch";
	public String adminNode = "dj.admin";
	public String modNode = "dj.mod";
	public List<String> watchList = new ArrayList<String>();
	public List<String> jailed = new ArrayList<String>();
	public Location jailLocation = null;

	public Config(DarkJustice instance)
	{
		plugin = instance;
		loadConfiguration(true);
		setupLoggers();
		saveConfiguration();
	}

	private void setupLoggers()
	{
		try
		{
			File temp = new File(plugin.getDataFolder().getAbsolutePath()
					+ "/compass.log");
			if (!temp.exists())
				temp.createNewFile();
			compassLogger.setUseParentHandlers(false);
			FileHandler handler = new FileHandler(plugin.getDataFolder().getAbsolutePath()
					+ "/compass.log", true);
			handler.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord logRecord)
				{
					return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z").format(new Date(logRecord.getMillis()))
							+ " " + logRecord.getMessage() + "\n";
				}
			});
			compassLogger.addHandler(handler);
		}
		catch (Exception e)
		{
			compassLogger.setUseParentHandlers(true);
			plugin.getLogger().severe("An error occured with setting up the compass logger.");
		}
		try
		{
			File temp = new File(plugin.getDataFolder().getAbsolutePath()
					+ "/creative.log");
			if (!temp.exists())
				temp.createNewFile();
			creativeLogger.setUseParentHandlers(false);
			FileHandler handler = new FileHandler(plugin.getDataFolder().getAbsolutePath()
					+ "/creative.log", true);
			handler.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord logRecord)
				{
					return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z").format(new Date(logRecord.getMillis()))
							+ " " + logRecord.getMessage() + "\n";
				}
			});
			creativeLogger.addHandler(handler);
		}
		catch (Exception e)
		{
			creativeLogger.setUseParentHandlers(true);
			plugin.getLogger().severe("An error occured with setting up the creative logger.");
		}
	}

	public void logCompassInfo(String message)
	{
		compassLogger.info(message);
	}

	public void logCompassSevere(String message)
	{
		compassLogger.info(message);
	}

	public void logCreativeInfo(String message)
	{
		creativeLogger.info(message);
	}

	public void logCreativeSevere(String message)
	{
		creativeLogger.info("[SEVERE] " + message);
	}

	private void checkData()
	{
		boolean shouldSetDefaults = false;
		if (throttleGroups == null)
		{
			throttleGroups = new ArrayList<String>();
			shouldSetDefaults = true;
		}
		if (logCommands == null)
		{
			logCommands = new ArrayList<String>();
			shouldSetDefaults = true;
		}
		if (shouldSetDefaults)
			setDefaults();
	}

	public void setDefaults()
	{
		logCommands = new ArrayList<String>();
		logCommands.add("kick");
		logCommands.add("ban");
		logCommands.add("tempban");
		logCommands.add("unban");
		logCommands.add("pardon");
		logCommands.add("banip");
		logCommands.add("pardonip");
		logCommands.add("unbanip");
		logCommands.add("mute");
		logCommands.add("gmute");

		throttleGroups = new ArrayList<String>();
		throttleGroups.add("nomad");
		throttleGroups.add("watch");

		throttleTime = Long.valueOf(500);
		maxSpamMessages = Integer.valueOf(50);

		adminNode = "dj.admin";
		modNode = "dj.mod";
		watchGroup = "watch";

		watchList = new ArrayList<String>();

		jailed = new ArrayList<String>();

		saveConfiguration();
	}

	public void loadConfiguration(boolean shouldCheck)
	{
		plugin.reloadConfig();
		adminNode = plugin.getConfig().getString("Permission.adminNode", adminNode);
		modNode = plugin.getConfig().getString("Permission.modNode", modNode);
		watchGroup = plugin.getConfig().getString("Permission.watchGroup", watchGroup);
		throttleGroups = plugin.getConfig().getStringList("ChatThrottle.throttleGroups");
		throttleTime = Long.valueOf(plugin.getConfig().getInt("ChatThrottle.throttleTime", throttleTime.intValue()));
		maxSpamMessages = Integer.valueOf(plugin.getConfig().getInt("ChatThrottle.maxSpamMessages", maxSpamMessages.intValue()));
		logCommands = plugin.getConfig().getStringList("FileLogging.logCommands");
		watchList = plugin.getConfig().getStringList("watchList");
		jailed = plugin.getConfig().getStringList("jail.jailed");
		jailLocation = C.stringToLocation(plugin.getConfig().getString("jail.jailLocation"));
		plugin.getLogger().info("Settings loading from the configuration file.");
		if (shouldCheck)
			checkData();
	}

	public void saveConfiguration()
	{
		plugin.getConfig().set("Permission.adminNode", adminNode);
		plugin.getConfig().set("Permission.modNode", modNode);
		plugin.getConfig().set("Permission.watchGroup", watchGroup);
		plugin.getConfig().set("ChatThrottle.throttleGroups", throttleGroups);
		plugin.getConfig().set("ChatThrottle.throttleTime", throttleTime);
		plugin.getConfig().set("ChatThrottle.maxSpamMessages", maxSpamMessages);
		plugin.getConfig().set("FileLogging.logCommands", logCommands);
		plugin.getConfig().set("watchList", watchList);
		plugin.getConfig().set("jail.jailed", jailed);
		plugin.getConfig().set("jail.jailLocation", C.locationToString(jailLocation));
		plugin.getLogger().info("Settings saved to the configurtaion file.");
		plugin.saveConfig();
	}

	public boolean isJailed(String name)
	{
		return jailed.contains(name);
	}

}