package com.cisco.dvbu.ps.utils.file;
/*
 * Source File: CopyFile.java
 *
 * Description: This Composite CJP copies a file from the source file system to a destination directory.
 *
 *   Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar\
 *
 *	Input:
 *		filePath - full path to a file that is to be copied.				Types.VARCHAR, DIRECTION_IN
 *		newFilePath - directory path to copy the file to.					Types.VARCHAR, DIRECTION_IN
 *
 *	Output: none
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.sql.Types;

//import com.compositesw.extension.CustomCursor;
import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;

public class CopyFile implements CustomProcedure
{
	private ExecutionEnvironment qenv;
	protected String filePath;
	protected String fileName;
	protected String newFilePath;

	public void initialize(ExecutionEnvironment qenv){
	  this.qenv = qenv;
	}
	
	 //
	 // Introspection methods
	 //	
	 public String getName() {
	     return "copyFile";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP copies a file from the source file system to a destination directory.";
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
		   new ParameterInfo("filePath", 		Types.VARCHAR, DIRECTION_IN),
		   new ParameterInfo("newFilePath", 	Types.VARCHAR, DIRECTION_IN)
	  };
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
	{
		if (inputValues[0] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter filePath must be provided.");
		}
		if (inputValues[1] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter newFilepath must be provided.");
		}		
		filePath = inputValues[0].toString();
		newFilePath = inputValues[1].toString();
		copyFile(filePath, newFilePath);
	}
	
	public void copyFile(String filePath, String newFilePath) 
			throws CustomProcedureException, SQLException {
		
		FileChannel srcChannel = null;
		FileChannel dstChannel = null;
		try {
			// Create channel on the source
			srcChannel = new FileInputStream(filePath).getChannel();

			// Create channel on the destination
			//FileChannel dstChannel = new FileOutputStream(newFilePath+baseFileName).getChannel();
			dstChannel = new FileOutputStream(newFilePath).getChannel();

			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

			// Force the copy - added to overcome copy error
			dstChannel.force(true);
		} catch (IOException e) {
			throw new CustomProcedureException("Error in CJP "+getName()+": An error was encountered: " + e.toString());
		} finally {
			try {
				// Close the channels
				if (srcChannel != null) srcChannel.close();
				if (dstChannel != null) dstChannel.close();
				srcChannel = null;
				dstChannel = null;
			} catch (IOException e) {
				throw new CustomProcedureException("Error in CJP "+getName()+": Error encountered while closing source and destination channels: " + e.toString());
			}
		}
	}
	
	public int getNumAffectedRows(){
		return 0;
	}

	public Object[] getOutputValues(){
		return new Object[] { };
	}

	public void close() throws SQLException {}
	

	public static void main(String[] args) {
		CustomProcedure cp = new CopyFile();
		String param1 = "/tmp/test002.csv";
		String param2 = "/tmp/archive/test002.csv";

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
