package com.sap.aii.adapter.as2.ra.api;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.as2.AS2Utils;
import com.sap.aii.adapter.as2.ra.api.pdu.AS2Message;
import com.sap.aii.adapter.as2.ra.api.pdu.mdn.ReceivedMessageDispositionNotification;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public class Deliverer {

	@Trace
	public ReceivedMessageDispositionNotification deliver(AS2Message as2Message, Channel channel, MessageKey amk, Message msg) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AS2Utils.addChannel(attributes, channel);
		AS2Utils.addMessageKey(attributes, amk);
		AS2Utils.addMessage(attributes, msg);
		AS2Utils.addAS2Message(attributes, as2Message);
		traced.addCustomAttributes(attributes);
		traced.setMetricName("Custom","SAP","AS2","Deliverer","deliver");
		
		return Weaver.callOriginal();
	}

	@Trace
	public void deliverMDN(Map<String, String> requestProperties, byte[] mdn, String dispositionType,
			String mdnErrorInfo, URL url, String messsageId, Channel channel,
			boolean basicAuthentication, String authUser, String authPassword, boolean proxy,
			String proxyHost, Integer proxyPort, boolean proxyBasicAuth, String proxyUser,
			String proxyPassword, boolean errorDuringAcceptance) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AS2Utils.addValue(attributes,"MessageID",messsageId);
		AS2Utils.addChannel(attributes, channel);
		AS2Utils.addValue(attributes, "URL", url);
		AS2Utils.addValue(attributes, "AuthUser", authUser);
		AS2Utils.addValue(attributes, "ProxyHost", proxyHost);
		AS2Utils.addValue(attributes, "ProxyPort", proxyPort);
		AS2Utils.addValue(attributes, "ProxyUser", proxyUser);
		traced.addCustomAttributes(attributes);
		traced.setMetricName("Custom","SAP","AS2","Deliverer","deliverMDN");
		
		Weaver.callOriginal();
	}
}
