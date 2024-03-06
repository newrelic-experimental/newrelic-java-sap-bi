package com.newrelic.instrumentation.sap.gateway_2;

public class Utils {

	public static boolean initialized = false;
	
	public static void init() {
		if(initialized) return;
		
		if(!GatewayPublicMonitor.initialized) GatewayPublicMonitor.initialize();
		
	}
	
	public static void setGatewayIGWControllerMonitorInitialized() {
		initialized = GatewayPublicMonitor.initialized;
	}
	
}
