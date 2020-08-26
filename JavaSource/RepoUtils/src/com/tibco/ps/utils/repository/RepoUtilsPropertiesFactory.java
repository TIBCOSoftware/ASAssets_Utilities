package com.tibco.ps.utils.repository;

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
    RepoUtilsPropertiesFactory:

    This factory class is used to either locate an existing RepoUtils properties file
    (in $CIS_HOME/conf/customjars) or create a new one if a properties file does not
    already exist.
    
    When the getProperties() method is called, the method will look to see if the
    properties file was updated since it was last loaded and will reload it if
    changes have been made.

    Author:      Calvin Goodrich
    Date:        6/10/2011
    CSW Version: 5.1.0

 */

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.compositesw.common.logging.Logger;
import com.compositesw.extension.*;

public class RepoUtilsPropertiesFactory {
    
    private static final String DEFAULT_PROPERTIES = 
        "## RepoUtils Properties\n" + 
        "#\n" + 
        "# Properties for the ApplyReservedListToPath() and ApplyReservedListToWord() CJP's\n" + 
        "# and ApplyReservedList class.\n" + 
        "#\n" + 
        "# Properties can be made to include the next line by using the backslash character (\\) at the end of\n" + 
        "# the first line. The property value will continue at the first non-whitespace character of the next\n" + 
        "# line. Example:\n" + 
        "#\n" + 
        "# my.prop = this is a \\\n" + 
        "#           single property\n" + 
        "#\n" + 
        "# Results in a single property \"my.prop\" with the value \"this is a single property\"\n" + 
        "#\n" + 
        "# Properties can also be referenced in other properties:\n" + 
        "#\n" + 
        "# prop01 = this is a\n" + 
        "# prop02 = ${prop01} single property\n" + 
        "#\n" + 
        "# The value of \"prop02\" resolves to  the value \"this is a single property\"\n" + 
        "#\n" + 
		"cis.reserved_words_re=absent|absolute|according|action|add|all|allocate|alter|and|any|are|as|asc|assertion|at|authorization|avg|\\\n" + 
		"                      base64|begin|between|binary|bit|bit_length|boolean|both|breadth|by|\\\n" + 
		"                      call|cascade|cascaded|case|cast|catalog|char|char_length|character|character_length|check|close|coalesce|\\\n" + 
		"                      collate|collation|collection|column|columns|commit|connect|connection|constant|constraint|constraints|content|\\\n" + 
		"                      continue|convert|corresponding|count|create|cross|current|current_date|current_time|current_timestamp|current_user|cursor|cycle|\\\n" + 
		"                      date|day|days|deallocate|dec|decimal|declare|default|deferrable|deferred|\\\n" + 
		"                      delete|dense_rank|depth|desc|describe|descriptor|diagnostics|disconnect|distinct|do|document|domain|double|dow|doy|drop|\\\n" + 
		"                      element|else|elseif|empty|end|end-exec|epoch|escape|except|exception|exclude|exec|execute|exists|explain|external|extract|\\\n" + 
		"                      false|fetch|first|float|fn|following|for|foreign|from|full|\\\n" + 
		"                      get|global|go|goto|grant|group|having|hex|host|hour|hours|\\\n" + 
		"                      id|identity|if|ignore|immediate|in|independent|index|indicator|initially|inner|inout|input|insensitive|insert|int|integer|intersect|interval|into|is|isolation|iterate|\\\n" + 
		"                      join|keep|key|language|last|latest|leading|leave|left|level|like|local|location|longvarchar|loop|lower|\\\n" + 
		"                      match|max|microsecond|microseconds|millisecond|milliseconds|min|minute|minutes|module|month|months|\\\n" + 
		"                      name|names|namespace|national|natural|nchar|next|nil|no|not|null|nullif|nulls|numeric|\\\n" + 
		"                      octet_length|of|offset|oj|on|only|open|option|or|order|others|out|outer|output|over|overlaps|\\\n" + 
		"                      pad|partial|partition|passing|path|pipe|port|position|preceding|precision|prepare|preserve|primary|prior|privileges|procedure|public|quarter|\\\n" + 
		"                      raise|range|read|real|recursive|ref|references|relative|repeat|replace|restrict|returning|revoke|right|rollback|row|rows|\\\n" + 
		"                      schema|scroll|search|second|seconds|section|select|sequence|session|session_user|set|size|smallint|some|space|\\\n" + 
		"                      sql|sql_bigint|sql_binary|sql_bit|sql_char|sql_date|sql_decimal|sql_double|sql_float|sql_guid|sql_integer|sql_interval_day|\\\n" + 
		"                      sql_interval_day_to_hour|sql_interval_day_to_minute|sql_interval_day_to_second|sql_interval_hour|\\\n" + 
		"                      sql_interval_hour_to_minute|sql_interval_hour_to_second|sql_interval_minute|sql_interval_minute_to_second|\\\n" + 
		"                      sql_interval_month|sql_interval_second|sql_interval_year|sql_interval_year_to_month|\\\n" + 
		"                      sql_longvarbinary|sql_longvarchar|sql_numeric|sql_real|sql_smallint|sql_time|sql_timestamp|sql_tinyint|\\\n" + 
		"                      sql_tsi_day|sql_tsi_frac_second|sql_tsi_hour|sql_tsi_minute|sql_tsi_month|sql_tsi_quarter|sql_tsi_second|sql_tsi_week|\\\n" + 
		"                      sql_tsi_year|sql_varbinary|sql_varchar|sql_wchar|sql_wlongvarchar|sql_wvarchar|sqlcode|sqlerror|sqlstate|strip|substring|sum|system_user|\\\n" + 
		"                      table|temporary|then|ties|time|timeseries|timestamp|timestampadd|timestampdiff|timezone_hour|\\\n" + 
		"                      timezone_minute|to|top|trailing|transaction|translate|translation|trim|true|ts|type|\\\n" + 
		"                      unbounded|union|unique|unknown|until|untyped|update|upper|uri|usage|use|user|using|\\\n" + 
		"                      value|values|varbinary|varchar|varying|vector|view|\\\n" + 
		"                      week|when|whenever|where|while|whitespace|with|within|work|write|\\\n" + 
		"                      xml|xmlagg|xmlattributes|xmlbinary|xmlcast|xmlcomment|xmlconcat|xmldocument|xmlelement|xmlexists|xmlforest|\\\n" + 
		"                      xmliterate|xmlnamespaces|xmlparse|xmlpi|xmlquery|xmlschema|xmlserialize|xmltable|xmltext|xmlvalidate|\\\n" + 
		"                      year|years|zone\n" + 
        "\n" + 
        "cis.path_quoting_rules=^[_0-9],\\\n" + 
        "                       [^A-Za-z0-9_],\\\n" + 
        "                       (?i)^(?:${cis.reserved_words_re})$\n";

