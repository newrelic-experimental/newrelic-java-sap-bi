package com.sap.aii.adapter.file;

import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.sdk.xi.lang.Binary;
import com.sap.aii.af.sdk.xi.mo.Message;

@Weave(type=MatchType.Interface)
public abstract class AdapterReceiver {

	@Trace(dispatcher=true)
	public Binary dispatchMessage(Message message) {
		NewRelic.getAgent().getLogger().log(Level.FINE, new Exception("call to dispatchMessage"), "Dispatching message: {0}", message);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","File","AdapterReceiver",getClass().getSimpleName(),"dispatchMessage");
		
		return Weaver.callOriginal();
	}
}
