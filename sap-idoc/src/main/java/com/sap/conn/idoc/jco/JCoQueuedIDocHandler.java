package com.sap.conn.idoc.jco;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idoc.NRIDocsUtils;
import com.sap.conn.idoc.IDocDocumentList;

@Weave(type = MatchType.Interface)
public abstract class JCoQueuedIDocHandler {

	@Trace(dispatcher = true)
	public void handleRequest(JCoIDocServerContext var1, IDocDocumentList var2) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRIDocsUtils.addJcoIDocServerContext(attributes, var1);
		NRIDocsUtils.addIDocDocumentList(attributes, var2);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
