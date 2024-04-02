package com.newrelic.instrumentation.sap.gateway_2;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import com.sap.igw.ejb.composite.DataSource;
import com.sap.igw.ejb.composite.MPLHeaderUI;
import com.sap.igw.ejb.composite.MPLSearchFilter;
import com.sap.it.op.jpahelper.MonitoringUI;

public class MPLMonitor implements Runnable {
	
	private static MonitoringUI monitoringUI = null;
	private static Date start = null;
	private static Date end = null;
	
	public static void setMonitoringUI(MonitoringUI ui) {
		monitoringUI = ui;
	}
	
	public static boolean initialized = false;
	private static MPLMonitor INSTANCE = null;

	private MPLMonitor() {
		
	}

	public static void initialize() {
		if(INSTANCE == null) {
			INSTANCE = new MPLMonitor();
		}
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(INSTANCE, 1L, 3L, TimeUnit.MINUTES);
		initialized = true;
		Utils.setGatewayIGWControllerMonitorInitialized();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, INSTANCE.getClass().getSimpleName() + " has been initialized");
		start = new Date();
	}
	
	@Override
	public void run() {
		NewRelic.incrementCounter("Custom/SAP/Gateway-2/" + INSTANCE.getClass().getSimpleName() + "/runs");

		int counter = 0;
		if(monitoringUI != null) {
			NewRelic.incrementCounter("Custom/SAP/Gateway-2/WithMonitoringUI/runs");
			MPLSearchFilter filter = new MPLSearchFilter();
			end = new Date();
			filter.setStart(start);
			filter.setEnd(end);
			List<MPLHeaderUI> allHeaders = monitoringUI.retrieveAllHeaders(filter, DataSource.All, counter);
			int size = allHeaders != null ? allHeaders.size() : 0;
			NewRelic.incrementCounter("Custom/SAP/Gateway-2/Headers",size);
			
			for(MPLHeaderUI header : allHeaders) {
				String result = logMPLHeader(header);
				if(result != null && !result.isEmpty()) {
					GatewayLogger.logMessage(result);
				}
			}
			NewRelic.incrementCounter("Custom/SAP/Gateway-2/HeadersReported");

			start = end;
		}
		
		
		counter = 0;
		
	}

	protected static String logMPLHeader(MPLHeaderUI header) {
		if(header == null) return null;
		
		StringBuffer sb = new StringBuffer();
		
		String value = header.getIntegrationFlow();
		addToBufferIfNecessary(sb, "Integration Flow", value);
		
		value = header.getCorrelationId();
		addToBufferIfNecessary(sb, "Correlation ID", value);
				
		value = header.getMessageGuid();
		addToBufferIfNecessary(sb, "Message Guid", value);

		value = header.getStatus();
		addToBufferIfNecessary(sb, "Status", value);

		Date start = header.getStart();
		value = start != null ? start.toString() : null;
		addToBufferIfNecessary(sb, "Start", value);

		Date end = header.getEnd();
		value = end != null ? end.toString() : null;
		addToBufferIfNecessary(sb, "End", value);

		if(start != null && end != null) {
			long duration = end.getTime() - start.getTime();
			addToBufferIfNecessary(sb, "Duration", Long.toString(duration));
		}
		
		value = header.getApplicationId();
		addToBufferIfNecessary(sb, "Application ID", value);
		
		value = header.getId();
		addToBufferIfNecessary(sb, "ID", value);
		
		DataSource ds = header.getDataSource();
		if(ds != null) {
			addToBufferIfNecessary(sb, "DataSource", ds.name());
		}
		
		return sb.toString();
	}
	
	private static void addToBufferIfNecessary(StringBuffer sb,String key, String value) {
		if(sb != null && key != null && value != null && !value.isEmpty()) {
			sb.append(key + ": ");
			sb.append(value);
			sb.append(',');
		}
	}

}
