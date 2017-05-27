package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.io.EOFException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.MainCore;
import net.hydrotekz.sync.indexing.NetworkIndexer;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

public class SocketConnection implements Runnable {

	private Socket socket;
	private Address address;

	public SocketConnection(Socket socket, Address address) {
		this.socket = socket;
		this.address = address;
	}

	private long task_id;
	private boolean isHost;
	private long lastHeartbeat;

	@SuppressWarnings("unchecked")
	private void heartbeat() throws Exception {
		// Find problems
		try {
			Thread.sleep(25000);
			if (!isTaskValid()) return;

			// Send heartbeat
			JSONObject json = new JSONObject();
			json.put("cmd", "keep_alive");
			SocketHandler.sendMessage(socket, json);

		} catch (Exception e){
			Printer.logError(e);
			Printer.printError("Failed to send heartbeat to " + address.toString() + "!");
		}

		// Continue loop
		heartbeat();
	}

	public void run () {
		// Variables
		task_id = (System.currentTimeMillis()/1000)-Utils.getRandomNumber(1, 99999);
		lastHeartbeat = System.currentTimeMillis()+10000;
		isHost = socket.getLocalPort() == SocketService.port;

		// Register task
		if (MainCore.tasks.containsKey(address.toString())){
			MainCore.tasks.remove(address.toString());
		}
		MainCore.tasks.remove(address.toString());
		MainCore.tasks.put(address.toString(), task_id);
		MainCore.sockets.put(address.toString(), socket);

		// Listen for messages and handle them
		try {
			// Setup
			socket.setKeepAlive(true);

			DataInputStream in = new DataInputStream(socket.getInputStream());

			// Start heartbeat thread
			Runnable r = new Runnable() {
				public void run() {
					try {
						heartbeat();
					} catch (Exception e) {
						Printer.printError(e);
					}
				}
			};
			new Thread(r).start();

			// Wait for data transmission
			while(isTaskValid()) {
				try {
					String text = in.readUTF();
					Printer.logMessage("Received: " + text);

					JSONObject msg = JsonHandler.getJson(text);
					String cmd = (String) msg.get("cmd");

					// Register heartbeat
					lastHeartbeat = System.currentTimeMillis();
					if (cmd.equals("keep_alive")){
						continue;
					}

					String syncName = (String) msg.get("sync");
					SyncBox syncBox = SyncBox.getSyncBox(syncName);

					// Handle commands
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
						Printer.printInfo("Delete command of " + syncFile.getFileName() + " received.");
					}

				} catch (EOFException|SocketTimeoutException|SocketException e) {
					// Check timeout
					if (e instanceof SocketTimeoutException){
						long timeout = (System.currentTimeMillis()-lastHeartbeat)/1000;
						if (timeout < 60*5*1000){
							Printer.printWarning("Connection to " + address.toString() + " have problems.");
							Thread.sleep(10000);
							continue;
						}
					}

					Printer.logError(e);
					Printer.printError("Lost connection to " + address.toString() + "!");

					// Wait a bit
					if (!isHost) Thread.sleep(45000);
					else Thread.sleep(Utils.getRandomNumber(90, 200)*1000);
					if (!isTaskValid()) return;

					// Close connection
					endSocket();

					// Reconnect
					SocketHandler.reconnect(address);
					return;
				}
			}

			// Debug
			if (!isTaskValid()){
				Printer.printDebug("Old connection to " + address.toString() + " was terminated.");
			}

		} catch (Exception e) {
			Printer.printError(e);
			Printer.printError("Connection to " + address.toString() + " was broken!");

		} finally {
			// Close connection
			endSocket();
		}
	}

	private boolean isTaskValid(){
		return MainCore.tasks.containsKey(address.toString()) && MainCore.tasks.get(address.toString()) == task_id;
	}

	private void endSocket(){
		try {
			if (!socket.isClosed()) socket.close();
			if (isTaskValid()){
				MainCore.sockets.remove(address.toString());
				MainCore.tasks.remove(address.toString());
			}

		} catch (Exception e) {
			Printer.printError("Failed to close socket connection!");
		}
	}
}