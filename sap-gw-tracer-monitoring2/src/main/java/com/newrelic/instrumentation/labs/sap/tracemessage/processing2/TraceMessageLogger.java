package com.newrelic.instrumentation.labs.sap.tracemessage.processing2;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.HarvestListener;
import com.newrelic.agent.deps.org.json.simple.JSONObject;
import com.newrelic.agent.stats.StatsEngine;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;
import com.sap.it.op.mpl.trace.TraceMessage;
import com.sap.it.op.mpl.trace.TraceMessageType;

public class TraceMessageLogger implements HarvestListener {

	protected static final String TRACEMESSAGE_LOG_NAME = "tracemessage.log";
	public static boolean initialized = false;
	private static Logger TRACEMESSAGE_LOGGER;
	protected static final String TRACEMESSAGELOGFILENAME = "SAP.gatewaylog.tracelog_file_name";
	protected static final String TRACEMESSAGELOGGINGROLLOVERINTERVAL = "SAP.gatewaylog.log_file_interval";
	protected static final String TRACEMESSAGELOGGINGROLLOVERSIZE = "SAP.gatewaylog.log_size_limit";
	protected static final String TRACEMESSAGELOGGINGROLLOVERSIZE2 = "SAP.gatewaylog.log_file_size";
	protected static final String TRACEMESSAGELOGGINGMAXFILES = "SAP.gatewaylog.log_file_count";
	protected static final String TRACEMESSAGELOGENABLED = "SAP.tracemessagelog.enabled";
	private static final String LOGEVENTS = "SAP/TracelogLogger/Events";

	private static TraceLogConfig currentTracelogConfig = null;

	private static TraceMessageLogger INSTANCE = null;

	private static NRLabsHandler handler;

	private static NRLabsTimerTask timerTask = null;
	private static Integer LoggingCount = 0;
	private static boolean enabled = true;

	private TraceMessageLogger() {

	}

