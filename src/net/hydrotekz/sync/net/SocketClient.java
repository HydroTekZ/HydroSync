package net.hydrotekz.sync.net;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.hydrotekz.sync.utils.Printer;

public class SocketClient {

	public static Socket socketClient;
	public static String ip = null;
	public static int port = 22222;

	private static long recon = 0;

	public static void connect(){
		try {
			socketClient = new Socket();
			socketClient.connect(new InetSocketAddress(ip, port), 30000);
			SocketConnection connection = new SocketConnection(socketClient);
			Thread t = new Thread(connection);
			t.start();
			sendMessage("", true);
			Printer.log("Successfully connected to socket server!");

		} catch (Exception e) {
			Printer.log("Failed to connect to socket server!");
		}
	}

	public static void reconnect(){
		try {
			if (recon == 0 || System.currentTimeMillis() > recon){
				socketClient = new Socket();
				socketClient.connect(new InetSocketAddress(ip, port), 10000);
				SocketConnection connection = new SocketConnection(socketClient);
				Thread t = new Thread(connection);
				t.start();
				sendMessage("", true);
				Printer.log("Successfully reconnected to socket server!");
				recon = System.currentTimeMillis()+30000;
			}

		} catch (Exception e) {
			Printer.log("Failed to reconnect to socket server!");
		}
	}

	public static boolean isConnection(){
		if (socketClient != null && !socketClient.isClosed() && socketClient.isConnected()){
			return true;
		} else return false;
	}

	public static boolean sendMessage(String msg, boolean print){
		try {
			if (isConnection()){
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