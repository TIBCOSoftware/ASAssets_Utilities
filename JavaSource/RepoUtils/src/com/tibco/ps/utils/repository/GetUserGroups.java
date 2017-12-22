package com.tibco.ps.utils.repository;

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
	Description:
	    Returns the all the groups that a user belongs to.
	
	Inputs:
	    DomainName - The user's domain
	  
	    UserName   - The user's name
	
	Outputs:
	    result (   - A cursor containing the list of groups
	        DomainName - The domain of the group
	        GroupName  - The group's name
	    )
	
	Exceptions:
	    None
	
	Author:      Calvin Goodrich
	Date:        3/30/2011
	CSW Version: 5.1.0
	
*/

// public API's
//

import com.compositesw.extension.*;

// Composite use only API's
//
import com.compositesw.server.customproc.ExecutionEnvironmentImpl;
import com.compositesw.server.repository.*;

import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;

public class GetUserGroups implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private ArrayList groupList;
    private CustomCursor outputCursor;

    public GetUserGroups() {
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
            new ParameterInfo ("DomainName", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("UserName", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("result", TYPED_CURSOR, DIRECTION_OUT,
                new ParameterInfo[] { 
                    new ParameterInfo ("DomainName", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("GroupName", Types.VARCHAR, DIRECTION_NONE) 
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
        String module = this.getClass().getName();
        String userName;
        String userDomain;
        RepositoryConnection repoConn = null;
        ArrayList groupRow;

        if (inputValues == null || inputValues.length != 2)
            throw new CustomProcedureException (module + ": Must receive two arguments.");
        
        userDomain = (String) inputValues[0];
        userName = (String) inputValues[1];

        if (userDomain == null)
            throw new CustomProcedureException (module + ": Unable to determine the current user's domain.");

        qenv.log (LOG_DEBUG, module + ": userDomain = " + userDomain);

        if (userName == null)
            throw new CustomProcedureException (module + ": Unable to determine the current user's name.");

        qenv.log (LOG_DEBUG, module + ": userName = " + userName);

        repoConn = ((ExecutionEnvironmentImpl) qenv).getParentRequest().getTransactionContext().getRepositoryConnection();
        if (repoConn == null)
            throw new CustomProcedureException (module + ": Unable to obtain repository connection.");

        Domain domain = repoConn.getDomain (userDomain);
        if (domain == null)
            throw new CustomProcedureException (module + ": Unable to locate the domain \"" + userDomain + "\" in the repository.");

        User user = domain.getUser (userName);
        if (user == null)
            throw new CustomProcedureException (module + ": Unable to locate the user \"" + userName + "\" in the repository under the domain \"" + userDomain + "\".");

        groupList = new ArrayList();
        List groups = user.getMemberGroups();
        for (int i = 0; i < groups.size(); i++) {
            groupRow = new ArrayList();
            Group g = (Group) groups.get (i);
            groupRow.add (g.getDomain().getName());
            groupRow.add (g.getName());
            groupList.add (groupRow);
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
                return new ParameterInfo[] { new ParameterInfo ("DomainName", Types.VARCHAR, DIRECTION_NONE),
                                             new ParameterInfo ("GroupName", Types.VARCHAR, DIRECTION_NONE) };
            }

            public Object[] next() throws CustomProcedureException, SQLException {
                if (counter >= groupList.size ()) {
                    return null;
                } else {
                    ArrayList groupRow = (ArrayList) groupList.get(counter++);
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
        return "GetUserGroups";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        return "Custom procedure that returns the groups that a user belongs to.";
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