    private static final int LOG_ERROR = 1;
    private static final int LOG_INFO = 2;
    private static final int LOG_DEBUG = 3;
    private static final String PROPS_FILE_NAME = "RepoUtils.properties";
    
    private static Properties props = null;
    private static String propsFileLocation = "";
    private static File propsFile = null;
    private static long propsLastUpdated = 0;
    private static Logger logger = null;
    private static Pattern propSubRE = null;

    static {
        propSubRE = Pattern.compile ("\\$\\{([^}]+)\\}"); // a regular expression that matches "${any_valid_properties_key}"
    }

    // Write out the RepoUtils properties.
    //
    public static void writeProperties() throws Exception {
        findPropertiesFile();
        
        // Force the write out an example properties file
        //
        writeSampleProperties();
    }
    
    // returns the RepoUtils properties.
    //
    // loads the properties if they haven't been loaded before (and writes
    // an example if there is no properties file.) if the properties have been
    // loaded, it looks to see if the properties file has been updated and
    // reloads the properties.
    //
    // synchronized for thread safety.
    //
    public static synchronized Properties getProperties() throws Exception {
        findPropertiesFile();
        
        // if the properties file is missing, write out an example properties file
        //
        // (this currently tries to write a file every time the getProperties() method
        // is invoked and the file is still missing. might not want to do this.)
        //
        if (! propsFile.exists()) {
            writeSampleProperties();
        }
            
        // properties file exists so we need to see if it's been updated
        //
        if (propsFile.canRead()) {
            log (LOG_DEBUG, "propsFile last modified = " + propsFile.lastModified() + "; internal props last modified = " + propsLastUpdated);
            
            if (propsFile.lastModified() > propsLastUpdated) {
                Properties tmpProps = new Properties();

                log (LOG_DEBUG, "Properties file has changed. Reading new properties from \"" + propsFileLocation + "\".");

                try {
                    FileInputStream fis = new FileInputStream (propsFile);
                    tmpProps.load (fis);
                    fis.close();
                } catch (Exception e) {
                    log (LOG_INFO, "Error reading properties from \"" + propsFileLocation + "\". Will continue using previously loaded properties.");
                }
                
                tmpProps = parseProps (tmpProps);
                propsLastUpdated = propsFile.lastModified();
                
                props = tmpProps;
            } else {
                log (LOG_DEBUG, "Properties file has not changed. Continuing to use previously loaded properties.");
            }
            
        // can't read the properties file
        //
        } else {
            log (LOG_INFO, "Unable to read properties from \"" + propsFileLocation + "\". Will continue using previously loaded properties.");
        }
        
        // if the properties weren't loaded, then the properties file didn't exist and the location couldn't
        // be written to. use the defaults as an inputstream to the properties loader.
        //
        if (props == null) {
            log (LOG_ERROR, "Unable to write properties file either. Loading properties from default properties.");

            props = new Properties();
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream (DEFAULT_PROPERTIES.getBytes());
                props.load (bais);
                bais.close();
            } catch (Exception e) {
                log (LOG_ERROR, "Error loading default properties: " + e.getMessage());
                throw e;
            }
            
        }
        
