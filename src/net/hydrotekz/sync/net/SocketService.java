package net.hydrotekz.sync.net;

import java.io.*;
import java.net.*;

import net.hydrotekz.sync.utils.Printer;

public class SocketService {

	public static int port = 1093;

	private static ServerSocket listener;

	// Listen for incoming connections and handle them
	public static void startService() {
		try {
			listener = new ServerSocket(port);

			Printer.log("Listening for connections...");
			while(!listener.isClosed()){
				Socket socket = listener.accept();
				SocketConnection connection = new SocketConnection(socket);
				Thread t = new Thread(connection);
				t.start();
				Printer.log(socket.getRemoteSocketAddress().toString() + " connected!");
			}

		} catch (IOException ioe) {
			Printer.log("Failed to start socket service!");
		}
	}

	public static void stopService(){
		try {
			if (!listener.isClosed() && listener != null){
				listener.close();
				Printer.log("Socket server was successfully closed!");
			}

		} catch (Exception e){
			Printer.log("Failed to stop socket service!");
		}	
	}
}