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
import net.hydrotekz.sync.crypto.Hasher;
import net.hydrotekz.sync.indexing.IndexHandler;
import net.hydrotekz.sync.net.SocketHandler;
import net.hydrotekz.sync.net.SocketService;
import net.hydrotekz.sync.sqlite.DbManager;
import net.hydrotekz.sync.utils.Address;
import net.hydrotekz.sync.utils.CfgBox;
import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;

public class MainCore {

	public static double version = 1.0;

	public static HashMap<String, SyncBox> syncBoxes = new HashMap<String, SyncBox>();
	public static HashMap<String, Socket> sockets = new HashMap<String, Socket>();
	public static HashMap<String, Long> tasks = new HashMap<String, Long>();

	public static void startApplication(){
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
								Printer.printError("Failed to hide folder.");
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
							Printer.printError(ex);
							Printer.printError("Failed create SQL connection successfully.");
						}

						// Configurate peers
						List<Address> peers = new ArrayList<Address>();

						String tracker = cfgBox.getTracker();
						Address address = Address.toAddress(tracker);

						peers.add(address);

						// Prepare key
						String key = cfgBox.getKey();
						key = Hasher.getStringHash(key);

						// Create the object
						SyncBox syncBox = new SyncBox(name, file, database, sqlConn, peers, key);
						syncBoxes.put(name, syncBox);

						// Create connections
						SocketHandler.establishConnections(syncBox);

						// Index
						IndexHandler.startIndexer(syncBox);
					}
				};
				new Thread(r).start();
			}

			// Add shutdown task
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					stopApplication(false);
				}
			});
		}
	}

	public static void restartApplication() {
		try {
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = new File(HydroSync.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			// Verify that it's a JAR
			if(!currentJar.getName().endsWith(".jar")) return;

			// Build the command
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			Printer.printInfo("Restarting application...");
			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();

		} catch (Exception e){
			Printer.printError(e);
			Printer.printError("Failed to restart application!");
		}

		// Shut down current
		stopApplication(true);
	}

	public static void stopApplication(boolean force) {
		// Close database connections
		try {
			for (SyncBox syncBox : MainCore.syncBoxes.values()){
				syncBox.getSqlConn().close();
				syncBox.getDataSource().close();
			}
			Printer.printInfo("Database connections closed.");

		} catch (Exception e){
			Printer.printError("Failed to close database connections.");
		}

		// Close socket connections
		try {
			SocketService.stopService();
			for (Socket peer : MainCore.sockets.values()){
				if (!peer.isClosed()) peer.close();
			}
			Printer.printInfo("Socket connections was closed.");

		} catch (Exception e){
			Printer.printError("Failed to close socket connections.");
		}

		// Shut down
		if (force){
			System.exit(0);
		}
	}
}