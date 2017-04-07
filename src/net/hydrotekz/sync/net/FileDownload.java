package net.hydrotekz.sync.net;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import net.hydrotekz.sync.sqlite.IndexDatabase;
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
			Printer.log("Downloading file... " + syncPath);

			Connection c = syncBox.getSqlConn();
			if (!IndexDatabase.doesExist(syncPath, c)){
				IndexDatabase.addFile(syncPath, size, "syncing", lastModified, hash, c);
			} else {
				IndexDatabase.updateFileHash(syncPath, hash, c);
				IndexDatabase.updateFileSize(syncPath, size, c);
				IndexDatabase.updateLastModified(syncPath, lastModified, c);
				IndexDatabase.updateStatus(syncPath, "syncing", c);
			}

			// Create temp file
			String tmpName = syncFile.getFileName().replace(syncFile.getFileExt(), "") + "_";
			tmpFile = File.createTempFile(tmpName, syncFile.getFileExt(), syncBox.getCacheBox());

			// Delete existing file
			if (toFile != null && toFile.exists()){
				syncFile.recycle();
				Printer.log("(existing file was removed and will be replaced)");
			}

			// Transfer file
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			FileOutputStream fos = new FileOutputStream(tmpFile);
			long res = IOUtils.copyLarge(dis, fos);

			// Finish
			fos.flush();
			dis.close();
			fos.close();
			socket.close();

			Printer.log("Finalizing download... (" + res + ")");

		} catch (Exception ex){
			Printer.log(ex);
			Printer.log("Download failed!");

		} finally {
			if (tmpFile.length() == size){
				try {
					// Finalize
					FileUtils.moveFile(tmpFile, toFile);
					Utils.setLastModified(toFile, lastModified);
					Connection c = syncBox.getSqlConn();
					IndexDatabase.updateStatus(syncPath, "synced", c);
					Thread.sleep(250);
					Printer.log("Download complete!");

				} catch (Exception e){
					Printer.log(e);
					Printer.log("Download was almost successful.");
				}
			} else {
				Printer.log("Download failed!");
			}
			inProgress.remove(syncPath);
		}
	}
}