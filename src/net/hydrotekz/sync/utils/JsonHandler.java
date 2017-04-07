package net.hydrotekz.sync.utils;

import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.hydrotekz.sync.HydroSync;
import net.hydrotekz.sync.net.SocketHandler;
import net.hydrotekz.sync.net.SocketService;

@SuppressWarnings("unchecked")
public class JsonHandler {

	public static JSONObject prepJson(SyncBox syncBox){
		JSONObject json = new JSONObject();

		json.put("ver", HydroSync.version);
		json.put("port", SocketService.port);
		json.put("sync", syncBox.getName());

		return json;
	}

	public static void sendAuth(SyncBox syncBox, Socket socket){
		try {
			JSONObject json = prepJson(syncBox);
			json.put("cmd", "auth");

			SocketHandler.sendMessage(socket, json);
			
		} catch (Exception e){
			Printer.log(e);
		}
	}

	public static JSONObject getJson(String text) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(text);
		return json;
	}
}