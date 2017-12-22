package com.tibco.ps.utils.repository;

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
 * --------------------------------------------
 * Description:
 * --------------------------------------------
 *     Custom procedure that returns a data source native data type mapping between a given CIS ANSI type and a given data source.
 *
 *    For example, in 6.1.0.00.34 (hotfix) Composite maps a DOUBLE is to a BINARY_DOUBLE for oracle 
 *    whereas it used to map to DOUBLE.  Therefore, it is necessary then to retrieve the CIS ANSI type mappings for specific data sources.
 *    
 *    How this procedure works    
 *    1.    Starting with the execution environment, cast that to ExecutionEnvironmentImpl and call getParentRequest().getTransactionContext()
 *    2.    With the tcontext object, retrieve the repository connection tcontext.getRepositoryConnection()
 *    3.    With repoConn, look up the id of the data source. repoConn.getResource(new Path(/shared/path/to/my/ds'), Metadata.TYPE_DATA_SOURCE).getId()
 *    4.    Now get the DataSource object, initializing it if it is not initialized yet. DataSourceManager.getInstance().getDataSource(id,repoConn)
 *    5.    Get a Capabilities object from the DataSource. Ds.getMetaInfo(tcontxt)
 *    6.  Use the hashmap created in the CJP to lookup the internal DataType for the incoming CIS type
 *    7.    Determine whether the incoming type is a plain data type or has scale or precision
 *
 *            a. For a plain data type you can do this directly�
 *                i. Use the internal type and the capabilities object to look up the external type from the composite.datatype keys in the capabilities files. 
 *                    String nativeType = capabilities.getCompatibleNativeDataType(DataType.DATA_TYPE_INTEGER);
 *    
 *            b. For scale only:  There is a little more you can do when a length or precision is available�
 *                i. Use DataTypeFactory to get an object of the internal type: dt = DataTypeFactory.createDataType('VARCHAR',DataType.DATA_TYPE_STRING)
 *                    PrimitiveDataType dt = (PrimitiveDataType)DataTypeFactory.createDataType("CHAR(40)",DataType.DATA_TYPE_STRING);
 *
 *                ii.  Set the scale and precision
 *                    dt.setMaxLength(40);
 *                    dt.setMinLength(40);
 *            
 *                iii. Use the internal type and the capabilities object to look up the external type from the composite.datatype keys in the capabilities files. 
 *                    String nativeType = capabilities.getCompatibleNativeDataType(dt);
 *
 *            c. For scale and precision:  Or you can do something like this�
 *                i. Use DataTypeFactory to get an object of the internal type: dt = DataTypeFactory.createDataType('VARCHAR',DataType.DATA_TYPE_STRING)
 *                    PrimitiveDataType dt = (PrimitiveDataType)DataTypeFactory.createDataType("DEICMAL(12,2)",DataType.DATA_TYPE_DECIMAL);
 *
 *                ii.  Set the scale and precision
 *                    dt.setMaxDigits(12);
 *                    dt.setMaxFractionalDigits(2);
 *
 *                iii. Use the internal type and the capabilities object to look up the external type from the composite.datatype keys in the capabilities files. 
 *                    String nativeType = capabilities.getCompatibleNativeDataType(dt);
 *    
 *    Inputs:
 *        datasourcePath - The CIS full path to the data source resource
 *            e.g. /shared/examples/ds_inventory
 *        cisType - The CIS variable type
 *            e.g. VARCHAR(40), DECIMAL(32,2), INTEGER, LONGVARCHAR, etc.
 *        
 *    Outputs:
 *      result (    - A cursor containing the CIS types and native data source types
 *           cisType,  VARCHAR - This is the same as the input CIS variable type.
 *               e.g. /shared/examples/ds_inventory
 *          cisNormalizedType,  VARCHAR - This applies upper case and normalizes any CIS types that are not in JdbcDataType definition.
 *              e.g. int --> INTEGER, varchar --> VARCHAR
 *           cisBaseType,  VARCHAR - Extracts the base type from the incoming cisType after it has been normalized
 *               e.g. varchar(40) --> VARCHAR, DECIMAL(32,2) --> DECIMAL
 *          cisScale,  INTEGER - The scale for the cisType input parameter.  A -1 is returned if there is no scale.
 *               e.g. The scale of VARCHAR(40) is 40, The scale of DECIMAL(32,2) is 32, The scale of INTEGER is -1.
 *           cisPrecision,  INTEGER - The precision for the cisType input parameter.  A -1 is returned if there is no precision.
 *               e.g. The precision of VARCHAR(40) is -1, The precision of DECIMAL(32,2) is 2
 *           dataTypeId,  INTEGER - A CIS internal JdbcDataType identifier.
 *            e.g. -1000 represents INTEGER
 *           dataTypeName,  VARCHAR - A CIS internal JdbcDataType name.
 *               e.g. INTEGER
 *           nativeType,  VARCHAR - The native data type associated with the passed in cisType
 *              e.g. For Oracle...INTEGER --> number(10,0),
 *           nativeBaseType,  VARCHAR - Extracts the base type from the native data type.
 *               e.g. number(10,0) --> number
 *           nativeScale,  INTEGER - The scale of the native data type
 *               e.g. number(10,0) --> The scale is 10.  The scale is -1 if there is no scale.
 *           nativePrecision,  INTEGER - The precision of the native data type
 *               e.g. number(10,0) --> The precision is 0.  The precision is -1 if there is not precision.
 *        )
 *
 *    Exceptions:
 *       Throws a custom JDBC exception if input parameters are missing or it cannot find the data type.
 *    
 *            
 * --------------------------------------------
 * JdbcDataType as shown below:
 * --------------------------------------------
 * For CIS 6.1, this is a complete listing of the CIS internal JDBC Data Types.
 * 
 * An hashmap variable "cisDataTypes" has been created within this CJP that maps to these CIS ANSI types.  A lookup is performed
 * by this CJP against this hashmap which will return the DataType value.  This value is used to invoke the native capabilities
 * using "capabilities.getCompatibleNativeDataType(dt)". 
 * 
 * If in the future, additional CIS ANSI types are added, this CJP will need to be updated to reflect that enhancement.
 * 
  private static final Map <String, DataType> cisDataTypes = new HashMap();
  
  public static final DataType TINYINT   = createIntegerType("TINYINT",-998,Byte.MIN_VALUE,Byte.MAX_VALUE);
  public static final DataType SMALLINT  = createIntegerType("SMALLINT",-999,Short.MIN_VALUE,Short.MAX_VALUE);
  public static final DataType INTEGER   = createIntegerType("INTEGER",-1000,Integer.MIN_VALUE,Integer.MAX_VALUE);
  public static final DataType BIGINT    = createIntegerType("BIGINT",-997,Long.MIN_VALUE,Long.MAX_VALUE);
  public static final DataType FLOAT     = createDataType("FLOAT",-996,Value.VALUE_TYPE_FLOAT);
  public static final DataType REAL      = createDataType("REAL",-995,Value.VALUE_TYPE_FLOAT);
  public static final DataType DOUBLE    = createDataType("DOUBLE",-994,Value.VALUE_TYPE_FLOAT);
  public static final DataType DECIMAL   = createDecimalType("DECIMAL",-993, DEFAULT_DECIMAL_MAX_DIGITS, DEFAULT_DECIMAL_MAX_FRACTIONAL_DIGITS);
  public static final DataType NUMERIC   = createDecimalType("NUMERIC",-987, DEFAULT_NUMERIC_MAX_DIGITS, DEFAULT_NUMERIC_MAX_FRACTIONAL_DIGITS);
  public static final DataType CHAR        = createDataType("CHAR",-992,Value.VALUE_TYPE_STRING, DEFAULT_STRING_LENGTH, DEFAULT_STRING_LENGTH);
  public static final DataType VARCHAR     = createDataType("VARCHAR",-991,Value.VALUE_TYPE_STRING, 0, DEFAULT_STRING_LENGTH);
  public static final DataType LONGVARCHAR = createDataType("LONGVARCHAR",-983,Value.VALUE_TYPE_STRING, 0, Integer.MAX_VALUE);
  public static final DataType DATE      = createDataType("DATE",-990,Value.VALUE_TYPE_DATE);
  public static final DataType TIME      = createDataType("TIME",-979,Value.VALUE_TYPE_TIME, 2);
  public static final DataType TIMESTAMP = createDataType("TIMESTAMP",-989,Value.VALUE_TYPE_DATETIME, 2);
  public static final DataType BINARY    = createDataType("BINARY",-985,Value.VALUE_TYPE_BINARY, DEFAULT_BINARY_LENGTH, DEFAULT_BINARY_LENGTH);
  public static final DataType VARBINARY = createDataType("VARBINARY",-988,Value.VALUE_TYPE_BINARY, 0, DEFAULT_BINARY_LENGTH);
  public static final DataType LONGVARBINARY = createDataType("LONGVARBINARY",-1003,Value.VALUE_TYPE_BINARY, 0, DEFAULT_BINARY_LENGTH);
  public static final DataType OTHER     = createDataType("OTHER",-981,Value.VALUE_TYPE_OTHER);
  public static final DataType BIT       = createIntegerType("BIT",-977,0,1);
  public static final DataType BOOLEAN   = createDataType("BOOLEAN",-986,Value.VALUE_TYPE_BOOLEAN);
  public static final DataType BLOB      = createDataType("BLOB",-984,Value.VALUE_TYPE_BLOB, 0, DEFAULT_BLOB_LENGTH);
  public static final DataType CLOB     = createDataType("CLOB",-982,Value.VALUE_TYPE_CLOB, 0, DEFAULT_CLOB_LENGTH);
  public static final DataType ARRAY     = createDataType("ARRAY",-1002,Value.VALUE_TYPE_ARRAY);
  public static final DataType NULL      = createDataType("NULL",-980,Value.VALUE_TYPE_NULL);
  public static final DataType XML = createDataType("XML",-974,Value.VALUE_TYPE_XML);
  public static final DataType INTERVAL_DAY = createIntervalDayDataType("INTERVAL DAY", -973, DataType.INTERVAL_DAY);
  public static final DataType INTERVAL_YEAR = createIntervalYearDataType("INTERVAL YEAR", -972, DataType.INTERVAL_YEAR);
  public static final DataType INTERVAL_YEAR_TO_MONTH = createIntervalYearDataType("INTERVAL YEAR TO MONTH",-971,DataType.INTERVAL_YEAR_TO_MONTH);
  public static final DataType INTERVAL_MONTH = createIntervalYearDataType("INTERVAL MONTH",-970,DataType.INTERVAL_MONTH);
  public static final DataType INTERVAL_YEAR_TO_MONTH = createIntervalYearDataType("INTERVAL YEAR TO MONTH",-971,DataType.INTERVAL_YEAR_TO_MONTH);
  public static final DataType INTERVAL_MONTH = createIntervalYearDataType("INTERVAL MONTH",-970,DataType.INTERVAL_MONTH);
  public static final DataType INTERVAL_DAY_TO_SECOND = createIntervalDayDataType("INTERVAL DAY TO SECOND",-969,DataType.INTERVAL_DAY_TO_SECOND);
  public static final DataType INTERVAL_DAY_TO_MINUTE = createIntervalDayDataType("INTERVAL DAY TO MINUTE",-968,DataType.INTERVAL_DAY_TO_MINUTE);
  public static final DataType INTERVAL_DAY_TO_HOUR = createIntervalDayDataType("INTERVAL DAY TO HOUR",-967,DataType.INTERVAL_DAY_TO_HOUR);
  public static final DataType INTERVAL_HOUR = createIntervalDayDataType("INTERVAL HOUR",-966,DataType.INTERVAL_HOUR);
  public static final DataType INTERVAL_HOUR_TO_SECOND = createIntervalDayDataType("INTERVAL HOUR TO SECOND",-965,DataType.INTERVAL_HOUR_TO_SECOND);
  public static final DataType INTERVAL_HOUR_TO_MINUTE = createIntervalDayDataType("INTERVAL HOUR TO MINUTE",-964,DataType.INTERVAL_HOUR_TO_MINUTE);
  public static final DataType INTERVAL_MINUTE = createIntervalDayDataType("INTERVAL MINUTE",-963,DataType.INTERVAL_MINUTE);
  public static final DataType INTERVAL_MINUTE_TO_SECOND = createIntervalDayDataType("INTERVAL MINUTE TO SECOND",-962,DataType.INTERVAL_MINUTE_TO_SECOND);
  public static final DataType INTERVAL_SECOND = createIntervalDayDataType("INTERVAL SECOND",-961,DataType.INTERVAL_SECOND);

	Author:      Mike Tinius
	Date:        5/2/2012
	CSW Version: 5.2.0
	
*/

//Composite use only API's
//

import com.compositesw.cdms.datasource.DataSource;
import com.compositesw.cdms.datasource.DataSourceManager;
import com.compositesw.cdms.datasource.metainfo.Capabilities;
import com.compositesw.common.repository.Path;
import com.compositesw.data.types.JdbcDataTypes;
import com.compositesw.data.DataType;
import com.compositesw.data.DataTypeFactory;
import com.compositesw.data.PrimitiveDataType;
import com.compositesw.extension.*;
import com.compositesw.server.customproc.ExecutionEnvironmentImpl;
import com.compositesw.server.repository.Metadata;
import com.compositesw.server.repository.RepositoryConnection;
import com.compositesw.server.request.TransactionContext;

//public API's
//
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GetAnsi2NativeMapping implements CustomProcedure {

    private ExecutionEnvironment qenv;
    private ExecutionEnvironmentImpl qenvInfo;

    // This the list of Composite JDBC Data Types
    private static final Map <String, DataType> cisDataTypes = new HashMap<String, DataType>()
    {         
        {   // String: Name of the DataType                            // DataType: Data Type value object
            put(JdbcDataTypes.TINYINT.getName(),                   JdbcDataTypes.TINYINT);
            put(JdbcDataTypes.SMALLINT.getName(),                  JdbcDataTypes.SMALLINT);
            put(JdbcDataTypes.INTEGER.getName(),                   JdbcDataTypes.INTEGER);
            put(JdbcDataTypes.BIGINT.getName(),                    JdbcDataTypes.BIGINT);
            put(JdbcDataTypes.FLOAT.getName(),                     JdbcDataTypes.FLOAT);
            put(JdbcDataTypes.REAL.getName(),                      JdbcDataTypes.REAL);
            put(JdbcDataTypes.DOUBLE.getName(),                    JdbcDataTypes.DOUBLE);
            put(JdbcDataTypes.DECIMAL.getName(),                   JdbcDataTypes.DECIMAL);
            put(JdbcDataTypes.NUMERIC.getName(),                   JdbcDataTypes.NUMERIC);
            put(JdbcDataTypes.CHAR.getName(),                      JdbcDataTypes.CHAR);
            put(JdbcDataTypes.VARCHAR.getName(),                   JdbcDataTypes.VARCHAR);
            put(JdbcDataTypes.LONGVARCHAR.getName(),               JdbcDataTypes.LONGVARCHAR);
            put(JdbcDataTypes.DATE.getName(),                      JdbcDataTypes.DATE);
            put(JdbcDataTypes.TIME.getName(),                      JdbcDataTypes.TIME);
            put(JdbcDataTypes.TIMESTAMP.getName(),                 JdbcDataTypes.TIMESTAMP);
            put(JdbcDataTypes.BINARY.getName(),                    JdbcDataTypes.BINARY);
            put(JdbcDataTypes.VARBINARY.getName(),                 JdbcDataTypes.VARBINARY);
            put(JdbcDataTypes.LONGVARBINARY.getName(),             JdbcDataTypes.LONGVARBINARY);
            put(JdbcDataTypes.OTHER.getName(),                     JdbcDataTypes.OTHER);
            put(JdbcDataTypes.BIT.getName(),                       JdbcDataTypes.BIT);
            put(JdbcDataTypes.BOOLEAN.getName(),                   JdbcDataTypes.BOOLEAN);
            put(JdbcDataTypes.BLOB.getName(),                      JdbcDataTypes.BLOB);
            put(JdbcDataTypes.CLOB.getName(),                      JdbcDataTypes.CLOB);
            put(JdbcDataTypes.ARRAY.getName(),                     JdbcDataTypes.ARRAY);
            put(JdbcDataTypes.NULL.getName(),                      JdbcDataTypes.NULL);
            put(JdbcDataTypes.XML.getName(),                       JdbcDataTypes.XML);
            put(JdbcDataTypes.INTERVAL_DAY.getName(),              JdbcDataTypes.INTERVAL_DAY);
            put(JdbcDataTypes.INTERVAL_YEAR.getName(),             JdbcDataTypes.INTERVAL_YEAR);
            put(JdbcDataTypes.INTERVAL_YEAR_TO_MONTH.getName(),    JdbcDataTypes.INTERVAL_YEAR_TO_MONTH);
            put(JdbcDataTypes.INTERVAL_MONTH.getName(),            JdbcDataTypes.INTERVAL_MONTH);
            put(JdbcDataTypes.INTERVAL_YEAR_TO_MONTH.getName(),    JdbcDataTypes.INTERVAL_YEAR_TO_MONTH);
            put(JdbcDataTypes.INTERVAL_MONTH.getName(),            JdbcDataTypes.INTERVAL_MONTH);
            put(JdbcDataTypes.INTERVAL_DAY_TO_SECOND.getName(),    JdbcDataTypes.INTERVAL_DAY_TO_SECOND);
            put(JdbcDataTypes.INTERVAL_DAY_TO_MINUTE.getName(),    JdbcDataTypes.INTERVAL_DAY_TO_MINUTE);
            put(JdbcDataTypes.INTERVAL_DAY_TO_HOUR.getName(),      JdbcDataTypes.INTERVAL_DAY_TO_HOUR);
            put(JdbcDataTypes.INTERVAL_HOUR.getName(),             JdbcDataTypes.INTERVAL_HOUR);
            put(JdbcDataTypes.INTERVAL_HOUR_TO_SECOND.getName(),   JdbcDataTypes.INTERVAL_HOUR_TO_SECOND);
            put(JdbcDataTypes.INTERVAL_HOUR_TO_MINUTE.getName(),   JdbcDataTypes.INTERVAL_HOUR_TO_MINUTE);
            put(JdbcDataTypes.INTERVAL_MINUTE.getName(),           JdbcDataTypes.INTERVAL_MINUTE);
            put(JdbcDataTypes.INTERVAL_MINUTE_TO_SECOND.getName(), JdbcDataTypes.INTERVAL_MINUTE_TO_SECOND);
            put(JdbcDataTypes.INTERVAL_SECOND.getName(),           JdbcDataTypes.INTERVAL_SECOND);
        } 
    };

    private CustomCursor outputCursor;
    private static class rowType {
        String cisType;
        String cisNormalizedType;
        String cisBaseType;
        int cisScale;
        int cisPrecision;
        int dataTypeId;
        String dataTypeName;
        String nativeType;
        String nativeBaseType;
        int nativeScale;
        int nativePrecision;
    }
    private static ArrayList<rowType> outputList = null;
    
    public GetAnsi2NativeMapping() {
    }

    /**
     * This is called once just after constructing the class.  The
     * environment contains methods used to interact with the server.
     */
    public void initialize (ExecutionEnvironment qenv) throws SQLException {
        this.qenv = qenv;
    }

    /**
     * Called during introspection to get the description of the input
     * and output parameters.  Should not return null.
     */
    public ParameterInfo[] getParameterInfo() {
        return new ParameterInfo[] { 
            new ParameterInfo ("datasourcePath", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("cisType", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo ("result", TYPED_CURSOR, DIRECTION_OUT,
                new ParameterInfo[] { 
                    new ParameterInfo ("cisType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("cisNormalizedType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("cisBaseType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("cisScale", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("cisPrecision", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("dataTypeId", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("dataTypeName", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("nativeType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("nativeBaseType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("nativeScale", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("nativePrecision", Types.INTEGER, DIRECTION_NONE),
                }
            ) 
        };
    }

    /**
     * Called to invoke the stored procedure.  Will only be called a
     * single time per instance.  Can throw CustomProcedureException or
     * SQLException if there is an error during invoke.
     */
    public void invoke (Object[] inputValues) throws CustomProcedureException, SQLException {
        
        try {
            if (inputValues == null || inputValues.length != 2)
                throw new CustomProcedureException ("Error in CJP "+getName()+": IN Parameters datasourcePath and cisType must be provided.");
                
            // Get input values
            String datasourcePath = (String) inputValues[0];
            String cisType = (String) inputValues[1];
            
            if (datasourcePath == null || datasourcePath.trim().length() == 0)
                throw new CustomProcedureException ("Error in CJP "+getName()+": IN Parameter datasourcePath may not be null or blank.");
            if (cisType == null || cisType.trim().length() == 0)
                throw new CustomProcedureException ("Error in CJP "+getName()+": IN Parameter cisType may not be null or blank.");
           
            // Initialize variables
            outputList = new ArrayList<rowType>();
            rowType row = new rowType();
            row.cisType = cisType;
  
            // Normalize CIS types that do not map to internal variables
            row.cisNormalizedType = cisType.toUpperCase();
            if (row.cisType.equalsIgnoreCase("INT"))
                row.cisNormalizedType = "INTEGER";
            
            // Get the data type from the list of CIS JDBC data types
            row.cisBaseType = getBaseType(row.cisNormalizedType);
            row.cisScale = getScale(row.cisNormalizedType);
            row.cisPrecision = getPrecision(row.cisNormalizedType);

            // Retrieve the CIS JDBC Data Type
            DataType dataType = cisDataTypes.get(row.cisBaseType);          
            
            if (dataType == null) {
                throw new CustomProcedureException ("Error in CJP "+getName()+": No DataType found for base type: "+row.cisBaseType);              
            } else {
                // Cast the ExecutionEnvironment
                qenvInfo = (ExecutionEnvironmentImpl) qenv;
            
                // Get the capabilities for a given CIS data source path
                Path dsPath = new Path(datasourcePath);
                TransactionContext tcontext = qenvInfo.getParentRequest().getTransactionContext();
                RepositoryConnection repoConn = tcontext.getRepositoryConnection();
                int dsId = repoConn.getResource(dsPath, Metadata.TYPE_DATA_SOURCE).getId();
                DataSource ds = DataSourceManager.Provider.getInstance((Object[]) null).getDataSource(dsId,repoConn); // using the Provider instance is new in 6.2
                Capabilities capabilities = ds.getMetaInfo(tcontext);
                
                // Get the native data type from the data source capabilities.
                // Set any scale and precision from incoming CIS variable as needed
                if (row.cisType.contains("(") && row.cisType.contains(")")) {
                    
                    // Get the primitive data type in order to set scale and precision
                    PrimitiveDataType dt = (PrimitiveDataType)DataTypeFactory.createDataType(row.cisBaseType,dataType);

                    if (dt != null) {
                        // Set scale
                        if (row.cisScale > -1 && row.cisPrecision < 0) {
                            dt.setMaxLength(row.cisScale);
                            dt.setMinLength(row.cisScale);
                        }
                        // Set scale and precision
                        if (row.cisScale > -1 && row.cisPrecision > -1) {
                            dt.setMaxDigits(row.cisScale);
                            dt.setMaxFractionalDigits(row.cisPrecision);        
                        } 
                        // Get native capabilities
                        row.dataTypeId = dt.getId();
                        row.dataTypeName = dt.getName();
                        row.nativeType = capabilities.getCompatibleNativeDataType(dt);
                        // Get native base type, scale and precision
                        row.nativeBaseType = getBaseType(row.nativeType);
                        row.nativeScale = getScale(row.nativeType);
                        row.nativePrecision = getPrecision(row.nativeType);
                    }
                } else {
                    // Get native capabilities
                    row.dataTypeId = dataType.getId();
                    row.dataTypeName = dataType.getName();
                    row.nativeType = capabilities.getCompatibleNativeDataType(dataType);
                    // Get native base type, scale and precision
                    row.nativeBaseType = getBaseType(row.nativeType);
                    row.nativeScale = getScale(row.nativeType);
                    row.nativePrecision = getPrecision(row.nativeType);
                }
            }
            outputList.add (row);

        } catch (Exception e) {
            throw new CustomProcedureException ("Error in CJP "+getName()+": "+e.toString());
        }

        outputCursor = createCustomCursor();
    }
    
    /**
     * Get the base type from a variable
     */
    private String getBaseType(String var) {
        String baseType = var;
        if (baseType.contains("(")) {
            baseType = baseType.substring(0, var.indexOf("(")).trim();
        }
        return baseType;
    }

    /**
     * Get the scale from a variable
     */
    private int getScale(String var) {
        int scale = -1;
        if (var.contains("(") && var.contains(")")) {
            String sc = null;
            if (var.contains(",")) {
                sc = var.substring(var.indexOf("(")+1, var.indexOf(",")).trim();
            } else {
                sc = var.substring(var.indexOf("(")+1, var.indexOf(")")).trim();
            }
            if (sc != null && sc.length() > 0)
                scale = Integer.parseInt(sc);
        }
        return scale;
    }
    
    /**
     * Get the precision from a variable
     */
    private int getPrecision(String var) {
        int precision = -1;
        if (var.contains("(") && var.contains(")") && var.contains(",")) {
               String pr = null;
            if (var.contains(",")) {
                pr = var.substring(var.indexOf(",")+1, var.indexOf(")")).trim();
                if (pr != null && pr.length() > 0)
                    precision = Integer.parseInt(pr);
            } 
        }
        return precision;
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
        return new Object[] { outputCursor };
    }

    /**
     * Create a custom cursor output.
     */
    private CustomCursor createCustomCursor() {
        return new CustomCursor() {
            
            private int counter = 0;
            
            public ParameterInfo[] getColumnInfo () {
                return new ParameterInfo[] { 
                    new ParameterInfo ("cisType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("cisNormalizedType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("cisBaseType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("cisScale", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("cisPrecision", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("dataTypeId", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("dataTypeName", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("nativeType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("nativeBaseType", Types.VARCHAR, DIRECTION_NONE),
                    new ParameterInfo ("nativeScale", Types.INTEGER, DIRECTION_NONE),
                    new ParameterInfo ("nativePrecision", Types.INTEGER, DIRECTION_NONE)
                };
                }

            public Object[] next() throws CustomProcedureException, SQLException {
                if (counter >= outputList.size ()) {
                    return null;
                } else {
                    return new Object[] {
                            (String) outputList.get(counter).cisType,
                            (String) outputList.get(counter).cisNormalizedType,
                            (String) outputList.get(counter).cisBaseType,
                            (int) outputList.get(counter).cisScale,
                            (int) outputList.get(counter).cisPrecision,
                            (int) outputList.get(counter).dataTypeId,
                            (String) outputList.get(counter).dataTypeName,
                            (String) outputList.get(counter).nativeType,
                               (String) outputList.get(counter).nativeBaseType,
                              (int) outputList.get(counter).nativeScale,
                            (int) outputList.get(counter++).nativePrecision,
                    };
                }
            }

            public void close() throws CustomProcedureException, SQLException {
                // do nothing
            }
        };
    }

    /**
     * Called when the procedure reference is no longer needed.  Close
     * may be called without retrieving any of the output values (such
     * as cursors) or even invoking, so this needs to do any remaining
     * cleanup.  Close may be called concurrently with any other call
     * such as "invoke" or "getOutputValues".  In this case, any pending
     * methods should immediately throw a CustomProcedureException.
     */
    public void close() throws CustomProcedureException, SQLException {
        if (outputCursor != null)
            outputCursor.close ();
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
        return "GetAnsi2NativeMapping";
    }

    /**
     * Called during introspection to get the description of the stored
     * procedure.  Should not return null.
     */
    public String getDescription() {
        return "Custom procedure that returns a data source native data type mapping between a given CIS ANSI type and a given data source.";
    }

    //
    // Transaction methods
    //

    /**
     * Returns true if the custom procedure uses transactions.  If this
     * method returns false then commit and rollback will not be called.
     */
    public boolean canCommit() {
        return false;
    }

    /**
     * Commit any open transactions.
     */
    public void commit() throws SQLException {
    }

    /**
     * Rollback any open transactions.
     */
    public void rollback() throws SQLException {
    }

    /**
     * Returns true if the transaction can be compensated.
     */
    public boolean canCompensate() {
        return false;
    }

    /**
     * Compensate any committed transactions (if supported).
     */
    public void compensate (ExecutionEnvironment qenv) throws SQLException {
    }
    
}
