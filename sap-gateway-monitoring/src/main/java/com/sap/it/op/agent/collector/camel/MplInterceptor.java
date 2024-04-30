package com.sap.it.op.agent.collector.camel;

import java.util.HashMap;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.mpl.GatewayUtils;

@Weave
public abstract class MplInterceptor {
	
	@Weave
	static class MplAsyncProcessor {
		
		private final Endpoint ep = Weaver.callOriginal();
		private final String stepId = Weaver.callOriginal();
		String modelStepId = Weaver.callOriginal();
		private final String adapterType = Weaver.callOriginal();

		
		@Trace
		public boolean process(Exchange exchange, AsyncCallback asyncCallback) {
			HashMap<String, Object> attributes = new HashMap<>();
			GatewayUtils.addAttribute(attributes, "AdapterType", adapterType);
			GatewayUtils.addAttribute(attributes, "ModelStepId", modelStepId);
			GatewayUtils.addAttribute(attributes, "StepId", stepId);
			GatewayUtils.addEndpoint(attributes, ep);
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
			return Weaver.callOriginal();
		}
	}

}
