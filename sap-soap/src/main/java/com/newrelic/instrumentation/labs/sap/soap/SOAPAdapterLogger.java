package com.newrelic.instrumentation.labs.sap.soap;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.api.agent.NewRelic;

public class SOAPAdapterLogger {

	public static boolean initialized = false;
	private static Logger LOGGER;
	private static NRLabsHandler handler;

	protected static final String LOGFILENAME = "soap-module-contextdata-attributes.log";

	public static void initialize() {

		try {
			handler = new NRLabsHandler(LOGFILENAME);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create details communication log file at {0}", LOGFILENAME);
		}

		if(LOGGER == null) {
			LOGGER = Logger.getLogger("AdapterLog");
		}

		if(LOGGER != null && handler != null) {
			LOGGER.addHandler(handler);
		}

		initialized = true;
	}
	
	public static void logNewAttribute(String attribute) {
		if(!initialized) {
			initialize();
		}
		LOGGER.info(attribute);
	}

	

}
