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
 * Source File: GetFileContentsBinary.java
 *
 * Description: This Composite CJP returns the Binary contents of a file from the file system.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *	Input:
 *		filePath - full path to a file to retrieve content.			Types.VARCHAR, DIRECTION_IN
 *
 *	Output: 
 *		fileContent - binary file content. 							createLongVarBinary("fileContent", DIRECTION_OUT, 2147483647)
 *
 *	Exceptions:  CustomProcedureException, SQLException
 *	Author:      Mike Tinius
 *	Date:        8/6/2010
 *	CSW Version: 5.1.0
 *
 */

import com.compositesw.extension.*;

import java.sql.SQLException;
import java.sql.Types;
import java.io.*;

public class GetFileContentsBinary implements CustomProcedure
{
	private static int BUFFER_SIZE = Short.MAX_VALUE;
	private ExecutionEnvironment qenv;
	public String filePath;
	public String fileName;
	//static StringBuilder fileContent = new StringBuilder();
	protected byte[] fileContent;  

	public void initialize(ExecutionEnvironment qenv){
	  this.qenv = qenv;
	}
	
	 //
	 // Introspection methods
	 //	
	 public String getName() {
	     return "getFileContentsBinary";
	 }
	
	 public String getDescription() {
	     return "This Composite CJP returns the Binary contents of a file from the file system.";
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
		   ParameterInfo.createLongVarBinary("fileContent", DIRECTION_OUT, 2147483647)
		   //new ParameterInfo("fileContent", 	Types.BLOB, DIRECTION_OUT)
	  };
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
	{
		if (inputValues[0] == null) {
			 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter filePath must be provided.");
		}
		filePath = inputValues[0].toString();
		getFileContentsBinary(filePath);
	}
	
	public  void getFileContentsBinary(String filePath) 
									throws CustomProcedureException, SQLException {
		
		fileContent = null;
    	try
    	{
    		
    		BufferedInputStream br = new BufferedInputStream(new FileInputStream(filePath));
    		ByteArrayOutputStream bo = new ByteArrayOutputStream(BUFFER_SIZE);
    		
    		
    		
    		byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ( (bytesRead = br.read(buffer,0,BUFFER_SIZE)) > -1 ) {
          
                bo.write( buffer, 0, bytesRead );
            }
            fileContent = bo.toByteArray();
            br.close();
            bo.close();
          
    		
    		//while((s = br.read(b, off, len)) != null) {
    		//	fileContent.append(s + "\n");
    		//}
    		//fr.close();            	
    	}
    	catch(IOException ex)
    	{
    		throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
    	}	        
	}
	
	public int getNumAffectedRows(){
		return 0;
	}

	public Object[] getOutputValues(){
		return new Object[] { fileContent };
	}

	public void close() throws SQLException {}
	
	public static void main(String[] args) {
		CustomProcedure cp = new GetFileContentsBinary();
		String param1 = "/tmp/test_new.txt";
		
		ParameterInfoDisplay pc = new ParameterInfoDisplay(); 
	    try {
	        ParameterInfo[] pia = cp.getParameterInfo();
	        pc.displayParameterInfo(pia);

	        cp.initialize(null);
	        System.out.println("invoke "+cp.getName());
	        cp.invoke(new Object[] {
	        		new String (param1)
	        });
       
	        String result = cp.getOutputValues()[0].toString();
	        System.out.println("File Contents:");
	        System.out.println(result);
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
 

