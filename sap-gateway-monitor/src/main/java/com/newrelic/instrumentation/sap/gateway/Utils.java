package com.newrelic.instrumentation.sap.gateway;

public class Utils {

	public static boolean initialized = false;
	
	public static void init() {
		if(initialized) return;
		
		if(!GatewayPublicMonitor.initialized) GatewayPublicMonitor.initialize();
		
		if(initialized) return;
		
		if(!GatewayMPLViewMonitor.initialized) GatewayMPLViewMonitor.initialize();
		
	}
	
	public static void setGatewayIGWControllerMonitorInitialized() {
		initialized = GatewayPublicMonitor.initialized && !GatewayMPLViewMonitor.initialized;
	}
	
	public static void setGatewayMPLViewMonitorInitialized() {
		initialized = GatewayPublicMonitor.initialized && !GatewayMPLViewMonitor.initialized;
	}
}
