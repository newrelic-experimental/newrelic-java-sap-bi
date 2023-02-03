package com.sap.engine.interfaces.messaging.spi;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;


@Weave(type=MatchType.BaseClass)
public abstract class EventHandler {

	@Trace(dispatcher=true)
	public void onSend(Services services, TransportableMessage tMessage) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","EventHandler",getClass().getSimpleName(),"onSend");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportableMessage onCall(Services services, TransportableMessage tMessage) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","EventHandler",getClass().getSimpleName(),"onCall");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void onDeliver(Services services, TransportableMessage tMessage, long timeOfLastDelivery, int deliveryCounter) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","EventHandler",getClass().getSimpleName(),"onDeliver");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void onReceive(Services services, TransportableMessage tMessage) throws MessagingException {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","EventHandler",getClass().getSimpleName(),"onReceive");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportableMessage onRequest(Services services, TransportableMessage tMessage) throws MessagingException {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","EventHandler",getClass().getSimpleName(),"onRequest");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void onTransmit(Services services, TransportableMessage tMessage) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","EventHandler",getClass().getSimpleName(),"onTransmit");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void onError(Services services, TransportableMessage messageInError, MessagingException exception) {
		NewRelic.noticeError(exception);
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","EventHandler",getClass().getSimpleName(),"onError");
		Weaver.callOriginal();
	}

}
