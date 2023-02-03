package com.sap.aii.adapter.rest.ejb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class RESTAdapter {

	@Trace
	public int service(HttpServletRequest req, HttpServletResponse res) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","REST","RESTAdapter","service");
		return Weaver.callOriginal();
	}
}
