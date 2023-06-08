package com.sap.aii.af.idoc.inbound;

import java.util.Properties;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.idoc.IDocDocumentList;

@Weave
public abstract class IDocReceiverBean {

	@Trace(dispatcher = true)
	public void onMessage(IDocDocumentList iDocList, String trxId, Properties info) {
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public void onMessage(IDocDocumentList[] iDocLists, String trxId, String queueName, Properties info) {
		NewRelic.getAgent().getTracedMethod().addCustomAttribute("QueueName", queueName);
		Weaver.callOriginal();
	}
}
