package com.cisco.dvbu.ps.utils.file;
/*
 * Source File: GetNewFiles.java
 *
 * Description: This Composite CJP returns a cursor of new files from the source file system.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *	Input:
 *		directoryPath - directory path to retrieve a cursor of file/dir info.		Types.VARCHAR, DIRECTION_IN
 *
 *	Output: 
 *		newFilenames - cursor listing of files in the directory		ProcedureConstants.TYPED_CURSOR, DIRECTION_OUT,
 *	     		filePath - full path to the file or directory		Types.VARCHAR, DIRECTION_OUT
 *	     		fileName - name of the file or directory			Types.VARCHAR, DIRECTION_OUT
 *	     		fileTimestamp - timestamp of file or directory		Types.TIMESTAMP, DIRECTION_OUT
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
import com.compositesw.extension.*;

import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.io.*;

public class GetNewFiles implements CustomProcedure, java.io.Serializable {
	
	static String directoryPath = ""; // input parameter: example: "C:/DIA/dcgs_files/datasource1"
	static File dir = null;
	static File[] files;
	static int i;
	private int numberOfRecords = 0;
	private transient CustomCursor outputCursor;
	private ExecutionEnvironment qEnv;
   
	 public void initialize(ExecutionEnvironment executionEnvironment) 
	 		throws CustomProcedureException, SQLException {                                                                            
	         qEnv = executionEnvironment;
	 }
	 
	 //
	 // Introspection methods
	 //	
	 public String getName() {
	     return "getNewFiles";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP returns a cursor of new files from the source file system.";
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
	             //new ParameterInfo("sinceDate", Types.TIMESTAMP, DIRECTION_IN),
	             new ParameterInfo("newFilenames", ProcedureConstants.TYPED_CURSOR, DIRECTION_OUT,
	             						new ParameterInfo[] { 
	     								new ParameterInfo("filePath", Types.VARCHAR),
	     								new ParameterInfo("fileName", Types.VARCHAR),
	     								new ParameterInfo("fileTimestamp", Types.TIMESTAMP)
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
 
	private CustomCursor createCustomCursor() {
		
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
		 					new Timestamp(files[counter++].lastModified()) };
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
		getNewFiles(directoryPath); 	
 	}
 	
 	public void getNewFiles ( String path ) throws CustomProcedureException, SQLException {
		
 		files = null;
		
		// Get a list of files from the directory
		dir = new File(path);

		files = dir.listFiles();

		if (files == null){
			 throw new CustomProcedureException("Error in CJP "+getName()+": Directory does not exist.");
		}
		// This filter only returns files
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.isFile();
		    }
		};
		files = dir.listFiles(fileFilter); 		
 	}

	public static void main(String[] args) {
		CustomProcedure cp = new GetNewFiles();
		String param1 = "/Temp/Utilities";

		ParameterInfoDisplay pc = new ParameterInfoDisplay(); 
        try {
	        ParameterInfo[] pia = cp.getParameterInfo();
	        pc.displayParameterInfo(pia);

	        cp.initialize(null);
            System.out.println("invoke "+cp.getName());
	        cp.invoke(new Object[] {
	        		new String (param1)
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

