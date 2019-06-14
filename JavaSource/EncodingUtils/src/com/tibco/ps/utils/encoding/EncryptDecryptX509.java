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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ProcedureConstants;

public class EncryptDecryptX509 {
    
	ExecutionEnvironment env = null ;
	
	public EncryptDecryptX509() {
	}
	public EncryptDecryptX509(ExecutionEnvironment ee) {
		env = ee;
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

    public static final String STRONG_DEFAULT_CIS_KEYSTORE = "C:/MySW/CIS7.0.8/conf/server/security/cis_server_keystore_strong.jks" ;
    public static final String STRONG_DEFAULT_CIS_KEY_ALIAS = "cis_server_strong" ;
    public static final String STRONG_DEFAULT_CIS_PASSWORD = "changeit" ;
    public static final String DEFAULT_CIS_KEYSTORE = "C:/MySW/CIS8.0/conf/server/security/cis_server_keystore.jks" ;
    public static final String DEFAULT_CIS_KEY_ALIAS = "cis_server" ;
    public static final String DEFAULT_CIS_PASSWORD = "changeit" ;
    
	public byte[] encrypt(String message, String password) throws Exception {
/* This code is for testing various scenarios where the output is XML and non-XML.  The XML output is not working.
		// original call using lookup procedure
        String result1 = ServerUtil.getServerAttribute(env, ServerUtil.KEYSTORE_LOCATION_ATTR);
        // alternative call using query
        
		String request = ServerUtil.GET_SERVER_ATTRIBUTES_REQUEST.replace(ServerUtil.SERVER_ATTRIBUTE_TAG, ServerUtil.KEYSTORE_LOCATION_ATTR);
        String result2 = ServerUtil.executeQuery(env, "getServerAttributes", 
        		"/services/webservices/system/admin/server/operations/getServerAttributes", request,null,null,null);
		// alternative call to a custom procedure with XML output
        String result3 = ServerUtil.executeQuery(env, "getBasicResourceXML", 
        		"/shared/ASAssets/Utilities/repository/lowerLevelProcedures/getBasicResourceXML", "/services/databases/NEWDB","DATA_SOURCE",null,null);
        // alternative call to a custom procedure with XML output
        String result4 = ServerUtil.executeQuery(env, "reverseXML", 
        		"/shared/ASAssets/Utilities/\"xml\"/reverseXML", request,null,null,null);
       // alternative call to a custom procedure with string output
        String result5 = ServerUtil.executeQuery(env, "getUtilitiesVersion", 
        		"/shared/ASAssets/Utilities/getUtilitiesVersion", null,null,null,null);
        // alternative call to a custom procedure with string output
        String result6 = ServerUtil.executeQuery(env, "getValueFromXML", 
        		"/shared/ASAssets/Utilities/\"xml\"/getValueFromXML", "N","xmlns:server=\"http://www.compositesw.com/services/system/admin/server\"","/server:getServerAttributes/server:paths/server:path",request);
*/
	  // Get sever version
		String version = ServerUtil.getServerAttributeAS(env, ServerUtil.VERSION);

		String certFile = null;
		if (version.substring(0,1).equalsIgnoreCase("6") || version.substring(0,1).equalsIgnoreCase("7")) {
			// Version 7
			certFile = (env == null ? STRONG_DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttributeAS(env, ServerUtil.STRONG_KEYSTORE_LOCATION_ATTR));
		} else {
			// Version 8
			certFile = (env == null ? DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_LOCATION_ATTR));
		}

		//System.out.println( "EncryptDecryptX509: certFile=" + certFile );
		if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [encrypt] certFile=" + certFile);
		InputStream inStream = new FileInputStream(certFile);
		KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
		keystore.load(inStream, password.toCharArray());

		String alias = null;
		if (version.substring(0,1).equalsIgnoreCase("6") || version.substring(0,1).equalsIgnoreCase("7")) {
			// Version 7
			alias = (env == null ? STRONG_DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttributeAS(env, ServerUtil.STRONG_KEYSTORE_KEY_ALIAS_ATTR));
		} else {
			// Version 8
			alias = (env == null ? DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_KEY_ALIAS_ATTR));				
		}
		
