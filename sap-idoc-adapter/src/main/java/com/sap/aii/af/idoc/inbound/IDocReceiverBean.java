package com.sap.aii.af.idoc.inbound;

import java.util.HashMap;
import java.util.Properties;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idocadapter.SAPIDocsUtils;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;

@Weave
public abstract class IDocReceiverBean {

	@Trace(dispatcher = true)
	public void onMessage(IDocDocumentList iDocList, String trxId, Properties info) {
		HashMap<String, Object> listAttributes = new HashMap<>();
		
		SAPIDocsUtils.addIDocDocumentList(listAttributes, iDocList);
		SAPIDocsUtils.addInstanceName(listAttributes);
		
		NewRelic.getAgent().getInsights().recordCustomEvent("IDOCLIST_RECV", listAttributes);

		int n = iDocList.getNumDocuments();
		
		for(int i=0; i<n;i++) {
			HashMap<String, Object> docAttributes = new HashMap<>();
			IDocDocument doc = iDocList.get(i);
			SAPIDocsUtils.addIDocDocument(docAttributes, doc);
			SAPIDocsUtils.addInstanceName(docAttributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("IDOC_RECV", docAttributes);
		}

		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public void onMessage(IDocDocumentList[] iDocLists, String trxId, String queueName, Properties info) {
		NewRelic.getAgent().getTracedMethod().addCustomAttribute("QueueName", queueName);

		for (IDocDocumentList iDocList : iDocLists) {
			HashMap<String, Object> listAttributes = new HashMap<>();
			SAPIDocsUtils.addIDocDocumentList(listAttributes, iDocList);
			SAPIDocsUtils.addInstanceName(listAttributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("IDOCLIST_RECV", listAttributes);
			int n = iDocList.getNumDocuments();
			for (int i = 0; i < n; i++) {
				HashMap<String, Object> docAttributes = new HashMap<>();
				IDocDocument doc = iDocList.get(i);
				SAPIDocsUtils.addIDocDocument(docAttributes, doc);
				SAPIDocsUtils.addInstanceName(docAttributes);
				NewRelic.getAgent().getInsights().recordCustomEvent("IDOC_RECV", docAttributes);
			} 
		}
		Weaver.callOriginal();
	}
}
