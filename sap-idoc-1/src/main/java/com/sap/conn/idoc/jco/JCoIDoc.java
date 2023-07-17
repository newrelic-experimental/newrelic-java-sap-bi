package com.sap.conn.idoc.jco;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idoc.SAPIDocsUtils;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.jco.JCoDestination;

@Weave(type = MatchType.BaseClass)
public abstract class JCoIDoc {
	
	@Trace
	public static void send(IDocDocument idoc, char idocVersion, JCoDestination destination, String tid, String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		SAPIDocsUtils.addIDocDocument(attributes, idoc);
		NewRelic.getAgent().getInsights().recordCustomEvent("IDoc-Send", attributes);
		attributes.clear();
		
		SAPIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		SAPIDocsUtils.addJCODestination(attributes, destination);
		SAPIDocsUtils.addAttribute(attributes, "TID", tid);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		
		Weaver.callOriginal();
		
	}
	
	@Trace
	public static void send(IDocDocument[] idocs, char idocVersion, JCoDestination destination, String tid, String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		for (int i = 0; i < idocs.length; i++) {
			SAPIDocsUtils.addIDocDocument(attributes, idocs[i]);
			NewRelic.getAgent().getInsights().recordCustomEvent("IDoc-Send", attributes);
		}
		attributes.clear();
		
		SAPIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		SAPIDocsUtils.addJCODestination(attributes, destination);
		SAPIDocsUtils.addAttribute(attributes, "TID", tid);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		
		Weaver.callOriginal();
		
	}
	
	public static void send(IDocDocumentList idocList, char idocVersion, JCoDestination destination, String tid, String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		SAPIDocsUtils.addIDocDocumentList(attributes, idocList);
		NewRelic.getAgent().getInsights().recordCustomEvent("IDocList-Send", attributes);
		
		for(int j=0;j<idocList.getNumDocuments();j++) {
			attributes.clear();
			SAPIDocsUtils.addIDocDocument(attributes, idocList.get(j));
			NewRelic.getAgent().getInsights().recordCustomEvent("IDoc-Send", attributes);
		}
		attributes.clear();
		
		SAPIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		SAPIDocsUtils.addJCODestination(attributes, destination);
		SAPIDocsUtils.addAttribute(attributes, "TID", tid);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		
		Weaver.callOriginal();
	}
	
	public static void send(IDocDocumentList[] idocLists, char idocVersion, JCoDestination destination, String tid, String queueName) {
		HashMap<String, Object> attributes = new HashMap<>();
		for(IDocDocumentList idocList : idocLists) {
			attributes.clear();
			SAPIDocsUtils.addIDocDocumentList(attributes, idocList);
			NewRelic.getAgent().getInsights().recordCustomEvent("IDocList-Send", attributes);


			for(int j=0;j<idocList.getNumDocuments();j++) {
				attributes.clear();
				SAPIDocsUtils.addIDocDocument(attributes, idocList.get(j));
				NewRelic.getAgent().getInsights().recordCustomEvent("IDoc-Send", attributes);
			}
		}

		attributes.clear();
		SAPIDocsUtils.addAttribute(attributes, "QueueName", queueName);
		SAPIDocsUtils.addJCODestination(attributes, destination);
		SAPIDocsUtils.addAttribute(attributes, "TID", tid);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);

		Weaver.callOriginal();
	}

}
