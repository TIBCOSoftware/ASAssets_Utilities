package com.tibco.ps.utils.text;

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
  This function uses Java's String.split() method to split a string using a regular expression and a limit.

  Paraphrased from String's javadoc:

  The cursor returned by this function contains each substring of this string that is terminated 
  by another substring that matches the given expression or is terminated by the end of the 
  string. The substrings in the cursor are in the order in which they occur in this string. If 
  the expression does not match any part of the input then the resulting cursor has just one 
  row, namely this string.

  The limit parameter controls the number of times the pattern is applied and therefore affects 
  the length of the resulting cursor. If the limit n is greater than zero then the pattern will 
  be applied at most n - 1 times, the cursor's cardinality will be no greater than n, and the cursor's 
  last row will contain all input beyond the last matched delimiter. If n is non-positive 
  then the pattern will be applied as many times as possible and the cursor can have any number of rows. 
  If n is zero then the pattern will be applied as many times as possible, the cursor can have 
  any number of rows, and trailing empty strings will be discarded.

  The string 'boo:and:foo', for example, yields the following results with these parameters:

  Regex   Limit   Result 
    :       2     'boo', 'and:foo' 
    :       5     'boo', 'and', 'foo' 
    :      -2     'boo', 'and', 'foo' 
    o       5     'b', '', ':and:f', '', '' 
    o      -2     'b', '', ':and:f', '', '' 
    o       0     'b', '', ':and:f'

Inputs:
  Input Text         - The text to search
  Regular Expression - The regular expression to search for
  Limit              - The upper limit to the number of times to apply the split

Outputs:
  result (           - A cursor containing a result set of the split string.
    splitElement     -   Text of a split element
  )

Exceptions:
  None

Author:      Calvin Goodrich
Date:        8/11/2010
CSW Version: 5.1.0

*/

import com.compositesw.extension.*;

import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class RegexSplit implements CustomProcedure {
  protected ExecutionEnvironment qenv;
  private List<Object[]> rows = new ArrayList<Object[]>();

  public RegexSplit() {}

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
      new ParameterInfo ("Input Text", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("Regular Expression", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("Limit", Types.INTEGER, DIRECTION_IN),
      new ParameterInfo ("result", TYPED_CURSOR, DIRECTION_OUT,
        new ParameterInfo[] {
          new ParameterInfo("splitElement", Types.VARCHAR, DIRECTION_OUT)
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
  
    // make sure we have the correct number of arguments
    //
    if (inputValues.length == 0 || inputValues.length > 3)
      throw new CustomProcedureException ("Invalid number of arguments");

    String  inputText;
    String  regex;
    int     limit;
    Pattern p;
    
    // get the input text
    //
    try {
      if (inputValues[0] == null) {
        return;
      }

      inputText = (String) inputValues[0];
    } catch (ClassCastException cce) {
      // this shouldn't happen
      //
      throw new CustomProcedureException ("Input value for \"Input Text\" is not a VARCHAR type.");
    }
    
    // get the regular expression
    //
    try {
      if (inputValues[1] == null) {
        return;
      }

      regex = (String) inputValues[1];
    } catch (ClassCastException cce) {
      // this shouldn't happen
      //
      throw new CustomProcedureException ("Input value for \"Regular Expression\" is not a VARCHAR type.");
    }
    
    // get the occurence number
    //
    try {
      if (inputValues[2] == null) {
        return;
      }

      limit = ((Integer) inputValues[2]).intValue();
      
    } catch (ClassCastException cce) {
      // this shouldn't happen
      //
      throw new CustomProcedureException ("Input value \"Limit\" is not a INTEGER type.");
    } catch (Exception e) {
      throw new CustomProcedureException (e.getMessage());
    }

    try {
      p = RegexPatternFactory.getPattern (regex);
    } catch (PatternSyntaxException pse) {
      throw new CustomProcedureException ("The supplied regular expression cannot be compiled: " + pse.getMessage());
    }
    
    String[] result = p.split (inputText, limit);
    for (int i = 0; i < result.length; i++) {
      Object[] row = new Object[1];
      row[0] = result[i];
      rows.add (row);
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
    ResultCursor rc = new ResultCursor (rows);
    return new Object[] {rc};
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
    return "RegexSplit";
  }

  /**
   * Called during introspection to get the description of the stored
   * procedure.  Should not return null.
   */
  public String getDescription() {
    return "Custom procedure to split text using a regular expression and split limit";
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

  private class ResultCursor implements CustomCursor {
    private List<Object[]> _rows;
    private int i = 0;

    public ResultCursor(List<Object[]> rows) {
      _rows = rows;
    }

    public ParameterInfo[] getColumnInfo() {
      return null;
    }

    public Object[] next() throws CustomProcedureException, SQLException {
      if (_rows != null && i < _rows.size()) {
        return (Object[]) _rows.get(i++);
      } else
        return null;
    }

    public void close() throws CustomProcedureException, SQLException {
    }
  }
}
