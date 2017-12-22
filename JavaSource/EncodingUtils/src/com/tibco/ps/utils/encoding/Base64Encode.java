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
Base64Encode:

    Accepts a string as input and returns the Base64 encoded value of the string.


	Input:
		inputString - The text to encode.  
			Values: Any text value


	Output:
		result - The input text encoded using Base64 encoding
			Values: Any Base64 value


	Exceptions:  none


	Author:      Gordon Rose
	Date:        10/18/2010
	CSW Version: 5.1.0

*/

import java.sql.SQLException;
import java.sql.Types;
import com.compositesw.extension.*;

public class Base64Encode implements CustomProcedure {

	private ExecutionEnvironment qenv;
	private String result;

	public Base64Encode() {
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
		
		result = Base64EncodeDecodeHelper.encodeString((String)inputValues[0]);

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

	//
	// Introspection methods
	//
	/**
	 * Called during introspection to get the short name of the stored
	 * procedure. This name may be overridden during configuration. Should not
	 * return null.
	 */
	public String getName() {
		return "Base64Encode";
	}

	/**
	 * Called during introspection to get the description of the stored
	 * procedure. Should not return null.
	 */
	public String getDescription() {
		return "This procedure adds ten to the supplied integer parameter";
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
