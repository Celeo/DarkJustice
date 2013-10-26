package com.darktidegames.celeo;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Celeo
 */
public class DatabaseManager
{

	private final DarkJustice plugin;
	public String databaseFileName = "DarkJustice_Database.db";
	public File databaseFile = null;
	public Connection connection = null;

	public DatabaseManager(DarkJustice plugin)
	{
		this.plugin = plugin;
		try
		{
			init();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public DarkJustice getPlugin()
	{
		return plugin;
	}

	/**
	 * Set up the database
	 */
	public DatabaseManager init() throws ClassNotFoundException, SQLException, IOException
	{
		databaseFile = new File(plugin.getDataFolder(), databaseFileName);
		if (!databaseFile.exists())
			databaseFile.createNewFile();
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		Statement stat = connection.createStatement();
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS `players` (name VARCHAR(25), lastseen VARCHAR(25), ip VARCHAR(25))");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS `commands` (sender VARCHAR(25), time VARCHAR(25), command VARCHAR(50), target VARCHAR(25), reason VARCHAR(50))");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS `warnings` (staff VARCHAR(25), player VARCHAR(25), reason VARCHAR(50))");
		stat.close();
		return this;
	}

	public void stop()
	{
		try
		{
			connection.close();
		}
		catch (Exception e)
		{}
	}

	/**
	 * Log the command sent by a player.
	 */
	public void logCommand(Map<String, String> params)
	{
		String sender = (params.containsKey("sender") ? params.get("sender") : "null");
		String time = (params.containsKey("time") ? params.get("time") : "0");
		String command = (params.containsKey("command") ? params.get("command").replace("'", "''") : "unknown");
		String target = (params.containsKey("target") ? params.get("target") : "self");
		String reason = (params.containsKey("reason") ? params.get("reason").replace("'", "''") : "no reason");
		try
		{
			Statement stat = connection.createStatement();
			stat.executeUpdate(String.format("Insert into `commands` values ('%s', '%s', '%s' ,'%s', '%s')", sender, time, command, target, reason));
			stat.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Log a warning against a player
	 * 
	 * @param staff
	 *            String
	 * @param player
	 *            String
	 * @param message
	 *            String
	 */
	public void logWarning(String staff, String player, String message)
	{
		String time = String.valueOf(System.currentTimeMillis());
		try
		{
			Statement stat = connection.createStatement();
			stat.executeUpdate(String.format("Insert into `warnings` values ('%s', '%s', '%s')", staff, player, message
					+ "<br>" + time));
			stat.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Updates a player's information in the database.
	 * 
	 * @param name
	 *            String
	 * @param ip
	 *            String
	 * @param time
	 *            String
	 */
	public void updatePlayer(String name, String ip, String time)
	{
		try
		{
			Statement stat = connection.createStatement();
			try
			{
				int result = stat.executeUpdate(String.format("Update `players` set `lastseen`='%s', `ip`='%s' where `name`='%s' and `ip`='%s'", time, ip, name, ip));
				if (result == 0)
				{
					stat.close();
					stat = connection.createStatement();
					stat.executeUpdate(String.format("Insert into `players` values ('%s', '%s', '%s')", name, time, ip));
				}
			}
			catch (SQLException e)
			{
				stat.close();
				stat = connection.createStatement();
				stat.executeUpdate(String.format("Insert into `players` values ('%s', '%s', '%s')", name, time, ip));
			}
			finally
			{
				stat.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param name
	 *            String
	 * @return Returns a List of String objects for all the previous IP
	 *         addressed logged with that name
	 */
	public List<String> getPreviousIPs(String name)
	{
		List<String> ret = new ArrayList<String>();
		try
		{
			Statement stat = connection.createStatement();
			ResultSet rs = stat.executeQuery(String.format("Select * from `players` where `name`='%s'", name));
			while (rs.next())
				ret.add(rs.getString("ip"));
			stat.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
}