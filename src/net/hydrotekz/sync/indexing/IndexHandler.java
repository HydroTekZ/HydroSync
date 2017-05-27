package net.hydrotekz.sync.indexing;

import java.io.IOException;

import net.hydrotekz.sync.MainCore;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class IndexHandler {

	// Runs on startup
	public static void startIndexer(SyncBox syncBox){
		try {
			// Start watcher
			Watcher.startWatch(syncBox);

			// Index folder
			ElementIndexer.executeIndex(syncBox.refresh());

			// Enable interval scan
			indexerTask(syncBox);

		} catch (Exception ex){
			Printer.printError(ex);
			Printer.printError("Failed to index successfully.");
			System.exit(0);
		}
	}

	// Start indexing
	public static void indexerTask(SyncBox syncBox) throws Exception {
		try {
			// Refresh sync
			NetworkIndexer.executeSync(syncBox.refresh());

			// Continue task
			Thread.sleep(1000*60*30);
			indexerTask(syncBox);

		} catch (IOException ioe){
			MainCore.restartApplication();

		} catch (Exception ex){
			Printer.printError(ex);
			Printer.printError("Indexing aborted due to critical error.");
			MainCore.stopApplication(true);
		}
	}
}