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

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class EncryptDecryptAES {

    public byte[] encrypt(String value, byte[] key, String initVector) throws Exception {
    	IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
    	SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    	cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

    	byte[] encrypted = cipher.doFinal(value.getBytes());
    	return encrypted;
    }

    public String decrypt(byte[] encrypted, byte[] key, String initVector) throws Exception {
    	IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
    	SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

    	Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    	decipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

    	byte[] plainText = decipher.doFinal(encrypted);
    	return new String(plainText,"UTF-8");
    }
	
	
	public static void main(String[] args) throws Exception {
		String text = args.length > 0 ? args[0]
				: "Test Composite CJP to encrypt / decrypt text using AES";
		String seed = args.length > 1 ? args[1] : "12345678123456781234567812345678";
//		String initVector = "RandomInitVector"; // must be 16 bytes
		String initVector = args.length > 2 ? args[2] : new BigInteger(80, new SecureRandom(seed.getBytes())).toString(32) ; 
		System.out.println("AES Key:" + seed + "\nAES IV: " + initVector);
				
		byte[] codedtext = new EncryptDecryptAES().encrypt(text,seed.getBytes("utf-8"),initVector);
		System.out.println("Encrypted:" + new String(codedtext, "utf-8"));
		String display = DatatypeConverter.printBase64Binary(codedtext) ;
		System.out.println("Encrypted:" + display) ;
		String decodedtext = new EncryptDecryptAES().decrypt(DatatypeConverter.parseBase64Binary(display),seed.getBytes("utf-8"),initVector);
		System.out.println("Decrypted:" + decodedtext);	
		display = DatatypeConverter.printHexBinary(codedtext) ;
		System.out.println("Encrypted:" + display) ;
		decodedtext = new EncryptDecryptAES().decrypt(DatatypeConverter.parseHexBinary(display),seed.getBytes("utf-8"),initVector);
		System.out.println("Decrypted:" + decodedtext);
	}

}
