package com.cisco.dvbu.ps.utils.xml;

/* 
	HTMLtoXML:
	
	This custom procedure invokes JTidy to convert HTML into well formed XML/XHTML
	
	
	Input:
	  inHTML - The HTML to parse.  
	    Values: Any HTML document
	
	
	Output:
	  outXML - The XML document translated from the HTML.
	    Values: An XML document
	
	
	Exceptions:  none
	
	
	Author:      Jeremy Akers
	Date:        4/6/2014
	CSW Version: 6.2.0
	
	(c) 2014 Cisco and/or its affiliates. All rights reserved.

*/

import com.compositesw.extension.*;
import org.w3c.tidy.*;
import org.w3c.dom.*;
import java.io.*;
import java.net.URL;

import java.sql.*;

public class HTMLtoXML
  implements CustomProcedure
{
  private ExecutionEnvironment qenv;
  private ProcedureReference proc;
  private String result;

  public HTMLtoXML() { }

  /**
   * This is called once just after constructing the class.  The
   * environment contains methods used to interact with the server.
   */
  public void initialize(ExecutionEnvironment qenv) {
    this.qenv = qenv;
  }

  /**
   * Called during introspection to get the description of the input
   * and output parameters.  Should not return null.
   */
  public ParameterInfo[] getParameterInfo() {
    return new ParameterInfo[] {
      new ParameterInfo("inHTML", Types.LONGVARCHAR, DIRECTION_IN),
      new ParameterInfo("outXML", Types.LONGVARCHAR, DIRECTION_OUT),
    };
  }

  public static void main(String args[])
  {
     try
     {

	URL url = new URL(args[0]);
	InputStream is = url.openStream();
	BufferedReader br = new BufferedReader(new InputStreamReader(is));

	String line;
	String buffer = "";
	while ( (line = br.readLine()) != null)
	   buffer += line;

	br.close();
	is.close();
	Object[] obj = { buffer };
	HTMLtoXML me = new HTMLtoXML(); 
	me.invoke(obj);
	obj = me.getOutputValues();
	System.out.println((String) obj[0]);
     }
     catch(Exception e)
     {
	e.printStackTrace();
	System.exit(1);
     }
     return;
     
  }

  /**
   * Called to invoke the stored procedure.  Will only be called a
   * single time per instance.  Can throw CustomProcedureException or
   * SQLException if there is an error during invoke.
   */
  public void invoke(Object[] inputValues)
    throws CustomProcedureException, SQLException
  {
     String arg1 =
	      (inputValues[0] != null ? ((String)inputValues[0]).toString() : "");
     Tidy tidy = new Tidy();
     tidy.setXmlOut(true);
     tidy.setShowErrors(0);
     tidy.setQuiet(true);
     tidy.setShowWarnings(false);
     tidy.setDocType("omit");
     StringWriter writer = new StringWriter();
     tidy.parseDOM(new StringReader(arg1), writer);

     result = writer.toString();
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
  public Object[] getOutputValues()
    throws CustomProcedureException, SQLException
  {
     return new Object[] { new String(result) };
  }

  /**
   * Called when the procedure reference is no longer needed.  Close
   * may be called without retrieving any of the output values (such
   * as cursors) or even invoking, so this needs to do any remaining
   * cleanup.  Close may be called concurrently with any other call
   * such as "invoke" or "getOutputValues".  In this case, any pending
   * methods should immediately throw a CustomProcedureException.
   */
  public void close()
    throws CustomProcedureException, SQLException
  {
    if (proc != null)
      proc.close();
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
    return "HTMLtoXML";
  }

  /**
   * Called during introspection to get the description of the stored
   * procedure.  Should not return null.
   */
  public String getDescription() {
    return "This procedure takes an HTML input and returns it as well formed XML/XHTML";
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
  public void commit() { }

  /**
   * Rollback any open transactions.
   */
  public void rollback() { }

  /**
   * Returns true if the transaction can be compensated.
   */
  public boolean canCompensate() {
    return false;
  }

  /**
   * Compensate any committed transactions (if supported).
   */
  public void compensate(ExecutionEnvironment qenv) { }
}
