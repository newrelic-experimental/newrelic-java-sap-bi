package com.sap.conn.idoc.jco;

import java.util.HashMap;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idoc.SAPIDocsUtils;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;

@Weave(type = MatchType.Interface)
public abstract class JCoQueuedIDocHandler {

	@Trace
	public 	void handleRequest(JCoIDocServerContext var1, IDocDocumentList idocList) {

		HashMap<String, Object> attributes = new HashMap<>();
		
		SAPIDocsUtils.addIDocDocumentList(attributes, idocList);
		
		Weaver.callOriginal();
		
		int n = idocList.getNumDocuments();
		
		for(int i=0; i<n;i++) {
			IDocDocument doc = idocList.get(i);
			SAPIDocsUtils.addIDocDocument(attributes, doc);
		}
		

	}
}
