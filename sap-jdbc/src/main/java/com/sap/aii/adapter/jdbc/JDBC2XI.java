package com.sap.aii.adapter.jdbc;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.jdbc.JDBCUtils;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class JDBC2XI {
	
	@Trace(dispatcher=true)
	void send(byte[] toSend, String eoGuid, MessageKey messageKey) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JDBC","JDBC2XI","send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		JDBCUtils.addMessageKey(attributes, messageKey);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
}
