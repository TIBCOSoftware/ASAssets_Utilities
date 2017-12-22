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
 * Source File: CreateFileAscii.java
 *
 * Description: This Composite CJP creates a new ASCII file in the file system.
 *
 *   Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar\
 *
 *	Input:
 *		filePath - full path to a file that is to be created.				Types.VARCHAR, DIRECTION_IN
 *		append - 0=do not append file, 1=append file.						Types.SMALLINT, DIRECTION_IN
 *		fileContent - The ascii text content to write.						Types.LONGVARCHAR, DIRECTION_IN
 *
 *	Output: none
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

public class CreateFileAscii implements CustomProcedure, java.io.Serializable {
			
		protected String filePath;
		protected String fileContent;
		protected int append;
		
		protected ExecutionEnvironment qEnv;
		protected boolean invoked;
		
		 public void initialize(ExecutionEnvironment executionEnvironment) throws CustomProcedureException,
         SQLException {                                                                            
			 qEnv = executionEnvironment;
}

//
// Introspection methods
//	
public String getName() {
return "createFileAscii";
}

public String getDescription() {
return "This Composite CJP creates a new ASCII file in the file system";
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

//look at java system library

public ParameterInfo[] getParameterInfo() {
    return new ParameterInfo[] {

            new ParameterInfo("filePath", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("append", Types.SMALLINT, DIRECTION_IN),
            new ParameterInfo("fileContent", Types.LONGVARCHAR, DIRECTION_IN)
            };
}
		
public Object[] getOutputValues() throws CustomProcedureException, SQLException {
	 
    return new Object[] {};
}
	

public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
{
	if (inputValues[0] == null) {
		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter filePath must be provided.");
	}
	if (inputValues[1] == null) {
		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter append must be provided.");
	}
	if (inputValues[2] == null) {
		 throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter fileContent must be provided.");
	}
	
	filePath = inputValues[0].toString();
	append = Integer.valueOf(inputValues[1].toString());
	fileContent = inputValues[2].toString();
	invoked = true;
	
	createFileAscii(filePath, (append==1)?true:false, fileContent);
}

public int getNumAffectedRows() throws CustomProcedureException,
SQLException {
return 0;
}

public void createFileAscii(String fileName, boolean append, String fileContent) 
								throws CustomProcedureException, SQLException {

			try
			{
				File file = new File(fileName);
				// if its not there create it
				if(!file.isFile()){
				  file.createNewFile();
				}				

				BufferedWriter out = new BufferedWriter(new FileWriter(fileName, append));
		        if (append) {
		        	out.newLine();
		        }
		        out.write(fileContent);
		        out.close();
				System.out.println("Your file has been written");        	
			}
			catch(IOException ex)
			{
				throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
			}	        
}

public void close() throws CustomProcedureException, SQLException {}


public static void main(String[] args) {
	CustomProcedure cp = new CreateFileAscii();
	String param1 = "/tmp/test_new.txt";
	int param2 = 1; //0=do not append file, 1=append file
	String param3 = "content for line 1";
	
	ParameterInfoDisplay pc = new ParameterInfoDisplay(); 
    try {
        ParameterInfo[] pia = cp.getParameterInfo();
        pc.displayParameterInfo(pia);

        cp.initialize(null);
        System.out.println("invoke "+cp.getName());
        cp.invoke(new Object[] {
        		new String (param1),
        		new Integer (param2),
        		new String (param3)
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
		
		
		
		
		
		
}
