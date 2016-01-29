package com.cisco.dvbu.ps.utils.encoding;

/*
DecryptFromAES() 

    Description:
        Decrypts an AES-encrypted string. 

    Input:
        "encrypted hex string" - The encrypted *Base64-encoded* string to decrypt.
            Values - Any text string

        "key" - The key that was used for encryption.
            Values - 16-, 24- or 32-byte text string

        "IV" - Initialization vector used for encryption.


    Output:
        "plain text" - The decrypted string
            Values - Any text string


    Exceptions:  none


    Modified Date:  Modified By:        CSW Version:    Reason:
    01/20/2016      Alex Dedov          7.0.1           Created new

    © 2014 Cisco and/or its affiliates. All rights reserved.

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

import java.sql.SQLException;
import java.sql.Types;

import javax.xml.bind.DatatypeConverter;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class DecryptFromAES extends EncodingUtilTemplate {

	private String decMessage = null;

	@Override
	public String getName() {
		procName = getClass().getSimpleName() ;
		return procName ;
	}

	@Override
	public String getDescription() {
		return "Tries to decrypt input (a base-64 representation of bin array) using AES algorithm";
	}
	@Override
    public ParameterInfo[] getParameterInfo() {
    	return new ParameterInfo[] {
    			new ParameterInfo("encrypted hex string", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("key", Types.VARCHAR, DIRECTION_IN),
    			new ParameterInfo("IV", Types.VARCHAR, DIRECTION_IN),
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
		String aesKey = (args.length > 1 && args[1] != null) ? (String)args[1] : "1234567812345678" ;
		String aesIV = (args.length > 2 && args[2] != null) ? (String)args[2] : String.format("%08X%08X",aesKey.substring(0,8).hashCode(),aesKey.substring(8).hashCode()) ;

		byte[] bytes = DatatypeConverter.parseBase64Binary(encText) ;

		decMessage = new String(new EncryptDecryptAES().decrypt(bytes,aesKey.getBytes(),aesIV)) ;
		return 0 ;
	}
	
}
