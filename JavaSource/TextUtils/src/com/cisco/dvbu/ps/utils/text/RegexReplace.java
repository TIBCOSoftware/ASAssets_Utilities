/*
	Description:
	  Finds an occurrence of a regular expression match in a VARCHAR and replaces the match
	  with the replacement text input value. The value of the occurrence input value 
	  determines which occurrence to replace (numbered starting at 1 from left to right. 
	  Use negative values to number occurrences from right to left.) Zero may be used as a 
	  value for an occurrence and indicates that ALL matches should be replaced. If no match 
	  is found, then the original input text is returned. If a NULL value is passed in 
	  as the value of any of the inputs, the original input text is returned. 
	
	  The regular expression language used is what is supported by the JDK used by CIS 
	  (currently 1.5 in CIS 4.6.0) See the javadoc for java.util.regex.Pattern for 
	  details on what is supported. Also see the javadoc for java.util.regex.Matcher
	  (specifically for the appendReplacement() method) for detail on how to include
	  grouped (as distinguished from "matched") text in the replacement text.
	
	Inputs:
	  Input Text         - The text to search
	  Regular Expression - The regular expression to search for
	  Replacement Text   - The expression to use for replacing the match
	  Occurrence         - The occurrance of the match (starting from 1 left to right or -1 right to left.) 0 means replace ALL occurrences.
	
	Outputs:
	  result             - The input text with the matche(s) replaced.
	
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

public class RegexReplace implements CustomProcedure {
  protected ExecutionEnvironment qenv;
  private String result = null;

  public RegexReplace() {}

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
      new ParameterInfo ("Replacement Text", Types.VARCHAR, DIRECTION_IN),
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
    if (inputValues.length == 0 || inputValues.length > 4)
      throw new CustomProcedureException ("Invalid number of arguments");

    String inputText;
    String regex;
    String replacementText;
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
        result = inputText;
        return;
      }

      regex = (String) inputValues[1];
    } catch (ClassCastException cce) {
      // this shouldn't happen
      //
      throw new CustomProcedureException ("Input value for \"Regular Expression\" is not a VARCHAR type.");
    }
    
    // get the replacement text
    //
    try {
      if (inputValues[2] == null) {
        result = inputText;
        return;
      }

      replacementText = (String) inputValues[2];
    } catch (ClassCastException cce) {
      // this shouldn't happen
      //
      throw new CustomProcedureException ("Input value for \"Replacement Text\" is not a VARCHAR type.");
    }
    
    // get the occurrence number
    //
    try {
      if (inputValues[3] == null) {
        result = inputText;
        return;
      }

      occurrence = ((Integer) inputValues[3]).intValue();
      
    } catch (ClassCastException cce) {
      // this shouldn't happen
      //
      throw new CustomProcedureException ("Input value \"Occurrence\" is not a INTEGER type.");
    }

    try {
      p = RegexPatternFactory.getPattern (regex);
    } catch (PatternSyntaxException pse) {
      throw new CustomProcedureException ("The supplied regular expression cannot be compiled: " + pse.getMessage());
    }
    
    StringBuffer sb = new StringBuffer();
    m = p.matcher (inputText);

    if (occurrence == 0) {

      sb.append (m.replaceAll (replacementText));

    } else {
      boolean found = false;

      // the Matcher object doesn't allow for starting at the end of the text
      // and working backwards, so we'll have to find out how many occurrences
      // there are, do a little math, and reset the Matcher object.
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
      
      if (found) {
        m.appendReplacement (sb, replacementText);
      }

      m.appendTail (sb);
    }
    
    result = sb.toString();

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
    return "RegexReplace";
  }

  /**
   * Called during introspection to get the description of the stored
   * procedure.  Should not return null.
   */
  public String getDescription() {
    return "Custom procedure to replace text in an input string using a regular expression";
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
