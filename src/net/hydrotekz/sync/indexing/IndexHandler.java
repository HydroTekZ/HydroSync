package net.hydrotekz.sync.indexing;

import net.hydrotekz.sync.sqlite.StatusDatabase;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class IndexHandler {

	// Runs on startup
	public static void enableIndexing(SyncBox syncBox){
		try {
			// Do first time scan
			if (!StatusDatabase.doesExist("FirstTimeScan", syncBox.getSqlConn())){
				FirstTimeIndexer.executeScan(syncBox);
				StatusDatabase.addEntry("FirstTimeScan", "complete", syncBox.getSqlConn());
			}

			// Enable interval scan
			RepeatIndexer.executeIndex(syncBox);

		} catch (Exception ex){
			Printer.log(ex);
			Printer.log("Failed to index successfully.");
			System.exit(0);
		}
	}
}