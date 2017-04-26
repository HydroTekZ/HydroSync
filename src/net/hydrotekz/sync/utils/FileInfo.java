package net.hydrotekz.sync.utils;

public class FileInfo {

	private long fileSize;
	private long lastModified;
	private String fileHash;

	public FileInfo(long fileSize, long lastModified, String fileHash){
		this.fileSize = fileSize;
		this.lastModified = lastModified;
		this.fileHash = fileHash;
	}

	public long getFileSize(){
		return fileSize;
	}

	public long getLastModifed(){
		return lastModified;
	}

	public String getFileHash(){
		return fileHash;
	}
}