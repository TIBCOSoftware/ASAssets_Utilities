package com.cisco.dvbu.ps.utils.encoding;

/*
    © 2011, 2014 Cisco and/or its affiliates. All rights reserved.
*/

import java.security.Provider;
import java.security.Security;

public class ProviderUtil {

	public static void main(String[] args) {
		for (Provider provider : Security.getProviders()) {
			System.out.println("Provider: " + provider.getName());
			for (Provider.Service service : provider.getServices()) {
				System.out.println("  Algorithm: " + service.getAlgorithm());
				System.out.println("  Service: " + service.toString());
			}
		}

	}

}
