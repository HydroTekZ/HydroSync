package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.net.*;

import org.json.simple.JSONObject;

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

			Printer.printInfo("Socket service started.");
			while(!listener.isClosed()){
				Socket socket = listener.accept();

				Address address = Address.toAddress(socket);

				try {
					DataInputStream stream = new DataInputStream(socket.getInputStream());
					String utf = stream.readUTF();
					JSONObject auth = JsonHandler.getJson(utf);
					address = Address.toAddress(socket, auth.get("port"));
					String cmd = (String) auth.get("cmd");

					if (cmd.equals("auth")){
						SocketConnection connection = new SocketConnection(socket, address);
						Thread t = new Thread(connection);
						t.start();
						Printer.printInfo("Connected to " + address.toString() + "!");

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
					Printer.logError(e);
					Printer.printError("Failed to authenticate " + address.toString() + "!");
					if (!socket.isClosed()) socket.close();
				}
			}

		} catch (Exception ioe) {
			Printer.printError(ioe);
			Printer.printError("Socket service failed!");
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