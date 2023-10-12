package com.sap.aii.af.service.alerting;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.alerting.AlertingUtils;

@Weave(type=MatchType.Interface)
public abstract class AlertConnection {

	public void send(ErrorInfo info) {
		AlertingUtils.reportAlert(info);
		Weaver.callOriginal();
	}
}
