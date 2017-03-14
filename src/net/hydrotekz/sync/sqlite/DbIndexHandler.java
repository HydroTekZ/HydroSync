package net.hydrotekz.sync.sqlite;

import java.sql.Connection;

public class DbIndexHandler {
	
	/*
	 * Registers file
	 */

	public static void addFile(String path, long fileSize, String status, long lastModified, String fileHash, Connection c){
		DbConnector.executeCommand("INSERT INTO `elements` (path, filesize, status, lastmodified, filehash) VALUES "
				+ "('"+ path + "', '" + fileSize + "', '" + status + "', '" + lastModified + "', '" + fileHash + "')", c);
	}
	
	/*
	 * Changes values
	 */

	public static void updateFileSize(String path, long fileSize, Connection c){
		DbConnector.executeCommand("UPDATE `elements` SET filesize='" + fileSize + "' "
				+ "WHERE path='" + path + "';", c);
	}

	public static void updateStatus(String path, String status, Connection c){
		DbConnector.executeCommand("UPDATE `elements` SET status='" + status + "' "
				+ "WHERE path='" + path + "';", c);
	}

	public static void updateLastModified(String path, long fileSize, Connection c){
		DbConnector.executeCommand("UPDATE `elements` SET lastmodified='" + fileSize + "' "
				+ "WHERE path='" + path + "';", c);
	}

	public static void updateFileHash(String path, long fileSize, Connection c){
		DbConnector.executeCommand("UPDATE `elements` SET filehash='" + fileSize + "' "
				+ "WHERE path='" + path + "';", c);
	}
	
	/*
	 * Check if exists
	 */

	public static boolean doesExist(String path, Connection c){
		String output = "path";
		String command = "SELECT `"+output+"` FROM `elements` WHERE path='" + path + "'";
		String result = DbConnector.getString(output, command, c);
		if (result == null) return false; else return true;
	}
	
	/*
	 * Get information
	 */

	public static String getStatus(String path, Connection c){
		String output = "status";
		String command = "SELECT `"+output+"` FROM `elements` WHERE path='" + path + "'";
		String result = DbConnector.getString(output, command, c);
		return result;
	}

	public static long getFileSize(String path, Connection c){
		String output = "filesize";
		String command = "SELECT `"+output+"` FROM `elements` WHERE path='" + path + "'";
		long result = DbConnector.getLong(output, command, c);
		return result;
	}

	public static long getLastModified(String path, Connection c){
		String output = "lastmodified";
		String command = "SELECT `"+output+"` FROM `elements` WHERE path='" + path + "'";
		long result = DbConnector.getLong(output, command,c);
		return result;
	}

	public static String getFileHash(String path, Connection c){
		String output = "filehash";
		String command = "SELECT `"+output+"` FROM `elements` WHERE path='" + path + "'";
		String result = DbConnector.getString(output, command, c);
		return result;
	}
}