package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.net.*;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.HydroSync;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
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

				Address address = Address.toAddress(socket);

				try {
					DataInputStream stream = new DataInputStream(socket.getInputStream());
					JSONObject auth = JsonHandler.getJson(stream.readUTF());
					address = Address.toAddress(socket, auth.get("port"));

					SocketConnection connection = new SocketConnection(socket, address);
					Thread t = new Thread(connection);
					t.start();

					HydroSync.connections.put(address, socket);
					Printer.log("Connected to " + address.toString() + "!");

				} catch (Exception e){
					Printer.log("Failed to authenticate " + address.toString() + "!");
					if (!socket.isClosed()) socket.close();
				}
			}

		} catch (Exception ioe) {
			Printer.log(ioe);
			Printer.log("Socket service failed!");
			System.exit(0);
		}
	}

	public static void stopService(){
		try {
			if (!listener.isClosed() && listener != null){
				listener.close();
			}

		} catch (Exception e){}	
	}
}