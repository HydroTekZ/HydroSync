package net.hydrotekz.sync.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Printer {

	private static void printMessage(String message) {
		if (message == null) return;
		logMessage(message);
		System.out.println(getClock() + ": " + message);
	}

	public static void printInfo(String message){
		printMessage("[INFO] " + message);
	}

	public static void printDebug(String message){
		printMessage("[DEBUG] " + message);
	}

	public static void printWarning(String message){
		printMessage("[WARNING] " + message);
	}

	public static void logDebug(String message){
		logMessage("[DEBUG] " + message);
	}
	
	public static void logError(String message){
		logMessage("[ERROR] " + message);
	}

	public static void printError(String message){
		printMessage("[ERROR] " + message);
	}

	public static void logMessage(String message) {
		if (message == null) return;
		try {
			if (message.length() < 1) return;

			File file = new File(System.getProperty("user.dir") + File.separator + "Logs");
			if (!file.exists()) {
				file.mkdirs();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter("Logs" + File.separator + getDate() + ".log", true));
			out.write(getClock() + ": " + message);
			out.newLine();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logError(Exception e){
		logMessage(getErrorText(e));
		String error = e.toString();
		if (error.contains(".")){
			String[] split = error.split("\\.");
			error = split[split.length-1];
		}
	}

	public static void printError(Exception e){
		printMessage(getErrorText(e));
		String error = e.toString();
		if (error.contains(".")){
			String[] split = error.split("\\.");
			error = split[split.length-1];
		}
	}

	private static String getErrorText(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return(sw.toString());
	}

	private static String getDate(){
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}

	private static String getClock(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
}