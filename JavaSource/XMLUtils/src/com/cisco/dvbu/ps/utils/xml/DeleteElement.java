package com.cisco.dvbu.ps.utils.xml;

import com.compositesw.extension.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* 
	DeleteElement:
	
	This CJP removes an element from an XML structure and all of it's children.
	
	
	Input:
	  inXml - The XML structure to modify.  
	    Values: Any XML document
	
	  inElementName - The name of the XML element to remove.
	    Values: Any XML element name
	
	  inElementNamespace - The namespace of the XML element to remove.
	    Values: Any XML element namespace URI
	
	  occurrence - The ordinal number of the occurrence of the element to remove (negative values here mean to start at the end of the XML document and work backwards.)
	    Values: A non-zero integer
	
	
	Output:
	  result - The XML document with the specified element removed.
	    Values: Any XML document
	
	
	Exceptions:  none
	
	
	Author:      Calvin Goodrich
	Date:        9/9/2011
	CSW Version: 5.2.0
	
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
public class DeleteElement implements CustomProcedure {
  protected static String className;
  private ExecutionEnvironment qenv;
  private String result = null;

  static {
    className = DeleteElementSpareChildren.class.getName();
  }

  public DeleteElement() {}

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
      new ParameterInfo ("inXML", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("inElementName", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("inElementNamespace", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("occurrance", Types.INTEGER, DIRECTION_IN),
      new ParameterInfo ("result", Types.VARCHAR, DIRECTION_OUT)
    };
  }

  /**
   * Called to invoke the stored procedure.  Will only be called a
   * single time per instance.  Can throw CustomProcedureException or
   * SQLException if there is an error during invoke.
   */
  public void invoke (Object[] inputValues) throws CustomProcedureException, SQLException {
    String inXMLString = null;
    String inElementName = null;
    String inElementNamespace = null;
    int occurrance = -1;
    Document doc = null;
    
    if (inputValues.length != 4) {
      throw new CustomProcedureException (className + ": invalid number of arguments to invoke() method.");
    }
    
    inXMLString = (String) inputValues[0];
    inElementName = (String) inputValues[1];
    inElementNamespace = (String) inputValues[2];
    occurrance = ((Integer) inputValues[3]).intValue();
    
    if (inXMLString == null || inElementName == null) {
      result = inXMLString;
      return;
    }
    
    if (occurrance == 0) {
      throw new CustomProcedureException (className + ": value of \"occurrance\" argument may not be zero.");
    }

    qenv.log (LOG_DEBUG, className + ": Creating DocumentBuilderFactory");
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    qenv.log (LOG_DEBUG, className + ": Setting DocumentBuilderFactory namespace awareness to true");
    dbf.setNamespaceAware (true);

    try {
      qenv.log (LOG_DEBUG, className + ": Creating DocumentBuilder");
      DocumentBuilder db = dbf.newDocumentBuilder();

      // the parser won't take a String as input so we have to convert it to an InputStream
      //
      qenv.log (LOG_DEBUG, className + ": Converting input XML string to a ByteArrayInputStream");
      InputStream is = new ByteArrayInputStream (inXMLString.getBytes ("UTF-8"));
      qenv.log (LOG_DEBUG, className + ": Parsing input XML");
      doc = db.parse (is);

      // find the element. if it's not found return the original XML
      //
      qenv.log (LOG_DEBUG, className + ": Looking for element named \"" + inElementName + "\" with a namespace URI of \"" + inElementNamespace + "\"");
      NodeList nl = doc.getElementsByTagNameNS (inElementNamespace, inElementName);
      if (nl.getLength() == 0) {
        qenv.log (LOG_DEBUG, className + ": Did not find element. Returning original XML");
        result = inXMLString;
        return;
      }

      // make sure there are enough occurrences
      //
      if ((occurrance > 0 && occurrance > nl.getLength()) || (occurrance < 0 && (nl.getLength() + occurrance) < 0)) {
        qenv.log (LOG_DEBUG, className + ": occurrence input (" + occurrance + ") falls outside of range of located elements (" + nl.getLength() + ")");
        result = inXMLString;
        return;
      }

      // locate the occurrance of the element. if occurrance is negative we work backwards from the end of the list.
      //
      qenv.log (LOG_DEBUG, className + ": Using occurrence #" + occurrance +" of element");
      Node e = (occurrance > 0) 
                 ? nl.item (occurrance - 1) 
                 : nl.item (nl.getLength() + occurrance);
      
      // locate the parent node
      //
      qenv.log (LOG_DEBUG, className + ": Getting parent node");
      Node p = e.getParentNode();
      qenv.log (LOG_DEBUG, className + ": parent node name \"" + p.getLocalName() + "\" with a namespace URI of \"" + p.getNamespaceURI() + "\"");
      
      // remove the element from the parent
      //
      qenv.log (LOG_DEBUG, className + ": removing element");
      p.removeChild (e);
      
      // serialize the resulting XML document
      //
      qenv.log (LOG_DEBUG, className + ": Serializing resulting XML");
      StringWriter sw = new StringWriter();
      TransformerFactory xformFactory = TransformerFactory.newInstance();
      Transformer idTransform = xformFactory.newTransformer();
      Source input = new DOMSource (doc);
      Result output = new StreamResult (sw);
      idTransform.transform (input, output);
      
      result = sw.toString();
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
    return "DeleteElement";
  }

  /**
   * Called during introspection to get the description of the stored
   * procedure.  Should not return null.
   */
  public String getDescription() {
    return "Custom procedure to remove an element from an XML structure including it's children.";
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
