package com.sap.aii.af.service.statistic.impl;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.service.statistic.IPerformanceCollectorData;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class DataAccess {

	@SuppressWarnings("rawtypes")
	@Trace(dispatcher = true)
	public ArrayList<IPerformanceCollectorData> insertOrUpdateIPerformanceCollectorData(ArrayList performanceCollectorDataArray)  {
		return Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public IPerformanceCollectorData[] selectIPerformanceCollectorData(MessageKey messageKey) {
		return Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public IPerformanceCollectorData[] selectIPerformanceCollectorData(Timestamp begin, Timestamp end, boolean processingTimestamp, boolean ascending) {
		return Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public void deleteIPerformanceCollectorData(MessageKey messageKey) {
		 Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public long deleteIPerformanceCollectorData(Timestamp olderThan, int chunkSize)  {
		return Weaver.callOriginal();
	}
}
