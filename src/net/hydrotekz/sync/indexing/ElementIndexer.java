package net.hydrotekz.sync.indexing;

import java.io.File;

import org.json.simple.JSONObject;

import net.hydrotekz.sync.crypto.Hasher;
import net.hydrotekz.sync.utils.FileInfo;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

@SuppressWarnings("unchecked")
public class ElementIndexer {

	public static void executeIndex(SyncBox syncBox) throws Exception {
		// Folder variables
		File rootDir = syncBox.getFolder();
		SyncFile syncFile = SyncFile.toSyncFile(syncBox, rootDir);

		// Start indexing
		Printer.log("Indexing...");
		FileInfo info = indexFolder(syncFile);
		Printer.log("Indexed " + Utils.getFileSizeText(info.getFileSize()) + "!");
	}

	private static FileInfo indexFolder(SyncFile syncDir) throws Exception {
		// Folder variables
		File dir = syncDir.getFile();
		long dirSize = 0;
		long dirLastModified = Utils.getLastModified(dir);
		JSONObject json = new JSONObject();

		// Loop through content
		for (File file : dir.listFiles()){
			SyncFile syncFile = SyncFile.toSyncFile(syncDir.getSyncBox(), file);
			if (syncFile.ignore()) continue;
			FileInfo info = indexElement(syncFile);

			// Add folder info
			dirSize+=info.getFileSize();
			json.put(syncFile.getSyncPath(), info.getFileHash());
		}

		// Index folder
		String dirHash = Hasher.getStringHash(json.toJSONString());
		syncDir.update(dirSize, "synced", dirLastModified, dirHash);

		FileInfo output = new FileInfo(dirSize, dirLastModified, dirHash);
		return output;

	}

	public static FileInfo indexElement(SyncFile syncFile) throws Exception {
		if (syncFile.isDir()) return indexFolder(syncFile);
		else return indexFile(syncFile);
	}

	private static FileInfo indexFile(SyncFile syncFile) throws Exception {
		// File variables
		File file = syncFile.getFile();
		long fileLastModified = Utils.getLastModified(file);
		long fileSize = 0;
		long dbLastModified = 0;
		String fileHash = null;

		// Check if already exists
		if (syncFile.doesExist()){
			fileSize = syncFile.getFileSize();
			dbLastModified = syncFile.getLastModified();
			fileHash = syncFile.getFileHash();
		}

		// Index file
		if (fileLastModified != dbLastModified || fileHash == null || fileSize != file.length()){
			// Get file hash
			fileHash = Hasher.getFileHash(file);
			fileSize = file.length();

			// Prepare undelete
			if (syncFile.isDeleted()){
				if (dbLastModified > fileLastModified) fileLastModified = Utils.getLastModified(file.getParentFile());
				else fileLastModified++;
				Utils.setLastModified(file, fileLastModified);
			}

			// Upload to database
			syncFile.update(fileSize, "synced", fileLastModified, fileHash);
			fixParents(syncFile);
			Printer.log("Update detected to: " + file.getName());

		} else fileLastModified = dbLastModified;

		FileInfo output = new FileInfo(fileSize, fileLastModified, fileHash);
		return output;
	}

	private static void fixParents(SyncFile syncFile) throws Exception {
		File file = syncFile.getFile();
		File root = syncFile.getSyncBox().getFolder();
		long fileLastModified = syncFile.getLastModified();

		while(true){
			file = file.getParentFile();
			if (file.getAbsolutePath().startsWith(root.getAbsolutePath())){
				SyncFile syncDir = SyncFile.toSyncFile(syncFile.getSyncBox(), file);
				long dirLastModified = syncDir.getLastModified();
				if (fileLastModified > dirLastModified){
					syncDir.setLastModified(fileLastModified);
					Printer.log("DEBUG: Fixed " + syncDir.getSyncPath());
				}

			} else break;
		}
	}
}