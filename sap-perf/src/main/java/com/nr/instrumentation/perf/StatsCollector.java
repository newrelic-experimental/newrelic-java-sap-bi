package com.nr.instrumentation.perf;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.management.ObjectName;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.statistic.ApplicationManager;
import com.sap.aii.af.service.statistic.IApplication;
import com.sap.aii.af.service.statistic.IApplicationFactory;
import com.sap.aii.af.service.statistic.IPeriod;
import com.sap.aii.af.service.statistic.IProfile;
import com.sap.aii.af.service.statistic.IProfileFactory;
import com.sap.aii.af.service.statistic.Interval;
import com.sap.aii.af.service.statistic.ProfileException;
import com.sap.aii.af.service.statistic.ProfileManager;
import com.sap.aii.af.service.statistic.ProfileRuntime;
import com.sap.aii.af.service.statistic.UIPeriod;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIAggrEntity;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIAggregation;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXILMSLocale;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIMinMaxAttr;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIOpStatus;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIPerfData;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIPerfDataType;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIPerfOvData;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXIPerfOvType;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.compositedata.SAP_ITSAMXISrchCriteria;
import com.sap.aii.mdt.itsam.mbeans.performanceMonitor.util.XIPerfMonUtil;
import com.sap.aii.rwb.agent.client.EJBAgent;
import com.sap.aii.rwb.core.XIAdapterFramework;
import com.sap.aii.rwb.core.XIComponent;
import com.sap.aii.rwb.core.XIDomain;
import com.sap.aii.rwb.exceptions.BuildLandscapeException;
import com.sap.aii.rwb.util.RWBApplication;
import com.sap.aii.utilxi.sld.SubSystemFactory;

public class StatsCollector implements Runnable {

	public static boolean intialized = false;
	private static Locale locale = null;

	public XIDomain xiDomain;
	public ArrayList<ObjectName> allXIComponents;
	public IProfile performanceProfile;
	public ArrayList<UIPeriod> timePeriods;
	private String thisComponentName = null;
	private Timestamp start = null;
	private static HashMap<String,String> hashesToName = new HashMap<String, String>();
	private static List<String> appKeys = new ArrayList<String>();

	public static void initialize() {
		intialized = true;
		locale = Locale.getDefault();
		StatsCollector collector = new StatsCollector();
		collector.start = new Timestamp(System.currentTimeMillis()-4L*60*1000);

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(collector,1, 5, TimeUnit.MINUTES);

		checkProfiles();

	}

