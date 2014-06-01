package com.cisco.dvbu.ps.utils.xml;

/*
	CSVFromXMLToFile:
	  Function to parse an XML string and convert the result set to a CSV string.
	  Write the result to a file.
	
	  Note:  For best results, the XML string should be formatted with repeated rows 
	  		 containing all expected columns in each row.   Deviation from this pattern may result
	  		 in unexpected behavior.   The specific XML node names and number of columns do not 
	  		 matter as long as it follows the example pattern shown below:
	  		 
	  		<?xml version="1.0"?>
			<p1:Customer xmlns:p1="http://www.compositesw.com/ps/FileProcessor">
				<row>
					<customerID>1</customerID>
					<companyName>Composite Software</companyName>
					<contactFirstName>John</contactFirstName>
					<contactLastName>Doe</contactLastName>
					<billingAddress>1234 First Avenue NE</billingAddress>
					<city>Reston</city>
					<stateOrProvince>VA</stateOrProvince>
					<postalCode>22190</postalCode>
					<countryRegion>USA</countryRegion>
					<contactTitle>Mr</contactTitle>
					<phoneNumber>(703)111-2222</phoneNumber>
					<faxNumber>(703)111-3333</faxNumber>
				</row>
				<row>
					<customerID>2</customerID>
					<companyName>Company 2</companyName>
					<contactFirstName>Jane</contactFirstName>
					<contactLastName>Doe</contactLastName>
					<billingAddress>5678 Second Street NW</billingAddress>
					<city>Washington</city>
					<stateOrProvince>DC</stateOrProvince>
					<postalCode>10002</postalCode>
					<countryRegion>US</countryRegion>
					<contactTitle>Mrs</contactTitle>
					<phoneNumber>202-111-2222</phoneNumber>
					<faxNumber>202-111-3333</faxNumber>
				</row>
			</p1:Customer>
			
	Inputs:
	  xml_string          - The XML to parse through to create a CSV file
	    values: Any valid XML string.
	
	  separator_character   - The character used to separate values.
	    values: Any single character or NULL (defaults to ','.)
		default=','
		
	  qualifier_character   - The character used to qualify values when they contain a separator character.
	    values: Any single character (other than the separator character) or NULL (defaults to '"'.)
		default='"'
		
	  create_column_headers - Indicates whether to create a column headers row as the first row of the output.
	    values: Any boolean value (such as "true" or "false".)
		default=false
		
	  total_columns    - Total number of columns to produce.  This is a validation against the number of fields discovered in the XML.
	    values: Any integer value
	    
	  filePath - Full path to a file that is to be created.				Types.VARCHAR, DIRECTION_IN
	  
	  append - 0=do not append file, 1=append file.						Types.SMALLINT, DIRECTION_IN
	
	Output:
	  error  - 0=success, 1=error
	    values: 0 or 1
	
	
	Exceptions:
	  CustomProcedureException - Thrown when illegal arguments are passed.
	
	
	Author:      Mike Tinius
	Date:        8/20/2011
	CSW Version: 5.2.0
	
	Updated:     Calvin Goodrich
	Date:        11/21/2011
	CSW Version: 6.0.0
	Reason:      Fixed an issue where passing 'false' to the "append" param
	             would result in an empty file.
	
	(c) 2011, 2014 Cisco and/or its affiliates. All rights reserved.
 */

