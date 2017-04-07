package net.hydrotekz.sync.indexing;

import java.io.File;
import java.sql.Connection;

import net.hydrotekz.sync.crypto.Hasher;
import net.hydrotekz.sync.sqlite.IndexDatabase;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

public class RepeatIndexer {

	// Start indexing
	public static void executeIndex(SyncBox syncBox) throws Exception {
		try {
			// Index folder
			Printer.log("Indexing...");
			File folder = syncBox.getFolder();
			loopIndex(syncBox, folder);
			Printer.log("Indexing complete!");

			// Refresh sync
			NetworkIndexer.executeSync(syncBox.refresh());

			// Continue task
			Thread.sleep(1000*45);
			executeIndex(syncBox.refresh());

		} catch (Exception ex){
			Printer.log(ex);
			Printer.log("Indexing aborted due to critical error.");
			System.exit(0);
		}
	}

	// Do the loop
	private static void loopIndex(SyncBox syncBox, File file) throws Exception {
		SyncFile syncFile = SyncFile.toSyncFile(syncBox, file);
		if (!syncFile.ignore()){
			// Index element
			checkElement(syncFile, syncBox);
		}
	}

	// Index element
	public static void checkElement(SyncFile syncFile, SyncBox syncBox) throws Exception {
		File file = syncFile.getFile();
		if (file == null || !file.exists()) return;

		Connection c = syncBox.getSqlConn();

		String path = syncFile.getSyncPath();

		long lastModified = Utils.getLastModified(file);
		long oldLastModified = 0;
		String status = null;
		if (IndexDatabase.doesExist(path, c)){
			oldLastModified = IndexDatabase.getLastModified(path, c);
			status = IndexDatabase.getStatus(path, c);
		}

		if ((status != null && status.equals("deleted")) || oldLastModified == 0 || lastModified != oldLastModified){
			long fileSize = -1;
			String fileHash = null;

			if (!path.equals(File.separator) && !path.equals("\\") && !path.equals("/")){
				// If it's a file
				if (file.isFile()){
					fileSize = file.length();
					fileHash = Hasher.getFileHash(file);
				}

				// Add/update element
				if (!IndexDatabase.doesExist(path, c)){
					IndexDatabase.addFile(path, fileSize, "synced", lastModified, fileHash, c);

				} else {
					if (file.isFile()){
						IndexDatabase.updateFileHash(path, fileHash, c);
						IndexDatabase.updateFileSize(path, fileSize, c);
					}
					IndexDatabase.updateLastModified(path, lastModified, c);
					IndexDatabase.updateStatus(path, "synced", c);
				}
			}

			// Add more elements to the loop
			if (file.isDirectory()){
				for (File f : file.listFiles()){
					loopIndex(syncBox, f);
				}
			}
		}
	}

}