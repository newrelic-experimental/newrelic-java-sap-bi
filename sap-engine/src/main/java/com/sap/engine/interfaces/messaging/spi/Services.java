package com.sap.engine.interfaces.messaging.spi;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.spi.transport.TransportPackage;

@Weave(type=MatchType.Interface)
public abstract class Services {

	public abstract String getConnectionName();

	@Trace(dispatcher=true)
	public void send(TransportableMessage var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"send");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void store(TransportableMessage var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"store");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void trigger(TransportableMessage var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"trigger");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportableMessage call(TransportableMessage var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"call");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void receive(TransportableMessage var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"receive");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportableMessage request(TransportableMessage var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"request");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void deliver(Message var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"deliver");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void deliver(MessagingException var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"sedelivernd");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportPackage transmit(TransportableMessage var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Services",getClass().getSimpleName(),"transmit");
		return Weaver.callOriginal();
	}

	public abstract String getCurrentUser();
}
