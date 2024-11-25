package com.newrelic.instrumentation.labs.sap.adapters.ejb;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.api.agent.NewRelic;

public class EJBAdapterLogger {

	public static boolean initialized = false;
	private static Logger LOGGER;
	private static Logger LOGGER2;
	private static AdapterLoggingConfig config;
	private static NRLabsHandler ctx_handler;
	private static NRLabsHandler supp_handler;

	protected static final String CONTEXT_LOGFILENAME = "context-data-attributes-ejb.log";
	protected static final String DATA_LOGFILENAME = "moduledata-supplemental-attributes-ejb.log";

	public static void initialize() {
		config = new AdapterLoggingConfig();
		String logFileName = config.getContextLog();
		
		try {
			ctx_handler = new NRLabsHandler(logFileName);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create Attribute log file at {0}", logFileName);
		}
		
		if(LOGGER == null) {
			LOGGER = Logger.getLogger("AdaptersEJBLog");
		}
		
		if(LOGGER != null && ctx_handler != null) {
			LOGGER.addHandler(ctx_handler);
		}
		
		logFileName = config.getSupplementalLog();
		
		try {
			supp_handler = new NRLabsHandler(logFileName);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create  supplemental attribute log file at {0}", logFileName);
		}
		
		if(LOGGER2 == null) {
			LOGGER2 = Logger.getLogger("AdaptersSupplementalLog");
		}
		
		if(LOGGER2 != null && supp_handler != null) {
			LOGGER2.addHandler(supp_handler);
		}
		initialized = true;
	}

	public static void logNewAttribute(String attribute) {
		if(!initialized) {
			initialize();
		}
		LOGGER.info(attribute);
	}

	public static void logNewSupplementalAttribute(String attribute) {
		if(!initialized) {
			initialize();
		}
		LOGGER2.info(attribute);
	}
}