	protected static void checkConfig() {

		Config agentConfig = NewRelic.getAgent().getConfig();

		TraceLogConfig newConfig = getConfig(agentConfig);
		if(currentTracelogConfig == null || !newConfig.equals(currentTracelogConfig)) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "In TracelogLogger.checkConfig, config changed, reinitializing");
			// reinitialize log
			currentTracelogConfig = newConfig;
			init();
		}
	}

	protected TraceLogConfig getConfig() {
		if(currentTracelogConfig != null) return currentTracelogConfig;
		Config agentConfig = NewRelic.getAgent().getConfig();
		return getConfig(agentConfig);
	}

	public static TraceLogConfig getConfig(Config agentConfig) {
		TraceLogConfig tracemessageConfig = new TraceLogConfig();
		Integer rolloverMinutes = agentConfig.getValue(TRACEMESSAGELOGGINGROLLOVERINTERVAL);
		if(rolloverMinutes != null) {
			tracemessageConfig.setRolloverMinutes(rolloverMinutes);
		}

		Integer maxFile = agentConfig.getValue(TRACEMESSAGELOGGINGMAXFILES);
		if(maxFile != null) {
			tracemessageConfig.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(TRACEMESSAGELOGGINGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			tracemessageConfig.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(TRACEMESSAGELOGGINGROLLOVERSIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				tracemessageConfig.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(TRACEMESSAGELOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			tracemessageConfig.setTraceMessageLog(filename);
		}

		boolean enabled = agentConfig.getValue(TRACEMESSAGELOGENABLED, Boolean.TRUE);
		tracemessageConfig.setTracelog_enabled(enabled);


		return tracemessageConfig;
	}

	@SuppressWarnings("unchecked")
	public static void log(TraceMessage traceMsg, String modelStepId) {
		if(traceMsg != null) {
			JSONObject json = new JSONObject();

			String encoding = traceMsg.getEncoding();
			Map<String, String> exchangeProps =  traceMsg.getExchangeProperties();
			Map<String,String> headers = traceMsg.getHeaders();
			String iflowModelElementId = traceMsg.getIflowModelElementId();
			String MPL_ID =traceMsg.getMplId();
			String runId = traceMsg.getRunId();
			TraceMessageType traceMsgType = traceMsg.getType();
			addStringPair(json, "MPL_Id" , MPL_ID);
			addStringPair(json, "Run_Id" , runId);
			addStringPair(json, "Encoding" , encoding);
			addStringPair(json,"IflowModelElementId" , iflowModelElementId);
			if(modelStepId != null) {
				if(!modelStepId.equals(iflowModelElementId)) {
					addStringPair(json,"IflowModelElementId" , modelStepId);
				}
			}
			if(traceMsgType != null) {
				addStringPair(json, "TraceMessageType" , traceMsgType.name());
			}

			JSONObject headerJson = addMapValues(headers);
			if(headerJson != null) {
				json.put("Headers", headerJson);
			}

			JSONObject exchangeProperties = addMapValues(exchangeProps);
			if(exchangeProperties != null) {
				json.put("Exchange-Properties", exchangeProperties);
			}
			log(json.toJSONString());
		}

	}

	protected static void log(JSONObject json) {
		log(json.toJSONString());
	}

	@SuppressWarnings("unchecked")
	public static void log(TraceMessage traceMsg) {
		if(traceMsg != null) {
			JSONObject json = new JSONObject();

			String encoding = traceMsg.getEncoding();
			Map<String, String> exchangeProps =  traceMsg.getExchangeProperties();
			Map<String,String> headers = traceMsg.getHeaders();
			String iflowModelElementId = traceMsg.getIflowModelElementId();
			String MPL_ID =traceMsg.getMplId();
			String runId = traceMsg.getRunId();
			TraceMessageType traceMsgType = traceMsg.getType();
			addStringPair(json, "MPL_Id" , MPL_ID);
			addStringPair(json, "Run_Id" , runId);
			addStringPair(json, "Encoding" , encoding);
			addStringPair(json,"IflowModelElementId" , iflowModelElementId);
			if(traceMsgType != null) {
				addStringPair(json, "TraceMessageType" , traceMsgType.name());
			}

			JSONObject headerJson = addMapValues(headers);
			if(headerJson != null) {
				json.put("Headers", headerJson);
			}

			JSONObject exchangeProperties = addMapValues(exchangeProps);
			if(exchangeProperties != null) {
				json.put("Exchange-Properties", exchangeProperties);
			}
			log(json.toJSONString());
		}
	}

	@SuppressWarnings("unchecked")
	protected static void addStringPair(JSONObject json, String key, String value) {
		if(json != null && key != null && !key.isEmpty() && value != null) {
			json.put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	protected static JSONObject addMapValues(Map<String, String> attributes) {
		if(attributes != null) {
			JSONObject json = new JSONObject();
			json.putAll(attributes);
			return json;
		}
		return null;
	}

	public static void log(String message) {
		if(!enabled) return;

		if(!initialized) {
			init();
		}
		Level level = TRACEMESSAGE_LOGGER.getLevel();
		if(level == null || !level.equals(Level.INFO)) {
			TRACEMESSAGE_LOGGER.setLevel(Level.INFO);
		}


		Handler[] handlers = TRACEMESSAGE_LOGGER.getHandlers();
		if(handlers.length == 0) {
			if(handler != null) {
				TRACEMESSAGE_LOGGER.addHandler(handler);
				handlers = TRACEMESSAGE_LOGGER.getHandlers();
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Couldn't add NRHandler for TraceMessageLogger because handler is not initialized");
			}
		}
		TRACEMESSAGE_LOGGER.log(Level.INFO, message);
		LoggingCount++;
	}

	public static void init() {
		if(currentTracelogConfig == null) {
			Config agentConfig = NewRelic.getAgent().getConfig();
			currentTracelogConfig = getConfig(agentConfig);
		}

		enabled = currentTracelogConfig.isTracelog_enabled();

		if(INSTANCE == null) {
			INSTANCE = new TraceMessageLogger();
		}

		if(timerTask == null) {
			timerTask = new NRLabsTimerTask();
			Timer timer = new Timer("TracelogConfig");
			timer.scheduleAtFixedRate(timerTask, 2L * 60000L,  2L * 60000);
		}

		int rolloverMinutes = currentTracelogConfig.getRolloverMinutes();
		if(rolloverMinutes < 1) {
			rolloverMinutes = 60;
		}

		String rolloverSize = currentTracelogConfig.getRolloverSize();
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

		String tracemessageFileName = currentTracelogConfig.getTraceMessageLog() + "2";
		File file = new File(tracemessageFileName);
		File parent = file.getParentFile();

		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for tracemessage log files: {0})", tracemessageFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for tracemessage log files: {0})", tracemessageFileName);			}
		}

		int maxFiles = currentTracelogConfig.getMaxLogFiles();
		if(handler != null) {
			handler.flush();
			handler.close();
			if(TRACEMESSAGE_LOGGER != null) {
				TRACEMESSAGE_LOGGER.removeHandler(handler);
			}
			handler = null;
		}

		try {
			handler = new NRLabsHandler(tracemessageFileName, rolloverMinutes, size, maxFiles);
			handler.setFormatter(new NRLabsFormatter());
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create NRHandler for tracemessage log");
		}

		if(TRACEMESSAGE_LOGGER == null) {
			TRACEMESSAGE_LOGGER = Logger.getLogger("TraceMessageLog");
		}
		TRACEMESSAGE_LOGGER.setLevel(Level.INFO);
		TRACEMESSAGE_LOGGER.setUseParentHandlers(false);

		if(TRACEMESSAGE_LOGGER != null && handler != null) {
			TRACEMESSAGE_LOGGER.addHandler(handler);
		}

		if(TRACEMESSAGE_LOGGER != null) {
			Handler[] handlers = TRACEMESSAGE_LOGGER.getHandlers();
			for(Handler h : handlers) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "TraceMessageLogger has handler: {0}",h);
			}
		}

		Map<String, Object> event = new HashMap<>();
		event.put("Initialized", new Date());
		event.put("GateLogFile",tracemessageFileName);
		event.put("RolloverSize",rolloverSize);
		event.put("RolloverMinutes",rolloverMinutes);
		event.put("Enabled", enabled);
		Insights insights = NewRelic.getAgent().getInsights();
		if(insights != null) {
			insights.recordCustomEvent("TracelogLogInit", event);
		} else {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Constructed TRACEMESSAGE_LOGGER using : {0}", event);
		}
		initialized = true;
	}

	@Override
	public void beforeHarvest(String appName, StatsEngine statsEngine) {
		synchronized(LoggingCount) {
			statsEngine.getStats(LOGEVENTS).recordDataPoint(LoggingCount);
			LoggingCount = 0;
		}
	}

	@Override
	public void afterHarvest(String appName) {
	}
}
