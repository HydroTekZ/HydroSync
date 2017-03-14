package net.hydrotekz.sync.sqlite;

import java.sql.Connection;

public class DbActionHandler {

	public static void addFile(String path, String command, long time, Connection c){
		DbConnector.executeCommand("INSERT INTO `action` (path, command, time) VALUES "
				+ "('"+ path + "', '" + command + "', '" + time + "')", c);
	}

	public static void removeCommand(String path, long fileSize, Connection c){
		DbConnector.executeCommand("DELETE FROM `index` WHERE path='" + path + "';", c);
	}
}