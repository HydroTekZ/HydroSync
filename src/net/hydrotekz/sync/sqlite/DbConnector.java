package net.hydrotekz.sync.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;

import net.hydrotekz.sync.utils.Printer;

public class DbConnector {

	public static BasicDataSource loadDataSource(String path){
		BasicDataSource dataSource = null;
		try {
			//			Class.forName("org.sqlite.JDBC");

			dataSource = new BasicDataSource();
			dataSource.setDriverClassName("org.sqlite.JDBC");
			dataSource.setUrl("jdbc:sqlite:" + path);
			System.out.println("Data source launched.");

		} catch ( Exception e ) {
			Printer.log(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return dataSource;
	}

	public static void createTables(Connection c){
		Statement stmt = null;
		try {
			stmt = c.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS elements (" +
					"path text not null, " + 
					"filesize signed bigint, " + 
					"status text not null, " + // Synced or unsynced
					"lastmodified signed bigint not null, " +
					"filehash text )");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS action (" +
					"path text not null, " +
					"command int not null, " + // Command >>> delete
					"time signed bigint not null )");
			stmt.close();
			c.close();
			System.out.println("Tables handled successfully.");

		} catch ( Exception e ) {
			Printer.log(e.getClass().getName() + ": " + e.getMessage());
			Printer.log(e);
			System.exit(0);
		}
	}

	public static long getLong(String output, PreparedStatement ps){
		try {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				long result = rs.getLong(output);
				ps.close();
				return result;
			}
			ps.close();
		} catch (Exception e) {
			Printer.log(e);
		}
		return 0;
	}

	public static String getString(String output, PreparedStatement ps) throws Exception {
		try {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String result = rs.getString(output);
				ps.close();
				return result;
			}
			ps.close();
		} catch (Exception e) {
			Printer.log(e);
		}
		return null;
	}
}