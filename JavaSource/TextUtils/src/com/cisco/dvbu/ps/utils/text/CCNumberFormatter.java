package com.cisco.dvbu.ps.utils.text;
/*
	Description:
	  Provides basic check and standard formatting of a Credit Card number.  
	  Validates the length of the supplied numeric field, tries matching it to one of the Visa, MC, AmEx 
	  or Discover card, and performs Luhn's validation on the number.
	
	Inputs:
	  inNumber      - a 15- or 16-digit number, arbitrary formatted
	
	Output:
	  outNumber     - Input number formatted as it appears on the card - 4 (or 3) groups separated by spaces. 
	
	Exceptions:
	  CustomProcedureException - if supplied input can not be formatted
	
	Author:      Alex Dedov
	Date:        9/21/2011
	CSW Version: 6.0.0
	
	(c) 2011, 2014 Cisco and/or its affiliates. All rights reserved.

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

public class CCNumberFormatter
    extends TextUtilTemplate
    implements CustomProcedure
{
    static {
        className = "CCNumberFormatter";
        logger = Logger.getLogger(CCNumberFormatter.class.getName());
    }

    private String outValue = null ;

    public CCNumberFormatter() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug(className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo("inCCNumber",      Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("outCCNumber",     Types.VARCHAR, DIRECTION_OUT)
        };
    }

    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException {
        if (logger.isDebug()) {
            logger.debug(className + ".invoke called");
            logger.debug("Invoked with inCCNumber  : " + (null==inputValues[0]?"[null]":inputValues[0].toString()));
        }

        String in = (String)inputValues[0] ;
        if ( in == null || in.length() == 0 ) {
        	outValue = null ;
        	return ;
        }
        String out = null ;
        try {
        	String num = FormattingUtil.cleanseCCNumber(in) ;
        	if (logger.isDebug()) {
        		logger.debug("CCNumberFormatter: cleansed CC number - " + num) ;
        	}
        	out = FormattingUtil.formatCCNumber(null, num) ;
        }
        catch(IllegalArgumentException iae) {
            throw new CustomProcedureException("CCNumberFormatter: Invalid number", iae) ;
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
        return "Validates and formats a Credit Card number" ;
    }
}
