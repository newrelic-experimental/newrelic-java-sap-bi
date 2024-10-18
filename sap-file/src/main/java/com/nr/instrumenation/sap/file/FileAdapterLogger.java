package com.nr.instrumenation.sap.file;

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
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.sdk.xi.lang.Binary;
import com.sap.aii.af.sdk.xi.mo.Attachment;
import com.sap.aii.af.sdk.xi.mo.Message;
import com.sap.engine.interfaces.messaging.api.Payload;
import com.sap.engine.interfaces.messaging.api.XMLPayload;

public class FileAdapterLogger {

	public static boolean initialized = false;
	private static Logger LOGGER;
	private static FileAdapterLoggingConfig config;
	private static NRLabsHandler handler;

	protected static final String LOGFILENAME = "sap-fileadapter.log";

	public static void initialize() {
		config = new FileAdapterLoggingConfig();
		
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
			LOGGER = Logger.getLogger("AdaptersLog");
		}
		
		if(LOGGER != null && handler != null) {
			LOGGER.addHandler(handler);
		}
		
		initialized = true;
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
			Attachment rootDoc = message.getRootDocument();
			JsonObject rootObj = new JsonObject();
			addAttribute("RootDocument-Type",rootDoc.getType(), rootObj);
			Binary rootData = rootDoc.getData();
			
			addAttribute("RootDocument-Data",rootData.toString(), rootObj);
			addAttribute("RootDocument-Name", rootDoc.getName(), rootObj);
			jsonObj.put("RootDocument", rootObj);
			
			
			String json = jsonObj.toJson();
			synchronized (config) {
				LOGGER.info(Jsoner.prettyPrint(json));
			}
			NewRelic.getAgent().getLogger().log(Level.FINE, "Wrote the following to sap-module.log: {0} ", json);
		}
		
		
	}
	
	public static void logMessage(com.sap.engine.interfaces.messaging.api.Message message, String source) {
		if(LOGGER == null) {
			initialize();
		}
		
		if(message == null) return;
		
		if(LOGGER != null) {
			JsonObject jsonObj = new JsonObject();
			jsonObj.put("Source", source);
			jsonObj.put("ObjectType", "Message");
			XMLPayload document = message.getDocument();
			if(document != null) {
				JsonObject docObj = new JsonObject();
				String text = document.getText();
				if(text != null) {
					docObj.put("Document-Text", text);
					String docName = document.getName();
					if(docName != null) {
						docObj.put("Document-Name", docName);
					}
					jsonObj.put("Document", docObj);
				}
				
			}
			Payload mainPayload = message.getMainPayload();
			if(mainPayload != null) {
				JsonObject mainObj = new JsonObject();
				mainObj.put("MainPayload-Name", mainPayload.getName());
				mainObj.put("MainPayload-ContentType", mainPayload.getContentType());
				mainObj.put("MainPayload-Desciption", mainPayload.getDescription());

				jsonObj.put("MainPayload", mainObj);
				
			}
			
			jsonObj.put("AttachmentCount", message.countAttachments());
			
			String json = jsonObj.toJson();
			synchronized (config) {
				LOGGER.info(Jsoner.prettyPrint(json));
			}
			NewRelic.getAgent().getLogger().log(Level.FINE, "Wrote the following to sap-module.log: {0} ", json);
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
