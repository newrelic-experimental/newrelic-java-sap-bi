package com.sap.it.op.agent.collector.camel;

import org.apache.camel.Message;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.tracemessage.processing2.TraceMessageLogger;
import com.sap.it.op.agent.api.Exchange;
import com.sap.it.op.mpl.MessageProcessingLogPart;
import com.sap.it.op.mpl.trace.TraceMessage;

@Weave
public class PayloadMessageBuilder {

	public static TraceMessage process(Exchange exchange, MessageProcessingLogPart mplPart, String tracingStepId,
			Message messageFromExchange, boolean isArchivingActive) {
		
		TraceMessage traceMsg = Weaver.callOriginal();
		if(traceMsg != null) {
			TraceMessageLogger.log(traceMsg);
		}
		return traceMsg;
	}
}
