package com.cisco.dvbu.ps.utils.repository;

/*
	CisPathQuoter:
	
	This class implements the CIS path quoting rules. The reserved word list it uses comes
	from the $CIS_HOME/conf/customjars/RepoUtils.properties file, which can be updated for
	future versions of CIS without having to update the RepoUtils CJP collection.
	
	Author:      Calvin Goodrich
	Date:        6/10/2011
	CSW Version: 5.1.0
	
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

import java.util.Properties;
import java.util.regex.Pattern;

public class CisPathQuoter {

    // determines if the input word is a CIS reserved word. Quoted strings are not considered reserved.
    //
    public static boolean isReservedWord (String inStr) throws CisPathQuoterException {
        Properties p;
        String rwRegex;
        boolean specialFound = false;
        
        if (inStr == null)
            return false;
        
        // should not be trimming the string to be tested.
        //
        //inStr = inStr.trim();
        
        if (inStr.startsWith("\"") && inStr.endsWith("\"")) {
            return false;
        }
        
        try {
            p = RepoUtilsPropertiesFactory.getProperties();
        } catch (Exception e) {
            throw new CisPathQuoterException (e);
        }
        
        if (p.getProperty ("cis.reserved_words_re") == null)
            throw new CisPathQuoterException ("Unable to locate property \"cis.path_quoting_rules\".");
        
        rwRegex = "(?i)^(?:" + p.getProperty ("cis.reserved_words_re") + ")$";

        // check against regular expressions in the reserved word list (includes leading numeric/underscore and special character searches too.)
        //
        if (Pattern.compile (rwRegex).matcher (inStr).find()) {
            specialFound = true;
        }

        return specialFound;      
    }

    // uses CIS quoting rules to determine if the input word would need to be quoted to be used in a path
    //
    public static boolean isQuotableWord (String inStr) throws CisPathQuoterException {
        Properties p;
        String[] regexList;
        boolean specialFound = false;
        
        if (inStr == null)
            return false;
        
        //inStr = inStr.trim();
        if (inStr.matches ("(?:^\\s+|\\s+$)"))
            return true;

        if (inStr.startsWith("\"") && inStr.endsWith("\"")) {
            return false;
        }
        
        try {
            p = RepoUtilsPropertiesFactory.getProperties();
        } catch (Exception e) {
            throw new CisPathQuoterException (e);
        }
        
        if (p.getProperty ("cis.path_quoting_rules") == null)
            throw new CisPathQuoterException ("Unable to locate property \"cis.path_quoting_rules\".");
        
        regexList = p.getProperty ("cis.path_quoting_rules").split(",", 0);

        // check against regular expressions in the reserved word list (includes leading numeric/underscore and special character searches too.)
        //
        regexLoop:
        for (int r = 0; r < regexList.length; r++) {
            if (Pattern.compile (regexList[r]).matcher (inStr).find()) {
                specialFound = true;
                break regexLoop;
            }
        }

        return specialFound;      
    }

    public static String quoteWord (String inStr) throws CisPathQuoterException {
        String result;
        
        if (inStr == null)
            return null;
        
        if (isQuotableWord (inStr))
            result = "\"" + inStr + "\"";
        else
            result = inStr;
        
        return result;
    }
    
    public static String quotePath (String inStr) throws CisPathQuoterException {
        String result = "";
        String[] pathList;
        String tmpName;
        
        if (inStr == null)
            return null;
        
        pathList = inStr.replace ("\"", "").split ("/", 0);

        pathLoop:
        for (int p = 0; p < pathList.length; p++) {
            tmpName = pathList[p];
            
            if (p > 0 && tmpName.equals (""))
                continue pathLoop;
            
            tmpName = CisPathQuoter.quoteWord (tmpName);
            
            if (p > 0)
                result += "/";
            
            result += tmpName;
        }

        return result;
    }
}
