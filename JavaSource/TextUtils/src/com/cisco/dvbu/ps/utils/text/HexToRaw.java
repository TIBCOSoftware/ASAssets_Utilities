package com.cisco.dvbu.ps.utils.text;

/*
    HexToRaw:

    This procedure converts a hexadecimal string into a binary array value.

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
        hexVal - The string to convert into a binary array  
            Values: Any text value whose characters are 0-9 and/or A-F


    Output:
        rawVal - The string converted to a binary array
            Values: A binary array


    Exceptions:  none


    Modified Date:  Modified By:        CSW Version:    Reason:
    07/26/2011      Alex Dedov          5.2.0           Created new

    © 2011, 2014 Cisco and/or its affiliates. All rights reserved.

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

import java.sql.SQLException;
import java.sql.Types;

import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ExecutionEnvironment;
import com.compositesw.extension.ParameterInfo;

public class HexToRaw implements CustomProcedure {

	private ExecutionEnvironment qenv;
	private byte[] b;

	public HexToRaw() {
	}

	public void initialize(ExecutionEnvironment qenv) {
		this.qenv = qenv;
	}

	public void close() {
	}

	public String getName() {
		return "HexToRaw";
	}

	public String getDescription() {
		return "Convert an string of Hex digits into a binary array.";
	}

	public ParameterInfo[] getParameterInfo() {
		return new ParameterInfo[] {
				new ParameterInfo("hexVal", Types.VARCHAR, DIRECTION_IN),
				new ParameterInfo("rawVal", Types.VARBINARY, DIRECTION_OUT), };
	}

	public void invoke(Object[] inputValues) throws CustomProcedureException,
			SQLException {
		log(LOG_DEBUG, "HexToRaw.invoke called");

		String hexVal = (String)inputValues[0] ;
		if ( hexVal.length() % 2 != 0 ) {
			hexVal = "0".concat(hexVal) ;
		}
		char[] digits = hexVal.toCharArray();

		b = new byte[digits.length / 2];

		int i = 0;
		while (i < digits.length) {
			int val = 0;
			switch (digits[i++]) {
			case '0':
				val += 0;
				break;
			case '1':
				val += 1;
				break;
			case '2':
				val += 2;
				break;
			case '3':
				val += 3;
				break;
			case '4':
				val += 4;
				break;
			case '5':
				val += 5;
				break;
			case '6':
				val += 6;
				break;
			case '7':
				val += 7;
				break;
			case '8':
				val += 8;
				break;
			case '9':
				val += 9;
				break;
			case 'a':
			case 'A':
				val += 0xA;
				break;
			case 'b':
			case 'B':
				val += 0xB;
				break;
			case 'c':
			case 'C':
				val += 0xC;
				break;
			case 'd':
			case 'D':
				val += 0xD;
				break;
			case 'e':
			case 'E':
				val += 0xE;
				break;
			case 'f':
			case 'F':
				val += 0xF;
				break;
			default:
				throw new CustomProcedureException(
						"Hex digits must be 0-9 and A-F or a-f");
			}
			val = val << 4;
			switch (digits[i++]) {
			case '0':
				val += 0;
				break;
			case '1':
				val += 1;
				break;
			case '2':
				val += 2;
				break;
			case '3':
				val += 3;
				break;
			case '4':
				val += 4;
				break;
			case '5':
				val += 5;
				break;
			case '6':
				val += 6;
				break;
			case '7':
				val += 7;
				break;
			case '8':
				val += 8;
				break;
			case '9':
				val += 9;
				break;
			case 'a':
			case 'A':
				val += 0xA;
				break;
			case 'b':
			case 'B':
				val += 0xB;
				break;
			case 'c':
			case 'C':
				val += 0xC;
				break;
			case 'd':
			case 'D':
				val += 0xD;
				break;
			case 'e':
			case 'E':
				val += 0xE;
				break;
			case 'f':
			case 'F':
				val += 0xF;
				break;
			default:
				throw new CustomProcedureException(
						"Hex digits must be 0-9 and A-F or a-f");
			}
			b[i / 2 - 1] = (byte) val;
		}

	}

	public Object[] getOutputValues() {
		return new Object[] { b };
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
		CustomProcedure cp = new HexToRaw();
		cp.initialize(null);
		cp.invoke(args);
		byte[] outValue = (byte[]) (cp.getOutputValues()[0]);

		System.out.println("got " + outValue);
		for (byte b : outValue) {
			System.out.println(b);
		}
		cp.close();
	}
}
