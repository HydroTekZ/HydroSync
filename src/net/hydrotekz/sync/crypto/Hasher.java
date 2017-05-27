package net.hydrotekz.sync.crypto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Formatter;

import org.apache.commons.codec.digest.DigestUtils;

import net.hydrotekz.sync.utils.Printer;

public class Hasher {

	public static String getFileHash(File file) {
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");

			try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
				final byte[] buffer = new byte[1024];
				for (int read = 0; (read = is.read(buffer)) != -1;) {
					messageDigest.update(buffer, 0, read);
				}
				is.close();
			}

			// Convert the bytes to hex format
			try (Formatter formatter = new Formatter()) {
				for (final byte b : messageDigest.digest()) {
					formatter.format("%02x", b);
				}
				return formatter.toString();
			}
		} catch (Exception e){
			Printer.printError(e);
			Printer.printError("Failed to hash file: " + file.getName());
		}
		return null;
	}

	public static String getStringHash(String content){
		return DigestUtils.sha1Hex(content);
	}
}