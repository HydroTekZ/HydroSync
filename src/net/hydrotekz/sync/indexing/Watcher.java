package net.hydrotekz.sync.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;

public class Watcher {

	public static void startWatch(SyncBox syncBox) throws Exception {
		Runnable r = new Runnable() {
			public void run() {
				try {
					registerRecursive(syncBox, true);

				} catch (Exception e) {
					Printer.printError(e);
					Printer.printError("Watcher crashed!");
					System.exit(0);
				}
			}
		};
		new Thread(r).start();
	}

	private static void registerRecursive(SyncBox syncBox, boolean startup) throws Exception {
		File dir = syncBox.getFolder();
		Path root = dir.toPath();
		WatchService watchService = root.getFileSystem().newWatchService();

		// Register all subfolders
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				SyncFile syncFile = SyncFile.toSyncFile(syncBox, dir.toFile());
				if (!syncFile.ignore()){
					dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		if (startup) Printer.printInfo("Watcher started.");
		else Printer.printInfo("Watcher restarted.");

		while(true){
			// This call is blocking until events are present
			WatchKey watchKey = watchService.take();
			Path parent = (Path) watchKey.watchable();

			// Poll for file system events on the WatchKey
			for (final WatchEvent<?> e : watchKey.pollEvents()) {
				Object context = e.context();
				if (context != null && context instanceof Path){
					Path path = parent.resolve((Path)context);
					File file = path.toFile();

					String kind = e.kind().toString();
					SyncFile syncFile = SyncFile.toSyncFile(syncBox, file);

					if (!syncFile.ignore()){
						Printer.logDebug(syncFile.getSyncPath() + " | " + kind);
						if (kind.equals("ENTRY_MODIFY") || kind.equals("ENTRY_CREATE")){
							if (!syncFile.isDir() && syncFile.fileExist()) ElementIndexer.indexElement(syncFile);

						} else if (kind.equals("ENTRY_DELETE")){
							syncFile.delete(System.currentTimeMillis());
						}
					}
				}
			}

			if(!watchKey.reset()) {
				Printer.printWarning("Watch key no longer valid.");
				watchKey.cancel();
				watchService.close();
				registerRecursive(syncBox, false);
				break;
			}
		}
	}
}