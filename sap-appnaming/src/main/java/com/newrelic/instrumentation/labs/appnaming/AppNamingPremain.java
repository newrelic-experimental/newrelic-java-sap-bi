package com.newrelic.instrumentation.labs.appnaming;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class AppNamingPremain {
	
	private static final String SAP_SYSTEM_NAME = "SAPSYSTEMNAME";
	private static final String SAP_MYNAME = "SAPMYNAME";
	private static final String SAPVERSION = "SAPJStartVersion";
	private static final String SAPPRODUCT = "SAP";

	public static void premain(String s, Instrumentation inst) {
		setup();
	}
	
	private static void setup() {
		String systemName = System.getProperty(SAP_SYSTEM_NAME);
		NewRelic.getAgent().getLogger().log(Level.INFO, "Value of SAPSSYSTEMNAME is {0}", systemName);
		String myName = System.getProperty(SAP_MYNAME);
		NewRelic.getAgent().getLogger().log(Level.INFO, "Value of SAPMYNAME is {0}", myName);
		
		if(systemName != null && !systemName.isEmpty()) {
			
		}
		
		if(myName != null && !myName.isEmpty()) {
			NewRelic.setInstanceName(myName);
			NewRelic.getAgent().getLogger().log(Level.INFO, "Set SAP instance name to {0}", myName);
		}
		
		String versionString = System.getProperty(SAPVERSION);
		NewRelic.getAgent().getLogger().log(Level.INFO, "Value of SAPJStartVersion is {0}", versionString);
		if(versionString != null && !versionString.isEmpty()) {
			int index = versionString.indexOf(',');
			if(index > 0) {
				String tmp = versionString.substring(0, index);
				index = tmp.indexOf('.');
				if(index >= 0) {
					NewRelic.setServerInfo(SAPPRODUCT, tmp);
					NewRelic.getAgent().getLogger().log(Level.INFO, "Set SAP server info to {0}, {1}", SAPPRODUCT, tmp);
				} else {
					String tmp2 = tmp.charAt(0) + "." + tmp.substring(1);
					NewRelic.setServerInfo(SAPPRODUCT, tmp2);
					NewRelic.getAgent().getLogger().log(Level.INFO, "Set SAP server info to {0}, {1}", SAPPRODUCT, tmp2);
				}
				
			}
		}
		
	}
}
