package com.cisco.dvbu.ps.utils.log;
/*
 * Source File: GetServerMetadataLog.java
 *
 * Description: This procedure parses the <CIS_HOME>/logs/cs_server_metadata.log file(s) and outputs the parsed files as a result set.
 *
 *  Input:
 *    N/A
 *
 *  Output: 
 *    result    - A cursor with the contents of the metadata change logs.
 *      Values: CURSOR (
 *                  change_time   TIMESTAMP   - The time that the change occurred
 *                  cid           INTEGER     - The internal CIS change ID
 *                  domain        LONGVARCHAR - The security domain of the user making the change
 *                  user          LONGVARCHAR - The user name of the user making the change
 *                  userid        INTEGER     - The internal CIS ID of the user
 *                  hostname      LONGVARCHAR - The host/IP that the user made the change from
 *                  operation     LONGVARCHAR - The type of change made: CREATED, UPDATED, DELETED, or IMPORTING
 *                  resource_id   INTEGER     - The internal CIS ID of the resource being changed
 *                  resource_path LONGVARCHAR - The path to the resource
 *                  resource_type LONGVARCHAR - The type of the resource
 *                  message       LONGVARCHAR - Any additional messaging (CREATED and UPDATED operations sometimes have privileges settings, IMPORTING operations will have information about the import.)
 *              )
 *
 *  Exceptions:  CustomProcedureException, SQLException
 *  
 *  Modified Date:  Modified By:        CSW Version:    Reason:
 *  04/15/2015      Calvin Goodrich     7.0.1           Created new
 *
 *  (c) 2015 Cisco and/or its affiliates. All rights reserved.

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.compositesw.extension.CustomCursor;
import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;


public class GetServerMetadataLog implements CustomProcedure {
    private ExecutionEnvironment qenv;
    private ResultCursor result = null;

    public GetServerMetadataLog() {
    }

    /**
     * Called during introspection to get the short name of the stored
     * procedure. This name may be overridden during configuration. Should not
     * return null.
     */
    public String getName() {
        return "GetServerMetadataLog";
    }
    
    
    /**
     * This is called once just after constructing the class. The environment
     * contains methods used to interact with the server.
     */
    public void initialize(ExecutionEnvironment qenv) {
        this.qenv = qenv;
    }

    /**
     * Called during introspection to get the description of the input and
     * output parameters. Should not return null.
     */
    public ParameterInfo[] getParameterInfo() {
        return new ParameterInfo[] {
            new ParameterInfo ("result", TYPED_CURSOR, DIRECTION_OUT,
                new ParameterInfo[] {
                    new ParameterInfo("change_time", Types.TIMESTAMP, DIRECTION_NONE),
                    new ParameterInfo("cid", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo("domain", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo("user", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo("userid", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo("hostname", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo("operation", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo("resource_id", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo("resource_path", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo("resource_type", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo("message", Types.VARCHAR, DIRECTION_NONE)
                }
            )
        };
    }

    /**
     * Called to invoke the stored procedure. Will only be called a single time
     * per instance. Can throw CustomProcedureException or SQLException if there
     * is an error during invoke.
     */
    public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException {
        result = new ResultCursor ();
    }

    /**
     * Called to retrieve the number of rows that were inserted, updated, or
     * deleted during the execution of the procedure. A return value of -1
     * indicates that the number of affected rows is unknown. Can throw
     * CustomProcedureException or SQLException if there is an error when
     * getting the number of affected rows.
     */
    public int getNumAffectedRows() {
        return 0;
    }

    /**
     * Called to retrieve the output values. The returned objects should obey
     * the Java to SQL typing conventions as defined in the table above. Output
     * cursors can be returned as either CustomCursor or java.sql.ResultSet. Can
     * throw CustomProcedureException or SQLException if there is an error when
     * getting the output values. Should not return null.
     */

    public Object[] getOutputValues() {
        return new Object[] { result };
    }

    /**
     * Called when the procedure reference is no longer needed. Close may be
     * called without retrieving any of the output values (such as cursors) or
     * even invoking, so this needs to do any remaining cleanup. Close may be
     * called concurrently with any other call such as "invoke" or
     * "getOutputValues". In this case, any pending methods should immediately
     * throw a CustomProcedureException.
     */
    public void close() throws SQLException {
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure. Should not return null.
     */
    public String getDescription() {
        return "This procedure parses the <CIS_HOME>/logs/cs_server_metadata.log files and returns the results in a cursor.";
    }

    //
    // Transaction methods
    //
    /**
     * Returns true if the custom procedure uses transactions. If this method
     * returns false then commit and rollback will not be called.
     */
    public boolean canCommit() {
        return false;
    }

    /**
     * Commit any open transactions.
     */
    public void commit() {
    }

    /**
     * Rollback any open transactions.
     */
    public void rollback() {
    }

    /**
     * Returns true if the transaction can be compensated.
     */
    public boolean canCompensate() {
        return false;
    }

    /**
     * Compensate any committed transactions (if supported).
     */
    public void compensate (ExecutionEnvironment qenv) {
    }

    // implements a streaming cursor that executes the input query and iterates through
    // the result rows as the next() method is called. This is returned to the calling
    // thread as a ResultSet object.
    //
    private class ResultCursor implements CustomCursor {
    	private final String HEADER_RE = "^\\s*(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) " + // timestamp
                                         "(?:" +
    			                             "([^/]+)/([^\\s]+) " + // domain / username
                                             "(?:" +
                                                 "\\((\\-?\\d+)\\) " +  // userid
    			                                 "(?:at ([^\\s]+))? " + // sometimes the hostname is listed
                                                 "saved following changes:" +
    			                             "|" +
				                                 "((?:UN)?LOCKED) " + // importing or unlocked operation
	                                             "(\\w+) " + // resource type
				                                 "'?(.*)'?" + // resource path
	                                             "(?: Comment:(.*))?" + // message
                                             ")" +
                                         "|" + 
                                             "(IMPORTING)" +
                                         ")\\s*$";
    	private final String CID_RE = "^\\s*\\d+ (\\d+)\\s*$";
    	private final String OPERATION_RE = "^\\s*(\\w+) (\\w+) (.*) \\((\\d+)\\)\\s*$";
    	private final String ALL_RE = "(?:" + HEADER_RE + ")|(?:" + CID_RE + ")|(?:" + OPERATION_RE + ")";

        private File[] logFiles = null;
        private int fileInd = 0;
        private Pattern headerPattern = Pattern.compile(HEADER_RE);
        private Pattern cidPattern = Pattern.compile(CID_RE);
        private Pattern operationPattern = Pattern.compile(OPERATION_RE);
        private Pattern allPattern = Pattern.compile(ALL_RE);
        private BufferedReader br = null;
        String line = null;
        private Timestamp change_time = null;
        private int cid = -1;
        private String domain = null;
        private String user = null;
        private int userid = -1;
        private String hostname = null;
    
        public ResultCursor() {
        	
        	// get the CIS installation folder
        	//
    		String installFolder = System.getProperty ("apps.install.dir"); 
    		
    		// get the list of metadata log files from the logs folder
    		//
    		File dir = new File (installFolder + File.separatorChar + "logs");
    		FileFilter ff = new WildcardFileFilter ("cs_server_metadata.log*");
    		logFiles = dir.listFiles (ff);
    		
    		// sort the list by date in ascending order
    		//
    		Arrays.sort(logFiles, new Comparator<File> () {
    			public int compare (File f1, File f2) {
    				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
    			}
    		});
        }
        
        public ParameterInfo[] getColumnInfo() {
            return new ParameterInfo[] {
                new ParameterInfo("change_time", Types.TIMESTAMP, DIRECTION_NONE),
                new ParameterInfo("cid", Types.INTEGER, DIRECTION_NONE),
                new ParameterInfo("domain", Types.VARCHAR, DIRECTION_NONE),
                new ParameterInfo("user", Types.VARCHAR, DIRECTION_NONE),
                new ParameterInfo("userid", Types.INTEGER, DIRECTION_NONE),
                new ParameterInfo("hostname", Types.VARCHAR, DIRECTION_NONE),
                new ParameterInfo("operation", Types.VARCHAR, DIRECTION_NONE),
                new ParameterInfo("resource_id", Types.INTEGER, DIRECTION_NONE),
                new ParameterInfo("resource_path", Types.VARCHAR, DIRECTION_NONE),
                new ParameterInfo("resource_type", Types.VARCHAR, DIRECTION_NONE),
                new ParameterInfo("message", Types.VARCHAR, DIRECTION_NONE)
            };
        }
        
        public Object[] next() throws CustomProcedureException, SQLException {
            List<Object> outputRow = new ArrayList<Object>();
            Matcher hm = null, cm = null, om = null, am = null;
            String operation = null;
            int resource_id = -1;
            String resource_path = null;
            String resource_type = null;
            String message = null;
            
            // if line is null, meaning the first line hasn't been read yet, get a line from the first log file.
            //
            if (line == null) {
    	        if ((line = readLineFromLog()) == null)
    	        	return null;
            }
            
            // skip over empty lines
            //
            while (line.matches("^\\s*$")) {
    	        if ((line = readLineFromLog()) == null)
    	        	break;
            }

            // attempt to match the timestamp, and either the user and hostname or the keywords UNLOCKED or IMPORTING (imports don't identify user/host for some reason.)
            //
            hm = headerPattern.matcher (line);
            if (hm.find()) {
            	change_time = Timestamp.valueOf (hm.group (1));
            	domain = hm.group (2);
            	user = hm.group (3);
            	userid = (hm.group (4) != null) ? Integer.valueOf (hm.group (4)).intValue() : -1;
            	hostname = hm.group (5);
            	operation = (hm.group (6) != null) ? hm.group (6) : hm.group (10);
            	resource_type = hm.group (7);
            	resource_path = hm.group (8);
            	message = hm.group (9);

                line = readLineFromLog();
            }
            
            // the line after the header contains the previous and current change id.
            //
            if (line != null) {
	            cm = cidPattern.matcher (line);
	            if (cm.find()) {
	            	cid = (cm.group (1) != null) ? Integer.valueOf (cm.group (1)).intValue() : -1;
	            	
	                line = readLineFromLog();
	            }
            }

            // the lines following the change id list what resources changed.
            //
            if (line != null) {
	            om = operationPattern.matcher (line);
	            if (om.find()) {
	            	operation = om.group (1);
	            	resource_type = om.group (2);
	            	resource_path = om.group (3).replaceAll("'", ""); // the DELETE operation puts single quotes around the resource path for some reason.
	            	resource_id = (om.group (4) != null) ? Integer.valueOf (om.group (4)).intValue() : -1;
	            	
	                line = readLineFromLog();
	            }
            }
            
            // there are also lines that list out import details or privilege settings. these are put into the "message" column.
            //
            if (line != null) {
	            am = allPattern.matcher(line);
	            if (! am.find()) {
	            	message = line;
	
	            	while ((line = readLineFromLog()) != null) {
	            		
	            		// skip extra lines
	            		//
	            		if (line.matches("^\\s*$"))
	            			continue;
	
	            		am = allPattern.matcher(line);
	
	            	    if (! am.find()) {
	            			message += line + "\n";
	            		} else {
	            			break;
	            		}
	            	}
	            }
            }

            outputRow.add (change_time);
            outputRow.add (cid);
            outputRow.add (domain);
            outputRow.add (user);
            outputRow.add (userid);
            outputRow.add (hostname);
            outputRow.add (operation);
            outputRow.add (resource_id);
            outputRow.add (resource_path);
            outputRow.add (resource_type);
            outputRow.add (message);
            
            return outputRow.toArray();
        }
    
        public void close() throws SQLException {
        	try {
	        	if (br != null) br.close();
        	} catch (IOException ioe) {
        		throw new SQLException (ioe);
        	}
        }
        
        private String readLineFromLog() throws CustomProcedureException {
        	String line;

            // make sure a log file is opened for reading and a line is read from it.
            //
            try {
	            // if the file reader is null or if reading a line from the log file returns a null, switch to the next file in the list
	            //
	            if (br == null || (line = br.readLine()) == null) {
	            	
	            	// if the file reader is still null after the switch or reading a line from the new file returns a null, then we're 
	            	// out of files and should indicate the end of the result set. there's a side effect that if a log file is empty, then
	            	// this proc will close out the result set before running out of files. this is an extremely unlikely situation.
	            	//
	            	if ((br = switchFiles()) == null || (line = br.readLine()) == null) {
	            		return null;
	            	}
	            }
            } catch (IOException ex) {
            	throw new CustomProcedureException (ex);
            }
            
            return line;
        }
        
        private BufferedReader switchFiles() {
        	br = null;

            if (fileInd < logFiles.length) {
            	try {
        	        br = new BufferedReader(new FileReader(logFiles[fileInd]));
            	} catch (Exception ignored) { ; } // throws FileNotFound exception. might be a race condition, but very unlikely.

            	fileInd++;
            }
            
            return br;
        }
    }
}