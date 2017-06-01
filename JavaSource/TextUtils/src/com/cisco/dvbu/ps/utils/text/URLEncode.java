/*
	Description:
	  URL encodes a string.
	
	  Valid character encodings:
	
	    US-ASCII 	  Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
	    ISO-8859-1  ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
	    UTF-8       Eight-bit UCS Transformation Format
	    UTF-16BE 	  Sixteen-bit UCS Transformation Format, big-endian byte order
	    UTF-16LE 	  Sixteen-bit UCS Transformation Format, little-endian byte order
	    UTF-16 	    Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
	
	
	
	
	Inputs:
	  InputText         - The text to encode
	  CharacterEncoding - The name of a valid character encoding. 'ISO-8859-1' if NULL.
	
	Outputs:
	  result            - The encoded text. NULL if InputText is NULL.
	
	Exceptions:
	  None
	
	Author:      Calvin Goodrich
	Date:        2/15/2013
	CSW Version: 6.0.0
	
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
package com.cisco.dvbu.ps.utils.text;

import com.compositesw.extension.*;

import java.net.URLEncoder;

import java.sql.SQLException;
import java.sql.Types;

public class URLEncode implements CustomProcedure {
  protected ExecutionEnvironment qenv;
  private String result = null;

  public URLEncode() {}

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
      new ParameterInfo ("InputText", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("CharacterEncoding", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("result", Types.VARCHAR, DIRECTION_OUT)
    };
  }

  /**
   * Called to invoke the stored procedure.  Will only be called a
   * single time per instance.  Can throw CustomProcedureException or
   * SQLException if there is an error during invoke.
   */
  public void invoke (Object[] inputValues) throws CustomProcedureException, SQLException {
    if (inputValues == null || inputValues.length != 2)
      throw new CustomProcedureException ("Invalid number of arguments.");
    
    String inText = (String) inputValues[0];
    String enc = (String) inputValues[1];
    
    if (inText == null) {
      result = null;
      return;
    }
    
    if (enc == null)
      enc = "ISO-8859-1";
    
    try {
      result = URLEncoder.encode(inText, enc);
    } catch (Exception e) {
      throw new CustomProcedureException (e);
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
    return "URLEncode";
  }

  /**
   * Called during introspection to get the description of the stored
   * procedure.  Should not return null.
   */
  public String getDescription() {
    return "Custom procedure to URL encode an input string";
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
