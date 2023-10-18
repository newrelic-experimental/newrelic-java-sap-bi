package com.newrelic.instrumentation.labs.sap.jcomonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.NewRelic;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.monitor.JCoConnectionData;
import com.sap.conn.jco.monitor.JCoDestinationMonitor;
import com.sap.conn.jco.monitor.JCoRepositoryMonitor;
import com.sap.conn.jco.monitor.JCoServerMonitor;

public class StatsCollector implements Runnable {

	public static boolean initialized = false;

	private static final List<JCoServerMonitor> serverMonitors = new ArrayList<JCoServerMonitor>();
	private static final List<JCoDestinationManager> destMgrs = new ArrayList<JCoDestinationManager>();
	private static final List<JCoRepositoryMonitor> repos = new ArrayList<JCoRepositoryMonitor>();
	
	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(HashMap<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		putValue(attributes, "Agent-InstanceName", instanceId);
	}
	

	public static void init() {
		initialized = true;
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new StatsCollector(), 1L, 5L, TimeUnit.MINUTES);
	}

	public static void addServerMonitor(JCoServerMonitor m) {
		if(!serverMonitors.contains(m)) {
			serverMonitors.add(m);
		}
	}
	
	public static void addDestinationManager(JCoDestinationManager mgr) {
		if(!destMgrs.contains(mgr)) {
			destMgrs.add(mgr);
		}
	}
	
	public static void addRepositoryMonitor(JCoRepositoryMonitor repo) {
		if(!repos.contains(repo)) {
			repos.add(repo);
		}
	}

	@SuppressWarnings({"unchecked" })
	public void run() {

		HashMap<String, Object> attributes = new HashMap<String, Object>();
		putValue(attributes, "JCOServers", serverMonitors.size());

		for(JCoServerMonitor server : serverMonitors) {
			HashMap<String, Object> serverAttributes = new HashMap<String, Object>();
			putValue(serverAttributes, "CurrentConnectionCount", server.getCurrentConnectionCount());
			putValue(serverAttributes, "CurrentServerThreadCount", server.getCurrentServerThreadCount());
			putValue(serverAttributes, "ServerState", server.getState());
			putValue(serverAttributes, "ServerThreadCount", server.getServerThreadCount());
			putValue(serverAttributes, "StatelessConnectionCount", server.getStatelessConnectionCount());
			putValue(serverAttributes, "UsedServerThreadCount", server.getUsedServerThreadCount());
			List<JCoConnectionData> clientMonitors = (List<JCoConnectionData>) server.getConnectionsData();
			putValue(serverAttributes, "ClientConnections", clientMonitors != null ? clientMonitors.size() : 0);
			addInstanceName(serverAttributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("JCoServer", serverAttributes);
		}

		putValue(attributes, "JCoDestinationManagers", destMgrs.size());
		int count = 1;
		for(JCoDestinationManager mgr : destMgrs) {
			List<String> destIds = mgr.getDestinationIDs();
			putValue(attributes, "JCoDestinationManager-"+count+"-Destinations", destIds.size());
			
			for(String destId : destIds) {
				HashMap<String, Object> destAttributes = new HashMap<String, Object>();
				JCoDestinationMonitor destMonitor = mgr.getDestinationMonitor(destId);
				putValue(destAttributes, "MonitorType","Destination");
				putValue(destAttributes, "DestinationID",destMonitor.getDestinationID());
				putValue(destAttributes, "LastActivityTimestamp",destMonitor.getLastActivityTimestamp());
				putValue(destAttributes, "MaxUsedCount",destMonitor.getMaxUsedCount());
				putValue(destAttributes, "PeakLimit",destMonitor.getPeakLimit());
				putValue(destAttributes, "PoolCapacity",destMonitor.getPoolCapacity());
				putValue(destAttributes, "PooledConnectionCount",destMonitor.getPooledConnectionCount());
				putValue(destAttributes, "UsedConnectionCount",destMonitor.getUsedConnectionCount());
				putValue(destAttributes, "WaitingThreadCount",destMonitor.getWaitingThreadCount());
				addInstanceName(destAttributes);
				NewRelic.getAgent().getInsights().recordCustomEvent("JCoDestination", destAttributes);
				
				JCoDestinationMonitor repoMonitor = mgr.getRepositoryDestinationMonitor(destId);
				HashMap<String, Object> repoAttributes = new HashMap<String, Object>();
				putValue(repoAttributes, "MonitorType","Repository");
				putValue(repoAttributes, "DestinationID",repoMonitor.getDestinationID());
				putValue(repoAttributes, "LastActivityTimestamp",repoMonitor.getLastActivityTimestamp());
				putValue(repoAttributes, "PeakLimit",repoMonitor.getPeakLimit());
				putValue(repoAttributes, "PoolCapacity",repoMonitor.getPoolCapacity());
				putValue(repoAttributes, "PooledConnectionCount",repoMonitor.getPooledConnectionCount());
				putValue(repoAttributes, "UsedConnectionCount",repoMonitor.getUsedConnectionCount());
				putValue(repoAttributes, "WaitingThreadCount",repoMonitor.getWaitingThreadCount());
				addInstanceName(repoAttributes);
				NewRelic.getAgent().getInsights().recordCustomEvent("JCoDestination", repoAttributes);
			}
			
			count++;
		}
		
		putValue(attributes, "JCoRepositoryMonitors", repos.size());
		for(JCoRepositoryMonitor repoMonitor : repos) {
			HashMap<String, Object> repoAttributes = new HashMap<String, Object>();
			putValue(repoAttributes, "ClassMetaDataCount",repoMonitor.getClassMetaDataCount());
			putValue(repoAttributes, "FunctionMetaDataCount",repoMonitor.getFunctionMetaDataCount());
			putValue(repoAttributes, "LastAccessTimestamp",repoMonitor.getLastAccessTimestamp());
			putValue(repoAttributes, "LastRemoteQueryTimestamp",repoMonitor.getLastRemoteQueryTimestamp());
			putValue(repoAttributes, "TypeMetaDataCount",repoMonitor.getTypeMetaDataCount());
			addInstanceName(repoAttributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("JCoRepositoryMonitor", repoAttributes);
			
		}

		addInstanceName(attributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("JCOMonitor", attributes);

	}

	private static void putValue(HashMap<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
