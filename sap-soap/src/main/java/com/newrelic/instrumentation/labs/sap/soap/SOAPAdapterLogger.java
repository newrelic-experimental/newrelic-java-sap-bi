package com.newrelic.instrumentation.labs.sap.soap;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.adapter.xi.ms.XIMessage;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.sdk.xi.lang.Binary;
import com.sap.aii.af.sdk.xi.mo.Attachment;
import com.sap.aii.af.sdk.xi.mo.Message;
import com.sap.aii.af.sdk.xi.mo.MessageContext;
import com.sap.engine.interfaces.messaging.api.XMLPayload;

public class SOAPAdapterLogger {

	public static boolean initialized = false;
	private static Logger LOGGER;
	private static SOAPAdapterLoggingConfig config;
	private static NRLabsHandler handler;

	protected static final String LOGFILENAME = "sap-idoc2.log";

	public static void initialize() {
		config = new SOAPAdapterLoggingConfig();

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
	
	public static void logModuleContext(MessageContext inmc, String source) {
		if(LOGGER == null) {
			initialize();
		}
		if(inmc == null) return;
		
		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			addAttribute("Source", source, jsonObj);
			addAttribute("Source", source, jsonObj);
			Message message = inmc.getMessage();
			
			Attachment document = message.getRootDocument();
			Binary data = document.getData();
			addAttribute("Root-Data", data.toString(),jsonObj);
			String json = jsonObj.toJson();
			synchronized (config) {
				LOGGER.info(Jsoner.prettyPrint(json));
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static void logModuleData(ModuleData data, String source) {
		if(LOGGER == null) {
			initialize();
		}
		
		if(data == null) return;
		
		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			addAttribute("Source", source,jsonObj);
			addAttribute("ObjectType", "ModuleData",jsonObj);
			
			Object principalData = data.getPrincipalData();
			if(principalData != null) {
				Map<String, Object> processed = SOAPUtils.processObject(principalData);
				if(processed != null && !processed.isEmpty()) {
					jsonObj.put("PrincipalData", getStringMap(processed));
				}
			}
			
			Enumeration supplementalNames = data.getSupplementalDataNames();
			JsonArray supplementalValues = new JsonArray();
			while(supplementalNames.hasMoreElements()) {
				String supplementalName = (String)supplementalNames.nextElement();
				if(supplementalName != null) {
					Object value = data.getSupplementalData(supplementalName);
					if(value != null) {
						JsonObject obj = new JsonObject();
						obj.put(supplementalName, value.toString());
						supplementalValues.add(obj);
					}
				}
			}
			jsonObj.put("SupplementalValues", supplementalValues);
			String json = jsonObj.toJson();
			synchronized (config) {
				LOGGER.info(Jsoner.prettyPrint(json));
			}
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public static void logModuleContext(com.sap.aii.af.lib.mp.module.ModuleContext moduleContext, String source) {
		if(LOGGER == null) {
			initialize();
		}
		
		if(moduleContext == null) return;
		
		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			addAttribute("Source", source,jsonObj);
			addAttribute("ObjectType", "com.sap.aii.af.lib.mp.module.ModuleContext",jsonObj);
			
			Enumeration keys = moduleContext.getContextDataKeys();
			JsonArray jsonArray = new JsonArray();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement().toString();
				String value = moduleContext.getContextData(key);
				if(value != null) {
					JsonObject keyJson = new JsonObject();
					keyJson.put(key, value);
					jsonArray.add(keyJson);
				}
			}
			jsonObj.put("ContextData", jsonArray);		
			String json = jsonObj.toJson();
			synchronized (config) {
				LOGGER.info(Jsoner.prettyPrint(json));
			}
		}
		
	}
	
	public static Map<String,Object> processObject(Object principalObject) {
		HashMap<String,Object> attributes = new HashMap<String, Object>();

		if(principalObject instanceof XIMessage) {
			XIMessage xiMessage = (XIMessage)principalObject;
			XMLPayload document = xiMessage.getDocument();
			if(document != null) {
				
			}
		}
		return attributes.isEmpty() ? null : attributes;
	}


	private static void addAttribute(String key, Object value, JsonObject json) {
		if(key != null && !key.isEmpty() && value != null && json != null) {
			json.put(key, value.toString());
		}
	}

	private static Map<String, String> getStringMap(Map<String,Object> attributes) {
		Map<String,String> map = new HashMap<String, String>();
		Set<String> keys = attributes.keySet();
		for(String key : keys) {
			Object value = attributes.get(key);
			map.put(key, value.toString());
		}
		return map;
	}

}
