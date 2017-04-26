package net.hydrotekz.sync.indexing;

import java.io.File;
import java.sql.Connection;

import net.hydrotekz.sync.crypto.Hasher;
import net.hydrotekz.sync.sqlite.IndexDatabase;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

public class _FirstTimeIndexer {

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
		SyncFile syncFile = SyncFile.toSyncFile(syncBox, file);
			if (!syncFile.ignore()){
				// Index element
				if (!syncFile.isRootDir())
					indexElement(syncFile, syncBox);

				// Get more files
				if (file.isDirectory()){
					for (File f : file.listFiles()){
						loopIndex(syncBox, f);
					}
				}
			}
	}

	// Index folder/file info
	private static void indexElement(SyncFile syncFile, SyncBox syncBox) throws Exception {
		Connection c = syncBox.getSqlConn();
		
		// Load variables
		long fileSize = -1;
		String fileHash = null;
		File file = syncFile.getFile();
		String path = syncFile.getSyncPath();

		if (IndexDatabase.doesExist(path, c)) return;

		long lastModified = Utils.getLastModified(file);

		// If it's a file
		if (file.isFile()){
			fileSize = file.length();
			fileHash = Hasher.getFileHash(file);
		}

		// Check against database
		if (!IndexDatabase.doesExist(path, c)){
			IndexDatabase.addFile(path, fileSize, "synced", lastModified, fileHash, c);
		}
	}
}