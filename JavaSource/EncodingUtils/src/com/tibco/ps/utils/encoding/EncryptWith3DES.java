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
    01/20/2016		Alex Dedov			7.0.1			Updated to use different hex binary converter

 */

import java.sql.SQLException;
import java.sql.Types;

import javax.xml.bind.DatatypeConverter;

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
		hexString = DatatypeConverter.printHexBinary(encMessage) ;
		return 0 ;
	}
	
}