	private static void checkProfiles() {
		try {
			IProfile[] activeProfiles = ProfileManager.getActiveIProfiles();
			if(activeProfiles != null) {

				for(IProfile iProfile : activeProfiles) {
					byte[] profileHash = iProfile.getHash();
					String profileHashInHex = HexUtil.encode(profileHash);
					if(!hashesToName.containsKey(profileHashInHex)) {
						String pName = iProfile.getName();
						IApplication app = iProfile.getIApplication();
						hashesToName.put(profileHashInHex, pName);
						HashMap<String, Object> attributes = new HashMap<String, Object>();
						putObject(attributes, "ProfileHash", profileHash);
						putObject(attributes, "ProfileHashAsString", new String(profileHash));
						putObject(attributes, "ProfileHashAsHex", HexUtil.encode(profileHash));
						List<String> rtKeys = iProfile.getRuntimeKeys();
						int keyCount = 1;
						for(String rtKey : rtKeys) {
							putObject(attributes, "Runtime-Key-"+keyCount, rtKey);
							keyCount++;
						}

						rtKeys = iProfile.getRuntimeValueKeys();
						keyCount = 1;
						for(String rtKey : rtKeys) {
							putObject(attributes, "Runtime-KeyValue-"+keyCount, rtKey);
							keyCount++;
						}

						putObject(attributes, "Name", pName);
						putObject(attributes, "App-name", app.getName());
						putObject(attributes, "App-key", app.getKey());
						putObject(attributes, "App-Namespace", app.getNamespace());
						putObject(attributes, "App-prefix", app.getPrefix());
						NewRelic.getAgent().getInsights().recordCustomEvent("ActiveProfiles", attributes);
					}
				}
			}



			IApplication[] regApps = IApplicationFactory.getInstance().getRegisteredIApplications();
			if(regApps != null) {
				for(IApplication regApp : regApps) {
					if(!appKeys.contains(regApp.getKey())) {
						appKeys.add(regApp.getKey());
						HashMap<String, Object> attributes = new HashMap<String, Object>();
						putObject(attributes, "AppName", regApp.getName());
						putObject(attributes, "AppNameSpace", regApp.getNamespace());
						putObject(attributes, "AppKey", regApp.getKey());
						putObject(attributes, "AppPrefix", regApp.getPrefix());

						IProfile[] profiles = IProfileFactory.getInstance().getRegisteredIProfiles(regApp);
						putObject(attributes, "NumberOfProfiles", profiles != null ? profiles.length : 0);
						if(profiles != null) {
							int count = 1;
							for(IProfile iProfile : profiles) {
								putObject(attributes, "Profile-"+count+"-Name", iProfile.getName());
								putObject(attributes, "Profile-"+count+"-Hash", iProfile.getHash());
								putObject(attributes, "Profile-"+count+"-HashStr", new String(iProfile.getHash()));
								putObject(attributes, "Profile-"+count+"-HashHex", HexUtil.encode(iProfile.getHash()));
								List<String> rtKeys = iProfile.getRuntimeKeys();
								int keyCount = 1;
								for(String rtKey : rtKeys) {
									putObject(attributes, "Runtime-Key-"+keyCount, rtKey);
									keyCount++;
								}

								rtKeys = iProfile.getRuntimeValueKeys();
								keyCount = 1;
								for(String rtKey : rtKeys) {
									putObject(attributes, "Runtime-KeyValue-"+keyCount, rtKey);
									keyCount++;
								}
								count++;
							}
						}
						NewRelic.getAgent().getInsights().recordCustomEvent("RegisteredApps", attributes);
					}
				}
			}
		} catch (ProfileException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to active profiles");
		}

	}

	public void doInitialization() throws BuildLandscapeException, ProfileException {

		allXIComponents = new ArrayList<ObjectName>();
		EJBAgent sldAgent = null; 
		try {
			sldAgent = new EJBAgent();
			xiDomain = sldAgent.getXIDomain(RWBApplication.getLocalDomain());
			XIComponent[] xiComponents = xiDomain.getSubComponents();
			for (int i = 0; i < xiComponents.length; ++i) {
				final XIComponent component = xiComponents[i];
				if (component instanceof XIAdapterFramework
						&& !allXIComponents.contains(component.getName())) {
					allXIComponents.add(component.getName());
				}
			}
		} catch (BuildLandscapeException e1) {
			try {
				allXIComponents = new ArrayList<ObjectName>();
				SubSystemFactory ssf = new SubSystemFactory();
				com.sap.aii.utilxi.sld.XIAdapterFramework sldAdapterEngine = (com.sap.aii.utilxi.sld.XIAdapterFramework) ssf
						.createXISubSystemFromAiiProps(com.sap.aii.utilxi.sld.XIAdapterFramework.APP_ID);
				com.sap.aii.utilxi.sld.XIDomain domain = ssf.createXIDomainFromAiiProps(false);
				XIAdapterFramework af = new XIAdapterFramework( sldAdapterEngine);
				allXIComponents.add(af.getName());
				xiDomain = new XIDomain(domain.getSldInstanceName());
				xiDomain.addXiAdapterEngine(af);


			} catch (Exception e) {
				throw e1;
			}
		} finally {
		}

		if(allXIComponents != null && !allXIComponents.isEmpty()) {
			thisComponentName = allXIComponents.get(0).getCanonicalName();
		}


		IApplication performanceApplication = ApplicationManager.getInstance()
				.getIApplication(IApplicationFactory.XPI_PERFORMANCE_STATISTIC);
		IProfile[] performanceProfiles = ProfileManager.getIProfiles(performanceApplication, true);

		performanceProfile = performanceProfiles[0];
		IPeriod[] periods = performanceProfile.getIPeriods();
		Timestamp begin = performanceProfile.getActivationTimestamp();
		if (begin == null) {
			begin = new Timestamp(0L);
		}
		final Timestamp end = new Timestamp(System.currentTimeMillis());
		final UIPeriod[] uiPeriods = ProfileRuntime.getUIPeriods(periods, begin, end, locale);
		timePeriods = new ArrayList<UIPeriod>(uiPeriods.length);
		if (uiPeriods != null) {
			for (int i = 0; i < uiPeriods.length; ++i) {
				final UIPeriod currPeriod = uiPeriods[i];
				Interval[] intervals = currPeriod.getIntervals();
				if(intervals != null && intervals.length > 0)
					timePeriods.add(currPeriod);
			}
		}
		start = end;
		checkProfiles();
	}


