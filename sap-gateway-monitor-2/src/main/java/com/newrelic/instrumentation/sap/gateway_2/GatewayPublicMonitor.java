package com.newrelic.instrumentation.sap.gateway_2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPublicIGWController;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPublicIGWController.IContextNode;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPublicIGWController.IMPLNodeElement;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPublicIGWController.IMPLNodeNode;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.InternalIGWController;
import com.sap.tc.webdynpro.progmodel.gci.IGCICustomController;

public class GatewayPublicMonitor implements Runnable {
	
	private static List<InternalIGWController> igwControllers = new ArrayList<>();
	private static List<IGCICustomController> igcICustomControllers = new ArrayList<>();
	private static List<IMPLNodeNode> implNodes = new ArrayList<>();
	
	public static boolean initialized = false;
	private static GatewayPublicMonitor INSTANCE = null;
	
	public static void addIMPLNodeNode(IMPLNodeNode node) {
		if(!initialized) {
			initialize();
		}
		if (node != null) {
			if (!implNodes.contains(node)) {
				implNodes.add(node);
				NewRelic.incrementCounter("Custom/SAP/Gateway-2/" + INSTANCE.getClass().getSimpleName() + "/IMPLNodeNode/Added");
			} 
		}
	}
	
	public static void addIContextNode(IContextNode node) {
		if(!initialized) {
			initialize();
		}
		if(node != null) {
			addIMPLNodeNode(node.nodeMPLNode());
		}
	}
	
	public static void addInternalIGWController(InternalIGWController controller) {
		if(!initialized) {
			initialize();
		}
		if(controller == null) return;
		if (!igwControllers.contains(controller)) {
			igwControllers.add(controller);
			IContextNode ctxNode = controller.wdGetContext();
			if (ctxNode != null) {
				addIContextNode(ctxNode);
			}
		}
	}
	
	public static void addIGCICustomController(IGCICustomController controller) {
		if(!initialized) {
			initialize();
		}
		if(controller == null) return;
		
		if(!igcICustomControllers.contains(controller)) {
			igcICustomControllers.add(controller);
			InternalIGWController delegate = new InternalIGWController(controller);
			if(!igwControllers.contains(delegate)) {
				addInternalIGWController(delegate);
			} else {
				delegate = null;
			}
		}
		
	}
	
	private GatewayPublicMonitor() {
		
	}

	public static void initialize() {
		if(INSTANCE == null) {
			INSTANCE = new GatewayPublicMonitor();
		}
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(INSTANCE, 15L, 15L, TimeUnit.SECONDS);
		initialized = true;
		Utils.setGatewayIGWControllerMonitorInitialized();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, INSTANCE.getClass().getSimpleName() + " has been initialized");
		
	}
	
	@Override
	public void run() {
		NewRelic.incrementCounter("Custom/SAP/Gateway-2/" + INSTANCE.getClass().getSimpleName() + "/runs");

		int counter = 0;
		for(IMPLNodeNode mplNode : implNodes) {
			
			int size = mplNode.size();
			for(int i=0; i< size;i++) {
				counter++;
				IMPLNodeElement element = mplNode.getMPLNodeElementAt(i);
		 		reportPublicIMPLNodeElement(element);
			}
		
		}
		
		NewRelic.recordMetric("Custom/SAP/Gateway-2/" + INSTANCE.getClass().getSimpleName() + "/IMPLNodeElements",counter);
		
		counter = 0;
		
	}
	
	private void reportPublicIMPLNodeElement(IPublicIGWController.IMPLNodeElement element) {
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
			NewRelic.incrementCounter("Custom/SAP/Gateway-2/" + INSTANCE.getClass().getSimpleName() + "/recordsWritten");
		}
		
	}

}
