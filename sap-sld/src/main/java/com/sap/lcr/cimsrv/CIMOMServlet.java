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
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		Weaver.callOriginal();
	}
	
	@Trace
	private HttpCimResponse processCimRequest(HttpServletRequest request, HttpServletResponse response, boolean isMPost) {
		return Weaver.callOriginal();
	}
	
	@SuppressWarnings("unused")
	private Object analyseHeaders(HttpServletRequest request, HttpServletResponse response, boolean isMPost) {
		Object obj = Weaver.callOriginal();
		
		if(obj instanceof HttpCimResponse) {
			NewRelic.getAgent().getTransaction().ignore();
		}
		 
		return obj;
	}
	
	@Weave
	private static final class HttpCimResponse {
		
	}
}