	public void run() {
		try {
			doInitialization();

			SAP_ITSAMXILMSLocale locale = new SAP_ITSAMXILMSLocale();
			locale.setCountry("us");
			locale.setVariant(TimeZone.getDefault().getDisplayName());

			XIPerfMonUtil util = new XIPerfMonUtil();

			Calendar cal = Calendar.getInstance();
			Date end = cal.getTime();
			cal.add(Calendar.HOUR, -1);

			for (UIPeriod uiPeriod : timePeriods) {
				HashMap<String, Object> attributes = new HashMap<String, Object>();
				attributes.put("UIPeriod-Begin", uiPeriod.getBeginTimestamp());
				attributes.put("UIPeriod-End", uiPeriod.getEndTimestamp());

				com.sap.aii.af.service.statistic.PeriodType uPeriodtype = uiPeriod.getIPeriod().getType();
				attributes.put("UIPeriod-PeriodType", uPeriodtype.getPeriodTypeName());
				SAP_ITSAMXIPerfOvType AggrInterval = new SAP_ITSAMXIPerfOvType();
				AggrInterval.setAggregationInterval(uPeriodtype.getPeriodTypeName());
				Interval activeInterval = uiPeriod.getActiveInterval();
				boolean getStats = false;

				if(activeInterval != null) {
					Timestamp toTime = new Timestamp(activeInterval.getEndTimestamp().getTime());
					Timestamp fromTime = new Timestamp(activeInterval.getBeginTimestamp().getTime());
					AggrInterval.setToTime(toTime);
					AggrInterval.setFromTime(fromTime);
					getStats = true;
				} else {
					Interval[] intervals = uiPeriod.getIntervals(); 
					if(intervals != null) {
						for(Interval interval : intervals) {
							if(interval.getBeginTimestamp().after(start) || interval.getEndTimestamp().before(end)) {
								AggrInterval.setAggrFromTime(interval.getBeginTimestamp());
								AggrInterval.setAggrToTime(interval.getEndTimestamp());
								getStats = true;
							}
						}
					}
				}
				putObject(attributes,"AggregatedInterval", AggrInterval);
				if(getStats) {

					SAP_ITSAMXISrchCriteria searchCriteria = new SAP_ITSAMXISrchCriteria();
					searchCriteria.setFromDate(cal.getTime());
					searchCriteria.setToDate(end);
					putObject(attributes,"SearchCriteria", searchCriteria);

					SAP_ITSAMXIAggregation entries = util.RetreiveAggrEntitiesForAggrInterval(AggrInterval , locale, searchCriteria, thisComponentName);
					putObject(attributes,"SAP_ITSAMXIAggregation", entries != null);

					List<String> hashes = new ArrayList<String>();

					if(entries != null) {
						SAP_ITSAMXIOpStatus aggrResult = entries.getAggrOperationResult();
						if(aggrResult != null) {
							boolean b = aggrResult.getStatus();
							putObject(attributes,"SAP_ITSAMXIAggregation-Status", b);
						}
						SAP_ITSAMXIAggrEntity[] entityArray = entries.getAggregationEntityArray();
						int length = entityArray != null ? entityArray.length : 0;
						putObject(attributes,"SAP_ITSAMXIAggregation-Entities", length);

						if(entityArray != null) {
							for(SAP_ITSAMXIAggrEntity entity : entityArray) {
								reportSAP_ITSAMXIAggrEntity(entity);
								hashes.add(entity.getHashCode());
							} 
						}
					}

					SAP_ITSAMXIPerfOvData perfOvData = util.RetreivePerfOverviewData(searchCriteria, 0, System.currentTimeMillis(), locale, thisComponentName);
					putObject(attributes,"SAP_ITSAMXIPerfOvData", perfOvData != null);

					if(perfOvData != null) {
						SAP_ITSAMXIOpStatus aggregOverview = perfOvData.getAggregatedOperationResult();
						putObject(attributes, "SAP_ITSAMXIPerfOvData-Status", aggregOverview.getStatus());

						SAP_ITSAMXIPerfOvType[] perfOverview = perfOvData.getPerformanceOverview();
						putObject(attributes, "SAP_ITSAMXIPerfOvData-PerfOverviews", perfOverview != null ? perfOverview.length : 0);
						if (perfOverview != null) {
							for (SAP_ITSAMXIPerfOvType data : perfOverview) {
								reportSAP_ITSAMXIPerfOvType(data);
							} 
						}
						for(String AggrEntityHashCode : hashes) {
							HashMap<String, Object> attributes3 = new HashMap<String, Object>();

							SAP_ITSAMXIPerfOvType perfData = new SAP_ITSAMXIPerfOvType();
							perfData.setAggregationInterval(uPeriodtype.getPeriodTypeName());
							perfData.setToTime(uiPeriod.getEndTimestamp());
							perfData.setFromTime(uiPeriod.getBeginTimestamp());
							String applicationName = hashesToName.get(AggrEntityHashCode);
							if(applicationName == null) {
								applicationName = AggrEntityHashCode;
							}
							putObject(attributes3, "Hash", AggrEntityHashCode);
							putObject(attributes3,"Application",hashesToName.get(AggrEntityHashCode));
							putObject(attributes3, "PerfOverviewType", perfData);

							SAP_ITSAMXIPerfData moduleData = util.RetrivePerformanceModuleData(AggrEntityHashCode, perfData, locale, thisComponentName);
							putObject(attributes3, "ModuleData", moduleData != null);
							if (moduleData != null) {
								SAP_ITSAMXIOpStatus aggrResult = moduleData.getAggregatedOpResult();
								putObject(attributes3, "AggregationStatus", aggrResult.getStatus());
								SAP_ITSAMXIPerfDataType[] perfData2 = moduleData.getPerformanceData();
								int length = perfData2 != null ? perfData2.length : 0;
								putObject(attributes, "ModuleData-PerfData", length);
								for(SAP_ITSAMXIPerfDataType dataType : perfData2) {
									reportSAP_ITSAMXIPerfDataType(dataType, applicationName);
								}
								SAP_ITSAMXIMinMaxAttr retryDetails = moduleData.getRetryDetails();
								putObject(attributes3, "RetryAvgValue", retryDetails.getAvgValue());
								putObject(attributes3, "RetryMaxValue", retryDetails.getMaxValue());
								putObject(attributes3, "RetryMinValue", retryDetails.getMinValue());
								SAP_ITSAMXIMinMaxAttr sizeDetails = moduleData.getSizeDetails();
								putObject(attributes3, "SizeAvgValue", sizeDetails.getAvgValue());
								putObject(attributes3, "SizeMaxValue", sizeDetails.getMaxValue());
								putObject(attributes3, "SizeMinValue", sizeDetails.getMinValue());
							}
							NewRelic.getAgent().getInsights().recordCustomEvent("SAPModuleData", attributes3);
						}
					}
				}
				NewRelic.getAgent().getInsights().recordCustomEvent("PeriodPerformance", attributes);
			}


		} catch(Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Error gettting perf data");
		}
	}

