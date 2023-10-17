package com.sap.engine.services.servlets_jsp.server;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.httpmonitor.HttpStatsCollector;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainer;

@Weave
public abstract class ServiceContext {

	
	public static ServiceContext getServiceContext() {
		return Weaver.callOriginal();
	}
	
	public ServiceContext(ApplicationServiceContext applicationServiceContext) {
		if(!HttpStatsCollector.initialized) {
			HttpStatsCollector.init();
		}
	}
	
	public abstract WebMonitoring getWebMonitoring();
	
	public abstract DeployContext getDeployContext();
	
	public abstract String getInstanceName();
	
	public abstract int getServerId();
	
	public abstract String getServerName();
	
	public abstract WebContainer getWebContainer();
	
	
}
