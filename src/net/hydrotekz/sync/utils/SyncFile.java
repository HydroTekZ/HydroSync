package net.hydrotekz.sync.utils;

import java.io.File;
import java.sql.Connection;

import org.apache.commons.io.FileUtils;

import net.hydrotekz.sync.indexing.ElementIndexer;
import net.hydrotekz.sync.net.FileDownload;
import net.hydrotekz.sync.net.FileUpload;
import net.hydrotekz.sync.sqlite.IndexDatabase;

public class SyncFile {

	private SyncBox syncBox;
	private File file;
	private String syncPath;

	public SyncFile(SyncBox syncBox, File file, String syncPath){
		this.syncBox = syncBox;
		this.file = file;
		this.syncPath = syncPath;
	}

	public SyncBox getSyncBox(){
		return syncBox;
	}

	public File getFile(){
		return file;
	}

	public String getSyncPath(){
		return syncPath;
	}

	public boolean isDir(){
		if (syncPath.endsWith(File.separator) || syncPath.endsWith("/") || syncPath.endsWith("\\"))
			return true;
		else return false;
	}

	public void setLastModified(long time) throws Exception {
		if (file.exists()) Utils.setLastModified(file, time);
	}

	public void remove(long lastModified) throws Exception {
		File trash = syncBox.getTrashBox();
		File moveTo = new File(trash + File.separator + file.getName());
		int num = 0;
		while (moveTo.exists()){
			num++;
			File to = new File(moveTo.getAbsolutePath().replace(getFileExt(), "") + " (" + num + ")" + getFileExt());
			if (!to.exists()){
				moveTo = to;
				break;
			}
		}
		if (isDir()) FileUtils.moveDirectory(file, moveTo);
		else FileUtils.moveFile(file, moveTo);

		Connection c = syncBox.getSqlConn();
		IndexDatabase.updateLastModified(syncPath, lastModified, c);
		IndexDatabase.updateStatus(syncPath, "deleted", c);
	}

	public void recycle() throws Exception {
		File bin = syncBox.getBinBox();
		File moveTo = new File(bin + File.separator + file.getName());
		int num = 0;
		while (moveTo.exists()){
			num++;
			File to = new File(moveTo.getAbsolutePath().replace(getFileExt(), "") + " (" + num + ")" + getFileExt());
			if (!to.exists()){
				moveTo = to;
				break;
			}
		}
		if (isDir()) FileUtils.moveDirectory(file, moveTo);
		else FileUtils.moveFile(file, moveTo);
	}

	public boolean isBusy(){
		return FileUpload.inProgress.contains(syncPath) || FileDownload.inProgress.contains(syncPath);
	}

	public boolean ignore(){
		if (syncPath.startsWith(".box" + File.separator) || syncPath.startsWith(".box\\") || syncPath.startsWith(".box/")
				|| file.isHidden()){
			return true;
		} else return false;
	}

	public boolean isRootDir(){
		if (syncPath.equals(File.separator) || syncPath.equals("\\") || syncPath.equals("/"))
			return true;
		else return false;
	}

	public String getFileName(){
		return file.getName();
	}

	public boolean fileExist(){
		return file != null && file.exists();
	}

	public void refresh() throws Exception {
		if (file.exists() && file != null) ElementIndexer.indexElement(this);
	}

	public String getFileExt(){
		String fileName = getFileName();
		if (fileName.contains(".")){
			StringBuilder ext = new StringBuilder();
			for (String c : fileName.split("")){
				if (c.equals(".")) ext = new StringBuilder();
				ext.append(c);
			}
			return ext.toString();
		}
		return null;
	}

	/*
	 * Database related
	 */

	public void delete(long overrideTime) throws Exception {
		if (doesExist()){
			String status = getStatus();
			if (!status.equals("deleted")){
				Printer.printInfo("Deletion detected to: " + file.getName());

				File dir = file.getParentFile();
				while (!dir.exists()){
					dir = dir.getParentFile();
				}
				long time = Utils.getLastModified(dir);
				if (overrideTime > 0) time = overrideTime;

				update(0, "deleted", time, null);
				ElementIndexer.fixParents(this);
			}
		}
	}

	public void addToDb(long fileSize, String status, long lastModified, String fileHash) throws Exception {
		Connection c = syncBox.getSqlConn();
		IndexDatabase.addFile(syncPath, fileSize, status, lastModified, fileHash, c);
	}

	public void update(long fileSize, String status, long lastModified, String fileHash) throws Exception {
		Connection c = syncBox.getSqlConn();
		if (!doesExist()){
			IndexDatabase.addFile(syncPath, fileSize, status, lastModified, fileHash, c);

		} else {
			updateFileSize(fileSize);
			updateStatus(status);
			updateLastModified(lastModified);
			updateFileHash(fileHash);
		}
	}

	public void updateFileSize(long fileSize) throws Exception {
		Connection c = syncBox.getSqlConn();
		IndexDatabase.updateFileSize(syncPath, fileSize, c);
	}

	public void updateFileHash(String fileHash) throws Exception {
		Connection c = syncBox.getSqlConn();
		IndexDatabase.updateFileHash(syncPath, fileHash, c);
	}

	public void updateLastModified(long lastModified) throws Exception {
		if (file.exists()){
			Connection c = syncBox.getSqlConn();
			IndexDatabase.updateLastModified(syncPath, lastModified, c);
		}
	}

	public void updateStatus(String status) throws Exception {
		Connection c = syncBox.getSqlConn();
		IndexDatabase.updateStatus(syncPath, status, c);
	}

	public boolean doesExist() throws Exception {
		Connection c = syncBox.getSqlConn();
		return IndexDatabase.doesExist(syncPath, c);
	}

	public long getLastModified() throws Exception {
		Connection c = syncBox.getSqlConn();
		return IndexDatabase.getLastModified(syncPath, c);
	}

	public String getFileHash() throws Exception {
		Connection c = syncBox.getSqlConn();
		return IndexDatabase.getFileHash(syncPath, c);
	}

	public long getFileSize() throws Exception {
		Connection c = syncBox.getSqlConn();
		return IndexDatabase.getFileSize(syncPath, c);
	}

	public String getStatus() throws Exception {
		Connection c = syncBox.getSqlConn();
		return IndexDatabase.getStatus(syncPath, c);
	}

	public boolean isDeleted() throws Exception {
		if (!doesExist()) return false;
		String status = getStatus();
		return status != null && status.equals("deleted");
	}

	/*
	 * Static
	 */

	public static SyncFile toSyncFile(SyncBox syncBox, String syncPath){
		return new SyncFile(syncBox, getFileInSync(syncBox, syncPath), syncPath);
	}

	public static SyncFile toSyncFile(SyncBox syncBox, File file){
		return new SyncFile(syncBox, file, getSyncPath(syncBox, file));
	}

	public static String getSyncPath(SyncBox syncBox, File file) {
		String localPath = file.getAbsolutePath();
		String syncRoot = syncBox.getFolder().getAbsolutePath();
		if (localPath.equals(syncRoot)) return File.separator;
		String path = localPath.replace(syncRoot + File.separator, "");
		if (file.isDirectory()) path += File.separator;
		return path;
	}

	public static File getFileInSync(SyncBox syncBox, String syncPath){
		String path = syncBox.getFolder().getAbsolutePath();
		if (syncPath.endsWith(File.separator) || syncPath.endsWith("\\") || syncPath.endsWith("/")){
			syncPath = syncPath.substring(0, syncPath.length() - 1);
		}
		path += File.separator + syncPath;
		File file = new File(path);
		return file;
	}
}