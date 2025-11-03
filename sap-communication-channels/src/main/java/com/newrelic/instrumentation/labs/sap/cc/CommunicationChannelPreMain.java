package com.newrelic.instrumentation.labs.sap.cc;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class CommunicationChannelPreMain {
	
	private static final String SAP_SYSTEM_NAME = "SAPSYSTEMNAME";
	private static final String SAP_MYNAME = "SAPMYNAME";
	private static final String SAPVERSION = "SAPJStartVersion";
	private static final String SAPPRODUCT = "SAP";
	private static ClassLoader LOGGING_CLASSLOADER = null;


	public static void premain(String s, Instrumentation inst) {
		init();
	}

	public static void init() {
		if(!isSAP()) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Skipping Communication Channel logging, application does not appear to be SAP");
			return;
		}
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to initialize Communcation Channel Logging");
		
		if(CommunicationChannelLogger.initialized) return;
		
		CommunicationChannelLogger.init();
	}

	private static boolean isSAP() {
		String p = System.getProperty(SAPPRODUCT);
		if(p != null) return true;
		
		p = System.getProperty(SAP_MYNAME);
		if(p != null) return true;

		p = System.getProperty(SAPVERSION);
		if(p != null) return true;

		p = System.getProperty(SAP_SYSTEM_NAME);
		if(p != null) return true;

		return false;
	}

	public static ClassLoader getLOGGING_CLASSLOADER() {
		return LOGGING_CLASSLOADER;
	}
}
