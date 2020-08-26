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


/*
EncryptWithAES() 

    Description:
        Encrypts a string using AES encryption (in CBC mode). 

    Input:
        "plain text" - The string to encrypt
            Values - Any text string

        "key" - The key to use for encryption.
            Values - 16-, 24- or 32-byte hexadecimal representation of a key (for CBC)

		"IV" - initialization vector to use in AES. 
				If not provided, will be generated from the key. Simple, but not secure and not recommended.

    Output:
        "encrypted raw bytes" - The encrypted raw bytes.
            Values - A byte array

        "encrypted hex string" - The encrypted string in Base64 encoding.
            Values - Any text string


    Exceptions:  none


    Modified Date:  Modified By:        CSW Version:    Reason:
    01/20/2016      Alex Dedov          7.0.1           Created new

 */

import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;
//import javax.xml.bind.DatatypeConverter;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class EncryptWithAES extends EncodingUtilTemplate {

	private byte[] encMessage = null;
	private String hexString = null;

	@Override
	public String getName() {
		procName = getClass().getSimpleName() ;
		return procName ;
	}

	@Override
	public String getDescription() {
		return "Encrypts input using AES algorithm. Returns binary representation along with its base-64 representation";
	}
	@Override
    public ParameterInfo[] getParameterInfo() {
    	return new ParameterInfo[] {
    			new ParameterInfo("plain text", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("key", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("IV", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("encrypted raw bytes", Types.VARBINARY, DIRECTION_OUT),
    			new ParameterInfo("encrypted string (base-64)", Types.VARCHAR, DIRECTION_OUT)
    	} ;
    }
	@Override
	public Object[] getOutputValues() throws CustomProcedureException, SQLException {
		return new Object[] { encMessage, hexString };
	}
	 	
	@Override
	public int execute(Object[] args) throws Exception {
		String plainText = args.length > 0 ? (String)args[0] : null ;
		String aesKey = (args.length > 1 && args[1] != null) ? (String)args[1] : "1234567812345678" ; 
		String aesIV = (args.length > 2 && args[2] != null) ? (String)args[2] : String.format("%08X%08X",aesKey.substring(0,8).hashCode(),aesKey.substring(8).hashCode()) ;

		encMessage = new EncryptDecryptAES().encrypt(plainText,aesKey.getBytes(),aesIV) ;
//MODIFIED
		hexString =  Base64.getEncoder().encodeToString(encMessage);
//		hexString = DatatypeConverter.printBase64Binary(encMessage) ;
		return 0 ;
	}
	
}
