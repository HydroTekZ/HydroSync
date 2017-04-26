package net.hydrotekz.sync.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import net.hydrotekz.sync.sqlite.IndexDatabase;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

@SuppressWarnings("unchecked")
public class FileUpload {

	public static List<String> inProgress = new ArrayList<String>();

	public static void uploadFile(SyncBox syncBox, Address address, SyncFile syncFile) throws Exception {
		try {
			File file = syncFile.getFile();
			if (!file.exists()){
				Printer.log("File \"" + syncFile.getFileName() + "\" does not exist!");
				return;
			}
			String syncPath = syncFile.getSyncPath();
			if (inProgress.contains(syncPath)) return;
			inProgress.add(syncPath);

			Printer.log("Uploading " + file.getName() + "...");

			// Check for changes
			Connection c = syncBox.getSqlConn();
			long lastModified = IndexDatabase.getLastModified(syncPath, c);

			if (lastModified != Utils.getLastModified(file)){
				Printer.log("Upload aborted, changes detected!");
				return;
			}

			// Open connection
			Socket socket = new Socket();
			socket.connect(address.toInetSocketAddress(), 10000);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

			// Send message
			JSONObject json = JsonHandler.prepJson(syncBox);
			json.put("cmd", "download_file");
			json.put("lastmodified", lastModified);
			json.put("hash", IndexDatabase.getFileHash(syncPath, c));
			json.put("path", syncPath);
			json.put("size", file.length());
			SocketHandler.sendMessage(socket, json);

			// Transfer file
			FileInputStream fis = new FileInputStream(file);
			IOUtils.copyLarge(fis, dos);

			// Finish
			dos.flush();
			fis.close();
			dos.close();
			socket.close();

			Thread.sleep(100);
			Printer.log("Successfully uploaded " + file.getName() + "!");

		} catch (Exception ex){
			Printer.log("Failed to upload!");

		} finally {
			inProgress.remove(syncFile.getSyncPath());
		}
	}
}