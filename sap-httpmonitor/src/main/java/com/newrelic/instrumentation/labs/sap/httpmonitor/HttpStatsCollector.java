package com.newrelic.instrumentation.labs.sap.httpmonitor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.newrelic.agent.HarvestListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.agent.stats.StatsEngine;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.httpserver.server.HttpMonitoring;
import com.sap.engine.services.servlets_jsp.server.DeployContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.WebMonitoring;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainer;

public class HttpStatsCollector implements HarvestListener {
	
	public static boolean collectHttp = false;
	public static boolean collectWeb = false;
	
	public static boolean initialized = false;
	
	static {
		if(!initialized) {
			init();
		}
	}
	
	public static void init() {
		ServiceFactory.getHarvestService().addHarvestListener(new HttpStatsCollector());
		initialized = true;
	}

	@Override
	public void afterHarvest(String arg0) {
		
	}

	@Override
	public void beforeHarvest(String arg0, StatsEngine arg1) {
		if (collectWeb) {
			ServiceContext serviceContext = ServiceContext.getServiceContext();
			WebMonitoring webMonitoring = serviceContext.getWebMonitoring();
			if (webMonitoring != null) {
				
				HashMap<String, Object> attributes = new HashMap<String, Object>();
				putServiceContext(attributes, serviceContext);
				putValue(attributes, "AllRequestsCount", webMonitoring.getAllRequestsCount());
				putValue(attributes, "AllResponsesCount", webMonitoring.getAllResponsesCount());
				putValue(attributes, "CurrentHttpSessions", webMonitoring.getCurrentHttpSessions());
				putValue(attributes, "CurrentSecuritySessions", webMonitoring.getCurrentSecuritySessions());
				putValue(attributes, "Error500Count", webMonitoring.getError500Count());
				putValue(attributes, "HttpSessionsInvalidatedByApplication",
						webMonitoring.getHttpSessionsInvalidatedByApplication());
				putValue(attributes, "SecuritySessionsInvalidatedByApplication",
						webMonitoring.getSecuritySessionsInvalidatedByApplication());
				putValue(attributes, "TimedOutHttpSessions", webMonitoring.getTimedOutHttpSessions());
				putValue(attributes, "TimedOutSecuritySessions", webMonitoring.getTimedOutSecuritySessions());
				putValue(attributes, "TotalResponseTime", webMonitoring.getTotalResponseTime());
				
				NewRelic.getAgent().getInsights().recordCustomEvent("WebMonitoring", attributes);
			} 
		}
		if (collectHttp) {
			HttpMonitoring httpMonitoring = com.sap.engine.services.httpserver.server.ServiceContext.getServiceContext()
					.getHttpMonitoring();
			if (httpMonitoring != null) {
				HashMap<String, Object> attributes = new HashMap<String, Object>();
				putValue(attributes, "ActiveThreadsCount", httpMonitoring.getActiveThreadsCount());
				putValue(attributes, "AllRequestsCount", httpMonitoring.getAllRequestsCount());
				putValue(attributes, "AllResponsesCount", httpMonitoring.getAllResponsesCount());
				putValue(attributes, "ResponsesFromCacheCount", httpMonitoring.getResponsesFromCacheCount());
				putValue(attributes, "ThreadsInProcessRate", httpMonitoring.getThreadsInProcessRate());
				putValue(attributes, "ThreadPoolSize", httpMonitoring.getThreadPoolSize());
				putValue(attributes, "TotalResponseTime", httpMonitoring.getTotalResponseTime());

				String[] methods = httpMonitoring.getMethodNames();
				for (String method : methods) {
					putValue(attributes, method + "-RequestsCount", httpMonitoring.getRequestsCount(method));
				}

				int[] responseCodes = httpMonitoring.getResponseCodes();
				for (int respCode : responseCodes) {
					putValue(attributes, respCode + "-ResponsesCount", httpMonitoring.getResponsesCount(respCode));
				}
				NewRelic.getAgent().getInsights().recordCustomEvent("HttpMonitoring", attributes);
			} 
		}
	}
	
	private static void putDeployContext(Map<String,Object> attributes, DeployContext context) {
		putValue(attributes, "DeployContext-StartedApplications", context.getStartedApplicationsCount());
	}

	private static void putWebContainer(Map<String,Object> attributes, WebContainer webContainer) {
		ContainerInfo containerInfo = webContainer.getContainerInfo();
		if(containerInfo != null) {
			putValue(attributes, "WebContainer-ModuleName", containerInfo.getModuleName());
			putValue(attributes, "WebContainer-Name", containerInfo.getName());
		}
	}
	
	private static void putServiceContext(Map<String, Object> attributes, ServiceContext serviceContext) {
		putValue(attributes, "ServerContext-InstanceName", serviceContext.getInstanceName());
		putValue(attributes, "ServerContext-ServerId", serviceContext.getServerId());
		putValue(attributes, "ServerContext-ServerName", serviceContext.getServerName());
		putDeployContext(attributes, serviceContext.getDeployContext());
		WebContainer webContainer = serviceContext.getWebContainer();
		if(webContainer != null) {
			putWebContainer(attributes, webContainer);
		}
	}
	
	private static void putValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
