package com.cisco.dvbu.ps.utils.encoding;

/*
    © 2011, 2014 Cisco and/or its affiliates. All rights reserved.
*/

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

import com.compositesw.extension.ExecutionEnvironment;

public class EncryptDecryptX509 {
    
	ExecutionEnvironment env = null ;
	
	public EncryptDecryptX509() {
	}
	public EncryptDecryptX509(ExecutionEnvironment ee) {
		env = ee ;
	}
	
    /* 
     * Convert into hex values
     */
    private static final String HEX_STRING = "0123456789ABCDEF";
    public static String hex(byte[]  bin) {
        StringBuilder newStr = new StringBuilder();
        try {
            for(int k=0; k < bin.length; k++ ){
                newStr.append(HEX_STRING.charAt(( bin[k] >> 4 )&0xF)).append(HEX_STRING.charAt(bin[k]&0xF));
            }   
        } catch (Throwable t) {
            System.out.println("Failed to convert byte[] to hex values: " + t);
        } 
        return newStr.toString() ;
    }
    /* 
     * Convert from hex values
     */
    public static byte[] bytes(String s) {
        int l = s.length();
        byte[] data = new byte[l/2];
        try {
        	for (int i = 0; i < l; i += 2) {
        		data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        	}
        }
        catch(Throwable t) {
            System.err.println("Failed to convert hex string to byte[]: " + t);
        }
        return data;
    }

    public static final String DEFAULT_CIS_KEYSTORE = "C:/CIS624/conf/server/security/cis_server_keystore_strong.jks" ;
    public static final String DEFAULT_CIS_KEY_ALIAS = "cis_server_strong" ;
    public static final String DEFAULT_CIS_PASSWORD = "changeit" ;
    
	public byte[] encrypt(String message, String password) throws Exception {
        String certFile = (env == null ? DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttribute(env, ServerUtil.KEYSTORE_LOCATION_ATTR));
        InputStream inStream = new FileInputStream(certFile);
        KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
        keystore.load(inStream, password.toCharArray());
        String alias = (env == null ? DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttribute(env, ServerUtil.KEYSTORE_KEY_ALIAS_ATTR));
        Certificate cert = keystore.getCertificate(alias);
        PublicKey pubKey = cert.getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
//          System.out.println( "Start encryption using " + cipher.getProvider().getInfo() );
        byte[] cipherText = cipher.doFinal(message.getBytes());
//          System.out.println( "Finish encryption: [" + hex(cipherText) + "]");
        return cipherText ;
	}

	public String decrypt(byte[] message, String password) throws Exception {
        String certFile = (env == null ? DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttribute(env, ServerUtil.KEYSTORE_LOCATION_ATTR));
        InputStream inStream = new FileInputStream(certFile);
        KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
        keystore.load(inStream, password.toCharArray());
        String alias = (env == null ? DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttribute(env, ServerUtil.KEYSTORE_KEY_ALIAS_ATTR));
        PrivateKey privKey = (PrivateKey)keystore.getKey(alias, password.toCharArray());
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
//      System.out.println( "Start decryption using " + cipher.getProvider() + " algorithm " + cipher.getAlgorithm() );
        byte[] plainText = cipher.doFinal(message);
//      System.out.println( "Finish decryption: [" + new String(plainText, "UTF8") + "]");
        return new String(plainText,"UTF8") ;
	}
	
    
    /* 
     * Encrypt a message using a CIS strong keystore
     * Decrypt the encrypted message back
     */
    public static void main(String [] args){
        String message = args.length > 1 ? (String)args[1] : "This message was encrypted with CIS public key" ;
        try {   
            byte[] cipherText = new EncryptDecryptX509().encrypt(message,DEFAULT_CIS_PASSWORD);
            System.out.println( "Encrypted: [" + hex(cipherText) + "]");
            String plainText = new EncryptDecryptX509().decrypt(cipherText,DEFAULT_CIS_PASSWORD);
            System.out.println( "Decrypted: [" + plainText + "]");

        } catch (Throwable t) {
            System.out.println( "BOOM!!! " + t );
            t.printStackTrace(System.err);
        }
   }
 
}
