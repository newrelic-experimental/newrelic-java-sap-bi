package com.sap.engine.messaging.runtime;

import java.util.Map;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.systemstatus.SystemStatusReporter;
import com.sap.engine.interfaces.messaging.api.systemstatus.SystemStatus;

@Weave
public abstract class MessagingSystem {

	public static SystemStatus getSystemStatus() {
		
		return Weaver.callOriginal();
	}
	
	public static void startup() {
		if(!SystemStatusReporter.initialized) {
			SystemStatusReporter.init();
		}
		Weaver.callOriginal();
	}
	
	@SuppressWarnings("rawtypes")
	public static void initialize(Map properties, Resources resources) {
		if(!SystemStatusReporter.initialized) {
			SystemStatusReporter.init();
		}
		Weaver.callOriginal();
	}
}
