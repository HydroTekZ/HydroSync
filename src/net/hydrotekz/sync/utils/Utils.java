package net.hydrotekz.sync.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class Utils {

	public static long getLastModified(File file) throws Exception {
		BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		long lastModified = attr.lastModifiedTime().toMillis();
		return lastModified;
	}
}