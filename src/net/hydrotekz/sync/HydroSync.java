package net.hydrotekz.sync;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import net.hydrotekz.sync.config.MainConfig;
import net.hydrotekz.sync.indexing.IndexHandler;
import net.hydrotekz.sync.net.SocketHandler;
import net.hydrotekz.sync.sqlite.DbManager;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.CfgBox;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class HydroSync {

	public static double version = 1.0;

	public static HashMap<String, SyncBox> syncBoxes = new HashMap<String, SyncBox>();
	public static HashMap<Address, Socket> connections = new HashMap<Address, Socket>();

	public static void main(String[] args){
		// Load config
		MainConfig.loadConfig();

		// Load sync boxes
		if (MainConfig.boxes != null){
			for (CfgBox cfgBox : MainConfig.boxes){
				// Start new thread
				Runnable r = new Runnable() {
					public void run() {
						String name = cfgBox.getName();
						String path = cfgBox.getPath();
						File file = new File(path);

						// Intel directory
						File intel = new File(file.getAbsolutePath() + File.separator + ".box");
						if (!intel.exists()){
							// Make directory
							intel.mkdir();

							// Hide directory
							try {
								Path intelPath = intel.toPath();
								Files.setAttribute(intelPath, "dos:hidden", true);

							} catch (IOException e) {
								Printer.log("Failed to hide folder.");
							}
						}

						// Create SQL connection for database
						BasicDataSource database = null;
						Connection sqlConn = null;
						try {
							File dbFile = new File(intel.getAbsolutePath() + File.separator + "data.db");
							database = DbManager.loadDataSource(dbFile.getAbsolutePath());
							sqlConn = database.getConnection();
							DbManager.createTables(sqlConn);

						} catch (Exception ex){
							Printer.log(ex);
							Printer.log("Failed create SQL connection successfully.");
						}

						// Configurate peers
						List<Address> peers = new ArrayList<Address>();

						String tracker = cfgBox.getTracker();
						Address address = Address.toAddress(tracker);

						peers.add(address);

						// Create the object
						SyncBox syncBox = new SyncBox(name, file, database, sqlConn, peers);
						syncBoxes.put(name, syncBox);

						// Create connections
						SocketHandler.establishConnections(syncBox);

						// Index
						IndexHandler.enableIndexing(syncBox);
					}
				};
				new Thread(r).start();
			}
		}
	}
}