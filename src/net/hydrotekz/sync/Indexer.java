package net.hydrotekz.sync;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;

import net.hydrotekz.sync.sqlite.DbIndexHandler;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class Indexer {

	// Index starter
	public static void startIndex(SyncBox syncBox){
		try {
			Printer.log("Indexing...");
			File folder = syncBox.getFolder();
			loopIndex(syncBox, folder);
			Printer.log("Indexing complete!");

		} catch (Exception ex){
			Printer.log(ex);
			Printer.log("Indexing aborted due to critical error.");
		}
	}

	// Do the indexing
	private static void loopIndex(SyncBox syncBox, File file) throws Exception {
		if (file.isFile() && !file.isDirectory()){
			indexFile(file, syncBox);

		} else if (file.isDirectory() && !file.getName().equals(".box")){
			for (File f : file.listFiles()){
				indexFolder(f, syncBox);
				loopIndex(syncBox, f);
			}
		}
	}

	// Index folder info
	private static void indexFolder(File folder, SyncBox syncBox){

	}

	// Index file info
	private static void indexFile(File file, SyncBox syncBox) throws Exception {
		// Load variables
		Connection c = syncBox.getSqlConn();
		if (c.isClosed()){
			c = syncBox.getDataSource().getConnection();
			syncBox.setSqlConn(c);
		}

		long fileSize = file.length();
		String fileHash = null;

		String path = syncBox.getSyncPath(file);

		// Read attributes
		BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		long lastModified = attr.lastModifiedTime().toMillis();

		// Check against database
		Printer.log(path);
		if (!DbIndexHandler.doesExist(path, c)){
			DbIndexHandler.addFile(path, fileSize, "synced", lastModified, fileHash, c);
		}
	}
}