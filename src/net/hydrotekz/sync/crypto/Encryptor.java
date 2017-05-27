package net.hydrotekz.sync.crypto;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import net.hydrotekz.sync.utils.SyncBox;

public class Encryptor {

	public static String encryptText(SyncBox syncBox, String text) throws Exception {
		byte[] bytes = text.getBytes("UTF-8");
		byte[] encrypted = encrypt(syncBox, bytes);
		String base64 = Base64.encodeBase64String(encrypted);
		return base64;
	}

	public static byte[] encrypt(SyncBox syncBox, byte[] bytes) throws Exception {
		String key = syncBox.getKey();
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");

		// Encrypt data
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey);
		byte[] data = cipher.doFinal(bytes);

		return data;
	}
}