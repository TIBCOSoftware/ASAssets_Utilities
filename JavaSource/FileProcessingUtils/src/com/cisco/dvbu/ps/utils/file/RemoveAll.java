package com.cisco.dvbu.ps.utils.file;
/*
 * Source File: RemoveAll.java
 *
 * Description: This Composite CJP removes all the files and directories from the file system 
 * 		starting at the path provided.  Optionally, the caller can choose to only delete files
 * 		and leave the directory structure in place.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *	Input:
 *		dirPath - directory path to remove.									Types.VARCHAR, DIRECTION_IN
 *		removeDirs - Y=remove all files and directories.					Types.VARCHAR, DIRECTION_IN
 *					 N=remove only files and leave directory structures in place.
 *
 *	Output: 
 *		success - true/1=directory removed, false/0=failed w/exception. 	Types.BOOLEAN, DIRECTION_OUT
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

public class RemoveAll implements CustomProcedure
{
	private ExecutionEnvironment qenv;
	public String dirPath;
	public boolean removeDirs;
	boolean success;

	public void initialize(ExecutionEnvironment qenv){
	  this.qenv = qenv;
	}
	
	 //
	 // Introspection methods
	 //	
	 public String getName() {
	     return "removeAll";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP removes all files and directories from the file system starting at the given directory path.";
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
			new ParameterInfo("removeDirs", 	Types.VARCHAR, DIRECTION_IN),
			new ParameterInfo("success", 		Types.BOOLEAN, DIRECTION_OUT)
	  };
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
	{
		if (inputValues[0] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter dirPath must be provided.");
		}
		if (inputValues[1] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter removeDirs must be provided.");
		}
		dirPath = inputValues[0].toString();
		if (inputValues[1].toString().toUpperCase().substring(0, 1).equalsIgnoreCase("Y")) {
			removeDirs = true;
		} else {
			removeDirs = false;
		}
		
		rmAll(dirPath, removeDirs);
		File fsObject = new File(dirPath);
   	    if ( (removeDirs) & (fsObject.exists()) & (fsObject.isDirectory()) ) {
	    		// remove the top level directory
	    		success = fsObject.delete();
   	    }
	}
	
	public  boolean rmAll(String dirPath, boolean removeDirs) throws CustomProcedureException, SQLException {
 		// Initialize the file/directory list
 		File[] files = null;
 		// Initialize the file system object (file or directory)
 		File fsObject = null;
 		// Initialize success
    	success = false;
    	try
    	{
    		// Get file info for the given path
    		files = getFileInfo(dirPath);
 
    		for (int i=0; i < files.length; i++) {
       	    	// Destination Directory
    	    	fsObject = new File(files[i].getPath());
        	    if (fsObject.exists()) {
        	    	if (files[i].isFile()) {
        	    		// remove the file
        	    		success = fsObject.delete();
        	    	}
        	    	if (files[i].isDirectory()) {
        	    		// recursively remove all sub-directories and files
        	    		success = rmAll(fsObject.getPath(), removeDirs);
        	    		if (removeDirs) {
        	    			// remove this directory on the way out since everything underneath is now deleted
        	    			success = fsObject.delete();
        	    		}
        	    	}
        	    } 
    		}  	
    	}
    	catch(Exception ex)
    	{
    		throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
    	}	
    	return success;
	}

 	public File[] getFileInfo ( String path ) throws CustomProcedureException, SQLException {
 		// Initialize the file/directory list
 		File[] files = null;
		
		// Get a list of files from the directory
		File dir = new File(path);
		
		// if this is a directory send back the files in the directory. If it's a file
		//  then just send back the information for that file
		if (dir.isDirectory()) {

			files = dir.listFiles(); 

			if (files == null){
				throw new CustomProcedureException("Error in CJP "+getName()+": Directory does not exist.");
			}
			files = dir.listFiles();			
		} else {
			files = new File[1];
			files[0] = dir;
		}
		return files;
 	}

	public int getNumAffectedRows(){
		return 0;
	}

	public Object[] getOutputValues(){
		return new Object[] { success };
	}

	public void close() throws SQLException {}
	
	public static void main(String[] args) {
		CustomProcedure cp = new RemoveAll();
		String param1 = "/temp/Utilities/xml";
		String param2 = "Y";

		ParameterInfoDisplay pc = new ParameterInfoDisplay(); 
        try {
	        ParameterInfo[] pia = cp.getParameterInfo();
	        pc.displayParameterInfo(pia);

	        cp.initialize(null);
            System.out.println("invoke "+cp.getName());
	        cp.invoke(new Object[] {
	        		new String (param1),
	        		new String (param2)
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
 



