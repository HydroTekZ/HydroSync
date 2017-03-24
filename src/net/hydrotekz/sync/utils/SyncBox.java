package net.hydrotekz.sync.utils;

import java.io.File;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.dbcp2.BasicDataSource;

import net.hydrotekz.sync.HydroSync;

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
	
	public static SyncBox getSyncBox(String name){
		return HydroSync.syncBoxes.get(name);
	}

	public SyncBox refresh(){
		return HydroSync.syncBoxes.get(name);
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

	public Connection getSqlConn() throws Exception {
		if (sql.isClosed()){
			sql = dataSource.getConnection();
			HydroSync.syncBoxes.put(name, this);
		}
		return sql;
	}

	public List<Address> getPeers(){
		return peers;
	}

	public List<Socket> getSockets(){
		List<Socket> sockets = new ArrayList<Socket>();
		for (Entry<Address, Socket> e : HydroSync.connections.entrySet()){
			if (peers.contains(e.getKey())){
				sockets.add(e.getValue());
			}
		}
		return sockets;
	}

	public String getSyncPath(File file){
		String localPath = file.getAbsolutePath();
		String syncRoot = folder.getAbsolutePath();
		String path = localPath.replace(syncRoot + File.separator, "");
		if (file.isDirectory()) path += File.separator;
		return path;
	}
	
	public File getFileInSync(String syncPath){
		String path = folder.getAbsolutePath();
		if (syncPath.endsWith(File.separator) || syncPath.endsWith("\\") || syncPath.endsWith("/")){
			syncPath = syncPath.substring(0, syncPath.length() - 1);
		}
		path += File.separator + syncPath;
		File file = new File(path);
		return file;
	}
}