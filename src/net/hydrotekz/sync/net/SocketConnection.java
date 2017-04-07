package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.indexing.NetworkIndexer;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;

public class SocketConnection implements Runnable {

	private Socket socket;
	private Address address;

	public static HashMap<Address, DataInputStream> streams = new HashMap<Address, DataInputStream>();

	public SocketConnection(Socket socket, Address address) {
		this.socket = socket;
		this.address = address;
	}

	public void run () {
		// Listen for messages and handle them
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());

			String text;
			while((text = in.readUTF()) != null) {
				try {
					Printer.log("Received: " + text);

					JSONObject msg = JsonHandler.getJson(text);
					String cmd = (String) msg.get("cmd");
					String syncName = (String) msg.get("sync");
					SyncBox syncBox = SyncBox.getSyncBox(syncName);

					if (cmd.equals("upload_file")){
						String syncPath = (String) msg.get("path");
						FileUpload.uploadFile(syncBox, syncBox.getSocketHostAddress(socket), SyncFile.toSyncFile(syncBox, syncPath));

					} else if (cmd.equals("check_element")){
						String syncPath = (String) msg.get("path");
						String status = (String) msg.get("status");
						long lastModified = (long) msg.get("lastmodified");
						String hash = (String) msg.get("hash");
						NetworkIndexer.incomingCheck(syncBox, socket, SyncFile.toSyncFile(syncBox, syncPath), status, lastModified, hash);

//					} else if (cmd.equals("create_folder")){
//						String syncPath = (String) msg.get("path");
//						SyncFile syncFile = SyncFile.toSyncFile(syncBox, syncPath);
//						if (!syncFile.fileExist()) syncFile.getFile().mkdirs();

					} else if (cmd.equals("delete_element")){
						String syncPath = (String) msg.get("path");
						long lastModified = (long) msg.get("lastmodified");
						SyncFile syncFile = SyncFile.toSyncFile(syncBox, syncPath);
						if (syncFile.fileExist()) syncFile.remove(lastModified);
					}

				} catch (Exception e){
					Printer.log(e);
					Printer.log("Something bad happened!");
				}
			}

		} catch (SocketException e) {
			Printer.log("Peer " + address.toString() + " disconnected!");

		} catch (Exception e) {
			Printer.log(e);
			Printer.log("Lost connection to " + address.toString() + "!");

		} finally {
			try {
				if (!socket.isClosed()) socket.close();

			} catch (Exception e) {
				Printer.log("Failed to close socket connection!");
			}
		}
	}
}