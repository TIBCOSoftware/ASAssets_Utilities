package com.cisco.dvbu.ps.utils.encoding;

/*
EncryptWith3DES() 

    Description:
        Encrypts a string using symmetrical Triple DES encryption. 

    Input:
        "plain text" - The string to encrypt
            Values - Any text string

        "digest seed" - The seed to use for encryption.
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

public class EncryptWith3DES extends EncodingUtilTemplate {

	private byte[] encMessage = null;
	private String hexString = null;

	@Override
	public String getName() {
		procName = getClass().getSimpleName() ;
		return procName ;
	}

	@Override
	public String getDescription() {
		return "Encrypts input using Triple DES algorithm";
	}
	@Override
    public ParameterInfo[] getParameterInfo() {
    	return new ParameterInfo[] {
    			new ParameterInfo("plain text", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("digest seed", Types.VARCHAR, DIRECTION_IN),
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
		String mdInput = (args.length > 1 && args[1] != null) ? (String)args[1] : "ZKZH1W7" ;
		encMessage = new EncryptDecrypt3DES().encrypt(plainText,mdInput.getBytes()) ;
		hexString = EncryptDecryptX509.hex(encMessage) ;
		return 0 ;
	}
	
}
