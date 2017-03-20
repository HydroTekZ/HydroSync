package net.hydrotekz.sync.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class PeersDatabase {

	/*
	 * Address				|	LastSeen
	 * 192.168.1.222:1093	-	  2017
	 */

	// Add peer
	public static void addPeer(String address, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("INSERT INTO `peers` (address, lastseen) VALUES "
				+ "(?, ?)");
		ps.setString(1, address);
		ps.setLong(2, System.currentTimeMillis());
		ps.execute();
		ps.close();
	}

	// Get peers
	public static List<String> getPeers(Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `address` FROM `peers`");
		List<String> result = DbManager.getStringList("lastseen", ps);
		return result;
	}

	// Get last time seen
	public static long getLastSeen(String address, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `lastseen` FROM `peers` WHERE address=?");
		ps.setString(1, address);
		long result = DbManager.getLong("lastseen", ps);
		return result;
	}

	// Update last time seen
	public static void updateLastSeen(String address, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("UPDATE `peers` SET lastseen=? "
				+ "WHERE address=?");
		ps.setLong(1, System.currentTimeMillis());
		ps.setString(2, address);
		ps.execute();
		ps.close();
	}

	// Delete peer
	public static void removePeer(String address, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("REMOVE FROM `peers` WHERE address=?");
		ps.setString(1, address);
		ps.execute();
		ps.close();
	}
}