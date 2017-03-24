package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import net.hydrotekz.sync.utils.SyncBox;

public class FileDownload {

	public static void downloadFile(SyncBox syncBox, Socket socket, String syncPath) throws Exception {
		File file = syncBox.getFileInSync(syncPath);
		
		DataInputStream dis = new DataInputStream(socket.getInputStream());

		FileOutputStream fos = new FileOutputStream(file);
		int res = IOUtils.copy(dis, fos);

		fos.close();
	}
}