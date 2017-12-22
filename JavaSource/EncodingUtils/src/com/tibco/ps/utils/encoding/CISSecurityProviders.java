package com.tibco.ps.utils.encoding;

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
CISSecurityProviders

    Description:
        Lists all JCE providers, services and algorithms configured in the CIS JVM. A wrapper simple
        view, CIS_JCE_PROVIDERS_VIEW, on top of the procedure allows for the lookup of algorithms. This
        procedure can be used, for example, to track down the root cause of failures in a client certificate 
        and/or mutual authentication schemes between CIS and secure data providers (REST and SOAP web-services, 
        some DBMS with advanced security mechanisms) or clients (SOAP and REST service consumes, app 
        servers, ESBs, etc.) when these failures are caused by unsupported security algorithms.

    Input:
        N/A


    Output:
        results - The list of providers and their respective algorithms and service descriptions
            CURSOR (
                Provider              LONGVARCHAR
                Algorithm             LONGVARCHAR
                "Service Description" LONGVARCHAR
            )


    Exceptions:  none


    Modified Date:  Modified By:        CSW Version:    Reason:
    10/27/2014      Alex Dedov          6.2.6           Created new

 */

import java.security.Provider;
import java.security.Security;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.compositesw.extension.CustomCursor;
import com.compositesw.extension.CustomProcedureException;
import com.compositesw.extension.ParameterInfo;

public class CISSecurityProviders extends EncodingUtilTemplate {

	private String complMessage = null;
	private CustomCursor outputCursor = null ;
	
	public static class ResultRecord implements Comparable<CISSecurityProviders.ResultRecord>{
		public String provider = null ;
		public String algorithm = null ;
		public String service = null ;

		public ResultRecord() {} 
		
		public ResultRecord( String p, String a, String s ) {
			provider = p ;
			algorithm = a ;
			service = s ;
		}
		
		public int compareTo(CISSecurityProviders.ResultRecord o) {
			if ( o == null )
				return -1 ;
			return toString().compareTo(o.toString()) ;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ResultRecord [provider=").append(provider)
					.append(", algorithm=").append(algorithm)
					.append(", service=").append(service).append("]");
			return builder.toString();
		}
	}

	@Override
	public String getName() {
		procName = getClass().getSimpleName() ;
		return procName ;
	}

	@Override
	public String getDescription() {
		return "Returns a cursor of all security providers and respective encryption algorithms supported by Composite JVM";
	}
	
	private static final ParameterInfo[] OUTPUT_CURSOR = new ParameterInfo[] {
		new ParameterInfo("Provider", Types.VARCHAR, DIRECTION_OUT),
		new ParameterInfo("Algorithm", Types.VARCHAR, DIRECTION_OUT),
		new ParameterInfo("Service Description", Types.VARCHAR, DIRECTION_OUT) 
	};
	
	@Override
    public ParameterInfo[] getParameterInfo() {
    	return new ParameterInfo[] {
			new ParameterInfo("results", TYPED_CURSOR, DIRECTION_OUT, OUTPUT_CURSOR)
    	} ;
    }
	@Override
	public Object[] getOutputValues() throws CustomProcedureException, SQLException {
		return new Object[] { outputCursor };
	}
	
	public String getComplMessage() {
		return complMessage ;
	}
	
	public class ResultsCursor  implements CustomCursor {
	 	private int counter = 0 ;
	 	private List<ResultRecord> results = null ;
	 	
	 	public ResultsCursor(List<ResultRecord> list) {
	 		results = list ; 
	 		Collections.sort(results) ;
	 	}
	 	
	 	public ParameterInfo[] getColumnInfo() { 
	 		return OUTPUT_CURSOR ; 
	 	}
	 	
	 	public Object[] next() throws CustomProcedureException, SQLException {	 			
	 		if ( results == null || counter >= results.size() ) {
	 			return null;
	 		} 
 			Object[] row = new Object[] {
				results.get(counter).provider,
				results.get(counter).algorithm,
				results.get(counter).service				
	 		} ;
 			counter ++ ;
 			return row ;
 		}
 		
	 	public void close() throws CustomProcedureException, SQLException {
 			if ( results != null )
 				results.clear() ;
 		}
	}
 	
	@Override
	public int execute(Object[] args) throws Exception {
		List<ResultRecord> rrl = new ArrayList<ResultRecord>() ;
		for (Provider provider : Security.getProviders()) {
//			System.out.println("Provider: " + provider.getName());
			for (Provider.Service service : provider.getServices()) {
//				System.out.println("  Algorithm: " + service.getAlgorithm());
//				System.out.println("  Service: " + service.toString());
				ResultRecord rr = new ResultRecord(provider.toString(),service.getAlgorithm(),service.toString()) ;
				rrl.add(rr) ;
			}
		}
		outputCursor = new ResultsCursor(rrl) ;
		return 0 ;
	}
	
}
