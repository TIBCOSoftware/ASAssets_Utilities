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

*/

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
