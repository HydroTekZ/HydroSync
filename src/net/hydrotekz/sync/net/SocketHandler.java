package net.hydrotekz.sync.net;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.HydroSync;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class SocketHandler {

	public static void establishConnections(SyncBox syncBox){
		// Connect to peers
		Printer.log("Connecting to peers...");
		List<Address> activePeers = new ArrayList<Address>();
		List<Address> peers = syncBox.getPeers();
		for (Address peer : peers){
			SocketClient client = new SocketClient(peer);
			if (client.connect()){
				Socket socket = client.getSocket();
				HydroSync.connections.put(peer, socket);
				activePeers.add(peer);
				JsonHandler.sendAuth(syncBox, socket);
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

	public static void closeConnections(){
		// Shutdown task
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					SocketService.stopService();
					for (Socket peer : HydroSync.connections.values()){
						peer.close();
					}
					Printer.log("Socket connections was closed.");

				} catch (Exception e){
					Printer.log("Failed to close socket connections!");
				}
			}
		});
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
				if (os != null && !socket.isInputShutdown()){
					String utf = msg.toJSONString();
					os.writeUTF(utf);
					Thread.sleep(100);
					return true;
				}
			}
		} catch (Exception e){
			return false;
		}
		return false;
	}
}