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
public abstract class JCoIDocQueueHandler {

	@Trace(dispatcher = true)
	public void handleRequest(JCoIDocServerContext context, IDocDocumentList[] docList) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRIDocsUtils.addJcoIDocServerContext(attributes, context);
		NRIDocsUtils.addAttribute(attributes, "Number of DocumentLists", docList.length);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
