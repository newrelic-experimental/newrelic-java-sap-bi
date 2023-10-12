package com.sap.aii.adapter.jdbc;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jdbc.JDBCUtils;
import com.sap.engine.interfaces.messaging.api.Message;

@Weave
public abstract class SapAdapterServiceFrameImpl {

	@Trace(dispatcher=true)
	public Object callSapAdapter(String channel, Message msMessage) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","JDBC","SapAdapterServiceFrameImpl","callSapAdapter");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		JDBCUtils.addMessageKey(attributes, msMessage.getMessageKey());
		JDBCUtils.addValue(attributes, "Channel", channel);
		JDBCUtils.addAction(attributes, msMessage.getAction());
		JDBCUtils.addParty(attributes, msMessage.getFromParty(), "From");
		JDBCUtils.addParty(attributes, msMessage.getToParty(), "To");
		JDBCUtils.addService(attributes, msMessage.getFromService(), "From");
		JDBCUtils.addService(attributes, msMessage.getToService(), "To");
		traced.addCustomAttributes(attributes);
		Object obj = Weaver.callOriginal();
		return obj;
	}
	
}
