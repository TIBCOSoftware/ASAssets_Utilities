package com.cisco.dvbu.ps.utils.repository;

/*
	CisPathQuoterException:
	
	A custom exception used by the CisPathQuoter class.
	
	Author:      Calvin Goodrich
	Date:        6/10/2011
	CSW Version: 5.1.0
	
	2011, 2014 Cisco and/or its affiliates. All rights reserved.
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
