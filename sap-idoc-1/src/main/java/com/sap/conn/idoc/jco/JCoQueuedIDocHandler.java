package com.sap.conn.idoc.jco;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idoc.IDoc1Logger;
import com.newrelic.instrumentation.labs.sap.idoc.SAPIDocsUtils;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;

@Weave(type = MatchType.Interface)
public abstract class JCoQueuedIDocHandler {

	@Trace
	public 	void handleRequest(JCoIDocServerContext var1, IDocDocumentList idocList) {

		HashMap<String, Object> listAttributes = new HashMap<>();
		
		SAPIDocsUtils.addIDocDocumentList(listAttributes, idocList);
		SAPIDocsUtils.addInstanceName(listAttributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("IDOCLIST_RECV", listAttributes);
		
		Weaver.callOriginal();
		
		int n = idocList.getNumDocuments();
		
		for(int i=0; i<n;i++) {
			HashMap<String, Object> docAttributes = new HashMap<>();
			IDocDocument doc = idocList.get(i);
			IDoc1Logger.logIDoc(doc, getClass().getName() + ".handleRequest, IDoc1");
			SAPIDocsUtils.addIDocDocument(docAttributes, doc);
			SAPIDocsUtils.addInstanceName(docAttributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("IDOC_RECV", docAttributes);
		}
		

	}
}
