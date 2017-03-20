package net.hydrotekz.sync.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class StatusDatabase {

	/*
	 * Key			 |		Value		|	Time
	 * firstTimeScan - complete/pending - 	2017
	 */

	// Add entry
	public static void addEntry(String key, String value, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("INSERT INTO `status` (key, value, time) VALUES "
				+ "(?, ?, ?)");
		ps.setString(1, key);
		ps.setString(2, key);
		ps.setLong(3, System.currentTimeMillis());
		ps.execute();
		ps.close();
	}

	// Check key
	public static boolean doesExist(String key, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `key` FROM `status` WHERE key=?");
		ps.setString(1, key);
		String result = DbManager.getString("key", ps);
		if (result == null) return false; else return true;
	}

	// Get value
	public static String getValue(String key, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `value` FROM `status` WHERE key=?");
		ps.setString(1, key);
		String result = DbManager.getString("value", ps);
		return result;
	}

	// Update last time seen
	public static void updateValue(String key, String value, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("UPDATE `status` SET value=? "
				+ "WHERE key=?");
		ps.setString(1, value);
		ps.setString(2, key);
		ps.execute();
		ps.close();
	}
}