package com.cisco.dvbu.ps.utils.text;
/*
	Description:
	  Function to convert a timestamp into a localized formatted string. The country code may be NULL, but if any of the
	  other input values are NULL, then a NULL will be returned.
	
	Inputs:
	  inValue            - Double value to convert 
	  inDateStyle        - Date formatting style: "FULL", "LONG", "MEDIUM", or "SHORT"
	  inTimeStyle        - Time formatting style: "FULL", "LONG", "MEDIUM", or "SHORT"
	  ISO639LangCode     - ISO standard language code (see http://www.loc.gov/standards/iso639-2/php/English_list.php)
	  ISO3166CountryCode - ISO standard country code (see http://www.iso.org/iso/country_codes/iso_3166_code_lists/english_country_names_and_code_elements.htm)
	
	Output:
	  outValue           - Input timestamp formatted in the requested language.
	
	Exceptions:
	  None
	
	Author:      Mike DeAngelo
	Date:        8/11/2010
	CSW Version: 5.1.0
	
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
import java.util.Locale;
import java.text.DateFormat;

public class LocalTimestampFormatter
    extends TextUtilTemplate
    implements CustomProcedure
{
    static {
        className = "LocalTimestampFormatter";
        logger = Logger.getLogger(LocalTimestampFormatter.class.getName());
    }

    private String outValue = null;

    public LocalTimestampFormatter() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug(className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo("inValue",            Types.TIMESTAMP, DIRECTION_IN),
            new ParameterInfo("inDateStyle",        Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("inTimeStyle",        Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO639LangCode",     Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO3166CountryCode", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("outValue",           Types.VARCHAR, DIRECTION_OUT),
        };
    }

    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException {
        if (logger.isDebug()) {
            logger.debug(className + ".invoke called");
            logger.debug("Invoked with inValue  : " + (null==inputValues[0]?"[null]":inputValues[0].toString()));
            logger.debug("             inDtStyle: " + (null==inputValues[1]?"[null]":inputValues[1].toString()));
            logger.debug("             inTmStyle: " + (null==inputValues[2]?"[null]":inputValues[2].toString()));
            logger.debug("             language : " + (null==inputValues[3]?"[null]":inputValues[3].toString()));
            logger.debug("             country  : " + (null==inputValues[4]?"[null]":inputValues[4].toString()));
        }

        Timestamp value           = (Timestamp)inputValues[0];
        String dateStyleCode      = (String)inputValues[1];
        String timeStyleCode      = (String)inputValues[2];
        String iso639LangCode     = (String)inputValues[3];
        String iso3166CountryCode = (String)inputValues[4];

        if (null == value) {
            return;  // Nothing to do, outValue will be returned as null;
        }
        
        if (null == dateStyleCode) {
            dateStyleCode = "MEDIUM";
            //throw new CustomProcedureException("LocalTimestampFormatter:No style specified");
        }

        int dateStyle;
        if ("FULL".equalsIgnoreCase(dateStyleCode))
            dateStyle = DateFormat.FULL;
        else if ("LONG".equalsIgnoreCase(dateStyleCode))
            dateStyle = DateFormat.LONG;
        else if ("MEDIUM".equalsIgnoreCase(dateStyleCode))
            dateStyle = DateFormat.MEDIUM;
        else if ("SHORT".equalsIgnoreCase(dateStyleCode))
            dateStyle = DateFormat.SHORT;
        else
            throw new CustomProcedureException("LocalTimestampFormatter:Invalid dateStyle specified, must be FULL|LONG|MEDIUM|SHORT");

        if (null == timeStyleCode) {
            timeStyleCode = "MEDIUM";
            //throw new CustomProcedureException("LocalTimestampFormatter:No style specified");
        }

        int timeStyle;
        if ("FULL".equalsIgnoreCase(timeStyleCode))
            timeStyle = DateFormat.FULL;
        else if ("LONG".equalsIgnoreCase(timeStyleCode))
            timeStyle = DateFormat.LONG;
        else if ("MEDIUM".equalsIgnoreCase(timeStyleCode))
            timeStyle = DateFormat.MEDIUM;
        else if ("SHORT".equalsIgnoreCase(timeStyleCode))
            timeStyle = DateFormat.SHORT;
        else
            throw new CustomProcedureException("LocalTimestampFormatter:Invalid timeStyle specified, must be FULL|LONG|MEDIUM|SHORT");

        Locale locale = Locale.getDefault();

        if (iso639LangCode != null) {
           if (iso3166CountryCode != null) {
               locale = new Locale(iso639LangCode, iso3166CountryCode);
           }
           else {
               locale = new Locale(iso639LangCode);
           }
        }

        DateFormat df = DateFormat.getDateTimeInstance(dateStyle,timeStyle,locale);

        outValue = df.format(value);

    }

    public Object[] getOutputValues() {
        if (logger.isDebug()) {
            logger.debug(className + ".getOutputValues called");
        }

        return new Object[] { outValue };
    }

    public String getDescription() {
        if (logger.isDebug()) {
            logger.debug(className + ".getDescription called");
        }
        return "Formats a date in the specified Locale.";
    }
}
