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
 * Source File: ParameterInfoDisplay.java
 *
 *	Author:      Mike Tinius
 *	Date:        8/2/2010
 *	CSW Version: 5.1.0
 *
 */

import com.compositesw.extension.ParameterInfo;
import com.compositesw.extension.ProcedureConstants;
import java.sql.Types;

public class ParameterInfoDisplay {
	
	//Direction Constants
	private static final int DIRECTION_IN = ProcedureConstants.DIRECTION_IN;
	private static final int DIRECTION_INOUT = ProcedureConstants.DIRECTION_INOUT;
	private static final int DIRECTION_OUT = ProcedureConstants.DIRECTION_OUT;
	private static final int DIRECTION_NONE = ProcedureConstants.DIRECTION_NONE;
	private static final int GENERIC_CURSOR = ProcedureConstants.GENERIC_CURSOR;
	private static final int TYPED_CURSOR = ProcedureConstants.TYPED_CURSOR;

	//Type Constants
	private static final int CHAR = Types.CHAR;
	private static final int VARCHAR = Types.VARCHAR;
	private static final int LONGVARCHAR = Types.LONGVARCHAR;
	private static final int NUMERIC = Types.NUMERIC;
	private static final int DECIMAL = Types.DECIMAL;
	private static final int INTEGER = Types.INTEGER;
	private static final int SMALLINT = Types.SMALLINT;
	private static final int TINYINT = Types.TINYINT;
	private static final int BIGINT = Types.BIGINT;
	private static final int BOOLEAN = Types.BOOLEAN;
	private static final int REAL = Types.REAL;
	private static final int FLOAT = Types.FLOAT;
	private static final int DOUBLE = Types.DOUBLE;
	private static final int BINARY = Types.BINARY;
	private static final int VARBINARY = Types.VARBINARY;
	private static final int LONGVARBINARY = Types.LONGVARBINARY;
	private static final int DATE = Types.DATE;
	private static final int TIME = Types.TIME;
	private static final int TIMESTAMP = Types.TIMESTAMP;
	private static final int CLOB = Types.CLOB;
	private static final int BLOB = Types.BLOB;
	private static final int CURSOR = 5521;

	 //
	 // displayParameterInfo - Display the parameter info area to the console
	 //	
	 public void displayParameterInfo(ParameterInfo[] pia) {
		String direction = null;
		String type = null;
		System.out.println("getParameterInfo:  ArgNum: Direction{direction constant} ColumnName: Type{type constant}");
	
        for (int i=0; i < pia.length; i++) {

        	direction = getDirection(pia[i].getDirection());
        	type = getType(pia[i].getType());       	
        	System.out.println("   arg"+i+": "+direction+"{"+pia[i].getDirection()+"} "+pia[i].getName() + ": " + type+"{"+pia[i].getType()+"}");
        	
        	if (type.equalsIgnoreCase("CURSOR")) {
        		int j=0;
	            for (ParameterInfo pic : pia[i].getColumns()) {
		        	direction = getDirection(pic.getDirection());
		        	type = getType(pic.getType());       	
		        	System.out.println("      arg"+j++ +": "+direction+"{"+pic.getDirection()+"} "+pic.getName() + ": " + type+"{"+pic.getType()+"}");
	            }	        		
        	}
        }
	 }
	 //
	 // getDirection - direction of the parameter
	 //	
	 public String getDirection(int intDirection) {
		 String direction = "";
	     switch (intDirection) {     
			case DIRECTION_IN: direction = "IN"; break;
			case DIRECTION_INOUT: direction = "INOUT"; break;
			case DIRECTION_OUT: direction = "OUT"; break;
			case DIRECTION_NONE: direction = "NONE"; break;
			case GENERIC_CURSOR: direction = "CURSOR"; break;
			case TYPED_CURSOR: direction = "CURSOR"; break;
			default: direction = "unknown"; break;
	     }
//		 System.out.println("        Direction="+direction+":"+intDirection);
	     return direction;
	 }

	 //
	 // getType - type of the parameter
	 //	
	 public String getType(int intType) {
		 String type = "";
     	 switch (intType) {
			case CHAR: type = "CHAR"; break;
			case VARCHAR: type = "VARCHAR"; break;
			case LONGVARCHAR: type = "LONGVARCHAR"; break;
			case NUMERIC: type = "NUMERIC"; break;
			case DECIMAL: type = "DECIMAL"; break;
			case INTEGER: type = "INTEGER"; break;
			case SMALLINT: type = "SMALLINT"; break;
			case TINYINT: type = "TINYINT"; break;
			case BIGINT: type = "BIGINT"; break;
			case BOOLEAN: type = "BOOLEAN"; break;
			case REAL: type = "REAL"; break;
			case FLOAT: type = "FLOAT"; break;
			case DOUBLE: type = "DOUBLE"; break;
			case BINARY: type = "BINARY"; break;
			case VARBINARY: type = "VARBINARY"; break;
			case LONGVARBINARY: type = "LONGVARBINARY"; break;
			case DATE: type = "DATE"; break;
			case TIME: type = "TIME"; break;
			case TIMESTAMP: type = "TIMESTAMP"; break;
			case CLOB: type = "CLOB"; break;
			case BLOB: type = "BLOB"; break;
			case CURSOR: type = "CURSOR"; break;
			default: type = "unknown"; break;
     	 }
//		 System.out.println("        Type="+type+":"+intType);
	     return type;
	 }

}
