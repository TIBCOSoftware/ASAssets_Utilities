/*
	Description:
	  This utility class manufactures regular expression patterns and maintains a cache of 
	  previously compiled regex patterns. A cap is placed on the maximum number of patterns 
	  that are stored so as to not impact CIS's memory management algorithms.
	
	  The Regex* CJP's use this class to manage regex patterns (so that when a CJP is applied
	  to one or more columns of a result set, the performance impact of repeatedly compiling
	  the same regex pattern is eliminated.)
	
	  A synchronized (thread-safe) LinkedHashMap is used to contain the patterns. When the max
	  number of patterns is reached, the least recently used pattern is replaced in the map
	  with the new pattern.
	
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

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexPatternFactory {
    
    // establish the max capacity of the pattern cache
    //
    protected static final int MAX_ENTRIES = 256;
    
    // create a thread-safe LinkedHashMap that removes patterns in a LRU manner when max capacity is reached.
    // NOTE: the RegexPatternCache class uses initial capacity as its max capacity.
    //
    protected static Map<String, Pattern> patternCache = Collections.synchronizedMap (new RegexPatternCache(MAX_ENTRIES, 0.75f, true));
    
    // shouldn't need to synchronize here as we're doing simple lookups, not iterations.
    //
    protected static Pattern getPattern (String patternString) throws PatternSyntaxException {
        Pattern p;

        p = patternCache.get (patternString);
        
        if (p == null) {
            p = Pattern.compile (patternString);
            patternCache.put (patternString, p);
        }

        return p;
    };
    
}
