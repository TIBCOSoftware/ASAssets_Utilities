package com.cisco.dvbu.ps.utils.date;

import com.compositesw.extension.*;
import java.sql.*;

/*
    BigintToTimestamp

    Description:
      Returns a new TIMESTAMP value based on a BIGINT value.
    
    
    Inputs:
      inBigint   - The BIGINT value to convert.
        values: Any long integer
    
    
    Outputs:
      result     - The Timestamp result.
        values: A timestamp
    
    Exceptions:
      None
    
    
    Modified Date:  Modified By:      CSW Version:  Reason:
    08/04/2013      Calvin Goodirch   6.2.0         Created new
    
    (c) 2013, 2014 Cisco and/or its affiliates. All rights reserved.
*/

public class BigintToTimestamp implements CustomProcedure {

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
            new ParameterInfo ("inBigint", Types.BIGINT, DIRECTION_IN),
            new ParameterInfo ("result", Types.TIMESTAMP, DIRECTION_OUT)
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
        
        result = new Timestamp ((Long) inputValues[0]);
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
        return "BigintToTimestamp";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    @Override
    public String getDescription() {
        return "Converts a BIGINT to a TIMESTAMP.";
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