package com.cisco.dvbu.ps.utils.text;

/*
	FixedFromCISQueryToFile:
	  Function to execute a CIS query and convert the result set to a Fixed length string with variable column widths.
	  Write the result to a file.
	
	Inputs:
	  query_string          - The query to execute. If NULL is passed as input a NULL will be returned.
	    values: Any valid CIS query.
	
	  format_string         - A string of pipe separated sizes (widths) for each column.  
	  	format: col1_Size|col2_Size|...|coln_Size
	    values: 4|3|10|18
	
	  create_column_headers - Indicates whether to create a column headers row as the first row of the output.
	    values: Any boolean value (such as "true" or "false".)
		default=false
		
	  total_columns         - Total number of columns to produce.  This is a validation against the number of fields queried
	    values: Any positive integer.
	    
	  filePath              - full path to a file that is to be created.
	    values: Any valid filesystem path to a flat text file (existing or not.)
	  
	  append                - Indicates whether or not to append to the file at filePath.
	    values: 0=do not append file, 1=append file.
	
	  buffer_size           - Number of rows to buffer before writing to the file.
	    values: Any positive integer.
	
	
	Output:
	  result                - Indicates success or failure
	    values: 1 or 0
	
	
	Exceptions:
	  CustomProcedureException - Thrown when illegal arguments are passed.
	
	
	Author:      Mike Tinius
	Date:        8/1/2011
	CSW Version: 5.2.0
	
	Updated By:  Calvin Goodrich
	Date:        8/23/2012
	CSW Version: 5.2.0
	Reason:      Updated to buffer writing rows to the file to improve write performance.
	
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

import com.compositesw.common.logging.Logger;
import com.compositesw.extension.CustomProcedure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.StringTokenizer;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class FixedFromCISQueryToFile extends TextUtilTemplate implements CustomProcedure {
  String loginfo = "Done.";
  int error = 1;
  ResultSet rs = null;
  
  static {
    className = "FixedFromCISQueryToFile";
    logger = Logger.getLogger(FixedFromCISQueryToFile.class.getName());
  }

  public String getDescription() {
    return "This procedure takes in a CIS query and converts the results to a Fixed length (variable column) string and writes the result to a file.";
  }

  public String getName() {
    return "FixedFromCISQueryToFile";
  }

  public Object[] getOutputValues() {
    return new Object[] { error };
  }

  public ParameterInfo[] getParameterInfo() {
    return new ParameterInfo[] {
      new ParameterInfo("query_string", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("format_string", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("create_column_headers", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("total_columns", Types.INTEGER, DIRECTION_IN),
      new ParameterInfo("file_Path", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("append", Types.SMALLINT, DIRECTION_IN),
      new ParameterInfo("buffer_size", Types.INTEGER, DIRECTION_IN),
      new ParameterInfo("result", Types.INTEGER, DIRECTION_OUT) 
    };
  }

  public void invoke(Object[] inputs) throws CustomProcedureException, SQLException {
    try {
      String queryString = null;
      String format_string = null;
      boolean createHeaders = false;
      int totalColumns = 0;
      String filePath = null;
      int appendNum = 0;
      boolean append = false;
      int bufferSize = 1000;
 
      /*
       * VALIDATE Input parameters for null
       */
  	  if (inputs[0] == null) {
       throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter query_string must be provided.");
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
      if (inputs[5] == null) {
       throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter append must be provided.");
      }

      /*
       * RETREIVE Input parameters
       */
      if (inputs[0] != null) {
    	  queryString = ((String) inputs[0]).trim();
      } 
      
      if (inputs[1] != null) {
    	  format_string = ((String) inputs[1]).trim();
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
      
      // Get buffer size from input
      if (inputs[6] != null) {
    	  bufferSize = (Integer)inputs[6];
      }
      loginfo += "DEBUG: \nARGS" +
           "\nQuery: " + queryString + 
           "\nFormat String: " + format_string +
           "\nCreate Column Headers: " + createHeaders +
           "\nTotal Columns: " + totalColumns +
           "\nFile Path: " + filePath +
           "\nAppend: " + append +
           "\nBuffer Size: " + bufferSize +
           "\n";
 
      /*
       * VALIDATE parameters for content
       */
      // Verify format string is not empty
      if (format_string.length() == 0) {
          throw new IllegalArgumentException ("The Format String must contain a format as follows: col1_Size|col2_Size|...|coln_Size");
      }
      if (bufferSize <= 0) {
    	  throw new IllegalArgumentException ("The buffer size must be a positive integer");
      }

      // Validate the number of widths in the format_string vs. the Total Number of Columns passed in and expected.  They should be equal.
      int tokenCount = getTokenCount(format_string);
      if (totalColumns > 0 && tokenCount != totalColumns) {
          throw new IllegalArgumentException ("The number of sizes(widths) in the format string ["+tokenCount+"] does not match the expected total_columns ["+totalColumns+"].");
      }

      rs = qenv.executeQuery (queryString, null);
      ResultSetMetaData rsmd = rs.getMetaData();
      int numColumns = rsmd.getColumnCount();
      loginfo += "QUERY COLUMN COUNT: " + numColumns;
            
      // Validate the number of columns queried vs. the Total Number of Columns passed in and expected.  They should be equal
      if (numColumns != totalColumns) {
          throw new IllegalArgumentException ("The Number of columns selected in the query ["+numColumns+"] does not match the expected total_columns ["+totalColumns+"].");
      }
      
      if (createHeaders) {
        StringBuffer sb = new StringBuffer();
        for (int x = 0; x < numColumns; x++) {
          String token = getToken(x, format_string);
          if (token != null) {
        	  int size = Integer.parseInt(token);

        	  String columnHeader = (rsmd.getColumnLabel (x + 1)).toString().trim();
        	  if (columnHeader.length() > size) {
        		  columnHeader = columnHeader.substring(0, size);
        	  }
        	  sb.append (rpad(columnHeader, size, " "));
          }
        }
        // Write the column header line to the file
        createFileAscii(filePath, append, sb.toString());
        //Now that the file has been written to make sure that additional writes are appended within the context of this procedure.
        append = true;

        if (logger.isDebug()) logger.debug ("createHeaders::"+sb.toString());
        // Comment this line out after debugging is completed
        //logger.info ("createHeaders::"+sb.toString());
      } else {
        createFileAscii(filePath, append, "");
      }

  		BufferedWriter out = new BufferedWriter(new FileWriter(filePath, append));

      int resultSize = 0;
      StringBuffer sb = new StringBuffer();

      while (rs.next()) {
        for (int x = 0; x < numColumns; x++) {
          
          String resultTmp = (rs.getString (x + 1)).toString().trim();
   
          // Get the next token size from the format string
          String token = getToken(x, format_string);
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

              if (logger.isDebug()) logger.debug ("main_loop::x="+x+"  size="+size+"  resultTmp=["+resultTmp+"]");
              // Comment this line out after debugging is completed
              //logger.info ("main_loop::x="+x+"  size="+size+"  resultTmp=["+resultTmp+"]");

	            sb.append (resultTmp);
	          }
          }
        }
        sb.append("\n");

        resultSize++;

        // Write the buffer to the file
        if (resultSize % bufferSize == 0) {
          out.write(sb.toString());
          sb = new StringBuffer();
        }

        //Now that the file has been written to make sure that additional writes are appended within the context of this procedure.
        append = true;
      }

      // Write remaining buffer to the file
      if (resultSize % bufferSize > 0)
        out.write(sb.toString());

      loginfo += "\nROWCOUNT = " + resultSize;
 
  		out.close();
    
    } catch (Throwable t) {
        error = 0;
        String message = "Exception Occurred:";
        if (t.getMessage() != null) message = message + " " + t.getMessage();
        message = message+"\n";
        logger.info (message+loginfo);
        t.printStackTrace();
        throw new CustomProcedureException(message+loginfo);
     
      } finally {
        try{
            if (rs != null) {
            	if (! rs.isClosed()) { rs.close(); }
            }
        } catch (Throwable t) {
          error = 0;
          String message = "Exception Occurred:";
          if (t.getMessage() != null) message = message + " " + t.getMessage();
          message = message+"\n";
          logger.info (message+loginfo);
          t.printStackTrace();
          throw new CustomProcedureException(message+loginfo);
        }

        qenv.log (LOG_DEBUG, loginfo);
        
        // Comment this line out after debugging is completed
        //logger.info (loginfo);
      }
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
			System.out.println("Your file has been written");        	
		}
		catch(IOException ex)
		{
			throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
		}	        
	}
}
