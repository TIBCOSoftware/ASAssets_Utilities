package com.cisco.dvbu.ps.utils.date;

/*
	Description:
	  Returns the timezone this instance of CIS is running in. Various display types are available:
	
	  ID         - The timezone ID (according to Java, i.e. "America/Los_Angeles")
	  SHORT_NAME - The name of the timezone in short format (i.e. "PDT")
	  LONG_NAME  - The name of the timezone in long format (i.e. "Pacific Daylight Time")
	  OFFSET     - The number of milliseconds to add to GMT time to get the current timezone's time (i.e. "-28800000")
	
	Inputs:
	  displayType - The desired format of the timezone
	
	Outputs:
	  outValue    - This CIS instance's timezone in the specified format
	
	Exceptions:
	  None
	
	Author:      Calvin Goodrich
	Date:        8/11/2010
	CSW Version: 5.1.0
	
	(c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.
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
            throw new CustomProcedureException("DisplayType must be specified as 'ID', 'LONG_NAME', 'SHORT_NAME', or 'OFFSET'");
        }
        
        if (displayType.equalsIgnoreCase("ID")) {
            outValue = tz.getID();
        } else if (displayType.equalsIgnoreCase("LONG_NAME")) {
            outValue = tz.getDisplayName(tz.useDaylightTime(), TimeZone.LONG);
        } else if (displayType.equalsIgnoreCase("SHORT_NAME")) {
            outValue = tz.getDisplayName(tz.useDaylightTime(), TimeZone.SHORT);
        } else if (displayType.equalsIgnoreCase("OFFSET")) {
            outValue = "" + tz.getRawOffset();
        } else {
            throw new CustomProcedureException("DisplayType must be specified as 'ID', 'LONG_NAME', 'SHORT_NAME', or 'NAME'");
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
