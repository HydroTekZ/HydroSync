package net.hydrotekz.sync.indexing;

import java.io.File;
import java.sql.Connection;

import net.hydrotekz.sync.crypto.Hasher;
import net.hydrotekz.sync.sqlite.IndexDatabase;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

public class _RepeatIndexer {

	// Execute index
	public static void executeIndex(SyncBox syncBox) throws Exception {
		Printer.log("Indexing...");
		File folder = syncBox.getFolder();
		loopIndex(syncBox, folder);
		Printer.log("Indexing complete!");
	}

	// Do the loop
	private static void loopIndex(SyncBox syncBox, File file) throws Exception {
		SyncFile syncFile = SyncFile.toSyncFile(syncBox, file);
		if (!syncFile.ignore()){
			// Index element
			checkElement(syncFile, syncBox, true, false);
		}
	}

	// Index element
	public static void checkElement(SyncFile syncFile, SyncBox syncBox, boolean expand, boolean force) throws Exception {
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

		if ((status != null && status.equals("deleted")) || force || oldLastModified == 0 || lastModified != oldLastModified){
			long fileSize = -1;
			String fileHash = null;
			if (status != null && status.equals("deleted")){
				if (oldLastModified > lastModified) lastModified = Utils.getLastModified(file.getParentFile());
				else lastModified++;
				Utils.setLastModified(file, lastModified);
			}

			if (!syncFile.isRootDir()){
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
			if (expand && file.isDirectory()){
				for (File f : file.listFiles()){
					loopIndex(syncBox, f);
				}
			}
		}
	}
}