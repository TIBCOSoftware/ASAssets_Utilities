package com.cisco.dvbu.ps.utils.date;

/*
	Description:
	  Returns the timezone this instance of CIS is running in. Various display types are available:
	
	  ID         - The timezone ID (according to Java, i.e. "America/Los_Angeles")
	  SHORT_NAME - The name of the timezone in short format (i.e. "PDT")
	  LONG_NAME  - The name of the timezone in long format (i.e. "Pacific Daylight Time")
	  OFFSET     - The number of milliseconds to add to GMT time to get the current timezone's time (i.e. "-28800000")
	  XML        - The timezone in hours:minutes format needed for XML timestamps
	
	Inputs:
	  displayType - The desired format of the timezone
	
	Outputs:
	  outValue    - This CIS instance's timezone in the specified format
	
	Exceptions:
	  None
	
    Modified Date:  Modified By:        CSW Version:    Reason:
    08/11/2010      Calvin Goodrich     5.1.0           Created new
    11/07/2014      Calvin Goodrich     6.2.6           Updated to include XML timezone format

	(c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.

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
import com.compositesw.common.logging.Logger;

import java.sql.*;

import java.util.TimeZone;

public class GetServerTimezone
    extends DateUtilTemplate
    implements CustomProcedure
{
    static {
        className = "GetServerTimezone";
        logger = Logger.getLogger(GetServerTimezone.class.getName());
    }

    private String outValue = null;

    public GetServerTimezone() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug(className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo("displayType", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("outValue",    Types.VARCHAR, DIRECTION_OUT),
        };
    }

    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException {
        if (logger.isDebug()) {
            logger.debug(className + ".invoke called");
            logger.debug("Invoked with displayType : " + (null==inputValues[0]?"[null]":inputValues[0].toString()));
        }

        TimeZone tz = TimeZone.getDefault();

        String displayType = ((String)inputValues[0]);
        if (null == displayType) {
            throw new CustomProcedureException("DisplayType must be specified as 'ID', 'LONG_NAME', 'SHORT_NAME', 'OFFSET', or 'XML'");
        }
        
        if (displayType.equalsIgnoreCase("ID")) {
            outValue = tz.getID();
        } else if (displayType.equalsIgnoreCase("LONG_NAME")) {
            outValue = tz.getDisplayName(tz.useDaylightTime(), TimeZone.LONG);
        } else if (displayType.equalsIgnoreCase("SHORT_NAME")) {
            outValue = tz.getDisplayName(tz.useDaylightTime(), TimeZone.SHORT);
        } else if (displayType.equalsIgnoreCase("OFFSET")) {
            outValue = "" + tz.getRawOffset();
        } else if (displayType.equalsIgnoreCase("XML")) {
        	int tzOff = tz.getRawOffset();
        	int hour = Math.abs((int) (tzOff / 3600000));
        	int min = Math.abs((int) (tzOff % 3600000));
        	
            outValue = ((tzOff >= 0) ? "+" : "-") +
        	           ((hour > 9) ? hour : "0" + hour) +
        	           ":" +
        	           ((min > 9) ? min : "0" + min);
        } else {
            throw new CustomProcedureException("DisplayType must be specified as 'ID', 'LONG_NAME', 'SHORT_NAME', 'OFFSET', or 'XML'");
        }
    }

    public Object[] getOutputValues()
    {
        if (logger.isDebug()) {
            logger.debug(className + ".getOutputValues called");
        }

        return new Object[] { outValue };
    }

    public String getDescription() {
        if (logger.isDebug()) {
            logger.debug(className + ".getDescription called");
        }
        return "Returns the timezone of this instance of CIS.";
    }
}
