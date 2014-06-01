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
