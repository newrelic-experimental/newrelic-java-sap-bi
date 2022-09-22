package com.sap.engine.services.servlets_jsp.server;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.httpmonitor.HttpStatsCollector;
import com.sap.engine.frame.ApplicationServiceContext;

@Weave
public abstract class ServletsAndJspServerFrame {

	
	public void start(final ApplicationServiceContext sc) {
		HttpStatsCollector.collectWeb = true;
		Weaver.callOriginal();
	}
	
	public void stop() {
		HttpStatsCollector.collectWeb = false;
		Weaver.callOriginal();
	}
}
