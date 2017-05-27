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

	// Creates a socket connection
	public boolean connect(){
		try {
			socket = new Socket();
			socket.connect(address.toInetSocketAddress(), 10000);
			SocketConnection connection = new SocketConnection(socket, address);
			Thread t = new Thread(connection);
			t.start();
			Printer.printInfo("Connected to " + address.toString() + "!");
			return true;

		} catch (Exception e) {
			Printer.printInfo("Peer " + address.toString() + " is unreachable.");
		}
		return false;
	}

	// Recreates a socket connection
	public boolean reconnect(){
		try {
			socket = new Socket();
			socket.connect(address.toInetSocketAddress(), 10000);
			SocketConnection connection = new SocketConnection(socket, address);
			Thread t = new Thread(connection);
			t.start();
			Printer.printInfo("Successfully reconnected to " + address.toString() + "!");
			return true;

		} catch (Exception e) {
			Printer.printError("Failed to reconnect to " + address.toString() + ".");
		}
		return false;
	}

	// Return current socket
	public Socket getSocket(){
		return socket;
	}
}