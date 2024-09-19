package com.sap.it.op.agent.trace;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.tracemessage.processing.TraceMessageSource;
import com.newrelic.instrumentation.labs.sap.tracemessage.processing.TraceMessageUtils;
import com.sap.it.op.mpl.trace.TraceMessage;

@Weave(type = MatchType.Interface)
public abstract class TraceWriteService {

	@Trace
	public String store(TraceMessage traceMsg) {
		TraceMessageUtils.logTraceMessage(TraceMessageSource.TraceWriteService, getClass().getName(), traceMsg);
		return Weaver.callOriginal();
	}
}
