/*
Base64Decode:

    Accepts a Base64 encoded string as input and returns the Base64 decoded value of the string.

	Input:
		base64EncodedString - The base64 encoded value to decode.  
			Values: Any Base64 value


	Output:
		result - The Base64 decoded value
			Values: Any text value


	Exceptions:  none


	Author:      Gordon Rose
	Date:        10/18/2010
	CSW Version: 5.1.0

    (c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.
*/
package com.cisco.dvbu.ps.utils.encoding;

import java.sql.SQLException;
import java.sql.Types;
import com.compositesw.extension.*;

public class Base64Decode implements CustomProcedure {

    private ExecutionEnvironment qenv;
	private String result;

	public Base64Decode() {
	}

	/**
	 * Called during introspection to get the short name of the stored
	 * procedure. This name may be overridden during configuration. Should not
	 * return null.
	 */
	public String getName() {
		return "Base64Decode";
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
				new ParameterInfo("base64EncodedString", Types.VARCHAR, DIRECTION_IN),
				new ParameterInfo("result", Types.VARCHAR, DIRECTION_OUT) };
	}

	/**
	 * Called to invoke the stored procedure. Will only be called a single time
	 * per instance. Can throw CustomProcedureException or SQLException if there
	 * is an error during invoke.
	 */
	public void invoke(Object[] inputValues) throws CustomProcedureException,
			SQLException {
		
		result = Base64EncodeDecodeHelper.decodeString((String)inputValues[0]);

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
