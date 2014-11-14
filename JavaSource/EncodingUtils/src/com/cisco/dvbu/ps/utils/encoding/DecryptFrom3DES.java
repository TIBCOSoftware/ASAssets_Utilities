package com.cisco.dvbu.ps.utils.encoding;

/*
DecryptFrom3DES() 

    Description:
        Decrypts a symmetrical Triple DES encrypted string. 

    Input:
        "encrypted hex string" - The encrypted string to decrypt.
            Values - Any text string

        "digest seed" - The seed that was used for encryption.
            Values - Any text string


    Output:
        "plain text" - The decrypted string
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

public class DecryptFrom3DES extends EncodingUtilTemplate {

	private String decMessage = null;

	@Override
	public String getName() {
		procName = getClass().getSimpleName() ;
		return procName ;
	}

	@Override
	public String getDescription() {
		return "Tries to decrypt input (a hexadecimal representation of bin array) using Triple DES algorithm";
	}
	@Override
    public ParameterInfo[] getParameterInfo() {
    	return new ParameterInfo[] {
    			new ParameterInfo("encrypted hex string", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("digest seed", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("plain text", Types.VARCHAR, DIRECTION_OUT)
    	} ;
    }
	@Override
	public Object[] getOutputValues() throws CustomProcedureException, SQLException {
		return new Object[] { decMessage };
	}
	 	
	@Override
	public int execute(Object[] args) throws Exception {
		String encText = args.length > 0 ? (String)args[0] : null ;
		String mdInput = (args.length > 1 && args[1] != null) ? (String)args[1] : "ZKZH1W7" ;
		byte[] bytes = EncryptDecryptX509.bytes(encText) ;
		decMessage = new String(new EncryptDecrypt3DES().decrypt(bytes,mdInput.getBytes())) ;
		return 0 ;
	}
	
}