        return props;
    }
    
    // used in a CJP to set a logger to write to the cs_server.log file.
    // this will throw a CustomProcedureException if not used in a CJP.
    //
    public static void setCisLogger () throws CustomProcedureException {
        if (logger == null) {
            try {
                RepoUtilsPropertiesFactory.logger = Logger.getLogger (RepoUtilsPropertiesFactory.class.getName());
            } catch (Exception e) {
                throw new CustomProcedureException (e);
            }
        }
    }
    
    // used internally to write messages to a CIS instance's cs_server.log file.
    // (see setCisLogger() above.)
    //
    private static void log (
        int    debugLevel,
        String msg
    ) {
//      System.out.println (debugLevel + ": " + msg);

        switch (debugLevel) {
            case LOG_DEBUG:
                if (logger != null && logger.isDebug()) logger.debug(msg);
                break;

            case LOG_INFO:
                if (logger != null && logger.isInfo()) logger.info(msg);
                break;

            case LOG_ERROR:
                if (logger != null) logger.log(msg);
                break;
        }
    }
    
    // if the properties file hasn't already been located, have a look for it
    // depending on whether this class is running in support of a CJP or an
    // application running independently of CIS.
    //
    private static void findPropertiesFile() {
        
        // assuming the previously located props file is still good so skip this processing if
        // propsFileLocation has been set.
        //
        if (propsFileLocation.equals("")) {

            // if the "apps.install.dir" system property is set, then we're running inside a CIS instance.
            // in this case the properties file should be located in $CIS_HOME/conf/customjars.
            // otherwise the properties file might be located in the CIS owner's home dir. if not located,
            // a sample file will be created in $CIS_HOME/conf/customjars.
            //
            if (System.getProperty ("apps.install.dir") != null) {
                propsFileLocation = System.getProperty ("apps.install.dir") + File.separator + "conf" + File.separator + "customjars" + File.separator + PROPS_FILE_NAME;
                log (LOG_DEBUG, "Attempting to read properties from \"" + propsFileLocation + "\".");
                propsFile = new File (propsFileLocation);
                
                if (! propsFile.exists() || ! propsFile.canRead()) {
                    log (LOG_DEBUG, "Unable to read properties from \"" + propsFileLocation + "\".");
                    propsFileLocation = System.getProperty ("user.dir") + File.separator + PROPS_FILE_NAME;
                    log (LOG_DEBUG, "Attempting to read properties from \"" + propsFileLocation + "\".");
                    propsFile = new File (propsFileLocation);
                    
                    if (! propsFile.exists() || ! propsFile.canRead()) {
                        log (LOG_DEBUG, "Unable to read properties from \"" + propsFileLocation + "\".");
                        propsFileLocation = System.getProperty ("apps.install.dir") + File.separator + "conf" + File.separator + "customjars" + File.separator + PROPS_FILE_NAME;
                        propsFile = new File(propsFileLocation);
                    }
                }
                
            // not running in CIS. look in the classpath and the user's home folder. if not
            // located, a sample file will be created in the user's home folder.
            //
            } else {
                boolean foundInClassPath = false;
                
                // attempt to locate the properties file in the class path
                //
                log (LOG_DEBUG, "Scanning JVM system property \"java.class.path\" for properties file \"" + PROPS_FILE_NAME + "\" ...");
                String[] cps = System.getProperty ("java.class.path").split (System.getProperty ("path.separator"), 0);
                for (int c = 0; c < cps.length; c++) {
                    
                    // skip jar files. extracting a properties file from a jar file would be painful
                    // and doesn't fit the requirement that the properties file be easy to update.
                    //
                    if (cps[c].endsWith (".jar"))
                        continue;
                    
                    propsFileLocation = cps[c] + File.separator + PROPS_FILE_NAME;
                    propsFile = new File (propsFileLocation);
                    log (LOG_DEBUG, "Attempting to read properties from \"" + propsFileLocation + "\".");

                    if (propsFile.exists() && propsFile.canRead()) {
                        log (LOG_DEBUG, "Found properties at \"" + propsFileLocation + "\".");
                        foundInClassPath = true;
                        break;
                    }
                }
                
                // if the properties file was not found in the JVM's class path, attempt to locate in the user's home directory.
                //
                if (! foundInClassPath) {
                    propsFileLocation = System.getProperty ("user.dir") + File.separator + PROPS_FILE_NAME;
                    propsFile = new File (propsFileLocation);
                    log (LOG_DEBUG, "No properties found in JVM class path. Attempting to read from user's home dir, \"" + propsFileLocation + "\".");

                    if (! propsFile.exists() || ! propsFile.canRead()) {
                        log (LOG_DEBUG, "Unable to read properties from \"" + propsFileLocation + "\".");
                   }
                }
            }
        }
    }
    
    // write out a sample properties file
    //
    private static void writeSampleProperties() {
        log (LOG_INFO, "Writing default properties to \"" + propsFileLocation + "\".");
        
        try {
            BufferedWriter bw = new BufferedWriter (new FileWriter (propsFile));
            bw.write(DEFAULT_PROPERTIES);
            bw.close();
        } catch (Exception e) {
            log (LOG_INFO, "Unable to write default properties to \"" + propsFileLocation + "\". Will continue using default properties.");
        }
    }
    
    // parse out the properties
    //
    private static Properties parseProps (Properties inProps) throws Exception {
        Properties outProps = new Properties();
        int numParsed;

        // loop until nothing parses.
        //
        do {
            numParsed = 0;
            
            // iterate over all the keys from the properties file
            //
            keyLoop:
            for (Enumeration e = inProps.keys(); e.hasMoreElements(); ) {
                String k = (String) e.nextElement();
                String v = inProps.getProperty (k);

                // if the property has not already been parsed
                //
                if (outProps.getProperty (k) == null) {
                    Matcher m = propSubRE.matcher (v);
                    StringBuffer sb = new StringBuffer();

                    log (LOG_DEBUG, "Found property \"" + k + "\" with the unparsed value of \"" + v + "\".");

                    // locate any substitutions and try to find the referenced property
                    //
                    while (m.find()) {
                        String sk = m.group (1);
                        String sv = outProps.getProperty (sk);
                        
                        log (LOG_DEBUG, "Property \"" + k + "\"; found substitution variable \"" + sk + "\".");

                        // if the substitution value has not already been parsed then skip
                        // to the next key (with the idea that the substitution value will
                        // be located later and the a subsequent pass throug the keys will 
                        // find it.
                        //
                        if (sv == null) {
                            log (LOG_DEBUG, "Property \"" + k + "\"; substitution variable \"" + sk + "\" not yet parsed. Skipping parsing \"" + k + "\" for now.");

                            continue keyLoop;
                        }
                        
                        log (LOG_DEBUG, "Property \"" + k + "\"; substitution variable \"" + sk + "\" resolves to \"" + sv + "\".");
                        m.appendReplacement (sb, sv);
                    }
                    
                    m.appendTail (sb);
                    
                    // if all the substitution keys parse out (or there weren't any to
                    // begin with), add the key and value to the parsed properties.
                    //
                    log (LOG_DEBUG, "Property \"" + k + "\" parses as \"" + sb + "\".");
                    outProps.setProperty (k, sb.toString());
                    numParsed++;                    
                }
            }
            
        } while (numParsed > 0);
        
        // make sure all the properties got parsed, otherwise throw an exception.
        //
        if (inProps.size() != outProps.size()) {
            String unparsedKeys = "";
            int i = 0;
            
            // construct a meaningful error message containing the unparsed keys.
            //
            for (Enumeration e = inProps.keys(); e.hasMoreElements(); ) {
                String k = (String) e.nextElement();
                if (outProps.getProperty(k) == null) {
                    if (i > 0)
                        unparsedKeys += ", ";
                    
                    unparsedKeys += k;
                    i++;
                }
            }
            throw new Exception ("Unable to parse the following keys: " + unparsedKeys);
        }
        
        return outProps;
    }
}
