package com.sap.engine.services.httpserver.server;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.httpmonitor.HttpStatsCollector;

@Weave
public abstract class Processor {

	
	public void runServer() {
		HttpStatsCollector.collectHttp = true;
		Weaver.callOriginal();
	}
	
	public void stopServer() {
		HttpStatsCollector.collectHttp = false;
		Weaver.callOriginal();
	}
}
