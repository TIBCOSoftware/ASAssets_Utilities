package com.cisco.dvbu.ps.utils.text;

/*
	Description:
	  This utility class is used by the various number formatting CJP's.
	
	Inputs:
	  N/A
	
	Outputs:
	  N/A
	
	Exceptions:
	  None

	Author:      Alex Dedov
	Date:        9/21/2011
	CSW Version: 6.0.0
	
	(c) 2011, 2014 Cisco and/or its affiliates. All rights reserved.
*/

public class FormattingUtil {

	public static String cleansePhoneNumber(String s) throws IllegalArgumentException {
		return cleanseNumber(s,10) ;
	}
	public static String cleanseSSN(String s) throws IllegalArgumentException {
		return cleanseNumber(s,9) ;
	}
	public static String cleanseCCNumber(String s) throws IllegalArgumentException {
		boolean isValid = false ;
		String ccnumber = s ;
		try {
		// Visa, MasterCard, Discover
			ccnumber = cleanseNumber(s,16) ;
			if ( ccnumber.startsWith("4")
			||   ccnumber.substring(0,2).equals("51") 
			||   ccnumber.substring(0,2).equals("52")
			||   ccnumber.substring(0,2).equals("53") 
			||   ccnumber.substring(0,2).equals("54") 
			||   ccnumber.substring(0,2).equals("55") 
			||   ccnumber.substring(0,4).equals("6011") ) { 
				isValid = checkLuhn(ccnumber) ;
			}
		}
		catch ( IllegalArgumentException iae ) {
		// Amex
			ccnumber = cleanseNumber(s,15) ;
            if ( ccnumber.substring(0,2).equals("34") 
            ||   ccnumber.substring(0,2).equals("37") ) {
            	isValid = checkLuhn(ccnumber) ;
            }
		}
		if ( isValid == false ) {
			throw new IllegalArgumentException("Not a valid credit card number [" + s + "]") ;
		}
		return ccnumber ;
	}
	public static String formatPhoneNumber(String f,String s) throws IllegalArgumentException {
		if ( s == null || s.length() != 10 ) {
			throw new IllegalArgumentException("[" + s + "]") ;
		}
		String format = ( f == null || f.length() == 0 ? "%.3s-%.3s-%.4s" : f ) ;
		String formatted = String.format(format, s, s.substring(3), s.substring(6)) ;
		return formatted ;
	}
	public static String formatSSN(String f,String s) throws IllegalArgumentException {
		if ( s == null || s.length() != 9 ) {
			throw new IllegalArgumentException("[" + s + "]") ;
		}
		String format = ( f == null || f.length() == 0 ? "%.3s-%.2s-%.4s" : f ) ;
		String formatted = String.format(format, s, s.substring(3), s.substring(5)) ;
		return formatted ;
	}
	public static String formatCCNumber(String f,String s) throws IllegalArgumentException {
		if ( s == null || s.length() < 15 ) {
			throw new IllegalArgumentException("[" + s + "]") ;
		}
		String format = f ;
		String formatted ;
		if ( s.length() == 16 ) {
			if ( format == null || format.length() == 0 ) {
				format = "%.4s %.4s %.4s %.4s" ;
			}
			formatted = String.format(format, s, s.substring(4), s.substring(8), s.substring(12)) ;
		}
		else {
			if ( format == null || format.length() == 0 ) {
				format = "%.4s %.6s %.5s" ;
			}
			formatted = String.format(format, s, s.substring(4), s.substring(10)) ;
		}
		return formatted ;
	}
	private static String cleanseNumber(String s,int reqLength) {
		if ( s == null || s.length() < reqLength ) {
			throw new IllegalArgumentException("[" + s + "]") ;
		}
		String cleansed = s.replaceAll("\\D", "");     
		if ( cleansed.length() != reqLength ) {
			throw new IllegalArgumentException("[" + s + "]") ;
		}
		return cleansed ;
	}
    private static boolean checkLuhn(String number) {
        char[] c = number.toCharArray() ;
        int checksum = 0 ;
        int par = c.length % 2 ;
        for ( int i = 0 ; i < c.length ; i ++ ) {
        	int n = Character.getNumericValue(c[i]) * (2-(i+par)%2) ;
            if ( n > 9 )
                n -= 9 ;
            checksum += n ;
        }
        return ( checksum % 10 == 0 ) ;
    }

	public static String charToHex(char c) {
		return Integer.toHexString((int)c).toUpperCase() ;
	}

	
//	public static void main(String[] args) {
////		String c = FormattingUtil.cleanseCCNumber("3-7-2-5-0-7-9-1-2-9-8-2-0-1-8.........") ;
////		String c = FormattingUtil.cleanseCCNumber("......48..28..6401--89..33//4 0 7 6") ;
//		String c = FormattingUtil.cleanseSSN("  114    84 - - -    8628 / asd  ") ;
//		c = FormattingUtil.formatSSN(null, c) ;
//		System.out.println("Result: " + c) ;
//	}

}
