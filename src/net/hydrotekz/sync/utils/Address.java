package net.hydrotekz.sync.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Address {

	private String ip;
	private int port;

	public Address(String ip, int port){
		this.ip = ip;
		this.port = port;
	}

	public String getIp(){
		return ip;
	}

	public int getPort(){
		return port;
	}

	public InetSocketAddress toInetSocketAddress(){
		return new InetSocketAddress(ip, port);
	}

	public String toString(){
		return ip + ":" + port;
	}

	public boolean equals(SyncBox syncBox){
		return syncBox.toString().equals(toString());
	}

	/*
	 * Static
	 */

	public static Address toAddress(String address){
		if (!address.contains(":")) address += ":1093";
		String[] split = address.split(":");
		Address tracker = new Address(split[0], Integer.parseInt(split[1]));
		return tracker;
	}

	public static Address toAddress(String ip, Object obj){
		long value = (long) obj;
		int port = (int) value;
		Address tracker = new Address(ip, port);
		return tracker;
	}

	public static Address toAddress(Socket socket, Object obj){
		long value = (long) obj;
		int port = (int) value;
		String ip = socket.getInetAddress().toString().replace("/", "");
		Address address = new Address(ip, port);
		return address;
	}

	public static Address toAddress(Socket socket, int port){
		String ip = socket.getInetAddress().toString().replace("/", "");
		Address address = new Address(ip, port);
		return address;
	}

	public static Address toAddress(Socket socket){
		String ip = socket.getInetAddress().toString().replace("/", "");
		Address address = new Address(ip, socket.getPort());
		return address;
	}

	public static Address toAddress(InetAddress ip, int port){
		String txt = ip.toString().replace("/", "");
		Address address = new Address(txt, port);
		return address;
	}
}