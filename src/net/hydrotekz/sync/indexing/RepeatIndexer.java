package net.hydrotekz.sync.indexing;

import java.io.File;
import java.sql.Connection;

import net.hydrotekz.sync.crypto.Hasher;
import net.hydrotekz.sync.sqlite.IndexDatabase;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
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

			// TODO: Refresh sync
			

			// Continue task
			Thread.sleep(1000*10);
			executeIndex(syncBox);

		} catch (Exception ex){
			Printer.log(ex);
			Printer.log("Indexing aborted due to critical error.");
			System.exit(0);
		}
	}

	// Do the loop
	private static void loopIndex(SyncBox syncBox, File file) throws Exception {
		String syncPath = syncBox.getSyncPath(file);
		if (!syncPath.equals(".box" + File.separator)){
			// Index element
			checkElement(file, syncBox);
		}
	}

	// Index element
	private static void checkElement(File file, SyncBox syncBox) throws Exception {
		Connection c = syncBox.getSqlConn();
		String path = syncBox.getSyncPath(file);

		long lastModified = Utils.getLastModified(file);
		long oldLastModified = IndexDatabase.getLastModified(path, c);

		if (lastModified != oldLastModified){
			long fileSize = -1;
			String fileHash = null;

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