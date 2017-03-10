package net.hydrotekz.sync.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import net.hydrotekz.sync.utils.Printer;

public class DbConnector {

	public static Connection createConnection(String path){
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + path);

		} catch ( Exception e ) {
			Printer.log(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully.");
		return c;
	}

	public static void createIndexTable(Connection c){
		Statement stmt = null;
		try {
			stmt = c.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS files (" +
					"path text not null, " + 
					"filesize int not null, " + 
					"status text not null, " + 
					"lastmodified long not null, " +
					"filehash text )";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();

		} catch ( Exception e ) {
			Printer.log(e.getClass().getName() + ": " + e.getMessage());
			Printer.log(e);
			System.exit(0);
		}
		System.out.println("Index table created successfully.");
	}
}