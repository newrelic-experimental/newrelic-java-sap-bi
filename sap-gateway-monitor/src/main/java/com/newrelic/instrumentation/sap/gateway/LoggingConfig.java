package com.newrelic.instrumentation.sap.gateway;

import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

import static com.newrelic.instrumentation.sap.gateway.GatewayLogger.*;

import java.util.logging.Level;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;

public class LoggingConfig implements AgentConfigListener {
	
	protected static GatewayMessageConfig gatewayConfig = null;
	private static LoggingConfig INSTANCE = null;
	protected static boolean initialized = false;
	
	protected static void setup(Config config) {
		if(INSTANCE == null || !initialized) {
			INSTANCE = new LoggingConfig();
		}
		
		if(config != null) {
			gatewayConfig = getGatewayConfig(config);
		}
	}
	
	private LoggingConfig() {
		if(INSTANCE == null) {
			INSTANCE = this;
		}
		ServiceFactory.getConfigService().addIAgentConfigListener(INSTANCE);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Added LoggingConfig as configuration listener");
		initialized = true;
	}
	
	private static GatewayMessageConfig getGatewayConfig(Config agentConfig) {
		
		GatewayMessageConfig gConfig = new GatewayMessageConfig();
		Object obj = agentConfig.getValue(MESSAGELOGFILENAME);
		if(obj != null) {
			String tmp = obj.toString();
			if(!tmp.isEmpty()) {
				gConfig.setMessageFile(tmp);
			}
		}
		obj = agentConfig.getValue(MESSAGELOGMAXFILES);
		int maxFiles = 0;
		if(obj != null && obj instanceof Number) {
			maxFiles = ((Number)obj).intValue();
		}
		gConfig.setMaxLogFiles(maxFiles);
		
		obj = agentConfig.getValue(MESSAGELOGROLLOVERSIZE);
		String rolloverSize = "100k";
		if(obj != null) {
			rolloverSize = (String)obj; 
		}
		gConfig.setRolloverSize(rolloverSize);
		
		obj = agentConfig.getValue(MESSAGELOGROLLOVERINTERVAL);
		int rolloverMinutes = 0;
		if(obj != null ) {
			rolloverMinutes = (int)obj; 
		}
		gConfig.setRolloverMinutes(rolloverMinutes);
		
		obj = agentConfig.getValue(MESSAGELOGENABLED);
		if(obj != null) {
			if(obj instanceof Boolean) {
				gConfig.setEnabled(((Boolean)obj));
			} else {
				Boolean b = Boolean.valueOf(obj.toString());
				gConfig.setEnabled(b);
			}
		}
		
		obj = agentConfig.getValue(MESSAGELOGSIMULATE);
		if(obj != null) {
			if(obj instanceof Boolean) {
				gConfig.setSimulate(((Boolean)obj));
			} else {
				Boolean b = Boolean.valueOf(obj.toString());
				gConfig.setSimulate(b);
			}
		}
		return gConfig;
	}

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {
		GatewayMessageConfig config = getGatewayConfig(agentConfig);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Agent configuration changed, processing change in Gateway, existing simulate {0}, new simulate {1}", gatewayConfig.isSimulate(),config.isSimulate());
		boolean same = config.equals(gatewayConfig);
		if(gatewayConfig.isSimulate() != config.isSimulate()) {
			GatewayLogger.checkSimulate(config);
			gatewayConfig.setSimulate(config.isSimulate());
		}
		if(!same) {
			gatewayConfig = config;
			GatewayLogger.initialize();
		}
	}

	
}
