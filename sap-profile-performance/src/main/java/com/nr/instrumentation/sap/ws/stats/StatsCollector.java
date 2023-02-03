package com.nr.instrumentation.sap.ws.stats;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.statistic.PeriodType;
import com.sap.aii.af.service.statistic.ws.ProfileProcessorBean;
import com.sap.aii.af.service.statistic.ws.impl.WSApplication;
import com.sap.aii.af.service.statistic.ws.impl.WSData;
import com.sap.aii.af.service.statistic.ws.impl.WSHashPerformanceObject;
import com.sap.aii.af.service.statistic.ws.impl.WSMeasuringPoint;
import com.sap.aii.af.service.statistic.ws.impl.WSPeriod;
import com.sap.aii.af.service.statistic.ws.impl.WSProfile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class StatsCollector implements Runnable {
	
	private static final List<ProfileProcessorBean> processors = new ArrayList<>();

	private static Calendar begin = null;

	private static Calendar end = null;

	static {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Profile StatsCollector initialized at {0}", new Date());
		long startTime = System.currentTimeMillis();
		begin = Calendar.getInstance();
		begin.setTimeInMillis(startTime);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new StatsCollector(), 1L, 5L, TimeUnit.MINUTES);
	}

	public static void addProcessor(ProfileProcessorBean bean) {
		processors.add(bean);
	}

	public void run() {
		HashMap<String, Object> attributes = new HashMap<>();
		WSPeriod period = new WSPeriod();
		period.setPeriodType(PeriodType.MINUTE.getPeriodTypeName());
		period.setBeginPeriod(begin);
		end = Calendar.getInstance();
		period.setEndPeriod(end);
		attributes.put("NumberOfProcessors", Integer.valueOf(processors.size()));
		int count = 1;
		for (ProfileProcessorBean processor : processors) {
			try {
				WSApplication[] apps = processor.getApplications();
				attributes.put("Processor-" + count + "-NumberOfApplications", Integer.valueOf(apps.length));
				int appNum = 1;
				for (WSApplication app : apps) {
					String appKey = app.getApplicationKey();
					attributes.put("Processor-" + count + "-Application-" + appNum + "-AppKey", appKey);
					WSProfile[] profiles = processor.getProfiles(appKey, Boolean.valueOf(true));
					if (profiles != null) {
						attributes.put("Processor-" + count + "-Application-" + appNum + "-NumberOfProfiles",Integer.valueOf(profiles.length));
						int profNum = 1;
						for (WSProfile profile : profiles) {
							attributes.put("Processor-" + count + "-Application-" + appNum + "-Profile-" + profNum,profile.getProfileKey());
							WSData wsData = processor.getWSData(profile, period);
							if (wsData != null) {
								attributes.put("Processor-" + count + "-Application-" + appNum + "-Profile-" + profNum + "-WSData-collected", Boolean.valueOf(true));
								WSHashPerformanceObject[] resultArray = wsData.getWSHashPerformance();
								attributes.put("Processor-" + count + "-Application-" + appNum + "-Profile-" + profNum + "-NumberProfObjs", Integer.valueOf(resultArray.length));
								for (WSHashPerformanceObject perfObj : resultArray)
									reportWSHashPerformanceObject(profile, perfObj);
							} else {
								attributes.put("Processor-" + count + "-Application-" + appNum + "-Profile-" + profNum + "-WSData-collected", Boolean.valueOf(false));
							}
							profNum++;
						} 
					} else {
						attributes.put("Processor-" + count + "-Application-" + appNum + "-NumberOfProfiles",0);
					}
					appNum++;
				} 
			} catch (Exception e) {
				attributes.put("Exception", e.getMessage());
			} 
			count++;
		} 
		begin = Calendar.getInstance();
		begin.setTimeInMillis(end.getTimeInMillis());
		NewRelic.getAgent().getInsights().recordCustomEvent("WSPerfCollection", attributes);
	}

	private static void reportWSHashPerformanceObject(WSProfile profile, WSHashPerformanceObject perfObj) {
		HashMap<String, Object> attributes = new HashMap<>();
		addObject("ApplicationKey", profile.getApplicationKey(), attributes);
		addObject("ProfileKey", profile.getProfileKey(), attributes);
		addObject("AvgRetry", perfObj.getAvgRetry(), attributes);
		addObject("AvgSize", perfObj.getAvgSize(), attributes);
		addObject("Counter", perfObj.getCounter(), attributes);
		addObject("MaxRetry", perfObj.getMaxRetry(), attributes);
		addObject("MinRetry", perfObj.getMinRetry(), attributes);
		addObject("MinSize", perfObj.getMinSize(), attributes);
		addObject("TimePeriodBegin", perfObj.getTimePeriodBegin(), attributes);
		addObject("TimePeriodEnd", perfObj.getTimePeriodEnd(), attributes);
		addObject("TimePeriodType", perfObj.getTimePeriodType(), attributes);
		WSMeasuringPoint[] measurePts = perfObj.getWSMeasuringPoint();
		double avgTime = 0.0D;
		for (WSMeasuringPoint measurePt : measurePts)
			avgTime += measurePt.getAvgValue().longValue(); 
		double totalTime = avgTime * perfObj.getCounter().longValue();
		addObject("TotalProcessingTime", Double.valueOf(totalTime), attributes);
		addObject("AvgProcessingTime", Double.valueOf(totalTime / perfObj.getCounter().longValue()), attributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("WSHashPerformance", attributes);
	}

	private static void addObject(String key, Object value, HashMap<String, Object> attributes) {
		if (key != null && !key.isEmpty() && value != null)
			attributes.put(key, value); 
	}
}
