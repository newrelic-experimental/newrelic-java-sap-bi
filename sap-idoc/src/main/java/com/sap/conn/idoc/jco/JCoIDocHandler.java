package com.sap.conn.idoc.jco;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.idoc.NRIDocsUtils;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.jco.server.JCoServerContext;

@Weave(type = MatchType.Interface)
public abstract class JCoIDocHandler {

	@Trace(dispatcher = true)
	public void handleRequest(JCoServerContext ctx, IDocDocumentList docList) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRIDocsUtils.addJcoServerContext(attributes, ctx);
		NRIDocsUtils.addIDocDocumentList(attributes, docList);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		Weaver.callOriginal();
	}
}
