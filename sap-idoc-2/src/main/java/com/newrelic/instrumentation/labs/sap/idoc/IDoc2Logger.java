package com.newrelic.instrumentation.labs.sap.idoc;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.newrelic.api.agent.NewRelic;
import com.sap.conn.idoc.IDocDocument;

public class IDoc2Logger {

	public static boolean initialized = false;
	private static Logger LOGGER;
	private static IDoc2LoggingConfig config;
	private static NRLabsHandler handler;

	protected static final String LOGFILENAME = "sap-idoc2.log";

	public static void initialize() {
		config = new IDoc2LoggingConfig();

		int rolloverMinutes = config.getRolloverMinutes();
		if(rolloverMinutes == 0) {
			rolloverMinutes = 60;
		}

		String rolloverSize = config.getRolloverSize();
		StringBuffer sb = new StringBuffer();
		int length = rolloverSize.length();
		for(int i = 0; i < length; i++) {
			char c = rolloverSize.charAt(i);
			if(Character.isDigit(c)) {
				sb.append(c);
			}
		}

		long size = Long.parseLong(sb.toString());
		char end = rolloverSize.charAt(length-1);
		switch (end) {
		case 'K':
			size *= 1024L;
			break;
		case 'M':
			size *= 1024L*1024L;
			break;
		case 'G':
			size *= 1024L*1024L*1024L;
			break;
		}

		// disallow less than 10K
		if(size < 10 * 1024L) {
			size =  10 * 1024L;
		}

		String logFileName = config.getAdapterLog();
		int maxFiles = config.getMaxLogFiles();

		try {
			handler = new NRLabsHandler(logFileName, rolloverMinutes, size, maxFiles);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create details communication log file at {0}", logFileName);
		}

		if(LOGGER == null) {
			LOGGER = Logger.getLogger("AdapterLog");
		}

		if(LOGGER != null && handler != null) {
			LOGGER.addHandler(handler);
		}

		initialized = true;
	}
	
	public static void logIDoc(IDocDocument document, String source) {
		if(LOGGER == null) {
			initialize();
		}
		if(document == null) return;

		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			addAttribute("Source", source, jsonObj);
			addAttribute("ObjectType", "IDocument1", jsonObj);

			String docString = document.toString();
			if(docString != null && !docString.isEmpty()) {
				addAttribute("Document-Contents", docString, jsonObj);
			}
			String json = jsonObj.toJson();
			synchronized (config) {
				LOGGER.info(Jsoner.prettyPrint(json));
			}
		}
		
	}

	private static void addAttribute(String key, Object value, JsonObject json) {
		if(key != null && !key.isEmpty() && value != null && json != null) {
			json.put(key, value.toString());
		}
	}
	
}
