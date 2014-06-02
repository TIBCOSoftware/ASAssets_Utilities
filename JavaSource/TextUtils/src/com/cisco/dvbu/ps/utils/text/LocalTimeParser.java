package com.cisco.dvbu.ps.utils.text;
/*
	Description:
	  Function to parse a localized time string into a time. The country code may be NULL, but if any of the
	  other input values are NULL, then a NULL will be returned.
	
	Inputs:
	  inValue            - String value to parse 
	  inStyle            - Formatting style: "FULL", "LONG", "MEDIUM", or "SHORT"
	  ISO639LangCode     - ISO standard language code (see http://www.loc.gov/standards/iso639-2/php/English_list.php)
	  ISO3166CountryCode - ISO standard country code (see http://www.iso.org/iso/country_codes/iso_3166_code_lists/english_country_names_and_code_elements.htm)
	
	Output:
	  outValue           - Parsed time value
	
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

public class LocalTimeParser
    extends TextUtilTemplate
    implements CustomProcedure
{
    static {
        className = "LocalTimeParser";
        logger = Logger.getLogger(LocalTimeParser.class.getName());
    }

    private Time outValue = null;

    public LocalTimeParser() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug(className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo("inValue",            Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("inStyle",            Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO639LangCode",     Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO3166CountryCode", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("outValue",           Types.TIME,    DIRECTION_OUT),
        };
    }

    public void invoke(Object[] inputValues)
        throws CustomProcedureException, SQLException {
        if (logger.isDebug()) {
            logger.debug(className + ".invoke called");
            logger.debug("Invoked with inValue  : " + (null==inputValues[0]?"[null]":inputValues[0].toString()));
            logger.debug("             inStyle  : " + (null==inputValues[1]?"[null]":inputValues[1].toString()));
            logger.debug("             language : " + (null==inputValues[2]?"[null]":inputValues[2].toString()));
            logger.debug("             country  : " + (null==inputValues[3]?"[null]":inputValues[3].toString()));
        }

        String value              = (String)inputValues[0];
        String styleCode          = (String)inputValues[1];
        String iso639LangCode     = (String)inputValues[2];
        String iso3166CountryCode = (String)inputValues[3];

        if (null == value) {
            return;  // Nothing to do, outValue will be returned as null;
        }
        
        if (null == styleCode) {
            styleCode = "MEDIUM";
            //throw new CustomProcedureException("LocalTimeParser:No style specified");
        }

        int style;
        if ("FULL".equalsIgnoreCase(styleCode))
            style = DateFormat.FULL;
        else if ("LONG".equalsIgnoreCase(styleCode))
            style = DateFormat.LONG;
        else if ("MEDIUM".equalsIgnoreCase(styleCode))
            style = DateFormat.MEDIUM;
        else if ("SHORT".equalsIgnoreCase(styleCode))
            style = DateFormat.SHORT;
        else
            throw new CustomProcedureException("LocalTimeParser:Invalid style specified, must be FULL|LONG|MEDIUM|SHORT");

        Locale locale = Locale.getDefault();

        if (iso639LangCode != null) {
           if (iso3166CountryCode != null) {
               locale = new Locale(iso639LangCode, iso3166CountryCode);
           }
           else {
               locale = new Locale(iso639LangCode);
           }
        }

        DateFormat df = DateFormat.getTimeInstance(style,locale);

        try {
            outValue = new Time(df.parse(value).getTime());
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
