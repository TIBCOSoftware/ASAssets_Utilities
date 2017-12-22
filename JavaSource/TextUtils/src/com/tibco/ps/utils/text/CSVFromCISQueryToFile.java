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
	CSVFromCISQueryToFile:
	  Function to execute a CIS query and convert the result set to a CSV string.
	  Write the result to a file.
	
	Inputs:
	  query_string          - The query to execute. If NULL is passed as input a NULL will be returned.
	    values: Any valid CIS query.
	
	  separator_character   - The character used to separate values.
	    values: Any single character or NULL (defaults to ','.)
		default=','
		
	  qualifier_character   - The character used to qualify values when they contain a separator character.
	    values: Any single character (other than the separator character) or NULL (defaults to '"'.)
		default='"'
		
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
	Date:        10/19/2011
	CSW Version: 5.1.0
	Reason:      Updated to fix issue where data elements containing a qualifier character don't get 
	             qualified (and the existing qualifiers escaped.)
	
	Updated By:  Calvin Goodrich
	Date:        8/23/2012
	CSW Version: 5.2.0
	Reason:      Updated to buffer writing rows to the file to improve write performance.
	
	Updated By:  Calvin Goodrich
	Date:        2/14/2014
	CSW Version: 5.2.0
	Reason:      Updated to fix issue with header row being placed on same line as first line of data.
	             When non-positive number is used for total_columns input, column count validation is bypassed.
	             Date, Time, and Timestamp columns now output in ANSI standard format.
	
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
import java.text.SimpleDateFormat;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class CSVFromCISQueryToFile extends TextUtilTemplate implements CustomProcedure {
  String loginfo = "Done.";
  int error = 1;
  ResultSet rs = null;
  final String NL = System.getProperty("line.separator");

  static {
	className = "CSVFromCISQueryToFile";
	logger = Logger.getLogger(CSVFromCISQueryToFile.class.getName());
  }

  public String getDescription() {
    return "This procedure takes in a CIS query and converts the results to a CSV string and writes the result to a file.";
  }

  public String getName() {
    return "CSVFromCISQueryToFile";
  }

  public Object[] getOutputValues() {
    return new Object[] { error };
  }

  public ParameterInfo[] getParameterInfo() {
    return new ParameterInfo[] {
      new ParameterInfo("query_string", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("separator_character", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("qualifier_character", Types.VARCHAR, DIRECTION_IN),
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
      String separator = ",";
      String qualifier = "\"";
      boolean createHeaders = false;
      int totalColumns = 0;
      String filePath = null;
      int appendNum = 0;
      boolean append = false;
      int bufferSize = 1000;

      /*
       * VALIDATE Input parameters
       */
  	  if (inputs[0] == null) {
  		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter query_string must be provided.");
  	  }
   	  if (inputs[4] == null) {
   		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter total_columns must be provided.");
   	  }
   	  if (inputs[5] == null) {
       throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter file_Path must be provided.");
      }
      if (inputs[6] == null) {
       throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter append must be provided.");
      }

      /*
       * RETREIVE Input parameters
       */
      // Get queryString from input
      if (inputs[0] != null) {
    	  queryString = ((String) inputs[0]).trim();
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
      // Get buffer size from input
      if (inputs[7] != null) {
    	  bufferSize = (Integer)inputs[7];
      }
      loginfo += "DEBUG: \nARGS" +
           "\nQuery: " + queryString + 
           "\nSeparator Character: " + separator +
           "\nQualifier Character: " + qualifier +
           "\nCreate Column Headers: " + createHeaders +
           "\nTotal Columns: " + totalColumns +
           "\nFile Path: " + filePath +
           "\nAppend: " + append +
           "\nBuffer Size: " + bufferSize +
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
      if (bufferSize <= 0) {
    	  throw new IllegalArgumentException ("The buffer size must be a positive integer");
      }

      rs = qenv.executeQuery (queryString, null);
      ResultSetMetaData rsmd = rs.getMetaData();
      int numColumns = rsmd.getColumnCount();
      loginfo += "COLUMN COUNT: " + numColumns;
    
      // Validate the number of columns queried vs. the Total Number of Columns passed in and expected.  They should be equal
      if (totalColumns > 0 && numColumns != totalColumns) {
          throw new IllegalArgumentException ("The Number of columns selected in the query [" + numColumns + "] does not match the expected Total Columns in the format [" + totalColumns + "].");
      }

      if (createHeaders) {
        StringBuffer sb = new StringBuffer();
        for (int x = 0; x < numColumns; x++) {
          if (x > 0) {
            sb.append (separator);
          }
          sb.append (rsmd.getColumnLabel (x + 1));
        }
        // Write the column header line to the file
        createFileAscii (filePath, append, sb.toString() + NL);

        //Now that the file has been written to make sure that additional writes are appended within the context of this procedure.
        append = true;

        if (logger.isDebug()) logger.debug ("createHeaders::"+sb.toString());
        // Comment this line out after debugging is completed
        //logger.info ("createHeaders::"+sb.toString());
      } else {
        createFileAscii (filePath, append, "");
      }

  		BufferedWriter out = new BufferedWriter (new FileWriter (filePath, append));

      int resultSize = 0;
      StringBuffer sb = new StringBuffer();

      SimpleDateFormat dateFormatter = new SimpleDateFormat ("yyyy-MM-dd");
      SimpleDateFormat timeFormatter = new SimpleDateFormat ("HH:mm:ss.SSS z");
      SimpleDateFormat timestampFormatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS z");

      while (rs.next()) {

        for (int x = 0; x < numColumns; x++) {
          if (x > 0) {
            sb.append (separator);
          }
          //String resultTmp = rs.getString (x + 1);
          
          String resultTmp;
          
          switch (rsmd.getColumnType (x + 1)) {
              case Types.DATE:
                resultTmp = dateFormatter.format (rs.getDate (x + 1));
                break;
              
              case Types.TIME:
                resultTmp = timeFormatter.format (rs.getTime (x + 1));
                break;
              
              case Types.TIMESTAMP:
                resultTmp = timestampFormatter.format (rs.getTimestamp (x + 1));
                break;
              
              default: 
                resultTmp = rs.getString (x + 1);
                break;
          }
          
  
          if (resultTmp != null) {
          
            // If the string representation of the result contains a separator, qualifier or newline, then it needs to be qualified. Any existing
            // qualifier characters in the result string need to be escaped (doubled). 
            //
            if (resultTmp.contains (separator) || resultTmp.contains (qualifier) || resultTmp.matches("(?s).*[\\n\\r\\u0085\\u2028\\u2029].*")) {
              resultTmp = resultTmp.replace (qualifier, qualifier + qualifier);
              resultTmp = qualifier + resultTmp + qualifier;
            }        
            sb.append (resultTmp);
          }
        }
        sb.append (NL);
        // Comment this line out after debugging is completed
        //logger.info ("Row::"+sb.toString());
        
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
        error = 1;
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
}
