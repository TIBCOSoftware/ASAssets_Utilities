package com.cisco.dvbu.ps.utils.repository;

/*
    applyReservedListToWord:

    -- CIS Repository Helper Procedure --
    This procedure is used to fix the leading characters and CIS reserved words used in a word. Any word 
    that is a reserved word, has a leading underscore '_' or a number '0123456789', or has a special
    character must be quoted.  

    This procedure would be called in conjunction with other procedures.  For example, when generating 
    a view based off of another view, the SELECT statement's FROM clause would require that the path to 
    the underlying view be fixed with double quotes if it meets any of the quoting criteria above.
 
    Dependencies:
    =============
    $CIS_INSTALL_HOME/conf/customjars/RepoUtils.properties - This properties file on the CIS host filesystem 
        provides a list of reserved words and characters that are used to match with the various parts 
        of a folder path.  Add any new CIS reserved words that appear from release to release in this list. This
        file can be updated without a restart of CIS.

    Input:
        word - The word to assess and fix.  Assumed to be a portion of a folder path.
        	Values: e.g. XML

    Output:
        result - Fixed word
            Values: e.g. "XML"

    Exceptions:  none

    Author:      Calvin Goodrich
    Date:        6/10/2011
    CSW Version: 5.1.0

    (c) 2011, 2014 Cisco and/or its affiliates. All rights reserved.

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

// public API's
//
import com.compositesw.extension.*;

import java.sql.SQLException;
import java.sql.Types;

public class ApplyReservedListToWord implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private String result = null;
    private String cjpName = "ApplyReservedListToWord";

    public ApplyReservedListToWord() {
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
            new ParameterInfo ("inWord", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("debug", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("result", Types.VARCHAR, DIRECTION_OUT) 
        };
    }

    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
    public void invoke (Object[] inputValues) throws CustomProcedureException, SQLException {
        String inWord;
        String debug; // deprecated

        if (inputValues == null || inputValues.length != 2)
            throw new CustomProcedureException (cjpName + ": Must receive two arguments.");
        
        inWord = (String) inputValues[0];
        debug = (String) inputValues[1];

        RepoUtilsPropertiesFactory.setCisLogger();
        try {
            result = CisPathQuoter.quoteWord (inWord);
        } catch (CisPathQuoterException cpqe) {
            throw new CustomProcedureException (cpqe);
        }
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
        return new Object[] { result };
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
        return "applyReservedListToWord";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        return "Applies CIS resource quoting rules to a word.";
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
