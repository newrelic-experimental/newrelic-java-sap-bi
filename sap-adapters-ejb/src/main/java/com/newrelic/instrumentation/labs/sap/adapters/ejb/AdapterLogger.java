package com.newrelic.instrumentation.labs.sap.adapters.ejb;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.newrelic.agent.deps.org.json.simple.JSONArray;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.Payload;
import com.sap.engine.interfaces.messaging.api.XMLPayload;

public class AdapterLogger {

	public static boolean initialized = false;
	private static Logger LOGGER;
	private static AdapterLoggingConfig config;
	private static NRLabsHandler handler;

	protected static final String LOGFILENAME = "sap-adapter.log";

	public static void initialize() {
		config = new AdapterLoggingConfig();
		
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
			LOGGER = Logger.getLogger("AdaptersEJBLog");
		}
		
		if(LOGGER != null && handler != null) {
			LOGGER.addHandler(handler);
		}
		
		initialized = true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void logModuleContext(ModuleContext context, String source) {
		if(LOGGER == null) {
			initialize();
		}
		if(context == null) return;
		
		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			addAttribute("Source", source, jsonObj);
			addAttribute("ChannelID", context.getChannelID(), jsonObj);
			addAttribute("ObjectType", "ModuleContext", jsonObj);
			
			Enumeration keys = context.getContextDataKeys();
			JSONArray jsonArray = new JSONArray();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement().toString();
				String value = context.getContextData(key);
				if(value != null) {
					JsonObject keyJson = new JsonObject();
					addAttribute(key, value, keyJson);
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
	
	public static void logMessage(Message message, String source) {
		if(LOGGER == null) {
			initialize();
		}
		
		if(message == null) return;
		
		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			addAttribute("Source", source, jsonObj);
			addAttribute("ObjectType", "Message", jsonObj);
			XMLPayload document = message.getDocument();
			if(document != null) {
				JsonObject docObj = new JsonObject();
				String text = document.getText();
				if(text != null) {
					addAttribute("Document-Text", text, docObj);
					String docName = document.getName();
					if(docName != null) {
						addAttribute("Document-Name", docName, docObj);
					}
					jsonObj.put("Document", docObj);
				}
				
			}
			Payload mainPayload = message.getMainPayload();
			if(mainPayload != null) {
				JsonObject mainObj = new JsonObject();
				addAttribute("MainPayload-Name", mainPayload.getName(), mainObj);
				addAttribute("MainPayload-ContentType", mainPayload.getContentType(), mainObj);
				addAttribute("MainPayload-Desciption", mainPayload.getDescription(), mainObj);

				jsonObj.put("MainPayload", mainObj);
				
			}
			
			jsonObj.put("AttachmentCount", message.countAttachments());
			
			String json = jsonObj.toJson();
			
			synchronized (config) {
				LOGGER.info(Jsoner.prettyPrint(json));
			}
		}
		
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void logModuleData(ModuleData data, String channelID, String source) {
		if(LOGGER == null) {
			initialize();
		}
		
		if(data == null) return;
		
		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			addAttribute("Source", source, jsonObj);
			addAttribute("ObjectType", "ModuleData", jsonObj);
			addAttribute("ChannelId", channelID, jsonObj);
			
			Object principalData = data.getPrincipalData();
			if(principalData != null) {
				Map<String, Object> processed = AdaptersUtils.processObject(principalData);
				if(processed != null && !processed.isEmpty()) {
					jsonObj.put("PrincipalData", getStringMap(processed));
				}
			}
			
			Enumeration supplementalNames = data.getSupplementalDataNames();
			JSONArray supplementalValues = new JSONArray();
			while(supplementalNames.hasMoreElements()) {
				String supplementalName = (String)supplementalNames.nextElement();
				if(supplementalName != null) {
					Object value = data.getSupplementalData(supplementalName);
					if(value != null) {
						JsonObject obj = new JsonObject();
						addAttribute(supplementalName, value,obj);
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
