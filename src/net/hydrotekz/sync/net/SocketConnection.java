package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.net.Socket;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

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
			while((text = in.readUTF()) != null) {
				Printer.log("Received: " + text);

				JSONObject msg = JsonHandler.getJson(text);
				String syncName = (String) msg.get("sync");
				String cmd = (String) msg.get("cmd");
				SyncBox syncBox = SyncBox.getSyncBox(syncName);

				if (cmd.equals("send_file")){
					String syncPath = (String) msg.get("path");
					FileDownload.downloadFile(syncBox, socket, syncPath);
				}
			}

		} catch (Exception e) {
			Printer.log("Lost connection " + address.toString() + "!");

		} finally {
			try {
				if (!socket.isClosed()) socket.close();

			} catch (Exception e) {
				Printer.log("Failed to close socket connection!");
			}
		}
	}
}