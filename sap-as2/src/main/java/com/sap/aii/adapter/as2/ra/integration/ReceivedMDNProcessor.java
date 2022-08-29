package com.sap.aii.adapter.as2.ra.integration;

import javax.servlet.http.HttpServletRequest;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.adapter.as2.ra.api.options.MDNOptions;
import com.sap.aii.adapter.as2.ra.api.pdu.mdn.ReceivedMachineReadablePart;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class ReceivedMDNProcessor {

	@Trace
	public void processFromServlet(HttpServletRequest request) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AS2","ReceivedMDNProcessor","processFromServlet");
		Weaver.callOriginal();
	}
	
	@Trace
	public void process(ReceivedMachineReadablePart machineReadablePart, MDNOptions options, Channel channel, MessageKey messageKey, String messageMIC) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AS2","ReceivedMDNProcessor","processFromServlet");
		Weaver.callOriginal();
		
	}
}
