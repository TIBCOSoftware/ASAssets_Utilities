package com.cisco.dvbu.ps.utils.encoding;

/*
    © 2011, 2014 Cisco and/or its affiliates. All rights reserved.
*/

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt3DES {

	public byte[] encrypt(String message, byte[] seed) throws Exception {
		MessageDigest md = MessageDigest.getInstance("md5");
		byte[] digest = md.digest(seed);
		byte[] keyBytes = Arrays.copyOf(digest, 24);
		for (int j = 0, k = 16; j < 8;) {
			keyBytes[k++] = keyBytes[j++];
		}

		SecretKey key = new SecretKeySpec(keyBytes, "DESede");
		IvParameterSpec iv = new IvParameterSpec(new byte[8]);
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);

		byte[] plainTextBytes = message.getBytes("utf-8");
		byte[] cipherText = cipher.doFinal(plainTextBytes);

		return cipherText;
	}

	public String decrypt(byte[] message, byte[] seed) throws Exception {
		MessageDigest md = MessageDigest.getInstance("md5");
		byte[] digest = md.digest(seed);
		byte[] keyBytes = Arrays.copyOf(digest, 24);
		for (int j = 0, k = 16; j < 8;) {
			keyBytes[k++] = keyBytes[j++];
		}

		SecretKey key = new SecretKeySpec(keyBytes, "DESede");
		IvParameterSpec iv = new IvParameterSpec(new byte[8]);
		Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		decipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] plainText = decipher.doFinal(message);

		return new String(plainText, "UTF-8");
	}

	public static void main(String[] args) throws Exception {

		String text = args.length > 0 ? args[0]
				: "Test Composite CJP to encrypt / decrypt text using Triple DES";
		String seed = args.length > 1 ? args[1] : "ZKZH1W7";

		byte[] codedtext = new EncryptDecrypt3DES().encrypt(text,seed.getBytes("utf-8"));
		System.out.println("Encrypted:" + new String(codedtext, "utf-8"));

		String decodedtext = new EncryptDecrypt3DES().decrypt(codedtext,seed.getBytes("utf-8"));
		System.out.println("Decrypted:" + decodedtext);
	}

}
