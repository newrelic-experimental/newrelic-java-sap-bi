package com.sap.lcr.cimsrv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class CIMOMServlet {

	@Trace
	private HttpCimResponse processCimRequest(HttpServletRequest request, HttpServletResponse response, boolean isMPost) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","CIMOMServlet","processCimRequest");
		return Weaver.callOriginal();
	}
	
	@Weave
	private static final class HttpCimResponse {
		
	}
}
