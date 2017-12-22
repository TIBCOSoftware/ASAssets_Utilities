package com.tibco.ps.utils.file;

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
 * Source File: ExistsDir.java
 *
 * Description: This Composite CJP tests for the existence of a directory in the file system.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *	Input:
 *		dirPath - full path to a directory to test for existence.				Types.VARCHAR, DIRECTION_IN
 *
 *	Output: 
 *		success - true/1=directory exists, false/0=directory does not exist. 	Types.BOOLEAN, DIRECTION_OUT
 *
 *	Exceptions:  CustomProcedureException, SQLException
 *	Author:      Mike Tinius
 *	Date:        8/6/2010
 *	CSW Version: 5.1.0
 *
 */

import java.io.File;
import java.sql.SQLException;
import java.sql.Types;

import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;
import com.compositesw.extension.CustomCursor;;

public class ExistsDir implements CustomProcedure
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
	     return "existsDir";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP tests for the existence of a directory in the file system.";
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
		existsDir(dirPath);
	}
	
	public  boolean existsDir(String dirPath) 
									throws CustomProcedureException, SQLException {
		  	
    	success = false;
    	try
    	{
    	    // Destination Directory
    	    File dir = new File(dirPath);

    	    // Check to make sure the destination directory exists
    	    if (dir.exists()) {
    	    	success = true;
    	    }      	
    	}
    	catch(Exception ex)
    	{
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
		CustomProcedure cp = new ExistsDir();
		String param1 = "/tmp";
		
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
 



