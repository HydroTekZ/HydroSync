package net.hydrotekz.sync.indexing;

import java.io.File;
import java.sql.Connection;

import net.hydrotekz.sync.crypto.Hasher;
import net.hydrotekz.sync.sqlite.IndexDatabase;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.Utils;

public class FirstTimeIndexer {

	// Index starter
	public static void executeScan(SyncBox syncBox){
		try {
			Printer.log("Indexing...");
			File folder = syncBox.getFolder();
			loopIndex(syncBox, folder);
			Printer.log("Indexing complete!");

		} catch (Exception ex){
			Printer.log(ex);
			Printer.log("Indexing aborted due to critical error.");
			System.exit(0);
		}
	}

	// Do the indexing
	private static void loopIndex(SyncBox syncBox, File file) throws Exception {
		String syncPath = syncBox.getSyncPath(file);
		if (!syncPath.equals(".box" + File.separator)){
			// Index element
			indexElement(file, syncBox);

			// Get more files
			if (file.isDirectory()){
				for (File f : file.listFiles()){
					loopIndex(syncBox, f);
				}
			}
		}
	}

	// Index folder/file info
	private static void indexElement(File file, SyncBox syncBox) throws Exception {
		// Load variables
		Connection c = syncBox.getSqlConn();

		long fileSize = -1;
		String fileHash = null;

		String path = syncBox.getSyncPath(file);
		
		if (IndexDatabase.doesExist(path, c)) return;
		
		long lastModified = Utils.getLastModified(file);

		// If it's a file
		if (file.isFile()){
			fileSize = file.length();
			fileHash = Hasher.getFileHash(file);
		}

		// Check against database
		Printer.log(path);
		if (!IndexDatabase.doesExist(path, c)){
			IndexDatabase.addFile(path, fileSize, "synced", lastModified, fileHash, c);
		}
	}
}