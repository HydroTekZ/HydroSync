package net.hydrotekz.sync.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DbIndexHandler {

	/*
	 * Registers file
	 */

	public static void addFile(String path, long fileSize, String status, long lastModified, String fileHash, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("INSERT INTO `elements` (path, filesize, status, lastmodified, filehash) VALUES "
				+ "(?, ?, ?, ?, ?)");
		ps.setString(1, path);
		ps.setLong(2, fileSize);
		ps.setString(3, status);
		ps.setLong(4, lastModified);
		ps.setString(5, fileHash);
		ps.execute();
		ps.close();
	}

	/*
	 * Changes values
	 */

	public static void updateFileSize(String path, long fileSize, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("UPDATE `elements` SET filesize=? "
				+ "WHERE path=?");
		ps.setLong(1, fileSize);
		ps.setString(2, path);
		ps.execute();
		ps.close();
	}

	public static void updateStatus(String path, String status, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("UPDATE `elements` SET status=? "
				+ "WHERE path=?");
		ps.setString(1, status);
		ps.setString(2, path);
		ps.execute();
		ps.close();
	}

	public static void updateLastModified(String path, long fileSize, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("UPDATE `elements` SET filesize=? "
				+ "WHERE path=?");
		ps.setLong(1, fileSize);
		ps.setString(2, path);
		ps.execute();
		ps.close();
	}

	public static void updateFileHash(String path, String fileHash, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("UPDATE `elements` SET status=? "
				+ "WHERE path=?");
		ps.setString(1, fileHash);
		ps.setString(2, path);
		ps.execute();
		ps.close();
	}

	/*
	 * Check if exists
	 */

	public static boolean doesExist(String path, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `path` FROM `elements` WHERE path=?");
		ps.setString(1, path);
		String result = DbConnector.getString("path", ps);
		if (result == null) return false; else return true;
	}

	/*
	 * Get information
	 */

	public static String getStatus(String path, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `status` FROM `elements` WHERE path=?");
		ps.setString(1, path);
		String result = DbConnector.getString("status", ps);
		return result;
	}

	public static long getFileSize(String path, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `filesize` FROM `elements` WHERE path=?");
		ps.setString(1, path);
		long result = DbConnector.getLong("filesize", ps);
		return result;
	}

	public static long getLastModified(String path, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `lastmodified` FROM `elements` WHERE path=?");
		ps.setString(1, path);
		long result = DbConnector.getLong("lastmodified", ps);
		return result;
	}

	public static String getFileHash(String path, Connection c) throws Exception {
		PreparedStatement ps = c.prepareStatement("SELECT `filehash` FROM `elements` WHERE path=?");
		ps.setString(1, path);
		String result = DbConnector.getString("filehash", ps);
		return result;
	}
}