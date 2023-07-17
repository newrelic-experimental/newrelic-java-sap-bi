package com.sap.conn.idoc.jco;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idoc.NRIDocsUtils;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.jco.JCoDestination;

@Weave
public abstract class JCoIDoc {

	@Trace(dispatcher = true)
	public static void send(IDocDocument idoc, char idocVersion, JCoDestination destination, String tid, String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRIDocsUtils.addJcoDestination(attributes, destination);
		NRIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		NRIDocsUtils.addAttribute(attributes, "TID", tid);
		NRIDocsUtils.addIDocDoument(attributes, idoc);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public static void send(IDocDocument[] idocs, char idocVersion, JCoDestination destination, String tid, String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRIDocsUtils.addJcoDestination(attributes, destination);
		NRIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		NRIDocsUtils.addAttribute(attributes, "TID", tid);
		NRIDocsUtils.addAttribute(attributes, "Number Docs", idocs != null ? idocs.length : 0);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public static void send(IDocDocumentList idocList, char idocVersion, JCoDestination destination, String tid,
			String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRIDocsUtils.addJcoDestination(attributes, destination);
		NRIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		NRIDocsUtils.addAttribute(attributes, "TID", tid);
		NRIDocsUtils.addAttribute(attributes, "Number Docs", idocList != null ? idocList.getNumDocuments() : 0);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public static void send(IDocDocumentList[] idocLists, char idocVersion, JCoDestination destination, String tid, String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRIDocsUtils.addJcoDestination(attributes, destination);
		NRIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		NRIDocsUtils.addAttribute(attributes, "TID", tid);
		NRIDocsUtils.addAttribute(attributes, "Number Docs", idocLists != null ? idocLists.length : 0);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
