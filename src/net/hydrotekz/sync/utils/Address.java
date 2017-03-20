package net.hydrotekz.sync.utils;

public class Address {

	private String ip;
	private int port;

	public Address(String ip, int port){
		this.ip = ip;
		this.port = port;
	}

	public static Address toAddress(String address){
		if (!address.contains(":")) address += ":1093";
		String[] split = address.split(":");
		Address tracker = new Address(split[0], Integer.parseInt(split[1]));
		return tracker;
	}

	public String getIp(){
		return ip;
	}

	public int getPort(){
		return port;
	}
}