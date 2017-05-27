package net.hydrotekz.sync.net;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.MainCore;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class SocketHandler {

	public static void establishConnections(SyncBox syncBox){
		// Connect to peers
		Printer.printInfo("Connecting to peers...");
		List<Address> peers = syncBox.getPeers();
		for (Address peer : peers){
			SocketClient client = new SocketClient(peer);
			if (client.connect()){
				Socket socket = client.getSocket();

				// Authenticate
				JsonHandler.sendAuth(socket);
			}
		}

		// Start socket listener
		Runnable r = new Runnable() {
			public void run() {
				// Start socket listener
				SocketService.startService();
			}
		};
		new Thread(r).start();
	}

	public static void reconnect(Address address){
		Printer.printInfo("Connecting to " + address.toString() + "...");
		SocketClient socketClient = new SocketClient(address);
		if (socketClient.reconnect()){
			Socket socket = socketClient.getSocket();
			MainCore.sockets.put(address.toString(), socket);

			// Authenticate
			JsonHandler.sendAuth(socket);

		} else {
			Printer.printError("Failed to reconnect to " + address.toString() + "!");
		}
	}

	public static boolean isConnected(Socket socket){
		if (socket != null && !socket.isClosed() && socket.isConnected()){
			return true;
		} else return false;
	}

	public static boolean sendMessage(Socket socket, JSONObject msg){
		try {
			if (isConnected(socket)){
				DataOutputStream os = new DataOutputStream(socket.getOutputStream());
				if (os != null && !socket.isOutputShutdown() && !socket.isInputShutdown()){
					String utf = msg.toJSONString();
					Printer.logMessage("Sent: " + utf);
					os.writeUTF(utf);
					return true;
				}
			}
		} catch (Exception e){
			return false;
		}
		return false;
	}
}