import com.compositesw.common.logging.Logger;
import com.compositesw.extension.CustomProcedure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class CSVFromXMLToFile extends XMLUtilTemplate implements CustomProcedure {
  String loginfo = "Done.";
  int error = 0;
  ResultSet rs = null;

  static {
	className = "CSVFromXMLToFile";
	logger = Logger.getLogger(CSVFromXMLToFile.class.getName());
  }

  public String getDescription() {
    return "This procedure takes in an XML string, parses it and converts the results to a CSV string and writes the result to a file.";
  }

  public String getName() {
    return "CSVFromXMLToFile";
  }

  public Object[] getOutputValues() {
    return new Object[] { error };
  }

  public ParameterInfo[] getParameterInfo() {
    return new ParameterInfo[] {
      new ParameterInfo("xml_string", Types.LONGVARCHAR, DIRECTION_IN),
      new ParameterInfo("separator_character", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("qualifier_character", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("create_column_headers", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("total_columns", Types.INTEGER, DIRECTION_IN),
      new ParameterInfo("file_Path", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("append", Types.SMALLINT, DIRECTION_IN),
      new ParameterInfo("result", Types.INTEGER, DIRECTION_OUT) 
    };
  }

  public void invoke(Object[] inputs) throws CustomProcedureException, SQLException {
    try {
      String xmlString = null;
      String separator = ",";
      String qualifier = "\"";
      boolean createHeaders = false;
      int totalColumns = 0;
      String filePath = null;
      int appendNum = 0;
      boolean append = false;

      /*
       * VALIDATE Input parameters
       */
  	  if (inputs[0] == null) {
  		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter xml_string must be provided.");
  	  }
   	  if (inputs[4] == null) {
   		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter total_columns must be provided.");
   	  }
   	  if (inputs[5] == null) {
 		    throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter file_Path must be provided.");
      }
/* -- not strictly needed with default value above and check for null below.
 	    if (inputs[6] == null) {
 		    throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter append must be provided.");
 	    }
*/

      /*
       * RETREIVE Input parameters
       */
     // Get queryString from input
      if (inputs[0] != null) {
    	  xmlString = ((String) inputs[0]).trim();
      } 
      // Get separator from input
      if (inputs[1] != null) {
    	  separator = (String) inputs[1];
      }
      // Get createHeaders from input
      if (inputs[2] != null) {
    	  qualifier = ((String) inputs[2]).trim();
      }
      // Get createHeaders from input
      if (inputs[3] != null) {
          createHeaders = (Boolean.parseBoolean((((String) inputs[3])).trim()));
      }
      // Get totalColumns from input
      if (inputs[4] != null) {
    	  totalColumns = (Integer)inputs[4];
      }
      // Get filePath from input
      if (inputs[5] != null) {
    	  filePath = inputs[5].toString();
      }
      // Get append from input
      if (inputs[6] != null) {
    	  appendNum = Integer.valueOf(inputs[6].toString());
          append = (appendNum==1)?true:false;
      }
      
      loginfo += "DEBUG: \nARGS" +
           "\nSeparator Character: " + separator +
           "\nQualifier Character: " + qualifier +
           "\nCreate Column Headers: " + createHeaders +
           "\nTotal Columns: " + totalColumns +
           "\nFile Path: " + filePath +
           "\nAppend: " + append +
           "\nXML String: " + xmlString + 
           "\n";
     
      /*
       * VALIDATE parameters for content
       */
      if (separator.length() > 1) {
    	  throw new IllegalArgumentException ("The separator character must be either null (defaults to ',') or a single character like ','");
      }
      if (qualifier.length() > 1) {
    	  throw new IllegalArgumentException ("The qualifier character must be either null (defaults to '\"') or a single character like '\"'");
      }
      if (separator == qualifier) {
    	  throw new IllegalArgumentException ("The separator and qualifier characters may not be the same: " + separator);
      }

      // if "append" input is false, zero out the input file and change "append"
      // to true so that all subsequent calls to createFileAscii DO append to the file.
      // (otherwise all you get is a file with the last written line, i.e. "")
      //
      if (! append) {
        createFileAscii (filePath, false, "");
        append = true;
      }

      processXMLStringDelimited(xmlString, separator, qualifier, createHeaders, totalColumns, filePath, append);
      
    } catch (Throwable t) {
      error = 1;
      String message = "Exception Occurred:";
      if (t.getMessage() != null) message = message + " " + t.getMessage();
      message = message+"\n";
      logger.info (message+loginfo);
      t.printStackTrace();
      throw new CustomProcedureException(message+loginfo);
   
    } finally {
      // Comment this line out after debugging is completed
      //logger.info (loginfo);

      if (qenv != null) {
    	  qenv.log (LOG_DEBUG, loginfo);
      }
    }
  }
  
	/****************************************************************
	 * DELIMITED METHODS
	 ****************************************************************/
  
	/****************************************************************
	 * processXMLStringDelimited - parse the XML string and write it to a CSV formatted file 
	 * 
	 * @param xmlString - The XML string to parse
	 * @param separator - The character used to separate values.
	 * @param qualifier - The character used to qualify values when they contain a separator character.
	 * @param createHeaders - Indicates whether to create a column headers row as the first row of the output.
	 * @param totalColumns - Total number of columns to produce.  This is a validation against the number of fields discovered in the XML.
	 * @param outputFilePath - Full path to a file that is to be created.	
	 * @param append - 0=do not append file, 1=append file.
	 * @throws CustomProcedureException 
	 * @throws SQLException 
	 ****************************************************************/
	@SuppressWarnings("unchecked")
	private void processXMLStringDelimited(String xmlString, String separator, String qualifier, boolean createHeaders, int totalColumns, String outputFilePath, boolean append) throws CustomProcedureException, SQLException {
	
		Element rootElement = null;
		try {
			rootElement = getDocumentFromString(xmlString);
        
			if(rootElement != null){
				
				try {
					List<Element> nodes = rootElement.getChildren();
					StringBuffer sb = new StringBuffer();
					
					if (createHeaders) {
						sb = processHeaderDelimited(nodes, sb, separator, qualifier, totalColumns, outputFilePath, append);
						append = true;					
					}
	
					int rowCount = 0;
					sb = new StringBuffer();
					sb = processChildNodesDelimited(nodes, sb, separator, qualifier, totalColumns, outputFilePath, append, rowCount);
					if (sb.toString() != null) {
						// Add a blank line for the last line in the file.
						createFileAscii(outputFilePath, append, "");					
					}
		
				} catch (Exception ex) {
					throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
				}
			}
		} catch (SQLException ex) {
			throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
		}
	}

	@SuppressWarnings("unchecked")
	private StringBuffer processHeaderDelimited(List<Element> childNodes, StringBuffer sb, String separator, String qualifier, int totalColumns, String outputFilePath, boolean append) throws CustomProcedureException{
		
		int elementCount = 0;
		int rowCount = 0;
		
		for (Element element : childNodes) {
			// Break out of the loop once the first row header is constructed
			if (rowCount == 1) {
				break;
			}
			if(element.getChildren() == null || element.getChildren().isEmpty()){
				
				if (elementCount > 0) {
					sb.append (separator);
				}
				sb.append (element.getName());

				elementCount++;
				// Break out of the loop once the total number of columns is met from the current column list
				if (elementCount == totalColumns) {
					try {
						createFileAscii(outputFilePath, append, sb.toString());
					} catch (Exception ex) {
						throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
					}
				}
				// Throw an exception if the number of elements retrieved is greater than the total number of elements expected
				if (elementCount > totalColumns) {
					throw new CustomProcedureException("Header::The Number of elements extracted from the XML ["+elementCount+"] is greater than the expected total elements ["+totalColumns+"].");
				}

			}else{
				rowCount++;
				sb = processHeaderDelimited(element.getChildren(), sb, separator, qualifier, totalColumns, outputFilePath, append);
			}
		}
		if (elementCount > 0) {
			if (elementCount < totalColumns) {
				throw new CustomProcedureException("Header::The Number of elements extracted from the XML ["+elementCount+"] is less than the expected total elements ["+totalColumns+"].");
			}
		}
		return sb;
	}
	
	@SuppressWarnings("unchecked")
	private StringBuffer processChildNodesDelimited(List<Element> childNodes, StringBuffer sb, String separator, String qualifier, int totalColumns, String outputFilePath, boolean append, int rowCount) throws CustomProcedureException{
		
		sb = new StringBuffer();	
		String resultTmp = "";
		int elementCount = 0;

		for (Element element : childNodes) {
			if(element.getChildren() == null || element.getChildren().isEmpty()){
				
				if (elementCount > 0) {
					sb.append (separator);
				}
				
				resultTmp = element.getValue();
				
				if(resultTmp != null){
			              
					// If the string representation of the result contains a separator, then it needs to be qualified. Any existing
					// qualifier characters in the result string need to be escaped (doubled). 
					//
					if (element.getValue().contains (separator)) {
						resultTmp = resultTmp.replace (qualifier, qualifier + qualifier);
						resultTmp = qualifier + resultTmp + qualifier;
					}        
					sb.append (resultTmp);

					elementCount++;
					// Write the row to the file once the number of elements extracted matches the total number of elements expected
					if (elementCount == totalColumns) {
						try {
							createFileAscii(outputFilePath, append, sb.toString());
						} catch (Exception ex) {
							throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
						}
					}
					// Throw an exception if the number of elements retrieved is greater than the total number of elements expected
					if (elementCount > totalColumns) {
						throw new CustomProcedureException("ROW["+rowCount+"]::The Number of elements extracted from the XML ["+elementCount+"] is more than the expected total elements ["+totalColumns+"].");
					}
				}

			}else{
				rowCount++;
				sb = processChildNodesDelimited(element.getChildren(), sb, separator, qualifier, totalColumns, outputFilePath, append, rowCount);
			}
		}
		if (elementCount > 0) {
			if (elementCount < totalColumns) {
				throw new CustomProcedureException("ROW["+rowCount+"]::The Number of elements extracted from the XML ["+elementCount+"] is less than the expected total elements ["+totalColumns+"].");
			}
		}
		return sb;
	}
	
	/****************************************************************
	 * COMMON METHODS
	 ****************************************************************/

	// -- returns root element of XML document
	private Element getDocumentFromString(String xmlString) throws CustomProcedureException, SQLException  {
		
		Element retval=null;	
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document document = null;
		try {
			document = (new SAXBuilder()).build(inStream);
	        retval = document.getRootElement();
		} catch(JDOMException e) {
			throw new CustomProcedureException(e);
		} catch (IOException e) {
			throw new CustomProcedureException(e);
		}
		
		return retval;
	}

	private void createFileAscii(String fileName, boolean append, String fileContent) 
	throws CustomProcedureException, SQLException {
    loginfo += "writing to file \"" + fileName + "\": " + fileContent + "\n";
    
		try {
			File file = new File(fileName);
			// if its not there create it
			if(!file.isFile()){
				file.createNewFile();
			}				
				
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, append));
			if (append) {
				out.newLine();
			}
			out.write(fileContent);
			out.close();
			//System.out.println("Your file has been written");        	
		}
		catch(IOException ex)
		{
			throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
		}	        
	}  
	
	public static void main(String[] args) {
		int xmlChoice = 1; //1=correct XML, 2=extra column, 3=one less column (1st row), 4=one less column (2nd row), 4=one less column (3rd row)

		
		CustomProcedure cp = new CSVFromXMLToFile();
		/*
		 * Parameters:
		      new ParameterInfo("xml_string", Types.LONGVARCHAR, DIRECTION_IN),
		      new ParameterInfo("separator_character", Types.VARCHAR, DIRECTION_IN),
		      new ParameterInfo("qualifier_character", Types.VARCHAR, DIRECTION_IN),
		      new ParameterInfo("create_column_headers", Types.VARCHAR, DIRECTION_IN),
		      new ParameterInfo("total_columns", Types.INTEGER, DIRECTION_IN),
		      new ParameterInfo("file_Path", Types.VARCHAR, DIRECTION_IN),
		      new ParameterInfo("append", Types.SMALLINT, DIRECTION_IN),
		      new ParameterInfo("result", Types.INTEGER, DIRECTION_OUT) 
		 */
		String xml_string = null;
		switch (xmlChoice) {
		// Correct number of columns
		case 1: xml_string = "<?xml version=\"1.0\"?><p1:Customer xmlns:p1=\"http://www.compositesw.com/ps/FileProcessor\">	<row>		<customerID>1</customerID>		<companyName>Composite Software</companyName>		<contactFirstName>John</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>1234 First Avenue NE</billingAddress>		<city>Reston</city>		<stateOrProvince>VA</stateOrProvince>		<postalCode>22190</postalCode>		<countryRegion>USA</countryRegion>		<contactTitle>Mr</contactTitle>		<phoneNumber>(703)111-2222</phoneNumber>		<faxNumber>(703)111-3333</faxNumber></row>	<row>		<customerID>2</customerID>		<companyName>Company 2</companyName>		<contactFirstName>Jane</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>5678 Second Street NW</billingAddress>		<city>Washington</city>		<stateOrProvince>DC</stateOrProvince>		<postalCode>10002</postalCode>		<countryRegion>US</countryRegion>		<contactTitle>Mrs</contactTitle>		<phoneNumber>202-111-2222</phoneNumber>		<faxNumber>202-111-3333</faxNumber>	</row></p1:Customer>";
			break;
		// Too many columns in first row (added col13)
		case 2: xml_string = "<?xml version=\"1.0\"?><p1:Customer xmlns:p1=\"http://www.compositesw.com/ps/FileProcessor\">	<row>		<customerID>1</customerID>		<companyName>Composite Software</companyName>		<contactFirstName>John</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>1234 First Avenue NE</billingAddress>		<city>Reston</city>		<stateOrProvince>VA</stateOrProvince>		<postalCode>22190</postalCode>		<countryRegion>USA</countryRegion>		<contactTitle>Mr</contactTitle>		<phoneNumber>(703)111-2222</phoneNumber>		<faxNumber>(703)111-3333</faxNumber>	<col13>novalue</col13></row>	<row>		<customerID>2</customerID>		<companyName>Company 2</companyName>		<contactFirstName>Jane</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>5678 Second Street NW</billingAddress>		<city>Washington</city>		<stateOrProvince>DC</stateOrProvince>		<postalCode>10002</postalCode>		<countryRegion>US</countryRegion>		<contactTitle>Mrs</contactTitle>		<phoneNumber>202-111-2222</phoneNumber>		<faxNumber>202-111-3333</faxNumber>	</row></p1:Customer>";
			break;
		// Not enough columns in first row (removed faxNumber)
		case 3: xml_string = "<?xml version=\"1.0\"?><p1:Customer xmlns:p1=\"http://www.compositesw.com/ps/FileProcessor\">	<row>		<customerID>1</customerID>		<companyName>Composite Software</companyName>		<contactFirstName>John</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>1234 First Avenue NE</billingAddress>		<city>Reston</city>		<stateOrProvince>VA</stateOrProvince>		<postalCode>22190</postalCode>		<countryRegion>USA</countryRegion>		<contactTitle>Mr</contactTitle>		<phoneNumber>(703)111-2222</phoneNumber>		</row>	<row>		<customerID>2</customerID>		<companyName>Company 2</companyName>		<contactFirstName>Jane</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>5678 Second Street NW</billingAddress>		<city>Washington</city>		<stateOrProvince>DC</stateOrProvince>		<postalCode>10002</postalCode>		<countryRegion>US</countryRegion>		<contactTitle>Mrs</contactTitle>		<phoneNumber>202-111-2222</phoneNumber>		<faxNumber>202-111-3333</faxNumber>	</row></p1:Customer>";
			break;
		// Not enough columns in second row (removed faxNumber)
		case 4: xml_string = "<?xml version=\"1.0\"?><p1:Customer xmlns:p1=\"http://www.compositesw.com/ps/FileProcessor\">	<row>		<customerID>1</customerID>		<companyName>Composite Software</companyName>		<contactFirstName>John</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>1234 First Avenue NE</billingAddress>		<city>Reston</city>		<stateOrProvince>VA</stateOrProvince>		<postalCode>22190</postalCode>		<countryRegion>USA</countryRegion>		<contactTitle>Mr</contactTitle>		<phoneNumber>(703)111-2222</phoneNumber>		<faxNumber>(703)111-3333</faxNumber>		</row>	<row>		<customerID>2</customerID>		<companyName>Company 2</companyName>		<contactFirstName>Jane</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>5678 Second Street NW</billingAddress>		<city>Washington</city>		<stateOrProvince>DC</stateOrProvince>		<postalCode>10002</postalCode>		<countryRegion>US</countryRegion>		<contactTitle>Mrs</contactTitle>		<phoneNumber>202-111-2222</phoneNumber></row></p1:Customer>";
			break;
			// Not enough columns in third row (removed faxNumber)
		case 5: xml_string = "<?xml version=\"1.0\"?><p1:Customer xmlns:p1=\"http://www.compositesw.com/ps/FileProcessor\">	<row>		<customerID>1</customerID>		<companyName>Composite Software</companyName>		<contactFirstName>John</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>1234 First Avenue NE</billingAddress>		<city>Reston</city>		<stateOrProvince>VA</stateOrProvince>		<postalCode>22190</postalCode>		<countryRegion>USA</countryRegion>		<contactTitle>Mr</contactTitle>		<phoneNumber>(703)111-2222</phoneNumber>		<faxNumber>(703)111-3333</faxNumber>		</row>	<row>		<customerID>2</customerID>		<companyName>Composite Software</companyName>		<contactFirstName>John</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>1234 First Avenue NE</billingAddress>		<city>Reston</city>		<stateOrProvince>VA</stateOrProvince>		<postalCode>22190</postalCode>		<countryRegion>USA</countryRegion>		<contactTitle>Mr</contactTitle>		<phoneNumber>(703)111-2222</phoneNumber>		<faxNumber>(703)111-3333</faxNumber>		</row><row>		<customerID>3</customerID>		<companyName>Company 2</companyName>		<contactFirstName>Jane</contactFirstName>		<contactLastName>Doe</contactLastName>		<billingAddress>5678 Second Street NW</billingAddress>		<city>Washington</city>		<stateOrProvince>DC</stateOrProvince>		<postalCode>10002</postalCode>		<countryRegion>US</countryRegion>		<contactTitle>Mrs</contactTitle>		<phoneNumber>202-111-2222</phoneNumber></row></p1:Customer>";
			break;
		default:
			break;
		}
		String separator_character = ",";
		String qualifier_character = "\"";
		String create_column_headers = "true";
		int total_columns = 12;
		String file_Path = "D:/tmp/CSVFromXML.txt";
		int append = 0;

        try {
	        cp.initialize(null);
	        System.out.println("invoke "+cp.getName());
	        cp.invoke(new Object[] {
	        		new String (xml_string),
	        		new String (separator_character),
	        		new String (qualifier_character),
	        		new String (create_column_headers),
	        		new Integer (total_columns),
	        		new String (file_Path),
	        		new Integer (append),
	        });
       
	        String result = cp.getOutputValues()[0].toString();
	        System.out.println("CSVFromXML Result:");
	        System.out.println(result);
	        
		} catch (Exception ex) {
			System.out.print(ex.toString());
		}
	}
}
