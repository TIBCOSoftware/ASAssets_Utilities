package com.cisco.dvbu.ps.utils.encoding;

import java.io.ByteArrayInputStream;
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

/*
    © 2011, 2014 Cisco and/or its affiliates. All rights reserved.
*/

public class ServerUtil {

	public static final String GET_SERVER_ATTRIBUTES_PROCEDURE = "/services/webservices/system/admin/server/operations/getServerAttributes";
	public static final String SERVER_ATTRIBUTE_TAG = "$SERVER_ATTRIBUTE$";

	public static final String GET_SERVER_ATTRIBUTES_REQUEST = "<server:getServerAttributes xmlns:server=\"http://www.compositesw.com/services/system/admin/server\">"
			+ "	<server:paths>"
			+ "		<server:path>"
			+ SERVER_ATTRIBUTE_TAG
			+ "</server:path>"
			+ "	</server:paths>"
			+ "</server:getServerAttributes>";
	public static final String KEYSTORE_LOCATION_ATTR = "/server/communications/strongKeystoreLocation";
	public static final String KEYSTORE_PASSWORD_ATTR = "/server/communications/strongKeystorePassword";
	public static final String KEYSTORE_KEY_ALIAS_ATTR = "/server/communications/strongKeystoreKeyAlias";
	

	public static String getServerAttribute(ExecutionEnvironment ee, String a) throws CustomProcedureException, SQLException {
		ProcedureReference webapi = ee.lookupProcedure(GET_SERVER_ATTRIBUTES_PROCEDURE);
		String request = GET_SERVER_ATTRIBUTES_REQUEST.replace(
				SERVER_ATTRIBUTE_TAG, a);
		Object[] args = { request };
		String result = null;
		try {
			ee.log(ProcedureConstants.LOG_INFO,
					"(a)ServerUtil.getServerAttribute retrieves value for ["
							+ a + "]");
			webapi.invoke(args);
			final Object[] response = webapi.getOutputValues();
			if (response != null && response.length > 0) {
				result = parseGetServerAttributesResponse(response[0].toString());
			}
			ee.log(ProcedureConstants.LOG_DEBUG,"(p)ServerUtil.getServerAttribute ["+a+"="+result+ "]");
		} finally {
			webapi.close();
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
