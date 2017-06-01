package com.cisco.dvbu.ps.utils.file;
/*
 * Source File: UnzipFile.java
 *
 * Description: This Composite CJP unzips a file from the source file system.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *	Input:
 *		filePath - full path to file to unzip.							Types.VARCHAR, DIRECTION_IN
 *
 *	Output: 
 *		success - true/1=file unzipped, false/0=failed w/exception. 	Types.BOOLEAN, DIRECTION_OUT
 *
 *	Exceptions:  CustomProcedureException, SQLException
 *	Author:      Mike Tinius
 *	Date:        8/2/2010
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;


public class UnzipFile implements CustomProcedure
{
	private ExecutionEnvironment qenv;
	public String filePath;
	public String fileName;
	boolean success;

	public void initialize(ExecutionEnvironment qenv){
		this.qenv = qenv;
	}

	//
	// Introspection methods
	//	
	public String getName() {
		return "unzipFile";
	}

	public String getDescription() {
		return "This Composite CJP unzips a file from the source file system.";
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
				new ParameterInfo("filePath", 				Types.VARCHAR, DIRECTION_IN),
				new ParameterInfo("success", 				Types.BOOLEAN, DIRECTION_OUT)
		};
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
	{
		if (inputValues[0] == null) {
			throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter filePath must be provided.");
		}
		filePath = inputValues[0].toString();
		unzipFile(filePath);
	}

	public  void unzipFile(String filePath) 
	throws CustomProcedureException, SQLException {

		Enumeration entries;
		ZipFile zipFile = null;
		String dirName = null;
		
	   	success = false;
		try {

			// Get the directory that this zip file resides in
			dirName = new File(filePath).getParent();
			
			// Unzip the file
			zipFile = new ZipFile(filePath);

			entries = zipFile.entries();

			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();

				if(entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					(new File(entry.getName())).mkdir();
					continue;
				}
				String targetName = dirName + File.separatorChar + entry.getName();
				
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(targetName)));
			}
		} catch (IOException ex) {
			throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
		} finally {
			try {
				if (zipFile != null) zipFile.close();
				zipFile = null;
				success = true;
			} catch (IOException ioe) {
				throw new CustomProcedureException("Error in CJP "+getName()+": Error encountered while closing the file: " + ioe.toString());
			}
		}
	}

	public final void copyInputStream(InputStream in, OutputStream out) 
			throws IOException {

		byte[] buffer = new byte[1024];
		int len;

		while((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	public int getNumAffectedRows(){
		return 0;
	}

	public Object[] getOutputValues(){
		return new Object[] { success };
	}

	public void close() throws SQLException {}

	public static void main(String[] args) {
		CustomProcedure cp = new UnzipFile();
		String param1 = "/tmp/zipped.zip";

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




