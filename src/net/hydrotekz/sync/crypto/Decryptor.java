package net.hydrotekz.sync.crypto;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import net.hydrotekz.sync.utils.SyncBox;

public class Decryptor {

	public static String decryptText(SyncBox syncBox, String base64) throws Exception {
		byte[] encrypted = Base64.decodeBase64(base64);
		byte[] decrypted = decrypt(syncBox, encrypted);
		String text = new String(decrypted, "UTF-8");
		return text;
	}

	public static byte[] decrypt(SyncBox syncBox, byte[] bytes) throws Exception {
		String key = syncBox.getKey();
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");

		// Decrypt data
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, aesKey);
		byte[] data = cipher.doFinal(bytes);

		return data;
	}
}