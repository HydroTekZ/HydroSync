package net.hydrotekz.sync.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import net.hydrotekz.sync.utils.Printer;

public class DbManager {

	public static BasicDataSource loadDataSource(String path){
		BasicDataSource dataSource = null;
		try {
			dataSource = new BasicDataSource();
			dataSource.setDriverClassName("org.sqlite.JDBC");
			dataSource.setUrl("jdbc:sqlite:" + path);
			Printer.printInfo("Data source launched.");

		} catch (Exception e) {
			Printer.printError(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return dataSource;
	}

	public static void createTables(Connection c){
		Statement stmt = null;
		try {
			stmt = c.createStatement();

			// Index database
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS elements (" +
					"path text not null, " + 
					"filesize signed bigint, " + 
					"status text not null, " + // Deleted or syncing
					"lastmodified signed bigint not null, " +
					"filehash text )");

			// Status database
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS status (" +
					"key text not null, " +
					"value int not null, " +
					"time signed bigint not null )");

			// TODO: Peers database
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS peers (" +
					"address text not null, " +
					"lastseen signed bigint not null )");

			stmt.close();
			c.close();
			Printer.printInfo("Tables handled successfully.");

		} catch (Exception e) {
			Printer.printError(e.getClass().getName() + ": " + e.getMessage());
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
			Printer.printError(e);
		}
		return 0;
	}

	public static String getString(String output, PreparedStatement ps) {
		try {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String result = rs.getString(output);
				ps.close();
				return result;
			}
			ps.close();
		} catch (Exception e) {
			Printer.printError(e);
		}
		return null;
	}

	public static List<String> getStringList(String output, PreparedStatement ps) {
		try {
			List<String> list = new ArrayList<String>();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getString(output));
			}
			ps.close();
			return list;

		} catch (Exception e) {
			Printer.printError(e);
		}
		return null;
	}
}