package net.hydrotekz.sync;

import java.io.File;

import net.hydrotekz.sync.utils.SyncBox;

public class Indexer {

	// Start indexer
	public static void loopIndex(SyncBox syncBox, File file){
		if (file.isFile() && !file.isDirectory()){
			indexFile(file);

		} else if (file.isDirectory()){
			for (File f : file.listFiles()){
				indexFolder(f);
				loopIndex(syncBox, f);
			}
		}
	}

	private static void indexFolder(File folder){

	}

	private static void indexFile(File file){

	}
}