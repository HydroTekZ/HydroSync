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

@SuppressWarnings("unchecked")
public class NetworkIndexer {

	// Check for external changes
	public static void executeSync(SyncBox syncBox) throws Exception {
		Printer.printInfo("Syncing...");
		Connection c = syncBox.getSqlConn();

		// Loop through database
		List<String> elements = IndexDatabase.getSyncPaths(c);
		for (String path : elements){
			SyncFile syncFile = SyncFile.toSyncFile(syncBox, path);
			File file = syncFile.getFile();
			String status = IndexDatabase.getStatus(path, c);
			if (!syncFile.isBusy()){
				// Check if deleted
				if (!file.exists() && !status.equals("deleted")){
					if (status.equals("syncing")){
						IndexDatabase.removeFile(path, c);
						Printer.printInfo("Removed " + file.getName() + " from database.");
						continue;

					} else {
						// Set as deleted
						syncFile.delete(0);
						Printer.printInfo("Delete of " + file.getName() + " detected.");
					}
				}

				// Index if necessary
				if (file.exists() && !syncFile.isDir()){
					String hash = IndexDatabase.getFileHash(path, c);
					if (hash == null){
						Printer.printInfo("Refresh of " + file.getName() + " needed.");
						syncFile.refresh();
					}
				}

				// Check with network
				for (Socket socket : syncBox.getSockets()){
					if (SocketHandler.isConnected(socket)){
						outgoingCheck(syncBox, syncFile, socket);
					}
				}
			}
		}

		// Debug network
		int connections = 0;
		for (Socket socket : syncBox.getSockets()){
			if (SocketHandler.isConnected(socket)){
				connections++;
			}
		}

		Printer.printInfo("Synced " + elements.size() + " elements with " + connections + " peers!");
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
			if (hash != null && hash.equals(myHash)){
				if (lastModified > myLastModified){
					syncFile.updateLastModified(lastModified);
					syncFile.setLastModified(lastModified);
					Printer.printInfo("Last modified for " + syncFile.getFileName() + " was updated.");
				}
				if (!syncFile.isDir()) return;
			}
		}

		// Compare last modified
		if (lastModified > myLastModified){
			// Update me
			if (status.equals("deleted")){
				if (syncFile.fileExist()){
					Printer.printInfo("Deletion of " + syncFile.getFileName() + " received.");
					syncFile.remove(lastModified);

				} else {
					syncFile.update(0, "deleted", lastModified, null);
				}

			} else {
				if (syncFile.isDir()){
					if (!syncFile.fileExist()){
						Printer.printInfo("Creating folder " + syncFile.getFileName() + " now.");
						syncFile.getFile().mkdirs();
						syncFile.updateLastModified(lastModified);
						syncFile.setLastModified(lastModified);
					}

				} else {
					Printer.printInfo("Requesting " + syncFile.getFileName() + " to download.");
					JSONObject json = JsonHandler.prepJson(syncBox);
					json.put("cmd", "upload_file");
					json.put("path", syncPath);
					SocketHandler.sendMessage(socket, json);

					syncFile.update(0, "syncing", lastModified, hash);
				}
			}

		} else if (myLastModified > lastModified){
			// Send update
			if (myStatus.equals("deleted")){
				if (!status.equals("deleted")){
					Printer.printInfo("Sending delete command of " + syncFile.getFileName() + ".");
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
					Printer.printInfo("Upload of " + syncFile.getFileName() + " is needed.");
					FileUpload.uploadFile(syncBox, syncBox.getSocketHostAddress(socket), syncFile);
				}
			}
		}
	}
}