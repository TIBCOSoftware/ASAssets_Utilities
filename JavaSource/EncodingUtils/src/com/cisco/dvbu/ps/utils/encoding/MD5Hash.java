/*
MD5Hash:

    Accepts a string as input and returns the MD5 hash value of the string.


	Input:
		inputString - The string to apply the MD5 hash to.  
			Values: Any text value


	Output:
		result - The input string with the MD5 hash applied
			Values: An MD5 hash string.


	Exceptions:  none


	Author:      Gordon Rose
	Date:        10/18/2010
	CSW Version: 5.1.0

    (c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.

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
package com.cisco.dvbu.ps.utils.encoding;

import com.compositesw.extension.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.*;
import java.security.*;

public class MD5Hash implements CustomProcedure {

	private ExecutionEnvironment qenv;
	private String result;

	public MD5Hash() {
	}

	/**
	 * This is called once just after constructing the class. The environment
	 * contains methods used to interact with the server.
	 */
	public void initialize(ExecutionEnvironment qenv) {
		this.qenv = qenv;
	}

	/**
	 * Called during introspection to get the description of the input and
	 * output parameters. Should not return null.
	 */
	public ParameterInfo[] getParameterInfo() {
		return new ParameterInfo[] {
				new ParameterInfo("inputString", Types.VARCHAR, DIRECTION_IN),
				new ParameterInfo("result", Types.VARCHAR, DIRECTION_OUT) };
	}

	/**
	 * Called to invoke the stored procedure. Will only be called a single time
	 * per instance. Can throw CustomProcedureException or SQLException if there
	 * is an error during invoke.
	 */
	public void invoke(Object[] inputValues) throws CustomProcedureException,
			SQLException {
		
		StringBuffer pad = new StringBuffer();
		String inputString = (String)inputValues[0];
		byte[] inputBytes = null;
		try {
			inputBytes = inputString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new CustomProcedureException(e.getMessage());
		}
		
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CustomProcedureException(e.getMessage());
		} 
		m.reset(); 
		m.update(inputBytes); 
		byte[] digest = m.digest(); 
		BigInteger bigInt = new BigInteger(1,digest); 
		
		result = bigInt.toString(16); 
		// -- pad as needed to length of 32 characters
		for (int i = result.length(); i < 32; i++) {
			pad.append("0");
		}
		pad.append(result);
		result = pad.toString();

	}

	/**
	 * Called to retrieve the number of rows that were inserted, updated, or
	 * deleted during the execution of the procedure. A return value of -1
	 * indicates that the number of affected rows is unknown. Can throw
	 * CustomProcedureException or SQLException if there is an error when
	 * getting the number of affected rows.
	 */
	public int getNumAffectedRows() {
		return 0;
	}

	/**
	 * Called to retrieve the output values. The returned objects should obey
	 * the Java to SQL typing conventions as defined in the table above. Output
	 * cursors can be returned as either CustomCursor or java.sql.ResultSet. Can
	 * throw CustomProcedureException or SQLException if there is an error when
	 * getting the output values. Should not return null.
	 */

	public Object[] getOutputValues() {
		return new Object[] { result };
	}

	/**
	 * Called when the procedure reference is no longer needed. Close may be
	 * called without retrieving any of the output values (such as cursors) or
	 * even invoking, so this needs to do any remaining cleanup. Close may be
	 * called concurrently with any other call such as "invoke" or
	 * "getOutputValues". In this case, any pending methods should immediately
	 * throw a CustomProcedureException.
	 */
	public void close() throws SQLException {

	}

	/**
	 * Called during introspection to get the short name of the stored
	 * procedure. This name may be overridden during configuration. Should not
	 * return null.
	 */
	public String getName() {
		return "MD5Hash";
	}

	/**
	 * Called during introspection to get the description of the stored
	 * procedure. Should not return null.
	 */
	public String getDescription() {
		return "This procedure generates an MD5 hash from the input string";
	}

	//
	// Transaction methods
	//
	/**
	 * Returns true if the custom procedure uses transactions. If this method
	 * returns false then commit and rollback will not be called.
	 */
	public boolean canCommit() {
		return false;

	}

	/**
	 * Commit any open transactions.
	 */
	public void commit() {
	}

	/**
	 * Rollback any open transactions.
	 */
	public void rollback() {
	}

	/**
	 * Returns true if the transaction can be compensated.
	 */
	public boolean canCompensate() {
		return false;
	}

	/**
	 * Compensate any committed transactions (if supported).
	 */
	public void compensate(ExecutionEnvironment qenv) {
	}

}
