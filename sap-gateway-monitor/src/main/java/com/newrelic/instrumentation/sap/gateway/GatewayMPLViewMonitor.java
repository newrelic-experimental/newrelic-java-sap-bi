package com.newrelic.instrumentation.sap.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMPLView;

public class GatewayMPLViewMonitor implements Runnable {
	
	private static List<IPrivateMPLView> privateViews = new ArrayList<>();
	
	public static boolean initialized = false;
	private static GatewayMPLViewMonitor INSTANCE = null;
	
	public static void addIPrivateMPLView(IPrivateMPLView view) {
		if(!privateViews.contains(view)) {
			privateViews.add(view);
			NewRelic.incrementCounter("Custom/SAP/Gateway/GatewayMPLViewMonitor/IPrivateMPLView/Added");
		}
	}
	
	private GatewayMPLViewMonitor() {
		
	}

	public static void initialize() {
		if(INSTANCE == null) {
			INSTANCE = new GatewayMPLViewMonitor();
		}
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(INSTANCE, 15L, 15L, TimeUnit.SECONDS);
		initialized = true;
		Utils.setGatewayMPLViewMonitorInitialized();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "GatewayMPLViewMonitor has been initialized");
	}
	
	@Override
	public void run() {
		NewRelic.incrementCounter("Custom/SAP/Gateway/GatewayMPLViewMonitor/runs");

		int counter = 0;
		for(IPrivateMPLView view : privateViews) {
			com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMPLView.IContextNode ctxNode = view.wdGetContext();
			if(ctxNode != null) {
				com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMPLView.IMPLNodeNode mplNode = ctxNode.nodeMPLNode();
				int size = mplNode.size();
				for(int i=0; i < size; i++) {
					counter++;
				    com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMPLView.IMPLNodeElement element = mplNode.getMPLNodeElementAt(i);
				    reportPrivateIMPLNodeElement(element);
				}
			}
		}
		NewRelic.incrementCounter("Custom/SAP/Gateway/GatewayMPLViewMonitor/IPrivateMPLView/IMPLNodeElements",counter);
		
	}
	
	private void reportPrivateIMPLNodeElement(com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMPLView.IMPLNodeElement element) {
		StringBuffer sb = new StringBuffer();
		
		String msgId = element.getMessageId();
		if(msgId != null && !msgId.isEmpty()) {
			sb.append("Message Id: " + msgId + ", ");
		}
		
		String status = element.getStatus();
		if(status != null && !status.isEmpty()) {
			sb.append("Status : " + status + ", ");
		}
		
		Date start = element.getStartDate();
		if(start != null) {
			sb.append("Processed At: ");
			sb.append(start);
			sb.append(", ");
		}
		
		Date end = element.getEndDate();
		if(end != null) {
			sb.append("Processing Finished At: ");
			sb.append(end);
			sb.append(", ");
		}
		
		String flow = element.getIntegrationFlow();
		if(flow != null && !flow.isEmpty()) {
			sb.append("Integration Flow: " + flow + ", ");
		}
		
		long duration = element.getDuration();
		sb.append("Duration: ");
		sb.append(duration);
		
		String result = sb.toString();
		if(!result.isEmpty()) {
			GatewayLogger.logMessage(result);
			NewRelic.incrementCounter("Custom/SAP/Gateway/GatewayMPLViewMonitor/recordsWritten");
		}
		
	}

}
