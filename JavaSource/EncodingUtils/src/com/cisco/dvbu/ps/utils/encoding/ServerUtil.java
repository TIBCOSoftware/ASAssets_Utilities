package com.cisco.dvbu.ps.utils.encoding;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ProcedureConstants;
import com.compositesw.extension.ProcedureReference;
import com.compositesw.server.customproc.cvt.CustomCursor2ResultSet;

/*
    © 2011, 2014 Cisco and/or its affiliates. All rights reserved.

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

public class ServerUtil {

	// When debugging change to LOG_INFO to put more info in the log during execution
	//		otherwise keep it at LOG_DEBUG level for normal operation.
	public static final int LOG_TYPE = ProcedureConstants.LOG_DEBUG;
	public static final String GET_SERVER_ATTRIBUTES_PROCEDURE = "/services/webservices/system/admin/server/operations/getServerAttributes";
	public static final String GET_SERVER_ATTRIBUTES_AS_PROCEDURE = "/shared/ASAssets/Utilities/repository/server/getServerAttribute";
	                                                              
	public static final String SERVER_ATTRIBUTE_TAG = "$SERVER_ATTRIBUTE$";

	public static final String GET_SERVER_ATTRIBUTES_REQUEST = "<server:getServerAttributes xmlns:server=\"http://www.compositesw.com/services/system/admin/server\">"
			+ "	<server:paths>"
			+ "		<server:path>"+ SERVER_ATTRIBUTE_TAG + "</server:path>"
			+ "	</server:paths>"
			+ "</server:getServerAttributes>";
	public static final String KEYSTORE_LOCATION_ATTR = "/server/communications/strongKeystoreLocation";
	public static final String KEYSTORE_PASSWORD_ATTR = "/server/communications/strongKeystorePassword";
	public static final String KEYSTORE_KEY_ALIAS_ATTR = "/server/communications/strongKeystoreKeyAlias";
	
	// Get the server attribute using the ASAssets getServerAttribute which returns a string value instead of XML.
	public static String getServerAttributeAS(ExecutionEnvironment ee, String request) throws CustomProcedureException, SQLException {

		String procName = "getServerAttributeAS";
		ProcedureReference webapi = ee.lookupProcedure(ServerUtil.GET_SERVER_ATTRIBUTES_AS_PROCEDURE);
		ee.log(LOG_TYPE, "(a)ASAssets."+procName+" invoke="+ServerUtil.GET_SERVER_ATTRIBUTES_AS_PROCEDURE);
		Object[] args = { request };
		String result = null;
		try {
			webapi.invoke(args);
			final Object[] response = webapi.getOutputValues();
			if (response != null && response.length > 0) {
				ee.log(LOG_TYPE, "(p)ASAssets."+procName+" response=["+response[0].toString()+"]");
				result = response[0].toString();
			}
			ee.log(LOG_TYPE,"(p)ASAssets."+procName+" result=["+result+"]");
		} finally {
			webapi.close();
		}
		return result;
	}

	// This code is not currently working
	// It does not return the response from a procedure with an XML output.
	public static String getServerAttribute(ExecutionEnvironment ee, String a) throws CustomProcedureException, SQLException {

		String procName = "getServerAttribute";
		ProcedureReference webapi = ee.lookupProcedure(GET_SERVER_ATTRIBUTES_PROCEDURE);
		String request = GET_SERVER_ATTRIBUTES_REQUEST.replace(SERVER_ATTRIBUTE_TAG, a);
		ee.log(LOG_TYPE, "(a)ServerUtil."+procName+" request="+request);
		Object[] args = { request };
		String result = null;
		try {
			ee.log(LOG_TYPE, "(a)ServerUtil."+procName+" retrieves value for [" + a + "]");
			webapi.invoke(args);
			final Object[] response = webapi.getOutputValues();
			if (response != null && response.length > 0) {
				ee.log(LOG_TYPE,"(p)ServerUtil."+procName+" response size=["+response.length+ "]");
				for (int i=0; i < response.length; i++) {
					if (response[i] != null) {
						ee.log(LOG_TYPE,"(p)ServerUtil."+procName+" response["+i+"]=["+response[i].toString()+ "]");
						result = parseGetServerAttributesResponse(response[i].toString());
					} else {
						ee.log(LOG_TYPE,"(p)ServerUtil."+procName+" response["+i+"] is null");						
					}
				}
			} else {
				ee.log(LOG_TYPE,"(p)ServerUtil."+procName+" no response.");
			}
			ee.log(LOG_TYPE,"(p)ServerUtil."+procName+" ["+a+"="+result+ "]");
		} finally {
			webapi.close();
		}
		return result;
	}

	// This is generic code for executing a procedure call.
	// It does not return the response from a procedure with an XML output.
	public static String executeQuery(ExecutionEnvironment ee, String procName, String procPath, String p1, String p2, String p3, String p4) 
			throws CustomProcedureException, SQLException {

		String request = null;
		if (p1 == null & p2 == null & p3 == null & p4 == null)
			request = "{call "+procPath+"()}";
		if (p1 != null & p2 == null & p3 == null & p4 == null)
			request = "{call "+procPath+"('"+p1+"')}";
		if (p1 != null & p2 != null & p3 == null & p4 == null)
			request = "{call "+procPath+"('"+p1+"','"+p2+"')}";
		if (p1 != null & p2 != null & p3 != null & p4 == null)
			request = "{call "+procPath+"('"+p1+"','"+p2+"','"+p3+"')}";
		if (p1 != null & p2 != null & p3 != null & p4 != null)
			request = "{call "+procPath+"('"+p1+"','"+p2+"','"+p3+"','"+p4+"')}";
		
		ee.log(LOG_TYPE,"(a)"+procName+" request="+request);
		ResultSet rs = null;
		String result = null;
		try {
			rs = ee.executeQuery(request, null);
			int i = 1;
			while (rs.next()) {
				if (rs.getObject(i) != null) {
					String response = rs.getObject(i).toString();
					ee.log(LOG_TYPE,"(p)"+procName+" response=["+response+ "]");
					result = response;
				}
				i++;
			}
			ee.log(LOG_TYPE,"(p)"+procName+" result=["+result+"]");
		} finally {
			if (rs != null)		{	rs.close();   }	
		}
		return result;
	}
	

	public static class ResourceNamespaceContext implements NamespaceContext {
		public String getNamespaceURI(String prefix) {
			if (prefix == null)
				throw new NullPointerException("Null prefix");
			else if ("server".equals(prefix))
				return "http://www.compositesw.com/services/system/admin/server";
			else if ("resource".equals(prefix))
				return "http://www.compositesw.com/services/system/admin/resource";
			else if ("common".equals(prefix))
				return "http://www.compositesw.com/services/system/util/common";
			else if ("xsi".equals(prefix))
				return XMLConstants.XML_NS_URI;
			return XMLConstants.NULL_NS_URI;
		}

		public Iterator<String> getPrefixes(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		public String getPrefix(String namespaceURI) {
			throw new UnsupportedOperationException();
		}
	}

	public static String parseGetServerAttributesResponse(String xml) {
		String val = null;
		XPathFactory f = XPathFactory.newInstance();
		XPath x = f.newXPath();
		x.setNamespaceContext(new ResourceNamespaceContext());
		try {
			InputSource source = new InputSource(new ByteArrayInputStream(xml.getBytes()));
			Node n = (Node) x.evaluate("//common:attribute[1]", source, XPathConstants.NODE);
			val = x.evaluate("common:value/text()", n);
		} 
		catch (Throwable t) {
			System.err.println("Error: " + t);
		}
		return val;
	}

	public static void main(String[] args) {
		String result = parseGetServerAttributesResponse("<server:getServerAttributesResponse xmlns:server=\"http://www.compositesw.com/services/system/admin/server\">\n"
			+ "<server:attributes>\n"
			+ "<common:attribute xmlns:common=\"http://www.compositesw.com/services/system/util/common\">\n"
			+ "<common:name>/server/communications/strongKeystoreKeyAlias</common:name>\n"
			+ "<common:type>STRING</common:type>\n"
			+ "<common:value>cis_server_strong</common:value>\n"
			+ "</common:attribute>\n"
			+ "</server:attributes>\n"
			+ "</server:getServerAttributesResponse>") ;
		System.out.printf("result=%s", result) ;
	}

}