	private static void reportSAP_ITSAMXIPerfDataType(SAP_ITSAMXIPerfDataType data, String appName ) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		String moduleName = data.getModuleName();
		putObject(attributes, "ApplicationName", appName);
		putObject(attributes,"ModuleName",moduleName);
		putObject(attributes, "ModuleSeqNumber", data.getModuleSequenceNr());
		SAP_ITSAMXIMinMaxAttr timeSpent = data.getTimeSpentInModule();
		putObject(attributes,"TimeSpentAvg", timeSpent.getAvgValue());
		putObject(attributes, "TimeSpentMax", timeSpent.getMaxValue());
		putObject(attributes, "TimeSpentMin", timeSpent.getMinValue());

		NewRelic.getAgent().getInsights().recordCustomEvent("SAP_ITSAMXIPerfDataType", attributes);
	}

	private static void reportSAP_ITSAMXIPerfOvType(SAP_ITSAMXIPerfOvType data) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		putObject(attributes, "AggregationInterval", data.getAggregationInterval());
		putObject(attributes, "AggrFromTime", data.getAggrFromTime());
		putObject(attributes, "AggrToTime", data.getAggrToTime());
		putObject(attributes, "AvgProcTime", data.getAvgProcTime());
		putObject(attributes, "FromTime", data.getFromTime());
		putObject(attributes, "FromTimeDisp", data.getFromTimeDisp());
		putObject(attributes, "InternalToDate", data.getInternalToDate());
		putObject(attributes, "ModuleCount", data.getModuleCount());
		putObject(attributes, "ProcTime", data.getProcTime());
		putObject(attributes, "TimeFrame", data.getTimeFrame());
		putObject(attributes, "TotalMsgCount", data.getTotalMsgCount());
		putObject(attributes, "TotalRetry", data.getTotalRetry());
		putObject(attributes, "TotalSize", data.getTotalSize());
		putObject(attributes, "ToTime", data.getToTime());
		putObject(attributes, "ToTimeDisp", data.getToTimeDisp());
		NewRelic.getAgent().getInsights().recordCustomEvent("SAP_ITSAMXIPerfOvType", attributes);
	}

	private static void reportSAP_ITSAMXIAggrEntity(SAP_ITSAMXIAggrEntity status) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		putObject(attributes, "AvgMessageSize", status.getAvgMsgSize());
		putObject(attributes, "AvgProcessTime", status.getAvgPrTime());
		putObject(attributes, "Direction", status.getDirection());
		String hashCode = status.getHashCode();
		putObject(attributes, "HashCode", hashCode);
		putObject(attributes,"ProfileName",hashesToName.get(hashCode));

		putObject(attributes, "InboundChannel", status.getInboundChannel());
		putObject(attributes, "Interface", status.getInterface());
		putObject(attributes, "InterfaceNameSpace", status.getInterfaceNameSpace());
		putObject(attributes, "MaxMsgSize", status.getMaxMsgSize());
		putObject(attributes, "MinMsgSize", status.getMinMsgSize());
		putObject(attributes, "MsgCountForAggrEntity", status.getMsgCountForAggrEntity());
		putObject(attributes, "OutboundChannel", status.getOutboundChannel());
		putObject(attributes, "QualityOfService", status.getQualityOfService());
		putObject(attributes, "ReceiverParty", status.getReceiverParty());
		putObject(attributes, "ReceiverService", status.getReceiverService());
		putObject(attributes, "ScenarioIdentifier", status.getScenarioIdentifier());
		putObject(attributes, "SenderParty", status.getSenderParty());
		putObject(attributes, "SenderService", status.getSenderService());
		putObject(attributes, "ServerNode", status.getServerNode());
		putObject(attributes, "TotalMsgSize", status.getTotMsgSize());
		putObject(attributes, "TotalProcessingTime", status.getTotPrTime());

		SAP_ITSAMXIMinMaxAttr retryDetails = status.getRetryDetails();
		if(retryDetails != null) {
			putObject(attributes, "AvgRetryValue", retryDetails.getAvgValue());
			putObject(attributes, "MaxRetryValue", retryDetails.getMaxValue());
			putObject(attributes, "MinRetryValue", retryDetails.getMinValue());
		}


		NewRelic.getAgent().getInsights().recordCustomEvent("AggregPerformanceData", attributes);
	}

	private static void putObject(Map<String,Object> map, String key, Object value) {
		if(value != null) {
			map.put(key, value);
		}
	}
}
