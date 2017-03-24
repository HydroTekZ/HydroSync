package net.hydrotekz.sync.net;

import java.net.Socket;

import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.Printer;

public class SocketClient {

	private Address address;

	public SocketClient(Address peer){
		this.address = peer;
	}

	private static Socket socket;
	private static long recon = 0;

	// Creates a socket connection
	public boolean connect(){
		try {
			socket = new Socket();
			socket.connect(address.toInetSocketAddress(), 10000);
			SocketConnection connection = new SocketConnection(socket, address);
			Thread t = new Thread(connection);
			t.start();
			Printer.log("Connected to " + address.toString() + "!");
			return true;

		} catch (Exception e) {
			Printer.log("Peer " + address.toString() + " is unreachable.");
		}
		return false;
	}

	// Recreates a socket connection
	public boolean reconnect(){
		try {
			if (recon == 0 || System.currentTimeMillis() > recon){
				socket = new Socket();
				socket.connect(address.toInetSocketAddress(), 10000);
				SocketConnection connection = new SocketConnection(socket, address);
				Thread t = new Thread(connection);
				t.start();
				Printer.log("Successfully reconnected to " + address.toString() + "!");
				recon = System.currentTimeMillis()+30000;
				return true;
			}

		} catch (Exception e) {
			Printer.log("Failed to reconnect to " + address.toString() + "!");
		}
		return false;
	}

	// Return current socket
	public Socket getSocket(){
		return socket;
	}
}