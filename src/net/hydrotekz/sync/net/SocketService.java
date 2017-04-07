package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.net.*;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.HydroSync;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;

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
					String cmd = (String) auth.get("cmd");

					if (cmd.equals("auth")){
						SocketConnection connection = new SocketConnection(socket, address);
						Thread t = new Thread(connection);
						t.start();

						HydroSync.connections.put(address, socket);
						Printer.log("Connected to " + address.toString() + "!");

					} else if (cmd.equals("download_file")){
						String path = (String) auth.get("path");
						long lastModified = (long) auth.get("lastmodified");
						String syncName = (String) auth.get("sync");
						long size = (long) auth.get("size");
						String hash = (String) auth.get("hash");
						SyncBox syncBox = SyncBox.getSyncBox(syncName);

						FileDownload.downloadFile(syncBox, socket, SyncFile.toSyncFile(syncBox, path), size, lastModified, hash);
					}

				} catch (Exception e){
					Printer.log(e);
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