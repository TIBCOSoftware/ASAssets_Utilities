/*
	Description:
	  This utility class extends LinkedHashMap to provide a sorted hash map of regex patterns to
	  to RegexPatternFactory. Overrides the removeEldestEntry() method to limit the number of
	  cached patterns to the initial capacity specifed when the class is instantiated.
	
	Inputs:
	  N/A
	
	Outputs:
	  N/A
	
	Exceptions:
	  None
	
	Author:      Calvin Goodrich
	Date:        1/12/2011
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
package com.cisco.dvbu.ps.utils.text;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegexPatternCache extends LinkedHashMap {

    private int maxEntries;

    RegexPatternCache (int i, float f, boolean b) {
        super (i, f, b);
        maxEntries = i; // set max capacity to initial capacity
    }

    // override the removeEldestEntry() method to create a size constrained container.
    // the put() method will call this to determine if the oldest entry in the map needs
    // to be removed.
    //
    @Override
    protected boolean removeEldestEntry (Map.Entry eldest) {
        return this.size() > this.maxEntries;
    }
}
