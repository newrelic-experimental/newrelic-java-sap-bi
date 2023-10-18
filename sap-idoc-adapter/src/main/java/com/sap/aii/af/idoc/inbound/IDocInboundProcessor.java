package com.sap.aii.af.idoc.inbound;

import java.util.HashMap;
import java.util.List;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idocadapter.SAPIDocsUtils;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.engine.interfaces.messaging.api.Message;

@Weave
public abstract class IDocInboundProcessor {

	@Trace(dispatcher = true)
	public void process(IDocDocumentList iDocList, String trxId, boolean tRFC, String iDocQueueName) {
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
	
	@Trace
	private void handleSingleIDocDocument(IDocDocument idocDocument, boolean persist, String autoNumber, boolean first) {
		Weaver.callOriginal();
	}
	
	@Trace
	private void processAleaudList(IDocDocumentList iDocList, boolean persist, boolean xiMsgProcessing) {
		Weaver.callOriginal();
	}
	
	@Trace
	private void processIDocListInSeparateXIMessages(IDocDocumentList iDocList, boolean persist) {
		Weaver.callOriginal();
	}
	
	@Trace
	private void processIDocListInSeveralXIMessages(IDocDocumentList idocList, boolean persist) {
		Weaver.callOriginal();
	}
	
	@Trace
	private void processIDocListInSingleXIMessage(IDocDocumentList idocList, boolean persist) {
		Weaver.callOriginal();
	}
	
	@Trace
	private void sendXiMessage(Message message, List<String> docNumList) {
		Weaver.callOriginal();
	}
}
