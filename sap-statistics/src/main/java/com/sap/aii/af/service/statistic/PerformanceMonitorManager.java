package com.sap.aii.af.service.statistic;

import java.sql.Timestamp;
import java.util.HashMap;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.service.statistic.impl.PerformanceAccumulatorObject;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class PerformanceMonitorManager {
	
	public static PerformanceMonitorManager getInstance() {
		return Weaver.callOriginal();
	}
	
	public static boolean ACTIVE = Weaver.callOriginal();
	
	public abstract HashMap<PerformanceAccumulatorObject, PerformanceAccumulatorObject> getPerformanceAccumulatorMap();
	

	public IPerformanceCollectorData getPerformanceCollectorData(MessageKey messageKey, boolean startImplicit) throws ProfileException {
		IPerformanceCollectorData result = Weaver.callOriginal();
		return result;
	}
	
	public PerformanceAccumulatorObject[] getPerformanceData(PeriodType periodType, Timestamp selectionBeginTime,
			Timestamp selectionEndTime, boolean strictPeriodStartEndCheck)
			throws ProfileException, ClusterCommunicationException {
		PerformanceAccumulatorObject[] result = Weaver.callOriginal();
		return result;
	}
	
	public abstract IPeriod[] getProfilePeriods();
	
	@Trace
	public void start(MessageKey messageKey) {
		Weaver.callOriginal();
	}
	
	@Trace
	public void startAggregator(boolean force, Timestamp startTime, boolean dumpOpenIntervals, boolean skipDataDeletion) {
		Weaver.callOriginal();
	}
	
	@Trace
	public void stop(MessageKey messageKey) {
		Weaver.callOriginal();
	}
	
	@Trace
	public void cancel(MessageKey messageKey) {
		Weaver.callOriginal();
	}
}
