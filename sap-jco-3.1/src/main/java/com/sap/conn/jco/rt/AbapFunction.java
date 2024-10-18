package com.sap.conn.jco.rt;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jco.NRJcoUtils;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.newrelic.api.agent.Trace;

@Weave
public abstract class AbapFunction {

	
	public abstract String getName();
	
	@Trace(dispatcher=true)
	public void execute(JCoDestination destination) throws JCoException {
		String destName = destination != null ? destination.getDestinationName() : "UnknownDestination";
		String abapName = getName() != null ? getName() : "UnknownABAP";
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","AbapFunction","execute",abapName,destName);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJcoDestination(attributes, destination);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		try {
			Weaver.callOriginal();
		} catch(Exception e) {
			if(e instanceof JCoException) {
				JCoException jce = (JCoException)e;
				NewRelic.noticeError(jce);
				throw jce;
			}
			
		}
	}
	
	@Trace(dispatcher=true)
	public void execute(JCoDestination destination, String tid) throws JCoException {
		String destName = destination != null ? destination.getDestinationName() : "UnknownDestination";
		String abapName = getName() != null ? getName() : "UnknownABAP";
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","AbapFunction","execute",abapName,destName);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJcoDestination(attributes, destination);
		NRJcoUtils.addAttribute(attributes, "TID", tid);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		try {
			Weaver.callOriginal();
		} catch(Exception e) {
			if(e instanceof JCoException) {
				JCoException jce = (JCoException)e;
				NewRelic.noticeError(jce);
				throw jce;
			}
			
		}
	}
	
	@Trace(dispatcher=true)
	public void execute(JCoDestination destination, String tid, String queueName) throws JCoException {
		String destName = destination != null ? destination.getDestinationName() : "UnknownDestination";
		String abapName = getName() != null ? getName() : "UnknownABAP";
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","AbapFunction","execute",abapName,destName);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJcoDestination(attributes, destination);
		NRJcoUtils.addAttribute(attributes, "TID", tid);
		NRJcoUtils.addAttribute(attributes, "QueueName", queueName);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		try {
			Weaver.callOriginal();
		} catch(Exception e) {
			if(e instanceof JCoException) {
				JCoException jce = (JCoException)e;
				NewRelic.noticeError(jce);
				throw jce;
			}
			
		}
	}
}
