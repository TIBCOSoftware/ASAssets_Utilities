package com.cisco.dvbu.ps.utils.net;
/*
 * Source File: FtpFile.java
 *
 * Description: This Composite CJP ftp's a file from the ftp system. CJP reads input filename, user id, password, ftp server connection and downloads t * the file to the valid directory on the target server.
 *
 *  Input:
 *    fileName   - Filename to ftp. 
 *    hostIp     - IP address of ftp server.
 *    userId     - Valid userId to connect to ftp server.
 *    userPass   - Valid password for userId.
 *    ftpDirName - Valid directory where the source ftp file resides on ftp server.
 *    dirName    - Valid directory on the target server.
 *
 *  Output: 
 *    success    - true/1=file ftp success, false/0=failed w/exception.
 *
 *  Exceptions:  CustomProcedureException, SQLException
 *  Author:      Niraj Vora
 *  Date:        11/5/2013
 *  CSW Version: 6.2.4
 *
 *  (c) 2013, 2014 Cisco and/or its affiliates. All rights reserved.
 */
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Types;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;


public class FtpFile implements CustomProcedure
{
  private ExecutionEnvironment qenv;
  private String fileName;
  private String hostIp;
  private String userId;
  private String userPass;
  private String ftpDirName;
  private String dirName;
  boolean success;

  public void initialize(ExecutionEnvironment qenv){
    this.qenv = qenv;
  }

  //
  // Introspection methods
  //  
  public String getName() {
    return "ftpFile";
  }

  public String getDescription() {
    return "This Composite CJP downloads a file from an ftp server.";
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
        new ParameterInfo("fileName",       Types.VARCHAR, DIRECTION_IN),
        new ParameterInfo("hostIp",         Types.VARCHAR, DIRECTION_IN),
        new ParameterInfo("userId",         Types.VARCHAR, DIRECTION_IN),
        new ParameterInfo("userPass",       Types.VARCHAR, DIRECTION_IN),
        new ParameterInfo("ftpDirName",     Types.VARCHAR, DIRECTION_IN),
        new ParameterInfo("dirName",        Types.VARCHAR, DIRECTION_IN),
        new ParameterInfo("success",        Types.BOOLEAN, DIRECTION_OUT)
    };
  }

  public void invoke(Object[] inputValues) throws CustomProcedureException, SQLException
  {
    if (inputValues[0] == null) {
      throw new CustomProcedureException("Error in CJP "+getName()+": IN Parameter fileName must be provided.");
    }
    fileName = inputValues[0].toString();
    hostIp = inputValues[1].toString();
    userId = inputValues[2].toString();
    userPass = inputValues[3].toString();
    ftpDirName = inputValues[4].toString();
    dirName = inputValues[5].toString();
    ftpFile(fileName);
  }

  public void ftpFile(String fileName) throws CustomProcedureException, SQLException {

    // new ftp client
    FTPClient ftp = new FTPClient();
    OutputStream output = null;

    success = false;
    try {
      //try to connect
      ftp.connect(hostIp);

      //login to server
      if (!ftp.login(userId,userPass)) {
        ftp.logout();
        qenv.log(LOG_ERROR, "Ftp server refused connection user/password incorrect.");
      }
      int reply = ftp.getReplyCode();

      //FTPReply stores a set of constants for FTP Reply codes
      if (!FTPReply.isPositiveCompletion(reply)){
        ftp.disconnect();
        qenv.log(LOG_ERROR, "Ftp server refused connection.");
      }
  
      //enter passive mode
      ftp.setFileType(FTPClient.BINARY_FILE_TYPE, FTPClient.BINARY_FILE_TYPE);
      ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
      ftp.enterLocalPassiveMode();

      //get system name
      //System.out.println("Remote system is " + ftp.getSystemType());

      //change current directory
      ftp.changeWorkingDirectory(ftpDirName);
      System.out.println("Current directory is " + ftp.printWorkingDirectory());
      System.out.println("File is " + fileName);
    
      output = new FileOutputStream(dirName + "/" + fileName);

      //get the file from the remote system
      success = ftp.retrieveFile(fileName, output);

      //close output stream
      output.close();

    } catch (IOException ex) {
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

} // end of class
