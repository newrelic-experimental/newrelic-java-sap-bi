package com.newrelic.instrumentation.labs.sap.tracemessage.processing2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.newrelic.api.agent.NewRelic;
import com.sap.it.op.mpl.trace.TraceMessage;

public class TraceMessageUtils {

	private static Set<String> archiveMsgClasses = new HashSet<>();
	private static Set<String> traceWriteClasses = new HashSet<>();
	
	private static void addArchiveMessageClass(String classname) {
		if(!archiveMsgClasses.contains(classname)) {
			archiveMsgClasses.add(classname);
			HashMap<String, String> attributes = new HashMap<>();
			attributes.put("ArchiveMsgServiceClass", classname);
			attributes.put("MessageSourceType", TraceMessageSource.ArchiveMessageWriteService.name());
			NewRelic.getAgent().getInsights().recordCustomEvent("SAPTraceMessageSource", attributes);
		}
	}
	
	private static void addTraceWriteMessageClass(String classname) {
		if(!traceWriteClasses.contains(classname)) {
			traceWriteClasses.add(classname);
			HashMap<String, String> attributes = new HashMap<>();
			attributes.put("TraceWriteService", classname);
			attributes.put("MessageSourceType", TraceMessageSource.TraceWriteService.name());
			NewRelic.getAgent().getInsights().recordCustomEvent("SAPTraceMessageSource", attributes);
		}
	}
	
	private static void addTraceMessageSource(TraceMessageSource source, String clazz) {
		if(source.equals(TraceMessageSource.ArchiveMessageWriteService)) {
			addArchiveMessageClass(clazz);
		} else {
			addTraceWriteMessageClass(clazz);
		}
	}
	
	public static void logTraceMessage(TraceMessageSource source, String clazz, TraceMessage traceMsg) {
		addTraceMessageSource(source,clazz);
		TraceMessageLogger.log(traceMsg);
	}
}
