package net.hydrotekz.sync.utils;

public class Address {
	
	private String ip;
	private int port;

	public Address (String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp(){
		return ip;
	}
	
	public int port(){
		return port;
	}
}