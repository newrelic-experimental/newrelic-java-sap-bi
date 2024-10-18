package com.sap.conn.jco;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class JCoRequest {
	
	public abstract String getName();

	@Trace(dispatcher = true)
	public JCoResponse execute(JCoDestination destination) throws JCoException {
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "JCoRequest", getClass().getSimpleName(), "execute", getName() });
		NewRelic.getAgent().getTracedMethod().addCustomAttribute("Name", getName());
		NewRelic.getAgent().getTracedMethod().addCustomAttribute("Destination", destination != null ? destination.getDestinationName() : "Unkown");
		JCoResponse response = null;
		try {
			response = Weaver.callOriginal();
		} catch (Exception e) {
			NewRelic.noticeError(e);
			throw e;
		}
		return response;
		
	}
	
	public void execute(JCoDestination destination, String tid) throws JCoException {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttribute("Name", getName());
		traced.addCustomAttribute("Destination", destination != null ? destination.getDestinationName() : "Unkown");
		traced.setMetricName(new String[] { "Custom", "JCoRequest", getClass().getSimpleName(), "execute", getName() });
		traced.addCustomAttribute("TID", tid);
		try {
			Weaver.callOriginal();
		} catch (Exception e) {
			NewRelic.noticeError(e);
			throw e;
		}
	}
	
	public void execute(JCoDestination destination, String tid, String queueName) throws JCoException {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName(new String[] { "Custom", "JCoRequest", getClass().getSimpleName(), "execute", getName() });
		traced.addCustomAttribute("TID", tid);
		traced.addCustomAttribute("QueueName", queueName);
		traced.addCustomAttribute("Name", getName());
		traced.addCustomAttribute("Destination", destination != null ? destination.getDestinationName() : "Unkown");
		try {
			Weaver.callOriginal();
		} catch (Exception e) {
			NewRelic.noticeError(e);
			throw e;
		}
	}
	
}
