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
