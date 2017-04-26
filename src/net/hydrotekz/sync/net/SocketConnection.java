package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.HydroSync;
import net.hydrotekz.sync.indexing.NetworkIndexer;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;

public class SocketConnection implements Runnable {

	private Socket socket;
	private Address address;

	public SocketConnection(Socket socket, Address address) {
		this.socket = socket;
		this.address = address;
	}

	public void run () {
		// Listen for messages and handle them
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());

			String text;
			while(true) {
				try {
					text = in.readUTF();
					if (text != null){
						Printer.debug("Received: " + text);

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

						} else if (cmd.equals("delete_element")){
							String syncPath = (String) msg.get("path");
							long lastModified = (long) msg.get("lastmodified");
							SyncFile syncFile = SyncFile.toSyncFile(syncBox, syncPath);
							if (syncFile.fileExist()) syncFile.remove(lastModified);
							Printer.log("Delete command of " + syncFile.getFileName() + " received.");
						}
					}

				} catch (SocketTimeoutException e){
					Printer.debug(e);
					if (!SocketHandler.isConnected(socket)){
						Printer.log("Attempting to reconnect to " + address.toString() + "...");
						SocketClient socketClient = new SocketClient(address);
						if (socketClient.reconnect()){
							socket = socketClient.getSocket();
							HydroSync.connections.remove(address);
							HydroSync.connections.put(address, socket);
							in = new DataInputStream(socket.getInputStream());
						}

					} else {
						Printer.log("Unable to read from data stream, attempting to fix the stream.");
						in = new DataInputStream(socket.getInputStream());
					}
				}
			}

		} catch (SocketException e) {
			Printer.debug(e);
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