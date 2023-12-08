package com.newrelic.instrumentation.sap.gateway;

import com.newrelic.api.agent.Config;
import static com.newrelic.instrumentation.sap.gateway.GatewayMonitor.*;

public class LoggingConfig {
	
	protected static GatewayMessageConfig gatewayConfig = null;
	
	protected static void setup(Config config) {
		if(config == null) {
			gatewayConfig = getGatewayConfig(config);
		}
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
		return gConfig;
	}

}
