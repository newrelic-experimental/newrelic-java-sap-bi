package com.newrelic.instrumentation.labs.sap.idocadapter;

import java.util.Map;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;

public class SAPIDocsUtils {

	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(Map<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		addAttribute(attributes, "Agent-InstanceName", instanceId);
	}
	
	public static void addAttribute(Map<String,Object> attributes, String key, Object value) {
		if(attributes != null && key != null && !key.isEmpty() && value != null) {
			attributes.put(key,value);
		}
	}

	public static void addIDocDocumentList(Map<String,Object> attributes, IDocDocumentList iDocList) {
		addAttribute(attributes, "IDocDocumentList-DocType", iDocList.getIDocType());	
		addAttribute(attributes, "IDocDocumentList-NumberOfDocs", iDocList.getNumDocuments());	
		addAttribute(attributes, "IDocDocumentList-DocTypeExtension", iDocList.getIDocTypeExtension());	
	}
	
	public static void addIDocDocument(Map<String,Object> attributes, IDocDocument doc) {
		addAttribute(attributes, "IDocDocument-Client", doc.getClient());
		addAttribute(attributes, "IDocDocument-Direction", doc.getDirection());
		addAttribute(attributes, "IDocDocument-EDIMessage", doc.getEDIMessage());
		addAttribute(attributes, "IDocDocument-EDIMessageType", doc.getEDIMessageType());
		addAttribute(attributes, "IDocDocument-IDocNumber", doc.getIDocNumber());
		addAttribute(attributes, "IDocDocument-IDocType", doc.getIDocType());
		addAttribute(attributes, "IDocDocument-IDocTypeExtension", doc.getIDocTypeExtension());
		addAttribute(attributes, "IDocDocument-MessageCode", doc.getMessageCode());
		addAttribute(attributes, "IDocDocument-MessageFunction", doc.getMessageFunction());
		addAttribute(attributes, "IDocDocument-MessageType", doc.getMessageType());
		addAttribute(attributes, "IDocDocument-RecipientAddress", doc.getRecipientAddress());
		addAttribute(attributes, "IDocDocument-OutputMode", doc.getOutputMode());
		addAttribute(attributes, "IDocDocument-SenderAddress", doc.getSenderAddress());
		addAttribute(attributes, "IDocDocument-Status", doc.getStatus());
	}

}
