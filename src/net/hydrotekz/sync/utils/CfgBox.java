package net.hydrotekz.sync.utils;

public class CfgBox {

	private String path;
	private String tracker;
	private String key;
	private String name;

	public CfgBox (String name, String path, String tracker, String key){
		this.path = path;
		this.tracker = tracker;
		this.key = key;
		this.name = name;
	}
	
	public String getName(){
		return name;
	}

	public String getKey(){
		return key;
	}

	public String getTracker(){
		return tracker;
	}

	public String getPath(){
		return path;
	}
}