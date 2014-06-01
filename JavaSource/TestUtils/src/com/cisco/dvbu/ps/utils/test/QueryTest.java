package com.cisco.dvbu.ps.utils.test;

/*
	QueryTest:
	  Tests an input query and verifies the result matches a CSV input.
	
	
	Inputs:
	  query             - The query to test.
	    value: Any valid CIS query. May not be NULL.
	
	  expectedResultCSV - A CSV string containing the expected result.
	    value: A valid CSV string. Field separator is a ',', field qualifier is a '"',
	           row separator is a carriage return. Fields may span multiple lines as
	           long as they are qualified with '"' characters. May not be NULL.
	
	
	Outputs:
	  result            - A string containing either 'OK' signifying this CJP received the
	                      expected result or a CSV string containing the actual results.
	    value: 'OK' or a CSV string.
	
	
	Exceptions:
	  CustomProcedureException - Thrown when invalid inputs are used.
	
	
	Author:      Calvin Goodrich
	Date:        11/23/2010
	CSW Version: 5.1.0

    (c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.
*/


import com.compositesw.common.logging.Logger;
import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class QueryTest
    extends TestUtilTemplate
    implements CustomProcedure
{
    static {
        className = "QueryTest";
        logger = Logger.getLogger (QueryTest.class.getName());
    }

    private String result = null;

    public QueryTest() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug (className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo ("query", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("expectedResultCSV", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("result", Types.VARCHAR, DIRECTION_OUT),
        };
    }

    public void invoke (Object[] inputValues) throws CustomProcedureException, SQLException {
        String query;
        String expectedResultCSV;
        List<List> expectedResult;
        ResultSet actualRS = null;
  
        if (logger.isDebug()) {
            logger.debug (className + ".invoke called");
            logger.debug ("Invoked with query: " + (null==inputValues[0]?"[null]":inputValues[0].toString()) + ", expectedResultCSV: " + (null==inputValues[1]?"[null]":inputValues[1].toString()));
        }

        if (inputValues.length != 2) {
            throw new CustomProcedureException ("incorrect number of parameters passed.");
        }

        query = ((String) inputValues[0]);
        if (null == query) {
            throw new CustomProcedureException ("query must not be NULL");
        }
          
        expectedResultCSV = ((String) inputValues[1]);
        if (null == expectedResultCSV) {
            throw new CustomProcedureException ("expectedResultCSV must not be NULL");
        }
        
        expectedResult = parseExpectedResult (expectedResultCSV);
        
        try {
            actualRS = qenv.executeQuery (query, null);

            result = compareActual2Expected (actualRS, expectedResult);
        } catch (Exception e) {
            throw new CustomProcedureException (e);
        } finally {
            try {
                if (actualRS != null && ! actualRS.isClosed()) { actualRS.close(); }
            } catch (Exception ee) {
                throw new CustomProcedureException (ee);
            }
        }
        
//logger.info ("expected = |" + expectedResultCSV + "|");
//logger.info ("actual = |" + actualResultCSV + "|");
    }

    public Object[] getOutputValues() {
        if (logger.isDebug()) {
            logger.debug (className + ".getOutputValues called");
        }

        return new Object[] { result };
    }

    public String getDescription() {
        if (logger.isDebug()) {
            logger.debug (className + ".getDescription called");
        }
        return "Executes an input query and compares it to an expected result in CSV format. Returns 'OK' or a report of the differences.";
    }

    // parses the expected result CSV and rebuilds it in the same way as parseQueryResult() does.
    // this is so that when the comparison of expected to actual results occurs, we're comparing 
    // apples to apples.
    //
    private List<List> parseExpectedResult (String expectedResultCSV) throws CustomProcedureException {
        String fieldRE;
        String multiLineStartRE;
        String multiLineContinueRE;
        String multiLineEndRE;
        String extractRE;
        Pattern p;
        Matcher m;
        String[] rowStrings;
        String multiTmp = null;
        List<String> tmpRows = new ArrayList<String>();
        List<List> rows = new ArrayList<List>();
        
        fieldRE = "(?s)^\\s*(?=,)|(?<=,)\\s*(?=,)|(?<=,)\\s*$" + // empty value
                  "|" +
                  "(?<=,)?\\s*\"(([^\"])|\"\")*\"\\s*" +     // value with qualifier
                  "|" +
                  "(?<=,)?\\s*([^,]+)\\s*" +                 // value without qualifier
                  "|" +
                  "^\\s*$";                                  // empty line

        multiLineStartRE = "^.*,\\s*\"(\\s|[^\"]|\"\")*?$" + // line that begins with other fields and ends in a qualified CSV field
                           "|" +
                           "^\\s*\"(\\s|[^\"]|\"\")*?$";     // line that begins with a qualified CSV field
        
        multiLineContinueRE = "^(\\s|[^\"]|\"\")*?\".*,\\s*\"(\\s|[^\"]|\"\")*?$";     // line that begins and ends with qualified CSV fields
        
        multiLineEndRE = "^(\\s|[^\"]|\"\")*?\"\\s*$" +      // line that begins with the end of a qualified CSV field
                         "|" +
                         "^(\\s|[^\"]|\"\")*?\"\\s*,.*$";    // line that begins with the end of a qualified CSV field and has additional fields.
        
        extractRE = "(?s)\\A\\s*,?\\s*\"?(.*?)\"?\\s*\\z"; // (?s) enables DOT_ALL mode so that the middle capturing group can capture multiple lines.

        try {
            p = Pattern.compile (fieldRE);
        } catch (PatternSyntaxException pse) {
            throw new CustomProcedureException ("The supplied regular expression cannot be compiled: " + pse.getMessage());
        }
        
        // split on any form of a line separator
        //
        rowStrings = expectedResultCSV.split ("\\r\\n|\\r|\\n|\\u0085|\\u2028|\\u2029", -1);
        
        // find any multi-line qualified fields and glue them back together.
        //
        multiLineSearchLoop:
        for (int r = 0; r < rowStrings.length; r++) {
//logger.info (r);
            String tmp = rowStrings[r];
            
            // if we're not in the middle of a multi-line field and the row string has the start of a
            // multi-line field, start a new multi-line field and skip to the next iteration.
            //
            if (multiTmp == null && tmp.matches (multiLineStartRE)) {
//logger.info ("found start");
                multiTmp = tmp;
                continue multiLineSearchLoop;
            }
            
            // if we're in the middle of a multi-line field and the row string has the end of a
            // multi-line field and ends in the start of another multi-line field, add to the 
            // multi-line field and skip to the next iteration.
            //
            if (multiTmp != null && tmp.matches (multiLineContinueRE)) {
//logger.info ("found continuation");
                multiTmp += "\n" + tmp;
                continue multiLineSearchLoop;
            }
            
            // if we are in the middle of a multi-line field ...
            //
            if (multiTmp != null) {
                multiTmp += "\n" + tmp;
                
                // see if the current line has the end of the multi-line field.
                //
                if (tmp.matches (multiLineEndRE)) {
//logger.info ("found end");
                    tmpRows.add (multiTmp);
                    multiTmp = null;
                }

                continue multiLineSearchLoop;
            }
            
            tmpRows.add (tmp);
        }
        
        rowStrings = new String[tmpRows.size()];
        tmpRows.toArray(rowStrings);
        
        // now loop through rows and separate values
        //
        for (int r = 0; r < rowStrings.length; r++) {
            List<String> al = new ArrayList<String>();
            
logger.info ("row " + r + " = |" + rowStrings[r] + "|");

            m = p.matcher (rowStrings[r]);
            while (m.find()) {

                String tmp = m.group();
logger.info ("found |" + tmp + "|; start = " + m.start() + ", end = " + m.end());
                
                // extract value from found match
                //
                tmp = tmp.replaceFirst (extractRE, "$1");
                
                // if the value contains an escaped qualifier, then unescape it.
                //
                if (tmp.contains ("\"\"")) {
                    tmp = tmp.replaceAll ("\"\"", "\"");
                }
logger.info ("adding |" + tmp + "|");
                
                al.add (tmp);
            }
            
            rows.add (al);
        }
        
        return rows;
    }

    private String compareActual2Expected (ResultSet actualRS, List<List> expectedResult) throws SQLException {
        ResultSetMetaData rsmd = actualRS.getMetaData();
        int numColumns = rsmd.getColumnCount();
        StringBuffer sb = new StringBuffer();
        int numRows = 0;
        
        if (expectedResult.get(0).size() != numColumns) {
            sb.append ("Expected result has " + expectedResult.get(0).size() + " columns and actual result has " + numColumns + "\n");
        }        

        actualRowsLoop: 
        while (actualRS.next()) {
            numRows++;
            
            if (numRows > expectedResult.size()) {
                sb.append ("Actual row " + numRows + " not expected\n");
                continue actualRowsLoop;
            }

            for (int i = 0; i < numColumns; i++) {
                String actual = actualRS.getString (i + 1);
                String expected = null;
                
                if (actual != null) {
                    actual = actual.replaceAll("\\r\\n|\\r|\\u0085|\\u2028|\\u2029", "\\n"); // replaceAll() deals with unexpected line separators.
                }
                
                if (i < expectedResult.get(numRows - 1).size()) {
                    expected = (String) expectedResult.get(numRows - 1).get(i);
                }
                
                if (! ((actual == null && expected == null) ||
                       (actual == null && expected.equals("")) || 
                       (actual == null && expected.equals("[NULL]")) || 
                       (actual != null && actual.equals("") && expected == null) ||
                       (actual != null && actual.equals(expected))
                      )
                ) {
                    sb.append (" row " + numRows + ", col " + i + ": value mismatch: expected = \"" + ((expected == null) ? "[NULL]" : expected) + "\", actual = \"" + ((actual == null) ? "[NULL]" : actual) + "\"");
                }
            }
        }
        
        if (expectedResult.size() != numRows) {
            sb.append ("Expected result has " + expectedResult.size() + " rows and actual result has " + numRows + "\n");
        }
            
        return (sb.length() == 0) ? "OK" : "FAILED: " + sb.toString();
    }
}