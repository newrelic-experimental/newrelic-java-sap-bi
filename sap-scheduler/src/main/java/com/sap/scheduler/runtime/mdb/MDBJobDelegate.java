package com.sap.scheduler.runtime.mdb;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

import javax.ejb.MessageDrivenContext;
import javax.jms.Message;

@Weave(type=MatchType.Interface)
public abstract class MDBJobDelegate {

	@Trace
	public void onMessage(Message message, MessageDrivenContext msgContext, MDBJob job) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Scheduler","MDBJobDelegate",getClass().getSimpleName(),"onMessage");
		Weaver.callOriginal();
	}
}
