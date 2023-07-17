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
public abstract class JCoIDocQueueHandler {

	@Trace
	public void handleRequest(JCoIDocServerContext var1, IDocDocumentList[] var2) {

		HashMap<String, Object> attributes = new HashMap<>();
		for(IDocDocumentList idocList : var2) {
			SAPIDocsUtils.addIDocDocumentList(attributes, idocList);
		}

		Weaver.callOriginal();

		for(IDocDocumentList idocList : var2) {
			int n = idocList.getNumDocuments();

			for(int i=0; i<n;i++) {
				IDocDocument doc = idocList.get(i);
				SAPIDocsUtils.addIDocDocument(attributes, doc);
			}
		}

	}
}
