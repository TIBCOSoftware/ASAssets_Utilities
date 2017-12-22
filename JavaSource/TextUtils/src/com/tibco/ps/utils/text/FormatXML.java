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
FormatXML

Description:
    Takes an XML string and indent width as input and formats the XML, effectively
    "pretty printing" it.


Inputs:
    UnformattedXML - The XML to format
        Values: Any XML document

Outputs:
    FormattedXML   - The formatted XML.
        Values: An XML document


Exceptions:
    N/A


Author:      Tony Young
Date:        9/13/2012
CSW Version: 5.2.0

*/

import java.sql.SQLException;
import java.sql.Types;

import com.compositesw.extension.*;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class FormatXML implements CustomProcedure {
    public static final String CJP_NAME = "FormatXML";
    public static final String CJP_DESC = "Uses standard Java XML libraries to format (indent) XML";
  
    private String formattedXML = "";
    
    public ParameterInfo[] getParameterInfo() {
        return new ParameterInfo[] {
            new ParameterInfo("UnformattedXML", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("FormattedXML", Types.VARCHAR, DIRECTION_OUT)
        };
    }
  
    public void initialize(ExecutionEnvironment arg0)
        throws CustomProcedureException, SQLException {
    }
  
    public void invoke(Object[] arg0) throws CustomProcedureException, SQLException {
        String unformattedXML;
        
        if (arg0.length != 1)
            throw new CustomProcedureException ("Expecting 2 arguments.");
        
        unformattedXML = (String)arg0[0];
        
        try {
            format(unformattedXML);
        } catch (Exception e) {
            throw new CustomProcedureException(e);
        }
    }
  
    private void format(String unformattedXml) throws TransformerException, SAXException, IOException, ParserConfigurationException {
        final Document document = parseXmlFile(unformattedXml);
    
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
        formattedXML = result.getWriter().toString();
    }
  
    private Document parseXmlFile(String in) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(in));
        return db.parse(is);
    }
    
    public boolean canCommit() {
      return false;
    }
  
    public boolean canCompensate() {
      return false;
    }
  
    public void close() throws CustomProcedureException, SQLException {
    }
  
    public void commit() throws CustomProcedureException, SQLException {
    }
  
    public void compensate(ExecutionEnvironment arg0) throws CustomProcedureException, SQLException {
    }
  
    public String getDescription() {
        return CJP_DESC;
    }
  
    public String getName() {
        return CJP_NAME;
    }
  
    public int getNumAffectedRows() throws CustomProcedureException, SQLException {
        return 1;
    }
  
    public Object[] getOutputValues() throws CustomProcedureException, SQLException {
        return new Object[] {formattedXML};
    }
  
    public void rollback() throws CustomProcedureException, SQLException {
    }
}
