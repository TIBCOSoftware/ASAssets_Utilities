package com.cisco.dvbu.ps.utils.encoding;

/*
© 2011, 2014 Cisco and/or its affiliates. All rights reserved.

This software is released under the Eclipse Public License. The details can be found in the file LICENSE. 
Any dependent libraries supplied by third parties are provided under their own open source licenses as 
described in their own LICENSE files, generally named .LICENSE.txt. The libraries supplied by Cisco as 
part of the Composite Information Server/Cisco Data Virtualization Server, particularly csadmin-XXXX.jar, 
csarchive-XXXX.jar, csbase-XXXX.jar, csclient-XXXX.jar, cscommon-XXXX.jar, csext-XXXX.jar, csjdbc-XXXX.jar, 
csserverutil-XXXX.jar, csserver-XXXX.jar, cswebapi-XXXX.jar, and customproc-XXXX.jar (where -XXXX is an 
optional version number) are provided as a convenience, but are covered under the licensing for the 
Composite Information Server/Cisco Data Virtualization Server. They cannot be used in any way except 
through a valid license for that product.

This software is released AS-IS!. Support for this software is not covered by standard maintenance agreements with Cisco. 
Any support for this software by Cisco would be covered by paid consulting agreements, and would be billable work.

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
