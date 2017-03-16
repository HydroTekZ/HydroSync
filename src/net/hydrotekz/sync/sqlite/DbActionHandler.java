package net.hydrotekz.sync.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DbActionHandler {

	public static void addFile(String path, String command, long time, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("INSERT INTO `action` (path, command, time) VALUES "
				+ "(?, ?, ?)");
		ps.setString(1, path);
		ps.setString(2, command);
		ps.setLong(3, time);
		ps.execute();
		ps.close();
	}

	public static void removeCommand(String path, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("DELETE FROM `index` WHERE path=?");
		ps.setString(1, path);
		ps.execute();
		ps.close();
	}
}