package com.cisco.dvbu.ps.utils.text;

/*
	CSVFromCISQuery:
	  Function to execute a CIS query and convert the result set to a CSV string.
	
	
	Inputs:
	  query_string          - The query to execute. If NULL is passed as input a NULL will be returned.
	    values: Any valid CIS query.
	
	  separator_character   - The character used to separate values.
	    values: Any single character or NULL (defaults to ','.)
	
	  qualifier_character   - The character used to qualify values when they contain a separator character.
	    values: Any single character (other than the separator character) or NULL (defaults to '"'.)
	
	  create_column_headers - Indicates whether to create a column headers row as the first row of the output.
	    values: Any boolean value (such as "true" or "false".)
	
	
	Output:
	  result                - The result set converted to a CSV string.
	    values: A CSV string.
	
	
	Exceptions:
	  CustomProcedureException - Thrown when illegal arguments are passed.
	
	
	Author:      Owen Taylor
	Date:        11/4/2010
	CSW Version: 5.1.0
	
	Updated By:  Mike Tinius 
	Date:        8/1/2011
	CSW Version: 5.1.0
	Reason:      Updated to fix issue with using TAB character as a separator.
	
	Updated By:  Calvin Goodrich 
	Date:        10/19/2011
	CSW Version: 5.1.0
	Reason:      Updated to fix issue where data elements containing a qualifier character don't get 
	             qualified (and the existing qualifiers escaped.)
	
	(c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.

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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class CSVFromCISQuery extends TextUtilTemplate implements CustomProcedure {
  String loginfo = "Done.";
  String result = null;
  ResultSet rs = null;

  static {
	className = "CSVFromCISQuery";
	logger = Logger.getLogger(CSVFromCISQuery.class.getName());

  }

  public String getDescription() {
    return "This procedure takes in a CIS query and converts the results to a CSV string";
  }

  public String getName() {
    return "CSVFromCISQuery";
  }

  public Object[] getOutputValues() {
    return new Object[] { result };
  }

  public ParameterInfo[] getParameterInfo() {
    return new ParameterInfo[] {
      new ParameterInfo("query_string", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("separator_character", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("qualifier_character", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("create_column_headers", Types.VARCHAR, DIRECTION_IN),
      new ParameterInfo("result", Types.VARCHAR, DIRECTION_OUT) 
    };
  }

  public void invoke(Object[] inputs) throws CustomProcedureException, SQLException {
    try {
      String queryString = null;
      String separator = ",";
      String qualifier = "\"";
      boolean createHeaders = false;
      StringBuffer sb = new StringBuffer();

      /*
       * VALIDATE Input parameters for null
       */
  	  if (inputs[0] == null) {
  		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter query_string must be provided.");
  	  }

      /*
       * RETREIVE Input parameters
       */
      if (inputs[0] != null) {
    	  queryString = ((String) inputs[0]).trim();
      } 

      if (inputs[1] != null) {
        separator = (String) inputs[1];
      }

      if (inputs[2] != null) {
        qualifier = ((String) inputs[2]).trim();
      }

      if (inputs[3] != null) {
        createHeaders = (Boolean.parseBoolean((((String) inputs[3])).trim()));
      }

      loginfo += "DEBUG: \nARGS" +
           "\nQuery: " + queryString + 
           "\nSeparator Character: " + separator +
           "\nQualifier Character: " + qualifier +
           "\nCreate Column Headers: " + createHeaders + "\n";
  
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

      rs = qenv.executeQuery (queryString, null);
      ResultSetMetaData rsmd = rs.getMetaData();
      int numColumns = rsmd.getColumnCount();
      loginfo += "COLUMN COUNT: " + numColumns;
    
      if (createHeaders) {
        for (int x = 0; x < numColumns; x++) {
          if (x > 0) {
            sb.append (separator);
          }
          sb.append (rsmd.getColumnLabel (x + 1));
        }
        sb.append ("\n");
        logger.info ("createHeaders::"+sb.toString());
      }

      int resultSize = 0;
      while (rs.next()) {
        for (int x = 0; x < numColumns; x++) {
          if (x > 0) {
            sb.append (separator);
          }
          
          String resultTmp = rs.getString (x + 1);
        
          if (resultTmp != null) {
          
            // If the string representation of the result contains a separator, qualifier, or newline, then it needs to be qualified. Any existing
            // qualifier characters in the result string need to be escaped (doubled). 
            //
            if (resultTmp.contains (separator) || resultTmp.contains (qualifier) || resultTmp.matches("(?s).*[\\n\\r\\u0085\\u2028\\u2029].*")) {
              resultTmp = resultTmp.replace (qualifier, qualifier + qualifier);
              resultTmp = qualifier + resultTmp + qualifier;
            }        
            sb.append (resultTmp);
         }
        }
        resultSize++;
        sb.append ("\n");
        logger.info ("Row::"+sb.toString());
      }
      loginfo += "\nROWCOUNT = " + resultSize;
        
      result = sb.toString();
    
    } catch (Throwable t) {
        /*
        String message = "Exception Occurred:";
        if (t.getMessage() != null) message = message + " " + t.getMessage();
        message = message+"\n";
        logger.info (message+loginfo);
        t.printStackTrace();
        throw new CustomProcedureException(message+loginfo);
        */

        logger.info (t.getMessage() + "\n" + loginfo);
        throw new CustomProcedureException (t);

    } finally {
      try{
        if (rs != null) {
        	if (! rs.isClosed()) { rs.close(); }
        }
      } catch (Throwable t) {
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
}
