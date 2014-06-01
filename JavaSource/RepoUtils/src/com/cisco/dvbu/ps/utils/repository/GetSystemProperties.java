package com.cisco.dvbu.ps.utils.repository;

/*
	Description:
	    Returns all the properties defined in the JVM running the current instance of CIS.
	
	Inputs:
	    N/A
	
	Outputs:
	    result (   - A cursor containing the list of properties and their values
	        Property - The property
	        Value    - The property's value
	    )
	
	Exceptions:
	    None
	
	Author:      Calvin Goodrich
	Date:        6/3/2011
	CSW Version: 5.1.0
	
	2011, 2014 Cisco and/or its affiliates. All rights reserved.
*/

import com.compositesw.extension.*;

import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Enumeration;

public class GetSystemProperties implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private ArrayList valueList;
    private CustomCursor outputCursor;
    private String cjpName = "GetSystemProperties";

    public GetSystemProperties() {
    }

    /**
     * This is called once just after constructing the class.  The
     * environment contains methods used to interact with the server.
     */
    public void initialize (ExecutionEnvironment qenv) throws SQLException {
        this.qenv = qenv;
    }

    /**
     * Called during introspection to get the description of the input
     * and output parameters.  Should not return null.
     */
    public ParameterInfo[] getParameterInfo() {
        return new ParameterInfo[] { 
            new ParameterInfo ("result", TYPED_CURSOR, DIRECTION_OUT,
                new ParameterInfo[] { 
                    new ParameterInfo ("Property", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("Value", Types.VARCHAR, DIRECTION_NONE),
                }
            ) 
        };
    }

    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
    public void invoke (Object[] inputValues) throws CustomProcedureException, SQLException {
        ArrayList valueRow;

        valueList = new ArrayList();

        for (Enumeration e = System.getProperties().keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            valueRow = new ArrayList();
            String v = System.getProperties().getProperty(key);
            valueRow.add(key);
            valueRow.add (v);
            valueList.add (valueRow);
        }

        outputCursor = createCustomCursor();
    }

    /**
     * Called to retrieve the number of rows that were inserted,
     * updated, or deleted during the execution of the procedure. A
     * return value of -1 indicates that the number of affected rows is
     * unknown.  Can throw CustomProcedureException or SQLException if
     * there is an error when getting the number of affected rows.
     */
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
    public Object[] getOutputValues() {
        return new Object[] { outputCursor };
    }

    /**
     * Create a custom cursor output.
     */
    private CustomCursor createCustomCursor() {
        return new CustomCursor() {

            private int counter = 0;

            public ParameterInfo[] getColumnInfo () {
                return new ParameterInfo[] { new ParameterInfo ("Property", Types.VARCHAR, DIRECTION_NONE),
                                             new ParameterInfo ("Value", Types.VARCHAR, DIRECTION_NONE) };
            }

            public Object[] next() throws CustomProcedureException, SQLException {
                if (counter >= valueList.size ()) {
                    return null;
                } else {
                    ArrayList groupRow = (ArrayList) valueList.get(counter++);
                    return new Object[] { (String) groupRow.get (0),  (String) groupRow.get (1)};
                }
            }

            public void close() throws CustomProcedureException, SQLException {
                // do nothing
            }
        };
    }

    /**
     * Called when the procedure reference is no longer needed.  Close
     * may be called without retrieving any of the output values (such
     * as cursors) or even invoking, so this needs to do any remaining
     * cleanup.  Close may be called concurrently with any other call
     * such as "invoke" or "getOutputValues".  In this case, any pending
     * methods should immediately throw a CustomProcedureException.
     */
    public void close() throws CustomProcedureException, SQLException {
        if (outputCursor != null)
            outputCursor.close ();
    }

    //
    // Introspection methods
    //

    /**
     * Called during introspection to get the short name of the stored
     * procedure.  This name may be overridden during configuration.
     * Should not return null.
     */
    public String getName() {
        return "GetSystemProperties";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        return "Returns all the properties defined in the JVM running the current instance of CIS.";
    }

    //
    // Transaction methods
    //

    /**
     * Returns true if the custom procedure uses transactions.  If this
     * method returns false then commit and rollback will not be called.
     */
    public boolean canCommit() {
        return false;
    }

    /**
     * Commit any open transactions.
     */
    public void commit() throws SQLException {
    }

    /**
     * Rollback any open transactions.
     */
    public void rollback() throws SQLException {
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
    public void compensate (ExecutionEnvironment qenv) throws SQLException {
    }
}
