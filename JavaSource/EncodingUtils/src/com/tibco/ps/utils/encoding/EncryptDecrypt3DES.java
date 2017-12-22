package com.tibco.ps.utils.encoding;

/**
 * (c) 2017 TIBCO Software Inc. All rights reserved.
 * 
 * Except as specified below, this software is licensed pursuant to the Eclipse Public License v. 1.0.
 * The details can be found in the file LICENSE.
 * 
 * The following proprietary files are included as a convenience, and may not be used except pursuant
 * to valid license to Composite Information Server or TIBCO(R) Data Virtualization Server:
 * csadmin-XXXX.jar, csarchive-XXXX.jar, csbase-XXXX.jar, csclient-XXXX.jar, cscommon-XXXX.jar,
 * csext-XXXX.jar, csjdbc-XXXX.jar, csserverutil-XXXX.jar, csserver-XXXX.jar, cswebapi-XXXX.jar,
 * and customproc-XXXX.jar (where -XXXX is an optional version number).  Any included third party files
 * are licensed under the terms contained in their own accompanying LICENSE files, generally named .LICENSE.txt.
 * 
 * This software is licensed AS-IS. Support for this software is not covered by standard maintenance agreements with TIBCO.
 * If you would like to obtain assistance with this software, such assistance may be obtained through a separate paid consulting
 * agreement with TIBCO.
 * 
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
