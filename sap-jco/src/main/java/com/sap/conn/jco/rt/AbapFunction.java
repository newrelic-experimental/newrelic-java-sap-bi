package com.sap.conn.jco.rt;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jco.NRJcoUtils;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;

@Weave
public abstract class AbapFunction {

	
	public abstract String getName();
	
	@Trace(dispatcher=true)
	public void execute(JCoDestination destination) throws JCoException {
		String destName = destination != null ? destination.getDestinationName() : "UnknownDestination";
		String abapName = getName() != null ? getName() : "UnknownABAP";
		HashMap<String, Object> attributes = new HashMap<>();
		NRJcoUtils.addAttribute(attributes, "FunctionName", abapName);
		NRJcoUtils.addJcoDestination(attributes, destination);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttributes(attributes);
		traced.setMetricName("Custom","AbapFunction","execute",abapName,destName);
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
		HashMap<String, Object> attributes = new HashMap<>();
		NRJcoUtils.addAttribute(attributes, "FunctionName", abapName);
		NRJcoUtils.addJcoDestination(attributes, destination);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttributes(attributes);
		traced.setMetricName("Custom","AbapFunction","execute",abapName,destName);
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
		HashMap<String, Object> attributes = new HashMap<>();
		NRJcoUtils.addAttribute(attributes, "FunctionName", abapName);
		NRJcoUtils.addJcoDestination(attributes, destination);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttributes(attributes);
		traced.setMetricName("Custom","AbapFunction","execute",abapName,destName);
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
