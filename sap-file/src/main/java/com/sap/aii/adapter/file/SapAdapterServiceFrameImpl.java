package com.sap.aii.adapter.file;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumenation.sap.file.FileUtils;
import com.sap.engine.interfaces.messaging.api.Message;

@Weave
public abstract class SapAdapterServiceFrameImpl {

	@Trace(dispatcher=true)
	public Object callSapAdapter(String channel, Message msMessage) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","File","SapAdapterServiceFrameImpl","callSapAdapter");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		FileUtils.addMessageKey(attributes, msMessage.getMessageKey());
		FileUtils.addValue(attributes, "Channel", channel);
		FileUtils.addAction(attributes, msMessage.getAction());
		FileUtils.addParty(attributes, msMessage.getFromParty(), "From");
		FileUtils.addParty(attributes, msMessage.getToParty(), "To");
		FileUtils.addService(attributes, msMessage.getFromService(), "From");
		FileUtils.addService(attributes, msMessage.getToService(), "To");
		traced.addCustomAttributes(attributes);

		Object obj = Weaver.callOriginal();
		return obj;
		
	}
}
