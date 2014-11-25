package com.cisco.dvbu.ps.utils.repository;

/*
	CisPathQuoterException:
	
	A custom exception used by the CisPathQuoter class.
	
	Author:      Calvin Goodrich
	Date:        6/10/2011
	CSW Version: 5.1.0
	
	2011, 2014 Cisco and/or its affiliates. All rights reserved.

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
