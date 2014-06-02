package com.cisco.dvbu.ps.utils.text;
/*
	Description:
	  Function to parse a localized timestamp string into a timestamp. The country code may be NULL, but if any of the
	  other input values are NULL, then a NULL will be returned.
	
	Inputs:
	  inValue            - String value to parse 
	  inDateStyle        - Date formatting style: "FULL", "LONG", "MEDIUM", or "SHORT"
	  inTimeStyle        - Time formatting style: "FULL", "LONG", "MEDIUM", or "SHORT"
	  ISO639LangCode     - ISO standard language code (see http://www.loc.gov/standards/iso639-2/php/English_list.php)
	  ISO3166CountryCode - ISO standard country code (see http://www.iso.org/iso/country_codes/iso_3166_code_lists/english_country_names_and_code_elements.htm)
	
	Output:
	  outValue           - Parsed timestamp value
	
	Exceptions:
	  None
	
	Author:      Mike DeAngelo
	Date:        8/11/2010
	CSW Version: 5.1.0
	
	(c) 2010, 2014 Cisco and/or its affiliates. All rights reserved.
 */

import com.compositesw.extension.*;
import com.compositesw.common.logging.Logger;

import java.sql.*;
import java.util.Locale;
import java.text.DateFormat;
import java.text.ParseException;

public class LocalTimestampParser
    extends TextUtilTemplate
    implements CustomProcedure
{
    static {
        className = "LocalTimestampParser";
        logger = Logger.getLogger(LocalTimestampParser.class.getName());
    }

    private Timestamp outValue = null;

    public LocalTimestampParser() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug(className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo("inValue",            Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("inDateStyle",        Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("inTimeStyle",        Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO639LangCode",     Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO3166CountryCode", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("outValue",           Types.TIMESTAMP,    DIRECTION_OUT),
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

        String value              = (String)inputValues[0];
        String dateStyleCode      = (String)inputValues[1];
        String timeStyleCode      = (String)inputValues[2];
        String iso639LangCode     = (String)inputValues[3];
        String iso3166CountryCode = (String)inputValues[4];

        if (null == value) {
            return;  // Nothing to do, outValue will be returned as null;
        }
        
        if (null == dateStyleCode) {
            dateStyleCode = "MEDIUM";
            //throw new CustomProcedureException("LocalTimestampParser:No dateStyle specified");
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
            throw new CustomProcedureException("LocalTimestampParser:Invalid dateStyle specified, must be FULL|LONG|MEDIUM|SHORT");

        if (null == timeStyleCode) {
            timeStyleCode = "MEDIUM";
            //throw new CustomProcedureException("LocalTimestampParser:No timeStyle specified");
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
            throw new CustomProcedureException("LocalTimestampParser:Invalid timeStyle specified, must be FULL|LONG|MEDIUM|SHORT");

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

        try {
            outValue = new Timestamp(df.parse(value).getTime());
        }
        catch (ParseException pe) {
            throw new CustomProcedureException("Error parsing time " + value);
        }

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
