package net.hydrotekz.sync.utils;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

public class SyncBox {

	private String name;
	private File folder;
	private BasicDataSource dataSource;
	private Connection sql;
	private List<Address> peers;

	public SyncBox (String name, File folder, BasicDataSource dataSource, Connection sql, List<Address> peers){
		this.name = name;
		this.folder = folder;
		this.dataSource = dataSource;
		this.sql = sql;
		this.peers = peers;
	}

	public String getName(){
		return name;
	}

	public File getFolder(){
		return folder;
	}

	public BasicDataSource getDataSource() {
		return dataSource;
	}

	public Connection getSqlConn() {
		return sql;
	}
	
	public void setSqlConn(Connection to) {
		sql = to;
	}

	public List<Address> getPeers(){
		return peers;
	}

	public String getSyncPath(File file){
		String localPath = file.getAbsolutePath();
		String syncRoot = folder.getAbsolutePath();
		String path = localPath.replace(syncRoot + File.separator, "");
		return path;
	}

}