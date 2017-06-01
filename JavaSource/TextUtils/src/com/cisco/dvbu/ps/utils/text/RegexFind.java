/*
	Description:
	  Finds an occurrence of a regular expression match in a VARCHAR and returns the match.
	  The value of the occurrence input value determines which occurrence to return (numbered
	  starting at 1 from left to right. Use negative values to number occurrences from right
	  to left.) If no match is found, then a NULL is returned. If a NULL value is passed in 
	  as the value of any of the inputs, a NULL is returned. Zero may not be used as a value
	  for an occurrence.
	
	  The regular expression language used is what is supported by the JDK used by CIS 
	  (currently 1.5 in CIS 4.0.1) See the javadoc for java.util.regex.Pattern for 
	  details on what is supported.
	
	Inputs:
	  Input Text         - The text to search
	  Regular Expression - The regular expression to search for
	  Occurrence         - The occurrance of the match (starting from 1 left to right or -1 right to left)
	
	Outputs:
	  result             - The matched text. NULL if no match found.
	
	Exceptions:
	  None
	
	Author:      Calvin Goodrich
	Date:        8/11/2010
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
package com.cisco.dvbu.ps.utils.text;

import com.compositesw.extension.*;

import java.sql.SQLException;
import java.sql.Types;

import java.util.regex.*;

public class RegexFind implements CustomProcedure {
  protected ExecutionEnvironment qenv;
  private String result = null;

  public RegexFind() {}

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
      new ParameterInfo ("Occurrence", Types.INTEGER, DIRECTION_IN),
      new ParameterInfo ("result", Types.VARCHAR, DIRECTION_OUT)
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

    String inputText;
    String regex;
    int    occurrence;
    
    Pattern p;
    Matcher m;
    
    // get the input text
    //
    try {
      if (inputValues[0] == null) {
        result = null;
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
        result = null;
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
        result = null;
        return;
      }

      occurrence = ((Integer) inputValues[2]).intValue();
      
      if (occurrence == 0)
        throw new Exception ("Input Value for \"Occurrence\" may not be zero.");
        
    } catch (ClassCastException cce) {
      // this shouldn't happen
      //
      throw new CustomProcedureException ("Input value \"Occurrence\" is not a INTEGER type.");
    } catch (Exception e) {
      throw new CustomProcedureException (e.getMessage());
    }

    try {
      p = RegexPatternFactory.getPattern (regex);
    } catch (PatternSyntaxException pse) {
      throw new CustomProcedureException ("The supplied regular expression cannot be compiled: " + pse.getMessage());
    }
    
    boolean found = false;
    m = p.matcher (inputText);

    // the Matcher object doesn't allow for starting at the end of the text and
    // working backwards, so we'll have to find out how many occurrences there
    // are, do a little math, and reset the Matcher object.
    //
    if (occurrence < 0) {
      int maxOccurrences = 0;
      
      while (m.find()) 
        maxOccurrences++;
      
      if (maxOccurrences - occurrence + 1 < 1) {
        result = null;
        return;
      } else {
        occurrence = maxOccurrences + occurrence + 1;
      }
      
      m.reset();
    }

    for (int i = 0; i < occurrence; i++)
      found = m.find();
      
    if (found)
      result = m.group();
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
    return "RegexFind";
  }

  /**
   * Called during introspection to get the description of the stored
   * procedure.  Should not return null.
   */
  public String getDescription() {
    return "Custom procedure to extract text from an input string";
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
}
