package net.hydrotekz.sync.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Random;

public class Utils {

	public static long getLastModified(File file) throws Exception {
		BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		long lastModified = attr.lastModifiedTime().toMillis();
		return lastModified;
	}

	public static int getRandomNumber(int start, int stop){
		Random r = new Random();
		int Low = start;
		int High = stop;
		int R = r.nextInt(High-Low) + Low;
		return R;
	}

	public static void setLastModified(File file, long lastModified) throws Exception {
		Path path = file.toPath();

		BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

		BasicFileAttributeView attributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
		FileTime modified = FileTime.fromMillis(lastModified);
		FileTime access = FileTime.fromMillis(attr.lastAccessTime().toMillis());
		FileTime creation = FileTime.fromMillis(attr.creationTime().toMillis());
		attributes.setTimes(modified, access, creation);
	}
}