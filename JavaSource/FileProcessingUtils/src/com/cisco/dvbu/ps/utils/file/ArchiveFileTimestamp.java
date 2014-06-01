package com.cisco.dvbu.ps.utils.file;
/*
 * Source File:ArchiveFileTimestamp.java
 *
 * Description: This Composite CJP moves a file from the source file system to an archival directory.
 *              This method automatically adds a timestamp to the end of the filename.
 *
 *   Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar\
 * 
 *	Input:
 *		filePath - full path to a file that is to be archived.				Types.VARCHAR, DIRECTION_IN
 *		archivalDirectoryPath - directory path to archive the file to.		Types.VARCHAR, DIRECTION_IN
 *
 *	Output: none
 *
 *	Exceptions:  CustomProcedureException, SQLException
 *	Author:      Mike Tinius
 *	Date:        8/6/2010
 *	CSW Version: 5.1.0
 *
 *  (c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.sql.Types;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

//import com.compositesw.extension.CustomCursor;
import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;

public class ArchiveFileTimestamp implements CustomProcedure {

	private ExecutionEnvironment qenv;
	public String filePath;
	public String archivalFilePath;
	private static Random randomGenerator = new Random();


	public void initialize(ExecutionEnvironment qenv){
		this.qenv = qenv;
	}
	
	 //
	 // Introspection methods
	 //	
	 public String getName() {
	     return "archiveFileTimestamp";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP moves a file from the source file system to an archival directory with a timestamp in the filename.";
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
		   new ParameterInfo("archivalDirectoryPath", 	Types.VARCHAR, DIRECTION_IN)
	  };
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
	{
		if (inputValues[0] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN parameter filePath must be provided.");
		}
		if (inputValues[1] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN parameter archivalDirectoryPath must be provided.");
		}		
		filePath = inputValues[0].toString();
		archivalFilePath = inputValues[1].toString();
		ArchiveFileTimestamp(filePath, archivalFilePath);
	}
	
	public void ArchiveFileTimestamp(String filePath, String archivalFilePath) 
			throws CustomProcedureException, SQLException {
		  	
    	FileChannel srcChannel = null;
		FileChannel dstChannel = null;
    	
		try {
			File fileToArchive = new File(filePath);
			int pos = fileToArchive.getName().indexOf(".");
			int len = fileToArchive.getName().length();
			String filename = null;
			String extension = null;
			if (pos > 0) {
				filename = fileToArchive.getName().substring(0,pos);
				extension = fileToArchive.getName().substring(pos+1);
			} else {
				filename = fileToArchive.getName().substring(0);				
			}
			filename = getUniqueFilename(filename, extension);
			File destFile = new File(archivalFilePath, filename);
			
			
			// validate file to archive
			if (!fileToArchive.exists()) {
				throw new CustomProcedureException("Error in CJP "+getName()+": File to archive does not exist: " + filePath);
			} else if (!fileToArchive.isFile()) {
				throw new CustomProcedureException("Error in CJP "+getName()+": File to archive exists but is not a file: " + filePath);
			}
			
			// validate the destination directory (and create it if necessary)...
			if (destFile.getParentFile() != null) {
				if (!destFile.getParentFile().exists() || (destFile.getParentFile().exists() && !destFile.getParentFile().isDirectory())) {
					if (!destFile.getParentFile().mkdirs()) {
						throw new CustomProcedureException("Error in CJP "+getName()+": Unable to create destination directory.");
					}
				}
			}
			
			// handle destination file already existing (use a different name)...
			while (destFile.exists() && destFile.isFile()) {
				destFile = new File(destFile.getParent(), (destFile.getName() + randomGenerator.nextInt()));
			}
			
			// copy the file...
			srcChannel = new FileInputStream(fileToArchive).getChannel();
			dstChannel = new FileOutputStream(destFile).getChannel();
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
			dstChannel.force(true);
			
			/*
			 * IMPORTANT for this to come before the delete!
			 * During testing, we saw that the delete would work on unix but
			 * failed on Windows when close() wasn't called before deleting.
			 */
			if (srcChannel != null) srcChannel.close();
			if (dstChannel != null) dstChannel.close();

			// remove the source file...
			if (!fileToArchive.delete()) {
				throw new CustomProcedureException("Error in CJP "+getName()+": Unable to delete source file (" + fileToArchive + "). Please note that the copy operation appears to have succeeded.");
			}
		} catch (Exception e) {
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
	
	// Generate unique file name based on a date
	public static String getUniqueFilename(String filename, String extension) {
		Format formatter;
		Date date = new Date();
		formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
		if (extension != null) {
			return filename+"_"+formatter.format(date)+"."+extension;
		} else {
			return filename+"_"+formatter.format(date);
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
		CustomProcedure cp = new ArchiveFileTimestamp();
		String param1 = "c:/tmp/test001.csv";
		String param2 = "c:/tmp/archive";

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
 


