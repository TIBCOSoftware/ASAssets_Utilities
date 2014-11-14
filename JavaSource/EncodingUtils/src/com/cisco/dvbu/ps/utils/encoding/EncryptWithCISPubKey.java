package com.cisco.dvbu.ps.utils.encoding;

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

    © 2014 Cisco and/or its affiliates. All rights reserved.
 */
import java.sql.SQLException;
import java.sql.Types;

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
	
}
