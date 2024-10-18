package com.newrelic.instrumentation.labs.sap.stats;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.statistic.IPerformanceCollectorData;
import com.sap.aii.af.service.statistic.PerformanceDataCache;
import com.sap.aii.af.service.statistic.ProfileException;
import com.sap.aii.af.service.statistic.impl.DataAccess;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class DataQuery implements Runnable {
	
	private static long lastQuery = System.currentTimeMillis();
	public static boolean initialized = false;
	
	public static void init() {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(new DataQuery(), 15L, 15L, TimeUnit.SECONDS);
		initialized = true;
	}
	
	
	public void run() {
		long current = System.currentTimeMillis();
		Timestamp start = new Timestamp(lastQuery);
		Timestamp end = new Timestamp(current);
		try {
			DataAccess dataAccess = DataAccess.getDataAccess();
			IPerformanceCollectorData[] perfData = dataAccess.selectIPerformanceCollectorData(start, end, true, true);
			int length = perfData != null ? perfData.length : 0;
			NewRelic.recordMetric("SAP/PerformanceData/DataQuery/DBEntries",length);
			if(perfData != null && perfData.length > 0) {
				for(IPerformanceCollectorData data : perfData) {
					MessageStats.reportPerformanceCollectorData(data);
				}
			}
			
			lastQuery = end.getTime();
			
			HashMap<MessageKey, IPerformanceCollectorData> perfCache = PerformanceDataCache.getInstance().getCompleteMemoryCache();
			int size = perfCache != null ? perfCache.size() : 0;
			NewRelic.recordMetric("SAP/PerformanceData/DataQuery/PerfCache",size);
			if(perfCache != null && !perfCache.isEmpty()) {
				Set<MessageKey> messageKeys = perfCache.keySet();
				for(MessageKey key : messageKeys) {
					IPerformanceCollectorData data = perfCache.get(key);
					if(data != null) {
						MessageStats.reportPerformanceCollectorData(data);
					}
				}
			}
			
		} catch (ProfileException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Error while collecting PerformanceCollectorData");
		}
		
	}

}
