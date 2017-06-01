package com.cisco.dvbu.ps.utils.file;
/*
 * Source File: GetFileInfo.java
 *
 * Description: This Composite CJP returns file info (path, filename, timestamp, size) for a file in the file system.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *	Input:
 *		directoryPath - directory path to retrieve a cursor of file/dir info.		Types.VARCHAR, DIRECTION_IN
 *		includeDirs - Y=include, N=do not include. 									Types.CHAR, DIRECTION_IN
 *
 *	Output: 
 *		FileInfo - cursor of file or directory information		ProcedureConstants.TYPED_CURSOR, DIRECTION_OUT,
 *	     		filePath - full path to the file or directory		Types.VARCHAR, DIRECTION_OUT
 *	     		fileName - name of the file or directory			Types.VARCHAR, DIRECTION_OUT
 *	     		fileTimestamp - timestamp of file or directory		Types.TIMESTAMP, DIRECTION_OUT
 *	     		fileSize - size of the file or directory			Types.BIGINT, DIRECTION_OUT
 *	     		isFile - 1=file, 0=directory						Types.SMALLINT, DIRECTION_OUT
 *	     		isDir - 0=file, 1=diretory							Types.SMALLINT, DIRECTION_OUT
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
import java.sql.Timestamp;
import java.io.*;

public class GetFileInfo implements CustomProcedure, java.io.Serializable {
	
	String directoryPath = ""; // input parameter: example: "C:/DIA/dcgs_files/datasource1"
	File dir = null;
	File[] files;
	int i;
	private int numberOfRecords = 0;
	private transient CustomCursor outputCursor;
	private ExecutionEnvironment qEnv;
   
	 public void initialize(ExecutionEnvironment executionEnvironment) throws CustomProcedureException,
	                                                                          SQLException {                                                                            
	         qEnv = executionEnvironment;
	 }
	 
	 //
	 // Introspection methods
	 //	
	 public String getName() {
	     return "getFileInfo";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP returns file info (path, filename, timestamp, size) for a file in the file system.";
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

	public ParameterInfo[] getParameterInfo() {
	     return new ParameterInfo[] {
	
	             new ParameterInfo("directoryPath", Types.VARCHAR, DIRECTION_IN),
	             new ParameterInfo("includeDirs", Types.CHAR, DIRECTION_IN), // Y=include, N=do not include
	             new ParameterInfo("FileInfo", ProcedureConstants.TYPED_CURSOR, DIRECTION_OUT,
	             						new ParameterInfo[] { 
	     								new ParameterInfo("filePath", Types.VARCHAR, DIRECTION_OUT),
	     								new ParameterInfo("fileName", Types.VARCHAR, DIRECTION_OUT),
	     								new ParameterInfo("fileTimestamp", Types.TIMESTAMP, DIRECTION_OUT),
	     								new ParameterInfo("fileSize", Types.BIGINT, DIRECTION_OUT),
	     								new ParameterInfo("isFile", Types.SMALLINT, DIRECTION_OUT),
	     								new ParameterInfo("isDir", Types.SMALLINT, DIRECTION_OUT)
	             						})
	             };
	}

	public int getNumAffectedRows() throws CustomProcedureException,
	                                        SQLException {
	     return numberOfRecords;
	}

 
	public Object[] getOutputValues() throws CustomProcedureException, SQLException {
	 
	     outputCursor = createCustomCursor();
	     return new Object[] { outputCursor };
	}
 
	private  CustomCursor createCustomCursor() {
		
	 	return new CustomCursor() {
	 		private int counter = 0;
	 		public ParameterInfo[] getColumnInfo() { return null; }
	 		
	 		public Object[] next()
	 			throws CustomProcedureException, SQLException
	 		{	 			
	 			if (counter >= files.length) {
    				return null;
    			} else { 
    				return new Object[] {
		 					new String(files[counter].getPath()),
		 					new String(files[counter].getName()),
		 					new Timestamp(files[counter].lastModified()),
		 					new Long(files[counter].length()),
		 					new Integer(files[counter].isFile()?1:0),
		 					new Integer(files[counter++].isDirectory()?1:0)
		 					};
    			}
	 		}
	
	 		public void close()
	 		throws CustomProcedureException, SQLException {}
	 	};
	}
 
	public void close() throws CustomProcedureException, SQLException {}

 	public void invoke(Object[] inputValues) throws CustomProcedureException,
                                            SQLException {
		directoryPath = null;
		 
		if (inputValues[0] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter directoryPath must be provided.");
		} 
		directoryPath = inputValues[0].toString();
	
		String includeDirs = (String) inputValues[1];
		if (includeDirs == null) {
			includeDirs = "N";
		}
		if (includeDirs.toUpperCase().contains("Y")) {
			includeDirs = "Y";
		} else {
			includeDirs = "N";
		}
		getFileInfo(directoryPath, includeDirs); 	
 	}
 	
 	public void getFileInfo ( String path , String includeDirs ) throws CustomProcedureException, SQLException {
		
 		files = null;
		
		// Get a list of files from the directory
		dir = new File(path);
		
		// if this is a directory send back the files in the directory. If it's a file
		//  then just send back the information for that file
		if (dir.isDirectory()) {

			files = dir.listFiles(); 

			if (files == null){
				throw new CustomProcedureException("Error in CJP "+getName()+": Directory does not exist.");
			}
			
			if (includeDirs.compareToIgnoreCase("Y") == 0) {
				files = dir.listFiles();			
			} else {
				// This filter only returns files and files that match the wildcard
				FileFilter fileFilter = new FileFilter() {
				    public boolean accept(File file) {
				        return file.isFile();
				    }
				};
				files = dir.listFiles(fileFilter);
			}
		} else {
			files = new File[1];
			files[0] = dir;
		}
 	}

	public static void main(String[] args) {
		CustomProcedure cp = new GetFileInfo();
		String param1 = "/Temp/Utilities";
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
       
		} catch (Exception ex) {
			System.out.print(ex.toString());
		}
	}

 	
} // end of class

