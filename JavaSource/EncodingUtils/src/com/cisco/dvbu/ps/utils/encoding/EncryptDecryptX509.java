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

    public static final String DEFAULT_CIS_KEYSTORE = "C:/CiscoSystems/CIS7.0.5/conf/server/security/cis_server_keystore_strong.jks" ;
    public static final String DEFAULT_CIS_KEY_ALIAS = "cis_server_strong" ;
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
        
      	String certFile = (env == null ? DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_LOCATION_ATTR));
      if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [encrypt] certFile=" + certFile);
        
      //System.out.println( "EncryptDecryptX509: certFile=" + certFile );
      if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [encrypt] certFile=" + certFile);
        InputStream inStream = new FileInputStream(certFile);
        KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
        keystore.load(inStream, password.toCharArray());
        String alias = (env == null ? DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_KEY_ALIAS_ATTR));
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

        String certFile = (env == null ? DEFAULT_CIS_KEYSTORE : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_LOCATION_ATTR));
      //System.out.println( "EncryptDecryptX509: certFile=" + certFile );
      if (env != null) env.log (ServerUtil.LOG_TYPE, "EncryptDecryptX509: [decrypt] certFile=" + certFile);
        InputStream inStream = new FileInputStream(certFile);
        KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
        keystore.load(inStream, password.toCharArray());
        String alias = (env == null ? DEFAULT_CIS_KEY_ALIAS : ServerUtil.getServerAttributeAS(env, ServerUtil.KEYSTORE_KEY_ALIAS_ATTR));
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
