package com.nr.instrumentation.sap.stats;

import java.util.ArrayList;
import java.util.HashMap;

import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.statistic.IMeasuringPoint;
import com.sap.aii.af.service.statistic.IPerformanceCollectorData;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class MessageStats  {


	public static void reportPerformanceCollectorData(IPerformanceCollectorData data) {
		Insights insights = NewRelic.getAgent().getInsights();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		reportObject(attributes,"ActionName", data.getActionName());
		reportObject(attributes,"ActionType", data.getActionType());
		reportObject(attributes,"AllInOneObjectID", data.getAllInOneObjectID());
		reportObject(attributes,"CreationTimestamp", data.getCreationTimestamp());
		reportObject(attributes,"DeliverySemantics", data.getDeliverySemantics());
		reportObject(attributes,"FromPartyName", data.getFromPartyName());
		reportObject(attributes,"FromServiceName", data.getFromServiceName());
		reportObject(attributes,"IcoScenarioIdentifier", data.getIcoScenarioIdentifier());

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
		insights.recordCustomEvent("PerformanceCollectorData", attributes);
	}

	private static void reportObject(HashMap<String, Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}

}
