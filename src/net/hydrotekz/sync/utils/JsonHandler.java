package net.hydrotekz.sync.utils;

import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.hydrotekz.sync.MainCore;
import net.hydrotekz.sync.net.SocketHandler;
import net.hydrotekz.sync.net.SocketService;

@SuppressWarnings("unchecked")
public class JsonHandler {

	public static JSONObject prepJson(SyncBox syncBox){
		JSONObject json = new JSONObject();

		json.put("sync", syncBox.getName());

		return json;
	}

	public static void sendAuth(Socket socket){
		try {
			JSONObject json = new JSONObject();
			json.put("cmd", "auth");
			json.put("ver", MainCore.version);
			json.put("port", SocketService.port);

			SocketHandler.sendMessage(socket, json);

		} catch (Exception e){
			Printer.logError(e);
			Printer.printError("Failed to authenticate!");
		}
	}

	public static JSONObject getJson(String text) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(text);
		return json;
	}
}