package com.cisco.dvbu.ps.utils.xml;

/*
	FixedFromXMLToFile:
	  Function to parse an XML string and convert the result set to a Fixed length (variable field) string.
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
	  xml_string      - The XML to parse through to create a CSV file
	    values: Any valid XML string.
	
	  format_string   - A string of pipe separated sizes (widths) for each column.  
	  	format: col1_Size|col2_Size|...|coln_Size
	    values: 4|3|10|18
	
	  create_column_headers - Indicates whether to create a column headers row as the first row of the output.
	    values: Any boolean value (such as "true" or "false".)
		default=false
		
	  total_columns    - Total number of columns to produce.  This is a validation against the number of fields queried
	    values: Any integer value
	    
	  filePath - full path to a file that is to be created.				Types.VARCHAR, DIRECTION_IN
	  
	  append - 0=do not append file, 1=append file.						Types.SMALLINT, DIRECTION_IN
		default=false
	
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
import java.util.StringTokenizer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class FixedFromXMLToFile extends XMLUtilTemplate implements CustomProcedure {
  String loginfo = "Done.";
  int error = 0;
  ResultSet rs = null;
  
  static {
    className = "FixedFromXMLToFile";
    logger = Logger.getLogger(FixedFromXMLToFile.class.getName());
  }

  public String getDescription() {
    return "This procedure takes in an XML string, parses it and converts the results to a Fixed length (variable column) string and writes the result to a file.";
  }

  public String getName() {
    return "FixedFromXMLToFile";
  }

  public Object[] getOutputValues() {
    return new Object[] { error };
  }

  public ParameterInfo[] getParameterInfo() {
    return new ParameterInfo[] {
      new ParameterInfo("xml_string", Types.LONGVARCHAR, DIRECTION_IN),
      new ParameterInfo("format_string", Types.VARCHAR, DIRECTION_IN),
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
      String formatString = null;
      boolean createHeaders = false;
      int totalColumns = 0;
      String filePath = null;
      int appendNum = 0;
      boolean append = false;
 
      /*
       * VALIDATE Input parameters for null
       */
  	  if (inputs[0] == null) {
 		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter xml_string must be provided.");
 	  }
  	  if (inputs[1] == null) {
 		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter format_string must be provided.");
 	  }
  	  if (inputs[3] == null) {
  		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter total_columns must be provided.");
  	  }
  	  if (inputs[4] == null) {
		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter file_Path must be provided.");
	  }

      /*
       * RETREIVE Input parameters
       */
      if (inputs[0] != null) {
    	  xmlString = ((String) inputs[0]).trim();
      } 
      
      if (inputs[1] != null) {
    	  formatString = ((String) inputs[1]).trim();
      }

      if (inputs[2] != null) {
          createHeaders = (Boolean.parseBoolean((((String) inputs[2])).trim()));
      }

      if (inputs[3] != null) {
    	  totalColumns = (Integer)inputs[3];
      }
     
      if (inputs[4] != null) {
    	  filePath = inputs[4].toString();
      }

      if (inputs[5] != null) {
    	  appendNum = Integer.valueOf(inputs[5].toString());
          append = (appendNum==1)?true:false;
      }
      
      loginfo += "DEBUG: \nARGS" +
           "\nFormat String: " + formatString +
           "\nCreate Column Headers: " + createHeaders +
           "\nTotal Columns: " + totalColumns +
           "\nFile Path: " + filePath +
           "\nAppend: " + append +
           "\nXML String: " + xmlString + 
           "\n";
 
      /*
       * VALIDATE parameters for content
       */
      // Verify format string is not empty
     if (formatString.length() == 0) {
          throw new IllegalArgumentException ("The Format String must contain a format as follows: col1_Size|col2_Size|...|coln_Size");
      }

      // Validate the number of widths in the format_string vs. the Total Number of Columns passed in and expected.  They should be equal.
      int tokenCount = getTokenCount(formatString);
      if (tokenCount != totalColumns) {
          throw new IllegalArgumentException ("The number of sizes(widths) in the format string ["+tokenCount+"] does not match the expected total_columns ["+totalColumns+"].");
      }

      // if "append" input is false, zero out the input file and change "append"
      // to true so that all subsequent calls to createFileAscii DO append to the file.
      // (otherwise all you get is a file with the last written line, i.e. "")
      //
      if (! append) {
        createFileAscii (filePath, false, "");
        append = true;
      }

      processXMLStringFixed(xmlString, formatString, createHeaders, totalColumns, filePath, append);		
      
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
	 * FIXED FORMAT METHODS
	 ****************************************************************/

  	/****************************************************************
  	 * processXMLStringFixed - 
  	 * 
	 * @param xmlString - The XML string to parse
	 * @param formatString - A string of pipe separated sizes (widths) for each column.  format: col1_Size|col2_Size|...|coln_Size
	 * @param createHeaders - Indicates whether to create a column headers row as the first row of the output.
	 * @param totalColumns - Total number of columns to produce.  This is a validation against the number of fields discovered in the XML.
	 * @param outputFilePath - Full path to a file that is to be created.	
	 * @param append - 0=do not append file, 1=append file.
	 * @throws CustomProcedureException 
	 * @throws SQLException 
	 ****************************************************************/
	@SuppressWarnings("unchecked")
	private void processXMLStringFixed(String xmlString, String formatString, boolean createHeaders, int totalColumns, String outputFilePath, boolean append) throws CustomProcedureException {
	
		Element rootElement = null;
		try {
			rootElement = getDocumentFromString(xmlString);
			if(rootElement != null){
				
				try {
					List<Element> nodes = rootElement.getChildren();
					StringBuffer sb = new StringBuffer();
					
					if (createHeaders) {
						sb = processHeaderFixed(nodes, sb, formatString, totalColumns, outputFilePath, append);
						append = true;					
					}

					int rowCount = 0;
					sb = new StringBuffer();
					sb = processChildNodesFixed(nodes, sb, formatString, totalColumns, outputFilePath, append, rowCount);
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
	private StringBuffer processHeaderFixed(List<Element> childNodes, StringBuffer sb, String formatString, int totalColumns, String outputFilePath, boolean append) throws CustomProcedureException{
		
		int elementCount = 0;
		int rowCount = 0;
		
		for (Element element : childNodes) {
			// Break out of the loop once the first row header is constructed
			if (rowCount == 1) {
				break;
			}
			
			if(element.getChildren() == null || element.getChildren().isEmpty()){
				
				String token = getToken(elementCount, formatString);
				if (token != null) {
						int size = Integer.parseInt(token);

						String columnHeader = (element.getName());
						if (columnHeader.length() > size) {
							columnHeader = columnHeader.substring(0, size);
						}
						sb.append (rpad(columnHeader, size, " "));
				}

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
					throw new CustomProcedureException("Header::The Number of elements extracted from the XML ["+elementCount+"] is more than the expected total elements ["+totalColumns+"].");
				}

			}else{
				rowCount++;
				sb = processHeaderFixed(element.getChildren(), sb, formatString, totalColumns, outputFilePath, append);
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
	private StringBuffer processChildNodesFixed(List<Element> childNodes, StringBuffer sb, String formatString, int totalColumns, String outputFilePath, boolean append, int rowCount) throws CustomProcedureException{
		
		sb = new StringBuffer();	
		String resultTmp = "";
		int elementCount = 0;

		for (Element element : childNodes) {
			if(element.getChildren() == null || element.getChildren().isEmpty()){

				resultTmp = element.getValue();
	
				// Get the next token size from the format string
				String token = getToken(elementCount, formatString);
				if (token != null) {
		        	  
					int size = Integer.parseInt(token);
		   
					// Append if not null
					if (resultTmp != null) {
			
						// Substring the result if larger than size
						if (resultTmp.length() > size) {
			        		resultTmp = resultTmp.substring(0, size);
			        	}
			        	// Apply padding on the result if needed
			        	resultTmp = rpad(resultTmp,size," ");
				        //if (logger.isDebug()) logger.debug ("main_loop::x="+formatString+"  size="+size+"  resultTmp=["+resultTmp+"]");
				        // Comment this line out after debugging is completed
				        //logger.info ("main_loop::x="+x+"  size="+size+"  resultTmp=["+resultTmp+"]");
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
				}
		          
			}else{
				rowCount++;
				sb = processChildNodesFixed(element.getChildren(), sb, formatString, totalColumns, outputFilePath, append, rowCount);
			}
		}
		if (elementCount > 0) {
			if (elementCount < totalColumns) {
				throw new CustomProcedureException("ROW["+rowCount+"]::The Number of elements extracted from the XML ["+elementCount+"] is less than the expected total elements ["+totalColumns+"].");
			}
		}
		return sb;
	}
	

	/**
	 * @return the nth token for a string value
	 * Example:
	 * tokenNum=3
	 * tokenString=4|5|8|9
	 * tokenized string= 4 5 8 9
	 * return the value "8"
	 */
	private String getToken(int tokenNum, String tokenString) {
		String separator = "|";
		if (tokenString.contains(",")) {
			separator = ",";
		}
		// Tokenize a path based on separator
	    StringTokenizer st = new StringTokenizer(tokenString, separator);
	    int i=0;
	    while (st.hasMoreTokens()) {
	    	String token = st.nextToken();
	    	if (i == tokenNum) {
	    		return token;
	    	}	
	    	i++;
	    }
	    return null;
	}
	
	/**
	 * @return the number of tokens in a tokenString
	 * Example:
	 * tokenString=4|5|8|9
	 * return the value "4"
	 */
	private int getTokenCount(String tokenString) {
		String separator = "|";
		if (tokenString.contains(",")) {
			separator = ",";
		}
		// Tokenize a path based on separator
	    StringTokenizer st = new StringTokenizer(tokenString, separator);
	    int i=0;
	    while (st.hasMoreTokens()) {
	    	i++;
	    	st.nextToken();
	    }
	    return i;
	}
	/**
	 * Pad to the right of a string for totalPadAmount using padChar
	 * @param str - string to pad
	 * @param totalPadAmount - the amount to pad
	 * @param padChar - the character to pad with e.g. " "
	 * @return String
	 */
	public static String rpad(String str, int totalPadAmount, String padChar) {
		// Pad a string with spaces starting on the left
		String padStr = str;
		if (padStr.length() < totalPadAmount) {
			String pad = "";
			for (int i=padStr.length(); i < totalPadAmount; i++) {
				pad = pad + padChar;
			}
			padStr = padStr + pad;
		}
		return padStr;
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
		
		CustomProcedure cp = new FixedFromXMLToFile();
		/*
		 * Parameters:
		      new ParameterInfo("xml_string", Types.LONGVARCHAR, DIRECTION_IN),
		      new ParameterInfo("format_string", Types.VARCHAR, DIRECTION_IN),
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
		
		String format_string = "9|50|20|30|50|50|2|20|5|20|14|14";
		String create_column_headers = "true";
		int total_columns = 12;
		String file_Path = "D:/tmp/FixedFromXML.txt";
		int append = 0;

        try {
	        cp.initialize(null);
	        System.out.println("invoke "+cp.getName());
	        cp.invoke(new Object[] {
	        		new String (xml_string),
	        		new String (format_string),
	        		new String (create_column_headers),
	        		new Integer (total_columns),
	        		new String (file_Path),
	        		new Integer (append),
	        });
       
	        String result = cp.getOutputValues()[0].toString();
	        System.out.println("FixedFromXML Result:");
	        System.out.println(result);
	        
		} catch (Exception ex) {
			System.out.print(ex.toString());
		}
	}	
}
