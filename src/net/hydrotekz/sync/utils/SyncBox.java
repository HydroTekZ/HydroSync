package net.hydrotekz.sync.utils;

import java.io.File;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.dbcp2.BasicDataSource;

import net.hydrotekz.sync.MainCore;

public class SyncBox {

	private String name;
	private File folder;
	private BasicDataSource dataSource;
	private Connection sql;
	private List<Address> peers;
	private String key;

	public SyncBox (String name, File folder, BasicDataSource dataSource, Connection sql, List<Address> peers, String key){
		this.name = name;
		this.folder = folder;
		this.dataSource = dataSource;
		this.sql = sql;
		this.peers = peers;
		this.key = key;
	}

	public SyncBox refresh(){
		return MainCore.syncBoxes.get(name);
	}

	public String getName(){
		return name;
	}

	public String getKey(){
		return key;
	}

	/*
	 * Database related
	 */

	public BasicDataSource getDataSource() {
		return dataSource;
	}

	public Connection getSqlConn() throws Exception {
		if (sql.isClosed()){
			sql = dataSource.getConnection();
			MainCore.syncBoxes.put(name, this);
		}
		return sql;
	}

	/*
	 * Socket related
	 */

	public List<Address> getPeers(){
		return peers;
	}

	public void removePeer(Address address){
		peers.remove(address);
		MainCore.syncBoxes.put(name, this);
	}

	public void addPeer(Address address){
		peers.add(address);
		MainCore.syncBoxes.put(name, this);
	}

	public List<Socket> getSockets(){
		List<Socket> sockets = new ArrayList<Socket>();
		for (Entry<String, Socket> e : MainCore.sockets.entrySet()){
			for (Address peer : peers){
				if (peer.toString().equals(e.getKey())){
					sockets.add(e.getValue());
				}
			}
		}
		return sockets;
	}

	public Address getSocketHostAddress(Socket socket){
		Address address = Address.toAddress(socket);
		for (Entry<String, Socket> e : MainCore.sockets.entrySet()){
			Address socketAddress = Address.toAddress(e.getValue());
			if (socketAddress.toString().equals(address.toString())){
				return Address.toAddress(e.getKey());
			}
		}
		return null;
	}

	/*
	 * File related
	 */

	public File getFolder(){
		return folder;
	}

	public File getIntelBox(){
		File file = new File(getFolder() + File.separator + ".box");
		return file;
	}

	public File getCacheBox(){
		File file = new File(getIntelBox() + File.separator + "cache");
		if (!file.exists()) file.mkdir();
		return file;
	}

	public File getBinBox(){
		File file = new File(getIntelBox() + File.separator + "bin");
		if (!file.exists()) file.mkdir();
		return file;
	}

	public File getTrashBox(){
		File file = new File(getIntelBox() + File.separator + "trash");
		if (!file.exists()) file.mkdir();
		return file;
	}

	/*
	 * Static
	 */

	public static SyncBox getSyncBox(String name){
		return MainCore.syncBoxes.get(name);
	}
}