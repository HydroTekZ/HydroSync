package net.hydrotekz.sync.net;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class SocketClient {
	
	private SyncBox syncBox;
	private String ip = null;
	private int port = 22222;
	
	public SocketClient(SyncBox syncBox, Address peer){
		this.syncBox = syncBox;
		this.ip = peer.getIp();
		this.port = peer.getPort();
	}

	private static Socket socketClient;
	private static long recon = 0;

	public void connect(){
		try {
			socketClient = new Socket();
			socketClient.connect(new InetSocketAddress(ip, port), 30000);
			SocketConnection connection = new SocketConnection(socketClient);
			Thread t = new Thread(connection);
			t.start();
			Printer.log("Successfully connected to socket server!");

		} catch (Exception e) {
			Printer.log("Failed to connect to socket server!");
		}
	}

	public void reconnect(){
		try {
			if (recon == 0 || System.currentTimeMillis() > recon){
				socketClient = new Socket();
				socketClient.connect(new InetSocketAddress(ip, port), 10000);
				SocketConnection connection = new SocketConnection(socketClient);
				Thread t = new Thread(connection);
				t.start();
				Printer.log("Successfully reconnected to socket server!");
				recon = System.currentTimeMillis()+30000;
			}

		} catch (Exception e) {
			Printer.log("Failed to reconnect to socket server!");
		}
	}

	public boolean isConnected(){
		if (socketClient != null && !socketClient.isClosed() && socketClient.isConnected()){
			return true;
		} else return false;
	}

	public boolean sendMessage(String msg, boolean print){
		try {
			if (isConnected()){
				DataOutputStream os = new DataOutputStream(socketClient.getOutputStream());
				if (os != null && !socketClient.isInputShutdown()){
					
					return true;

				} else {
					Printer.log("Failed to send socket to message: " + msg);
				}
			} else {

			}
		} catch (Exception e){
			Printer.log("Failed to send socket to message: " + msg);
			reconnect();
		}
		return false;
	}
}