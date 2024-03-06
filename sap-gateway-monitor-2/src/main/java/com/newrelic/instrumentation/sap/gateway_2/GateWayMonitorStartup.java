package com.newrelic.instrumentation.sap.gateway_2;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class GateWayMonitorStartup {

	public static void premain(String s, Instrumentation inst) {
		init();
	}

	public static void init() {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to initialize Gateway Logging");
		
		if(GatewayLogger.initialized) return;
		
		GatewayLogger.initialize();
		
		if(GatewayLogger.initialized) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "GatewayLogger2 has been initialized");
		} else {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to initialize GatewayLogger2");
		}
	}
}
