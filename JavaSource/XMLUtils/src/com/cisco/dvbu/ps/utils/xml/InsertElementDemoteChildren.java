package com.cisco.dvbu.ps.utils.xml;

/* 
	InsertElementDemoteChildren:
	
	This CJP inserts an element into an XML structure and moves the new element's siblings under the new element.
	
	
	Input:
	  inXml - The XML structure to modify.  
	    Values: Any XML document
	
	  ParentElementName - The name of the XML parent element to insert into.
	    Values: Any XML element name
	
	  ParentElementNamespace - The namespace of the XML parent element to insert into.
	    Values: Any XML element namespace URI
	
	  occurrence - The ordinal number of the occurrence of the parent element to insert into (negative values here mean to start at the end of the XML document and work backwards.)
	    Values: A non-zero integer
	
	  ElementName - The name of the XML element to insert.
	    Values: Any XML element name
	
	  ElementNamespace - The namespace of the XML element to insert.
	    Values: Any XML element namespace URI
	
	
	Output:
	  result - The XML document with the specified element inserted.
	    Values: Any XML document
	
	
	Exceptions:  none
	
	
	Author:      Calvin Goodrich
	Date:        8/13/2009
	CSW Version: 4.6.0
	
	(c) 2009, 2014 Cisco and/or its affiliates. All rights reserved.

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

import com.compositesw.extension.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;

import javax.xml.parsers.*;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InsertElementDemoteChildren implements CustomProcedure {
  protected static String className;
  private ExecutionEnvironment qenv;
  private String result = null;
  
  static {
      className = InsertElementDemoteChildren.class.getName();
  }

  public InsertElementDemoteChildren() {}

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
      new ParameterInfo ("XML", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("ParentElementName", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("ParentElementNamespace", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("occurrance", Types.INTEGER, DIRECTION_IN),
      new ParameterInfo ("ElementName", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo ("ElementNamespace", Types.VARCHAR, DIRECTION_IN),
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
    String inParentElementName = null;
    String inParentElementNamespace = null;
    int occurrance = -1;
    String inElementName = null;
    String inElementNamespace = null;
    Document doc = null;
    
    if (inputValues.length != 6) {
      throw new CustomProcedureException (className + ": invalid number of arguments to invoke() method.");
    }
    
    inXMLString = (String) inputValues[0];
    inParentElementName = (String) inputValues[1];
    inParentElementNamespace = (String) inputValues[2];
    occurrance = ((Integer) inputValues[3]).intValue();
    inElementName = (String) inputValues[4];
    inElementNamespace = (String) inputValues[5];
    
    qenv.log (LOG_DEBUG, className + ": inParentElementName = " + inParentElementName);
    qenv.log (LOG_DEBUG, className + ": inParentElementNamespace = " + inParentElementNamespace);
    qenv.log (LOG_DEBUG, className + ": occurrance = " + occurrance);
    qenv.log (LOG_DEBUG, className + ": inElementName = " + inElementName);
    qenv.log (LOG_DEBUG, className + ": inElementNamespace = " + inElementNamespace);

    if (inXMLString == null || inParentElementName == null || inElementName == null) {
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
      //doc.setStrictErrorChecking(false);
      
      qenv.log (LOG_DEBUG, className + ": document root element name is " + doc.getDocumentElement().getLocalName() + " with namespace of " + doc.getDocumentElement().getNamespaceURI());

      // find the parent element. if it's not found return the original XML
      //
      qenv.log (LOG_DEBUG, className + ": Looking for element named \"" + inParentElementName + "\" with a namespace URI of \"" + inParentElementNamespace + "\"");
      NodeList nl = doc.getElementsByTagNameNS (inParentElementNamespace, inParentElementName);
      if (nl.getLength() == 0) {
        qenv.log (LOG_DEBUG, className + ": Did not find element. Returning original XML");
        result = inXMLString;
        return;
      }

      // make sure there are enough occurrences
      //
      if ((occurrance > 0 && occurrance > nl.getLength()) || (occurrance < 0 && (nl.getLength() + occurrance) < 0)) {
        qenv.log (LOG_DEBUG, className + ": occurrence input (" + occurrance + ")falls outside of range of located elements (" + nl.getLength() + ")");
        result = inXMLString;
        return;
      }

      // locate the occurrance of the element. if occurrance is negative we work backwards from the end of the list.
      //
      qenv.log (LOG_DEBUG, className + ": Using occurrance #" + occurrance +" of element");
      Node p = (occurrance > 0) 
                 ? nl.item (occurrance - 1) 
                 : nl.item (nl.getLength() + occurrance);
      
      // get all the parent's children
      //
      qenv.log (LOG_DEBUG, className + ": Getting parent's children");
      NodeList children = p.getChildNodes();

      // create the new element and attach it to the parent
      //
      qenv.log (LOG_DEBUG, className + ": Creating new element named \"" + inElementName + "\" with a namespace URI of \"" + inElementNamespace + "\"");
      String prefix = p.lookupPrefix (inElementNamespace);
      Element e = doc.createElementNS (inElementNamespace, inElementName);
      e.setPrefix (prefix);
      
      ArrayList c = new ArrayList();
      
      // detach the children from the parent
      //
      qenv.log (LOG_DEBUG, className + ": Detaching parent's children");
      if (children != null) {
        for (int i = children.getLength() - 1; i >= 0; i--) {
          c.add (children.item (i).cloneNode (true)); // children are going in here in reverse order. need to attach them to the new element in the correct order
          qenv.log (LOG_DEBUG, className + ": removing {" + children.item(i).getNamespaceURI() + "}" + children.item(i).getLocalName() + " from {" + p.getNamespaceURI() + "}" + p.getLocalName());
          p.removeChild (children.item(i));
        }
      }
      
      // reverse the order of the children again so the go onto the new element in the correct order
      //
      qenv.log (LOG_DEBUG, className + ": Reataching children to new element");
      for (int i = c.size() - 1; i >= 0; i--) {
        e.appendChild ((Node) c.get (i));
      }
      
      qenv.log (LOG_DEBUG, className + ": Appending new element to parent");
      p.appendChild (e);

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
    return "InsertElementDemoteChildren";
  }

  /**
   * Called during introspection to get the description of the stored
   * procedure.  Should not return null.
   */
  public String getDescription() {
    return "Custom procedure to insert an element into an XML structure and move the parent's children under the new element.";
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
