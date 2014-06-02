/*
	Blob2Varchar:
	
	Description:
	  Converts a BLOB data type to a VARCHAR. Use CAST to cast CLOB data types to VARCHAR.
	
	
	Inputs:
	  BlobVal      - A BLOB value to convert.
	
	
	Output:
	  result       - The result of conversion to VARCHAR. 
	
	
	Exceptions:
	  CustomProcedureException - If supplied input can not be converted
	
	
	Modified Date:	Modified By:		CSW Version:	Reason:
	09/22/2010		Kevin O'Brien		5.1.0			Created new
	
	
	© 2010, 2014 Cisco and/or its affiliates. All rights reserved. 
*/

package com.cisco.dvbu.ps.utils.text;

import com.compositesw.extension.*;
import java.sql.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Blob2Varchar implements CustomProcedure
{
    private ExecutionEnvironment qenv;
    private Object result;
    private Blob inputBlob;
    private InputStream is;
    private BufferedReader bufReader;
    private int data;
    private StringBuffer stringBuf;
    private String resultString;

    public Blob2Varchar()
    {
    }

    /**
     * This is called once just after constructing the class.  The
     * environment contains methods used to interact with the server.
     */
    public void initialize(ExecutionEnvironment qenv) throws SQLException
    {
        this.qenv = qenv;
    }

    /**
     * Called during introspection to get the description of the input
     * and output parameters.  Should not return null.
     */
    public ParameterInfo[] getParameterInfo()
    {

        return new ParameterInfo[]
        {
            new ParameterInfo("BlobVal", Types.BLOB, DIRECTION_IN),
            new ParameterInfo("result", Types.LONGVARCHAR, DIRECTION_OUT)
        };
    }

    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException
    {
        if (inputValues[0] == null)
        {
            resultString = null;
        }
        else
        {
            try
            {
                result = inputValues[0];
                inputBlob = (Blob)result;
                //
                // Blob.getBinaryStream() not implemented in CIS JDBC driver
                //
                //is = inputBlob.getBinaryStream();
                is = new ByteArrayInputStream(inputBlob.getBytes(1L, new Long(inputBlob.length()).intValue()));

                bufReader = new BufferedReader(new InputStreamReader(is));
                stringBuf = new StringBuffer();
                data = bufReader.read();
                while(data != -1)
                {
                    char theChar = (char) data;
                    stringBuf.append(theChar);
                    data = bufReader.read();
                }
                resultString = stringBuf.toString();
            }
            catch(IOException ioe)
            {
                throw new CustomProcedureException(ioe);
            }
            finally
            {
                try
                {
                    if(bufReader != null)
                    {
                        bufReader.close();
                    }
                }
                catch(IOException ignored)
                {
                    ;
                }
            }
        }
    }

    /**
     * Called to retrieve the number of rows that were inserted,
     * updated, or deleted during the execution of the procedure. A
     * return value of -1 indicates that the number of affected rows is
     * unknown.  Can throw CustomProcedureException or SQLException if
     * there is an error when getting the number of affected rows.
     */
    public int getNumAffectedRows() {
        return 0;
    }

    /**
     * Called to retrieve the output values.  The returned objects
     * should obey the Java to SQL typing conventions as defined in the
     * table above.  Output cursors can be returned as either
     * CustomCursor or java.sql.ResultSet.  Can throw
     * CustomProcedureException or SQLException if there is an error
     * when getting the output values.  Should not return null.
     */
    public Object[] getOutputValues() {

        Object[] outputValues = new Object[1];

        outputValues[0] = resultString;

        return outputValues;
    }

    /**
     * Called when the procedure reference is no longer needed.  Close
     * may be called without retrieving any of the output values (such
     * as cursors) or even invoking, so this needs to do any remaining
     * cleanup.  Close may be called concurrently with any other call
     * such as "invoke" or "getOutputValues".  In this case, any pending
     * methods should immediately throw a CustomProcedureException.
     */
    public void close()
        throws SQLException
    {
    }

    //
    // Introspection methods
    //

    /**
     * Called during introspection to get the short name of the stored
     * procedure.  This name may be overridden during configuration.
     * Should not return null.
     */
    public String getName() {
        return "Blob2Varchar";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        return "Converts a BLOB data type to a VARCHAR (use CAST to convert CLOB to VARCHAR)";
    }

    //
    // Transaction methods
    //

    /**
     * Commit any open transactions.
     */
    public void commit()
        throws SQLException
    { }

    /**
     * Rollback any open transactions.
     */
    public void rollback()
        throws SQLException
    { }

    /**
     * Returns true if the transaction can be compensated.
     */
    public boolean canCompensate() {
        return false;
    }

    public boolean canCommit(){
        return false;
    }

    /**
     * Compensate any committed transactions (if supported).
     */
    public void compensate(ExecutionEnvironment qenv)
        throws CustomProcedureException, SQLException {
    }
} 
