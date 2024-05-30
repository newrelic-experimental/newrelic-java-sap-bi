package com.sap.it.op.agent.collector.camel;

import java.net.URI;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.tracemessage.processing.TraceMessageLogger;
import com.sap.it.op.agent.api.ArchiveMessageWriteService;
import com.sap.it.op.mpl.trace.TraceMessage;

@Weave
public abstract class ArchiveWriter {

	@Trace
	public static URI write(TraceMessage traceMessage, ArchiveMessageWriteService archiveWriteService, String modelStepId) {
		TraceMessageLogger.log(traceMessage, modelStepId);
		return Weaver.callOriginal();
	}
}
