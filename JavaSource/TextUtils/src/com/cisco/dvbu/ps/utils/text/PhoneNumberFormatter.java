package com.cisco.dvbu.ps.utils.text;
/*
	Description:
	  Provides standard formatting of phone numbers. If format string is not specified as an input (null or blank),
	  a number will be formatted as 999-999-9999. See http://download.oracle.com/javase/6/docs/api/java/util/Formatter.html
	  for details on formatting syntax.
	
	Inputs:
	  inPhoneNumber      - a 10-digit phone number, arbitrary formatted
	  inOutputFormat     - output formatting pattern
	
	Output:
	  outPhoneNumber     - Input number formatted according to the pattern
	
	Exceptions:
	  CustomProcedureException - if supplied input can not be formatted
	
	Author:      Alex Dedov
	Date:        9/21/2011
	CSW Version: 6.0.0
	
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

import java.sql.SQLException;
import java.sql.Types;

import com.compositesw.common.logging.Logger;
import com.compositesw.extension.CustomProcedure;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class PhoneNumberFormatter
    extends TextUtilTemplate
    implements CustomProcedure
{
    static {
        className = "PhoneNumberFormatter";
        logger = Logger.getLogger(PhoneNumberFormatter.class.getName());
    }

    private String outValue = null ;

    public PhoneNumberFormatter() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug(className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo("inPhoneNumber",      Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("inOutputFormat",     Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("outPhoneNumber",     Types.VARCHAR, DIRECTION_OUT)
        };
    }

    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException {
        if (logger.isDebug()) {
            logger.debug(className + ".invoke called");
            logger.debug("Invoked with inPhoneNumber  : " + (null==inputValues[0]?"[null]":inputValues[0].toString()));
            logger.debug("             inOutputFormat : " + (null==inputValues[1]?"[null]":inputValues[1].toString()));
        }
        String in = (String)inputValues[0] ;
        if ( in == null || in.length() == 0 ) {
        	outValue = null ;
        	return ;
        }
        String inOutFormat = (String)inputValues[1] ;
        String out = null ;
        try {
        	String numPhone = FormattingUtil.cleansePhoneNumber(in) ;
        	if (logger.isDebug()) {
        		logger.debug("PhoneNumberFormatter: cleansed phone - " + numPhone) ;
        	}
        	out = FormattingUtil.formatPhoneNumber(inOutFormat, numPhone) ;
        }
        catch(IllegalArgumentException iae) {
            throw new CustomProcedureException("PhoneNumberFormatter: Invalid phone number", iae) ;
        }
        outValue = out ;
    }

    public Object[] getOutputValues() {
        if (logger.isDebug()) {
            logger.debug(className + ".getOutputValues called");
        }

        return new Object[] { outValue };
    }

    public String getDescription() {
        if (logger.isDebug()) {
            logger.debug(className + ".getDescription() called") ;
        }
        return "Converts phone number to the specified format" ;
    }
}
