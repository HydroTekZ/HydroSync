package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.net.Socket;

import net.hydrotekz.sync.utils.Printer;

public class SocketConnection implements Runnable {

	private Socket socket;

	SocketConnection(Socket socket) {
		this.socket = socket;
	}

	public void run () {
		// Listen for messages and handle them
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());

			String text;
			while((text = in.readUTF()) != null) {
				try {
					

				} catch (Exception e){
					Printer.log(e);
					Printer.log("Failed to handle input!");
				}
			}

		} catch (Exception e) {
			Printer.log("Lost connection to socket server!");

		} finally {
			try {
				if (!socket.isClosed()) socket.close();

			} catch (Exception e) {
				Printer.log("Failed to close socket connection!");
			}
		}
	}
}