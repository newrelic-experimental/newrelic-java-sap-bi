package com.newrelic.instrumentation.labs.sap.stats;

import java.util.ArrayList;
import java.util.HashMap;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.statistic.IMeasuringPoint;
import com.sap.aii.af.service.statistic.IPerformanceCollectorData;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class MessageStats  {

	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(HashMap<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		reportObject(attributes, "Agent-InstanceName", instanceId);
	}

	public static void reportPerformanceCollectorData(IPerformanceCollectorData data, String source) {
		Insights insights = NewRelic.getAgent().getInsights();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		reportObject(attributes, "Source", source);
		reportObject(attributes,"ActionName", data.getActionName());
		reportObject(attributes, "ActionType", data.getActionType());
		reportObject(attributes, "AllInOneObjectID", data.getAllInOneObjectID());
		reportObject(attributes,"CreationTimestamp", data.getCreationTimestamp());
		reportObject(attributes,"DeliverySemantics", data.getDeliverySemantics());
		reportObject(attributes,"FromPartyName", data.getFromPartyName());
		reportObject(attributes,"FromServiceName", data.getFromServiceName());

		MessageKey messageKey = data.getMessageKey();
		if(messageKey != null) {
			reportObject(attributes,"MessageKey-ID", messageKey.getMessageId());
			reportObject(attributes,"MessageKey-Direction", messageKey.getDirection());
		}
		reportObject(attributes,"MessageSize", data.getMessageSize());
		reportObject(attributes,"ReceiverAction", data.getReceiverActionName());
		reportObject(attributes,"ReceiverActionType", data.getReceiverActionType());
		reportObject(attributes,"RefToMessageID", data.getRefToMessageID());
		reportObject(attributes,"SentReceiveTimestamp", data.getSentReceiveTimestamp());
		reportObject(attributes,"TransDeliveryTimestamp", data.getTransDeliveryTimestamp());
		reportObject(attributes,"RetryCounter", data.getRetryCounter());
		reportObject(attributes,"ToPartyName", data.getToPartyName());
		reportObject(attributes,"ToServiceName", data.getToServiceName());
		reportObject(attributes,"IsLoopback", data.isLoopback());
		reportObject(attributes,"IsSyncResponse", data.isSyncResponse());
		reportObject(attributes, "isTimePeriodCalculationFinalize", data.isTimePeriodCalculationFinalize());
		if(data.hasIMeasuringPoints()) {
			ArrayList<IMeasuringPoint> measuringPts = data.getMeasuringPoints();
			int size = measuringPts.size();
			reportObject(attributes,"NumberMeasuringPoints", size);
			double avgTime = 0D;
			for(IMeasuringPoint measuringPt : measuringPts) {
				avgTime += measuringPt.getAvgTimePeriod();
			}
			double totalProcessing = avgTime * measuringPts.size();
			double avgProcessing = size > 0 ? totalProcessing/size : 0D;
			reportObject(attributes,"TotalProcessingTime", totalProcessing);
			reportObject(attributes,"AverageProcessingTime", avgProcessing);

		}
		addInstanceName(attributes);
		insights.recordCustomEvent("PerformanceCollectorData", attributes);
	}

	private static void reportObject(HashMap<String, Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}

}
