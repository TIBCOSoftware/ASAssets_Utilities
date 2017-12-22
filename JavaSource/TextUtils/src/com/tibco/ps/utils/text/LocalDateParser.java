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
	  Function to parse a localized date string into a date. The country code may be NULL, but if any of the
	  other input values are NULL, then a NULL will be returned.
	
	Inputs:
	  inValue            - String value to parse 
	  inStyle            - Formatting style: "FULL", "LONG", "MEDIUM", or "SHORT"
	  ISO639LangCode     - ISO standard language code (see http://www.loc.gov/standards/iso639-2/php/English_list.php)
	  ISO3166CountryCode - ISO standard country code (see http://www.iso.org/iso/country_codes/iso_3166_code_lists/english_country_names_and_code_elements.htm)
	
	Output:
	  outValue           - Parsed date value
	
	Exceptions:
	  None
	
	Author:      Mike DeAngelo
	Date:        8/11/2010
	CSW Version: 5.1.0
	
 */

import com.compositesw.extension.*;
import com.compositesw.common.logging.Logger;

import java.sql.*;
import java.util.Locale;
import java.text.DateFormat;
import java.text.ParseException;

public class LocalDateParser
    extends TextUtilTemplate
    implements CustomProcedure
{
    static {
        className = "LocalDateParser";
        logger = Logger.getLogger(LocalDateParser.class.getName());
    }

    private Date outValue = null;

    public LocalDateParser() {}

    public ParameterInfo[] getParameterInfo() {
        if (logger.isDebug()) {
            logger.debug(className + ".getParameterInfo called");
        }

        return new ParameterInfo[] {
            new ParameterInfo("inValue",            Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("inStyle",            Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO639LangCode",     Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("ISO3166CountryCode", Types.VARCHAR, DIRECTION_IN),
            new ParameterInfo("outValue",           Types.DATE,    DIRECTION_OUT),
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
            //throw new CustomProcedureException("LocalDateParser:No style specified");
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
            throw new CustomProcedureException("LocalDateParser:Invalid style specified, must be FULL|LONG|MEDIUM|SHORT");

        Locale locale = Locale.getDefault();

        if (iso639LangCode != null) {
           if (iso3166CountryCode != null) {
               locale = new Locale(iso639LangCode, iso3166CountryCode);
           }
           else {
               locale = new Locale(iso639LangCode);
           }
        }

        DateFormat df = DateFormat.getDateInstance(style,locale);

        try {
            outValue = new Date(df.parse(value).getTime());
        }
        catch (ParseException pe) {
            throw new CustomProcedureException("Error parsing date " + value);
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
