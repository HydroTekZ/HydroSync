package net.hydrotekz.sync.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import net.hydrotekz.sync.utils.SyncBox;

public class FileUpload {

	public static void uploadFile(SyncBox syncBox, Socket socket, File file) throws Exception {
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

		String syncPath = syncBox.getSyncPath(file);
		dos.writeUTF(""); // TODO

		FileInputStream fis = new FileInputStream(file);
		int res = IOUtils.copy(fis, dos);

		fis.close();
	}
}