		//System.out.println( "EncryptDecryptX509: alias=" + alias );
		if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [encrypt] alias=" + alias);
        Certificate cert = keystore.getCertificate(alias);
        PublicKey pubKey = cert.getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        //System.out.println( "EncryptDecryptX509: Start encryption using " + cipher.getProvider().getInfo() );
        if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [encrypt] Start encryption using " + cipher.getProvider().getInfo());
        byte[] cipherText = cipher.doFinal(message.getBytes());
        //System.out.println( "EncryptDecryptX509: Finish encryption: [" + hex(cipherText) + "]");
        if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [encrypt] Finish encryption: [" + hex(cipherText) + "]");
        return cipherText ;
	}

	public String decrypt(byte[] message, String password) throws Exception {

		// Get sever version
		String version = ServerUtil.getServerAttributeAS(env, ServerUtil.VERSION);
		
        String certFile = null;
		if (version.substring(0,1).equalsIgnoreCase("6") || version.substring(0,1).equalsIgnoreCase("7")) {
			// Version 7
			certFile = (env == null ? STRONG_DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttributeAS(env, ServerUtil.STRONG_KEYSTORE_LOCATION_ATTR));
		} else {
			// Version 8
			certFile = (env == null ? DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_LOCATION_ATTR));
		}

		//System.out.println( "EncryptDecryptX509: certFile=" + certFile );
		if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [decrypt] certFile=" + certFile);
		InputStream inStream = new FileInputStream(certFile);
		KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
		keystore.load(inStream, password.toCharArray());
		        
		String alias = null;
		if (version.substring(0,1).equalsIgnoreCase("6") || version.substring(0,1).equalsIgnoreCase("7")) {
			// Version 7
			alias = (env == null ? STRONG_DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttributeAS(env, ServerUtil.STRONG_KEYSTORE_KEY_ALIAS_ATTR));
		} else {
			// Version 8
			alias = (env == null ? DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_KEY_ALIAS_ATTR));				
		}

		//System.out.println( "EncryptDecryptX509: alias=" + alias );
		if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [decrypt] alias=" + alias);
		PrivateKey privKey = (PrivateKey)keystore.getKey(alias, password.toCharArray());
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		//System.out.println( "EncryptDecryptX509: Start decryption using " + cipher.getProvider() + " algorithm " + cipher.getAlgorithm() );
		if (env != null) env.log (ProcedureConstants.LOG_DEBUG, "EncryptDecryptX509: [decrypt] Start decryption using " + cipher.getProvider().getInfo());
		byte[] plainText = cipher.doFinal(message);
		//System.out.println( "EncryptDecryptX509: Finish decryption: [" + new String(plainText, "UTF8") + "]");
		if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [decrypt] Finish decryption: [" + new String(plainText, "UTF8") + "]");
		return new String(plainText,"UTF8") ;
	}
	
    
    /* 
     * Encrypt a message using a CIS strong keystore
     * Decrypt the encrypted message back
     */
    public static void main(String [] args){
        String message = args.length > 1 ? (String)args[1] : "This message was encrypted with CIS public key" ;
        String version = "7.0.8"; // Change the version to 7 for TDV 7.x testing.  Change the version to 8 for TDV 8.x testing.
        try {   
    		if (version.substring(0,1).equalsIgnoreCase("6") || version.substring(0,1).equalsIgnoreCase("7")) {
	            System.out.println( "Encrypting:");
	            byte[] cipherText = new EncryptDecryptX509().encrypt(message,STRONG_DEFAULT_CIS_PASSWORD);
	            System.out.println( "Encrypted: [" + hex(cipherText) + "]");
	            System.out.println( "Decrypting:");
	            String plainText = new EncryptDecryptX509().decrypt(cipherText,STRONG_DEFAULT_CIS_PASSWORD);
	            System.out.println( "Decrypted: [" + plainText + "]");
    		} else {
	            System.out.println( "Encrypting:");
	            byte[] cipherText = new EncryptDecryptX509().encrypt(message,DEFAULT_CIS_PASSWORD);
	            System.out.println( "Encrypted: [" + hex(cipherText) + "]");
	            System.out.println( "Decrypting:");
	            String plainText = new EncryptDecryptX509().decrypt(cipherText,DEFAULT_CIS_PASSWORD);
	            System.out.println( "Decrypted: [" + plainText + "]");
    		}
        } catch (Throwable t) {
            System.out.println( "BOOM!!! " + t );
            t.printStackTrace(System.err);
        }
   }
 
}
