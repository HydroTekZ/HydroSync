package net.hydrotekz.sync.utils;

import java.io.File;
import java.sql.Connection;
import java.util.List;

public class SyncBox {
	
	private String name;
	private File folder;
	private Connection sql;
	private List<Address> peers;
	
	public SyncBox (String name, File folder, Connection sql, List<Address> peers){
		this.name = name;
		this.folder = folder;
		this.sql = sql;
		this.peers = peers;
	}
	
	public String getName(){
		return name;
	}
	
	public File getFolder(){
		return folder;
	}
	
	public Connection getSqlConn(){
		return sql;
	}
	
	public List<Address> getPeers(){
		return peers;
	}

}