package com.tibco.ps.utils.text;

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
	RawToHex:
	
	This procedure converts a binary array value into a hexadecimal string.
	
    NOTE - Calls to this procedure can be pushed to Oracle data sources by 
    adding some custom code to the Oracle capabilities file. Simply edit the
    $CIS_HOME/conf/adapters/system/oracle_<ver>_<type>_driver/oracle_<ver>_<type>_driver_values.xml  
    by adding the following lines just before the closing "</common:attributes>"
    line. CIS may need to be restarted after making this change.

    <ns726:attribute xmlns:ns726="http://www.compositesw.com/services/system/util/common">
        <ns726:name>/custom/HexToRaw(@null)</ns726:name>
        <ns726:type>STRING</ns726:type>
        <ns726:value>HexToRaw($1)</ns726:value>
        <ns726:configID>HexToRaw(~string)</ns726:configID>
    </ns726:attribute>
    <ns725:attribute xmlns:ns725="http://www.compositesw.com/services/system/util/common">
        <ns725:name>/custom/RawToHex(@null)</ns725:name>
        <ns725:type>STRING</ns725:type>
        <ns725:value>RawToHex($1)</ns725:value>
        <ns725:configID>RawToHex(~binary)</ns725:configID>
    </ns725:attribute> 
	
	
	Input:
	    rawVal - The binary array to convert into a string  
	        Values: Any binary array
	
	
	Output:
	    hexVal - The binary array converted to a string
	        Values: A text value whose characters are 0-9 and/or A-F
	
	
	Exceptions:  none
	
	
	Modified Date:  Modified By:        CSW Version:    Reason:
	07/26/2011      Alex Dedov          5.2.0           Created new
	
*/

import java.sql.SQLException;
import java.sql.Types;

import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;

public class RawToHex implements CustomProcedure {

	private ExecutionEnvironment qenv;
	private char[] c;

	public RawToHex() {
	}

	public void initialize(ExecutionEnvironment qenv) {
		this.qenv = qenv;
	}

	public void close() {
	}

	public String getName() {
		return "RawToHex";
	}

	public String getDescription() {
		return "Convert a binary value into a string of Hex digits.";
	}

	public ParameterInfo[] getParameterInfo() {
		return new ParameterInfo[] {
				new ParameterInfo("rawVal", Types.VARBINARY, DIRECTION_IN),
				new ParameterInfo("hexVal", Types.VARCHAR, DIRECTION_OUT), };
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException,
			SQLException {
		log(LOG_DEBUG, "RawToHex.invoke called");

		byte[] rawVal = (byte[]) inputValues[0];
		c = new char[rawVal.length * 2];

		int i = 0;
		for (byte b : rawVal) {
			switch ((b >>> 4) & 0xF) {
			case 0:
				c[i++] = '0';
				break;
			case 1:
				c[i++] = '1';
				break;
			case 2:
				c[i++] = '2';
				break;
			case 3:
				c[i++] = '3';
				break;
			case 4:
				c[i++] = '4';
				break;
			case 5:
				c[i++] = '5';
				break;
			case 6:
				c[i++] = '6';
				break;
			case 7:
				c[i++] = '7';
				break;
			case 8:
				c[i++] = '8';
				break;
			case 9:
				c[i++] = '9';
				break;
			case 0xA:
				c[i++] = 'A';
				break;
			case 0xB:
				c[i++] = 'B';
				break;
			case 0xC:
				c[i++] = 'C';
				break;
			case 0xD:
				c[i++] = 'D';
				break;
			case 0xE:
				c[i++] = 'E';
				break;
			case 0xF:
				c[i++] = 'F';
				break;
//			default:
//				c[i++] = '?' ;
			}
			switch (b & 0xF) {
			case 0:
				c[i++] = '0';
				break;
			case 1:
				c[i++] = '1';
				break;
			case 2:
				c[i++] = '2';
				break;
			case 3:
				c[i++] = '3';
				break;
			case 4:
				c[i++] = '4';
				break;
			case 5:
				c[i++] = '5';
				break;
			case 6:
				c[i++] = '6';
				break;
			case 7:
				c[i++] = '7';
				break;
			case 8:
				c[i++] = '8';
				break;
			case 9:
				c[i++] = '9';
				break;
			case 0xA:
				c[i++] = 'A';
				break;
			case 0xB:
				c[i++] = 'B';
				break;
			case 0xC:
				c[i++] = 'C';
				break;
			case 0xD:
				c[i++] = 'D';
				break;
			case 0xE:
				c[i++] = 'E';
				break;
			case 0xF:
				c[i++] = 'F';
				break;
//			default:
//				c[i++] = '?' ;
			}
		}

	}

	public Object[] getOutputValues() {
		return new Object[] { new String(c) };
	}

	public int getNumAffectedRows() {
		return -1;
	}

	public boolean canCommit() {
		return false;
	}

	public boolean canCompensate() {
		return false;
	}

	public void commit() {
	}

	public void rollback() {
	}

	public void compensate(ExecutionEnvironment qenv) {
	}

	private void log(int level, String msg) {
		if (qenv == null) {
			System.out.println(msg);
		} else {
			qenv.log(level, msg);
		}
	}

	public static void main(String[] args) throws Exception {
		if ( args.length < 1 ) {
			System.out.println("No input, exiting...") ;
			return ;
		}
		System.out.println("Input: " + args[0]) ;
		
		CustomProcedure cp = new RawToHex();
		cp.initialize(null);
		
		if ( args.length > 1 && "-n".equalsIgnoreCase(args[1])) {
			Integer i = Integer.decode(args[0]) ;
			byte[] a = new byte[8] ;
			a[7] = (byte)(0x000F & i) ;
			a[6] = (byte)(0x000F & (i >>>  4)) ;
			a[5] = (byte)(0x000F & (i >>>  8)) ;
			a[4] = (byte)(0x000F & (i >>> 12)) ;			
			a[3] = (byte)(0x000F & (i >>> 16)) ;
			a[2] = (byte)(0x000F & (i >>> 20)) ;
			a[1] = (byte)(0x000F & (i >>> 24)) ;
			a[0] = (byte)(0x000F & (i >>> 28)) ;
			cp.invoke(new Object[]{a}) ;
		}
		else {
			cp.invoke(new Object[] { args[0].getBytes() });
		}
		String outValue = (String) (cp.getOutputValues()[0]);

		System.out.println("got " + outValue);
		cp.close();
	}
}
