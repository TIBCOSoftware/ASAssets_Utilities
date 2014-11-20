package com.cisco.dvbu.ps.utils.file;
/*
 * Source File: MakeDirs.java
 *
 * Description: This Composite CJP makes a directory and it's sub-directories in the file system.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *	Input:
 *		dirPath - directory path to create.									Types.VARCHAR, DIRECTION_IN
 *
 *	Output: 
 *		success - true/1=directory created, false/0=failed w/exception. 	Types.BOOLEAN, DIRECTION_OUT
 *
 *	Exceptions:  CustomProcedureException, SQLException
 *	Author:      Mike Tinius
 *	Date:        8/6/2010
 *	CSW Version: 5.1.0
 *
 *  (c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.

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
import com.compositesw.extension.*;

import java.sql.SQLException;
import java.sql.Types;
import java.io.*;

public class MakeDirs implements CustomProcedure
{
	private ExecutionEnvironment qenv;
	public String dirPath;
	boolean success;

	public void initialize(ExecutionEnvironment qenv){
	  this.qenv = qenv;
	}
	
	 //
	 // Introspection methods
	 //	
	 public String getName() {
	     return "makeDirs";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP makes a directory and it's sub-directories in the file system.";
	 }
	 
	 //
	 // Transaction methods
	 //	
	 public boolean canCommit() {
	     return false;
	 }
	
	 public void commit() throws CustomProcedureException, SQLException {
	 }
	
	 public void rollback() throws CustomProcedureException, SQLException {
	 }
	
	 public boolean canCompensate() {
	     return false;
	 }
	
	 public void compensate(ExecutionEnvironment executionEnvironment) throws CustomProcedureException,
	                                                                          SQLException {
	 }

	public ParameterInfo[] getParameterInfo(){
	  return new ParameterInfo[]{
		   new ParameterInfo("dirPath", 		Types.VARCHAR, DIRECTION_IN),
		   new ParameterInfo("success", 		Types.BOOLEAN, DIRECTION_OUT)
	  };
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
	{
		if (inputValues[0] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter dirPath must be provided.");
		}
		dirPath = inputValues[0].toString();
		makeDir(dirPath);
	}
	
	public  boolean makeDir(String dirPath) 
			throws CustomProcedureException, SQLException {

    	success = false;
    	try {
    	    // Destination Directory
    	    File dir = new File(dirPath);

    	    // Check to make sure the destination directory exists
    	    if (dir.exists()) {
    	    	success = true;
    	    } else {
    	    	dir.mkdirs();
    	    	success = true;
    	    }      	
    	} catch(Exception ex) {
    		throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
    	}

    	return success;
	}
	
	public int getNumAffectedRows(){
		return 0;
	}

	public Object[] getOutputValues(){
		return new Object[] { success };
	}

	public void close() throws SQLException {}
	
	public static void main(String[] args) {
		CustomProcedure cp = new MakeDirs();
		String param1 = "/tmp/dir1/dir2";

		ParameterInfoDisplay pc = new ParameterInfoDisplay(); 
        try {
	        ParameterInfo[] pia = cp.getParameterInfo();
	        pc.displayParameterInfo(pia);

	        cp.initialize(null);
            System.out.println("invoke "+cp.getName());
	        cp.invoke(new Object[] {
	        		new String (param1)
	        });

	        boolean result = Boolean.valueOf(cp.getOutputValues()[0].toString());
	        System.out.println("Result="+result);
/*	//This code is not applicable       
	        System.out.println("Output Values:");
	        CustomCursor cc = (CustomCursor)(cp.getOutputValues()[0]);
	        Object[] data = cc.next();
	        while (data != null) {
	        	System.out.print("    ");
	            for(Object o : data) {
	                System.out.print((o!=null?o.toString():"null") + " ");
	            }
	            System.out.println("");
	            data = cc.next();
	        }
*/       
		} catch (Exception ex) {
			System.out.print(ex.toString());
		}		
	}
	
} // end of class
