package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import net.hydrotekz.sync.utils.Printer;
import net.hydrotekz.sync.utils.SyncBox;
import net.hydrotekz.sync.utils.SyncFile;
import net.hydrotekz.sync.utils.Utils;

public class FileDownload {

	public static List<String> inProgress = new ArrayList<String>();

	public static void downloadFile(SyncBox syncBox, Socket socket, SyncFile syncFile, long size, long lastModified, String hash) throws Exception {
		String syncPath = syncFile.getSyncPath();
		if (inProgress.contains(syncPath)) return;
		File toFile = syncFile.getFile();;
		File tmpFile = null;
		inProgress.add(syncPath);

		try {
			Printer.printInfo("Downloading " + syncFile.getFileName() + "...");

			syncFile.update(size, "syncing", lastModified, hash);

			// Create temp file
			String tmpName = syncFile.getFileName().replace(syncFile.getFileExt(), "") + "_";
			tmpFile = File.createTempFile(tmpName, syncFile.getFileExt(), syncBox.getCacheBox());

			// Delete existing file
			if (toFile != null && toFile.exists()){
				syncFile.recycle();
				Printer.printInfo("(existing file was removed and will be replaced)");
			}

			// Transfer file
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			FileOutputStream fos = new FileOutputStream(tmpFile);
			IOUtils.copyLarge(dis, fos);

			// Finish
			fos.flush();
			dis.close();
			fos.close();
			socket.close();

		} catch (Exception ex){
			Printer.printError(ex);

		} finally {
			if (tmpFile.length() == size){
				try {
					// Finalize
					toFile.getParentFile().mkdirs();
					FileUtils.moveFile(tmpFile, toFile);

					syncFile.updateStatus("synced");
					Utils.setLastModified(toFile, lastModified);

					Thread.sleep(250);
					Printer.printInfo("Successfully downloaded " + syncFile.getFileName() + "!");

				} catch (Exception e){
					Printer.logError(e);
					Printer.printError("Download was almost successful! Check logs for more info.");
				}
			} else {
				Printer.printError("Download failed!");
			}
			inProgress.remove(syncPath);
		}
	}
}