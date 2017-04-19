package net.hydrotekz.sync.indexing;

import java.io.File;
import java.net.Socket;
import java.sql.Connection;
import java.util.List;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.net.FileUpload;
import net.hydrotekz.sync.net.SocketHandler;
import net.hydrotekz.sync.sqlite.IndexDatabase;
import net.hydrotekz.sync.utils.JsonHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

@SuppressWarnings("unchecked")
public class NetworkIndexer {

	// Check for external changes
	public static void executeSync(SyncBox syncBox) throws Exception {
		try {
			Printer.log("Syncing...");
			if (!syncBox.getSockets().isEmpty()){
				Connection c = syncBox.getSqlConn();

				// Loop through database
				List<String> elements = IndexDatabase.getElements(c);
				for (String path : elements){
					SyncFile syncFile = SyncFile.toSyncFile(syncBox, path);
					File file = syncFile.getFile();
					String status = IndexDatabase.getStatus(path, c);
					if (!syncFile.isBusy()){
						if (file.exists()){
							// Check with network
							for (Socket socket : syncBox.getSockets()){
								outgoingCheck(syncBox, syncFile, socket);
							}

						} else if (!status.equals("deleted") && !status.equals("syncing")){
							// Set as deleted
							IndexDatabase.updateFileHash(path, null, c);
							IndexDatabase.updateStatus(path, "deleted", c);
							IndexDatabase.updateFileSize(path, 0, c);
							File dir = file.getParentFile();
							while (!dir.exists()){
								dir = dir.getParentFile();
							}
							IndexDatabase.updateLastModified(path, Utils.getLastModified(dir), c);
						}
					}
				}
				Printer.log("Synced " + elements.size() + " elements!");

			} else {
				Printer.log("No connections found!");
			}

		} catch (Exception e) {
			Printer.log(e);
		}
	}

	// Send outgoing check
	private static void outgoingCheck(SyncBox syncBox, SyncFile syncFile, Socket socket) throws Exception {
		Connection c = syncBox.getSqlConn();

		// Load variables
		String syncPath = syncFile.getSyncPath();
		String status = IndexDatabase.getStatus(syncPath, c);
		long lastModified = IndexDatabase.getLastModified(syncPath, c);
		String hash = IndexDatabase.getFileHash(syncPath, c);

		// Send message
		JSONObject json = JsonHandler.prepJson(syncBox);
		json.put("cmd", "check_element");
		json.put("path", syncPath);
		json.put("status", status);
		json.put("lastmodified", lastModified);
		json.put("hash", hash);
		SocketHandler.sendMessage(socket, json);
	}

	// Handle incoming check
	public static void incomingCheck(SyncBox syncBox, Socket socket, SyncFile syncFile, String status, long lastModified, String hash) throws Exception {
		// Check for recent changes
		if (syncFile.isBusy()) return;
		syncFile.refresh();

		// Load variables
		Connection c = syncBox.getSqlConn();
		String syncPath = syncFile.getSyncPath();

		long myLastModified = 0;
		String myStatus = null;
		String myHash = null;
		if (IndexDatabase.doesExist(syncPath, c)){
			myLastModified = IndexDatabase.getLastModified(syncPath, c);
			myStatus = IndexDatabase.getStatus(syncPath, c);
			myHash = IndexDatabase.getFileHash(syncPath, c);
			if (!syncFile.isDir() && hash.equals(myHash)){
				return;
			}
		}

		// Compare last modified
		if (lastModified > myLastModified){
			// Update me
			if (status.equals("deleted")){
				if (syncFile.fileExist()){
					Printer.log("Deletion of " + syncFile.getFileName() + " received.");
					syncFile.remove(lastModified);
				}

			} else {
				if (syncFile.isDir()){
					//					if (!syncFile.fileExist()){
					//						file.mkdirs();
					//					}

				} else {
					Printer.log("Requesting " + syncFile.getFileName() + " to download.");
					JSONObject json = JsonHandler.prepJson(syncBox);
					json.put("cmd", "upload_file");
					json.put("path", syncPath);
					SocketHandler.sendMessage(socket, json);

					if (!IndexDatabase.doesExist(syncPath, c)){
						IndexDatabase.addFile(syncPath, 0, "syncing", lastModified, hash, c);
					} else {
						IndexDatabase.updateFileHash(syncPath, hash, c);
						IndexDatabase.updateFileSize(syncPath, 0, c);
						IndexDatabase.updateLastModified(syncPath, lastModified, c);
						IndexDatabase.updateStatus(syncPath, "syncing", c);
					}
				}
			}

		} else if (lastModified == 0 || lastModified < myLastModified){
			// Send update
			if (myStatus.equals("deleted")){
				if (!status.equals("deleted")){
					Printer.log("Sending delete command of " + syncFile.getFileName() + ".");
					JSONObject json = JsonHandler.prepJson(syncBox);
					json.put("cmd", "delete_element");
					json.put("path", syncPath);
					json.put("lastmodified", myLastModified);
					SocketHandler.sendMessage(socket, json);
				}

			} else {
				if (syncFile.isDir()){
					// Ignore

				} else {
					// File
					Printer.log("Upload of " + syncFile.getFileName() + " is needed.");
					FileUpload.uploadFile(syncBox, syncBox.getSocketHostAddress(socket), syncFile);
				}
			}
		}
	}
}