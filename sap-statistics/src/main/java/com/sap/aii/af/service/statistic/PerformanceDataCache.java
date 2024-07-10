package com.sap.aii.af.service.statistic;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.stats.MessageStats;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class PerformanceDataCache {

	
	public static PerformanceDataCache getInstance() {
		return Weaver.callOriginal();
	}
	
	public abstract HashMap<MessageKey, IPerformanceCollectorData> getCompleteMemoryCache();
	
	protected boolean removeCacheEntry(IPerformanceCollectorData entry) {
		NewRelic.incrementCounter("Custom/PerformanceDataCache/removeCacheEntry/Calls");
		MessageStats.reportPerformanceCollectorData(entry, "PerformanceDataCache-removeCacheEntry");
		return Weaver.callOriginal();
	}
	
	protected void addCacheEntry(IPerformanceCollectorData entry) {
		NewRelic.incrementCounter("Custom/PerformanceDataCache/addCacheEntry/Calls");
		Weaver.callOriginal();
	}
	
	@SuppressWarnings("unused")
	private IPerformanceCollectorData swapIn(MessageKey messageKey) {
		NewRelic.incrementCounter("Custom/PerformanceDataCache/swapIn/Calls");
		IPerformanceCollectorData returned = Weaver.callOriginal();
		MessageStats.reportPerformanceCollectorData(returned, "PerformanceDataCache-swapIn");
		return returned;
	}
	
	@SuppressWarnings("unused")
	private void swapOut() {
		NewRelic.incrementCounter("Custom/PerformanceDataCache/swapOut/Calls");
		Weaver.callOriginal();
	}
}
