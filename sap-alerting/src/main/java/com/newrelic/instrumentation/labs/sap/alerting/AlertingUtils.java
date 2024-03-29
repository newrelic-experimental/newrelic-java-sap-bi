package com.newrelic.instrumentation.labs.sap.alerting;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.alerting.ErrorInfo;

public class AlertingUtils {

	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(Map<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		reportValue(attributes, "Agent-InstanceName", instanceId);
	}
	

	public static void reportAlert(ErrorInfo info) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
//		reportValue(attributes, "AdapterNamespace", info.getAdapterNamespace());
		reportValue(attributes, "AdapterType", info.getAdapterType());
		reportValue(attributes, "Channel", info.getChannel());
		reportValue(attributes, "ChannelParty", info.getChannelParty());
		reportValue(attributes, "ChannelService", info.getChannelService());
		reportValue(attributes, "CreateTime", info.getCreateTime());
		reportValue(attributes, "ErrorCategory", info.getErrorCategory());
		reportValue(attributes, "ErrorCode", info.getErrorCode());
		reportValue(attributes, "ErrorText", info.getErrorText());
		reportValue(attributes, "FromParty", info.getFromParty());
		reportValue(attributes, "FromService", info.getFromService());
		reportValue(attributes, "InterfaceName", info.getInterfaceName());
//		reportValue(attributes, "InterfaceNamespace", info.getInterfaceNamespace());
		reportValue(attributes, "MessageId", info.getMessageId());
		reportValue(attributes, "ScenarioId", info.getScenarioId());
		reportValue(attributes, "ToParty", info.getToParty());
		reportValue(attributes, "ToService", info.getToService());
		addInstanceName(attributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("SAPAlert", attributes);
	}
	
	private static void reportValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
