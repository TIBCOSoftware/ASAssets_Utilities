package com.cisco.dvbu.ps.utils.date;

import com.compositesw.extension.*;
import java.sql.*;

/*
    TimestampToBigint

    Description:
      Returns a new BIGINT value based on a TIMESTAMP value.
    
    
    Inputs:
      inTimestamp   - The TIMESTAMP value to convert.
        values: Any timestamp value.
    
    
    Outputs:
      result     - The BIGINT result.
        values: A long integer.
    
    Exceptions:
      None
    
    
    Modified Date:  Modified By:      CSW Version:  Reason:
    08/04/2013      Calvin Goodirch   6.2.0         Created new
    
    (c) 2013, 2014 Cisco and/or its affiliates. All rights reserved.

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

public class TimestampToBigint implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private Object result;


    /**
     * This is called once just after constructing the class.  The
     * environment contains methods used to interact with the server.
     */
     @Override
    public void initialize (ExecutionEnvironment qenv) throws SQLException {
        this.qenv = qenv;
    }


    /**
     * Called during introspection to get the description of the input
     * and output parameters.  Should not return null.
     */
    @Override
    public ParameterInfo[] getParameterInfo() {

        return new ParameterInfo[] {
            new ParameterInfo ("inTimestamp", Types.TIMESTAMP, DIRECTION_IN),
            new ParameterInfo ("result", Types.BIGINT, DIRECTION_OUT)
        };
    }


    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
    @Override
    public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException {
        if (inputValues == null)
            throw new CustomProcedureException ("Input values may not be null");
        
        if (inputValues.length != 1)
            throw new CustomProcedureException ("Exactly one input value is required.");
        
        result = new Long (((Timestamp) inputValues[0]).getTime());
    }



    /**
     * Called to retrieve the number of rows that were inserted,
     * updated, or deleted during the execution of the procedure. A
     * return value of -1 indicates that the number of affected rows is
     * unknown.  Can throw CustomProcedureException or SQLException if
     * there is an error when getting the number of affected rows.
     */
    @Override
    public int getNumAffectedRows() {
        return 0;
    }

    /**
     * Called to retrieve the output values.  The returned objects
     * should obey the Java to SQL typing conventions as defined in the
     * table above.  Output cursors can be returned as either
     * CustomCursor or java.sql.ResultSet.  Can throw
     * CustomProcedureException or SQLException if there is an error
     * when getting the output values.  Should not return null.
     */
    @Override
    public Object[] getOutputValues() {

        Object[] outputValues = new Object[1];

        outputValues[0] = result;

        return outputValues;
    }

    /**
     * Called when the procedure reference is no longer needed.  Close
     * may be called without retrieving any of the output values (such
     * as cursors) or even invoking, so this needs to do any remaining
     * cleanup.  Close may be called concurrently with any other call
     * such as "invoke" or "getOutputValues".  In this case, any pending
     * methods should immediately throw a CustomProcedureException.
     */
    @Override
    public void close() throws SQLException {}

    //
    // Introspection methods
    //

    /**
     * Called during introspection to get the short name of the stored
     * procedure.  This name may be overridden during configuration.
     * Should not return null.
     */
    @Override
    public String getName() {
        return "TimestampToBigint";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    @Override
    public String getDescription() {
        return "Converts a TIMESTAMP to a BIGINT.";
    }

    //
    // Transaction methods
    //

    /**
     * Commit any open transactions.
     */
    @Override
    public void commit() throws SQLException {}

    /**
     * Rollback any open transactions.
     */
    @Override
    public void rollback() throws SQLException {}

    /**
     * Returns true if the transaction can be compensated.
     */
    @Override
    public boolean canCompensate() {
        return false;
    }

    @Override
    public boolean canCommit() {
        return false;
    }

    /**
     * Compensate any committed transactions (if supported).
     */
    @Override
    public void compensate (ExecutionEnvironment qenv) throws CustomProcedureException, SQLException {}
}