package com.cisco.dvbu.ps.utils.repository;

/*
    UpdateDsColumnAnnotation:

    This procedure is used to update annotations for data source table columns (for other types of
    tables/views, please use the updateSqlTable() admin API.)

    Input:
        column_path - The path to the data source column.
        	Values: A CIS path

        annotation -  The text of the annotation.
        	Values: Any text value


    Output:
        result -      A message stating that the update completed successfully (an exception is thrown if
                      the column cannot be updated for some reason.)
        	Values: "Column annotation updated."

    Exceptions:  none

    Author:      Calvin Goodrich
    Date:        6/28/2012
    CSW Version: 6.0.0

    2012, 2014 Cisco and/or its affiliates. All rights reserved.
 */

import com.compositesw.extension.*;

import com.compositesw.common.repository.Path;

import com.compositesw.common.security.AuthorizationConstants;

import com.compositesw.server.repository.*;

import java.sql.*;

public class UpdateDsColumnAnnotation implements CustomProcedure, AuthorizationConstants {
    private ExecutionEnvironment qenv;
    private String result = "Column annotation updated.";
  
    public UpdateDsColumnAnnotation() {}
  
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
            new ParameterInfo ("column_path", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("annotation", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("result", Types.VARCHAR, DIRECTION_OUT)
        };
    }
  
    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
    public void invoke (Object[] inputValues) throws CustomProcedureException, SQLException {
        String colPathName;
        String annotation;
        RepositoryConnection repoConn;
        
        if (inputValues.length != 2)
            throw new CustomProcedureException ("Expecting 2 arguments. Received " + inputValues.length);
        
        colPathName = (String) inputValues[0];
        annotation = (String) inputValues[1];
        
        // CIS 6.2 was changed to use the Provider framework for getting repository connections.
        //repoConn = RepositoryManager.getReadWriteConnection();
        repoConn = RepositoryManager.Provider.getInstance((Object[]) null).getReadWriteConnection();
        Path path = new Path (colPathName);
        Metadata meta = repoConn.getResource (path.getParentPath(), Metadata.TYPE_TABLE, WRITE);
        if (meta == null) {
            throw new CustomProcedureException ("Unable to locate table at " + path.getParent());
        }
        meta = meta.getChild(path.getName(), Metadata.TYPE_COLUMN);
        if (meta == null) {
            throw new CustomProcedureException ("Unable to locate column at " + colPathName);
        }
        meta.setAnnotation (annotation);
        repoConn.close (Repository.COMMIT);
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
        return new Object[] {result};
    }
  
    /**
     * Called when the procedure reference is no longer needed.  Close
     * may be called without retrieving any of the output values (such
     * as cursors) or even invoking, so this needs to do any remaining
     * cleanup.  Close may be called concurrently with any other call
     * such as "invoke" or "getOutputValues".  In this case, any pending
     * methods should immediately throw a CustomProcedureException.
     */
    public void close() throws SQLException {}
  
    //
    // Introspection methods
    //
  
    /**
     * Called during introspection to get the short name of the stored
     * procedure.  This name may be overridden during configuration.
     * Should not return null.
     */
    public String getName() {
        return "UpdateDsColumnAnnotation";
    }
  
    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        return "Custom procedure to set the annotation of a data source table's column. (For other types of tables/views, please use the updateSqlTable() admin API.)";
    }
  
    //
    // Transaction methods
    //
  
    /**
     * Returns true if the custom procedure uses transactions.  If this
     * method returns false then commit and rollback will not be called.
     * 
     * Committing batches of records instead of waiting until the end to
     * commit, so commit not supported.
     */
    public boolean canCommit() {
        return false;
    }
  
    /**
     * Commit any open transactions.
     */
    public void commit() throws SQLException {}
  
    /**
     * Rollback any open transactions.
     */
    public void rollback() throws SQLException {}
  
    /**
     * Returns true if the transaction can be compensated.
     */
    public boolean canCompensate() {
        return false;
    }
  
    /**
     * Compensate any committed transactions (if supported).
     */
    public void compensate (ExecutionEnvironment qenv) throws SQLException {}
}
