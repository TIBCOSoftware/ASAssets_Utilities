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
	CisPathQuoterException:
	
	A custom exception used by the CisPathQuoter class.
	
	Author:      Calvin Goodrich
	Date:        6/10/2011
	CSW Version: 5.1.0
	
*/

public class CisPathQuoterException extends Exception {

    public CisPathQuoterException (Throwable throwable) {
        super (throwable);
    }

    public CisPathQuoterException (String string, Throwable throwable) {
        super (string, throwable);
    }

    public CisPathQuoterException (String string) {
        super (string);
    }

    public CisPathQuoterException () {
        super ();
    }
}
