package com.sap.aii.adapter.rest.ejb;

import java.util.List;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.adapter.rest.ejb.sender.ChannelSelectorResult;
import com.sap.aii.af.service.cpa.Channel;

@Weave
public abstract class RESTChannelManager {

	@Trace
	public Channel routeCall(String method, String path, byte[] data, Map<String, List<ChannelSelectorResult>> results) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","REST","RESTChannelManager","routeCall");
		traced.addCustomAttribute("Method", method);
		traced.addCustomAttribute("Path", path);
		return Weaver.callOriginal();
	}
}
