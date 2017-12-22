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
EncryptWithCISPubKey() 

    Description:
        Encrypts a string using CIS's built in SSL certificate. 

    Input:
        "plain text" - The string to encrypt
            Values - Any text string

        "keystore password" - The password of the CIS SSL keystore.
            Values - Any text string


    Output:
        "encrypted raw bytes" - The encrypted raw bytes.
            Values - A byte array

        "encrypted hex string" - The encrypted string.
            Values - Any text string


    Exceptions:  none


    Modified Date:  Modified By:        CSW Version:    Reason:
    10/27/2014      Alex Dedov          6.2.6           Created new

 */

import java.sql.SQLException;
import java.sql.Types;

import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class EncryptWithCISPubKey extends EncodingUtilTemplate {

	private byte[] encMessage = null;
	private String hexString = null;

	@Override
	public String getName() {
		procName = getClass().getSimpleName() ;
		return procName ;
	}

	@Override
	public String getDescription() {
		return "Encrypts input using CIS Public Key (from CIS keystore file)";
	}
	@Override
    public ParameterInfo[] getParameterInfo() {
    	return new ParameterInfo[] {
    			new ParameterInfo("plain text", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("keystore password", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("encrypted raw bytes", Types.VARBINARY, DIRECTION_OUT),
    			new ParameterInfo("encrypted hex string", Types.VARCHAR, DIRECTION_OUT)
    	} ;
    }
	@Override
	public Object[] getOutputValues() throws CustomProcedureException, SQLException {
		return new Object[] { encMessage, hexString };
	}
	 	
	@Override
	public int execute(Object[] args) throws Exception {
		String plainText = args.length > 0 ? (String)args[0] : null ;
		String password = args.length > 1 && args[1].toString().trim().length() > 0 ? (String)args[1] : EncryptDecryptX509.DEFAULT_CIS_PASSWORD ;

		encMessage = new EncryptDecryptX509(cjpenv).encrypt(plainText,password) ;
		hexString = EncryptDecryptX509.hex(encMessage) ;
		return 0 ;
	}

	public static void main(String[] args) {
		
		CustomProcedure cp = new EncryptWithCISPubKey();
		/*
		 * Parameters:
    			new ParameterInfo("plain text", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("keystore password", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("encrypted raw bytes", Types.VARBINARY, DIRECTION_OUT),
    			new ParameterInfo("encrypted hex string", Types.VARCHAR, DIRECTION_OUT)
		 */
		String plain_string = "This is a plain text string"; 
		String keystore_password = "changeit";

        try {
	        cp.initialize(null);
	        System.out.println("invoke "+cp.getName());
	        cp.invoke(new Object[] {
	        		new String (plain_string),
	        		new String (keystore_password)
	        });
       
	        String result = cp.getOutputValues()[1].toString();
	        System.out.println("EncryptWithCISPubKey Result:");
	        System.out.println(result);
	        
		} catch (Exception ex) {
			System.out.print(ex.toString());
		}
	}
}
