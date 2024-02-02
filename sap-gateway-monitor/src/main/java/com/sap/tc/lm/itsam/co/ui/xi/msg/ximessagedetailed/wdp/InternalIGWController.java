package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;
import com.sap.tc.webdynpro.progmodel.api.IWDCustomEvent;

@Weave
public abstract class InternalIGWController {
	
	@WeaveAllConstructors
	public InternalIGWController() {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}
		GatewayMonitor.addInternalIGWController(this);
	}

	public abstract IPublicIGWController.IContextNode wdGetContext();
	
	@Trace
	public Object wdInvokeEventHandler(String handlerName, IWDCustomEvent event)  {
		return Weaver.callOriginal();
	}
	
	@Trace
	public void readAttachmentData() {
		Weaver.callOriginal();
	}

}
