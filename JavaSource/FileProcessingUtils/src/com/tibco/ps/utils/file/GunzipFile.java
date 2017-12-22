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
 * Source File: GunzipFile.java
 *
 * Description: This Composite CJP gunzips a file from the source file system.
 *
 * Note: The import [import com.compositesw.extension.*] requires CIS_HOME\apps\extension\lib\csext.jar
 * 
 *  Input:
 *    filePath - full path to file to gunzip.             Types.VARCHAR, DIRECTION_IN
 *
 *  Output: 
 *    success - true/1=file gunzipped, false/0=failed w/exception.  Types.BOOLEAN, DIRECTION_OUT
 *
 *  Exceptions:  CustomProcedureException, SQLException
 *  Author:      Niraj Vora
 *  Date:        11/2/2013
 *  CSW Version: 6.2.0
 *
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Types;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import java.io.*;

import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;


public class GunzipFile implements CustomProcedure
{
  private ExecutionEnvironment qenv;
  private String filePath;
  boolean success;

  public void initialize(ExecutionEnvironment qenv){
    this.qenv = qenv;
  }

  //
  // Introspection methods
  //  
  public String getName() {
    return "gunzipFile";
  }

  public String getDescription() {
    return "This Composite CJP gunzips a file from the source file system.";
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

  public void compensate(ExecutionEnvironment executionEnvironment) throws CustomProcedureException, SQLException {
  }

  public ParameterInfo[] getParameterInfo(){
    return new ParameterInfo[]{
        new ParameterInfo("filePath",       Types.VARCHAR, DIRECTION_IN),
        new ParameterInfo("success",        Types.BOOLEAN, DIRECTION_OUT)
    };
  }

  public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
  {
    success = false;

    if (inputValues[0] == null) {
      throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter filePath must be provided.");
    }
    filePath = inputValues[0].toString();
    gunzipFile(filePath);
  }

  public  void gunzipFile(String filePath) 
  throws CustomProcedureException, SQLException {

    String outFilePath = filePath.replace(".gz","");
    OutputStream out = null;
      
    String dirName = null;
  
    try {

      // Get the directory that this zip file resides in
      dirName = new File(filePath).getParent();
      
      // GUnzip the file
      InputStream gzipFile = new GZIPInputStream(new FileInputStream(filePath));
      Reader reader = new InputStreamReader(gzipFile, "UTF-8");
      out = new FileOutputStream(outFilePath);

      byte[] buf = new byte[10240];
      int len;
      while ((len = gzipFile.read(buf)) > 0) {
        out.write(buf, 0, len);
      }

      reader.close();
      gzipFile.close();
      out.close();
      success = true;
    }   
      catch (IOException ex) {
      throw new CustomProcedureException("Error in CJP "+getName()+": " + ex.toString());
    } 
  }


  public int getNumAffectedRows(){
    return 0;
  }

  public Object[] getOutputValues(){
    return new Object[] { success };
  }

  public void close() throws SQLException {}

}
