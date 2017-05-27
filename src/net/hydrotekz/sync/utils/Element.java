package net.hydrotekz.sync.utils;

public class Element {

	private String path;
	private long fileSize;
	private String status;
	private long lastModified;
	private String fileHash;

	public Element(String path, long fileSize, String status, long lastModified, String fileHash){
		this.path = path;
		this.fileSize = fileSize;
		this.status = status;
		this.lastModified = lastModified;
		this.fileHash = fileHash;
	}

	public String getSyncPath(){
		return path;
	}

	public long getFileSize(){
		return fileSize;
	}

	public String getStatus(){
		return status;
	}

	public long getLastModified(){
		return lastModified;
	}

	public String getFileHash(){
		return fileHash;
	}
}