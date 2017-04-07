package net.hydrotekz.sync.utils;

import java.io.File;
import java.sql.Connection;

import org.apache.commons.io.FileUtils;

import net.hydrotekz.sync.indexing.RepeatIndexer;
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
		Utils.setLastModified(file, time);
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
		return file.exists();
	}

	public void refresh() throws Exception {
		RepeatIndexer.checkElement(this, syncBox);
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