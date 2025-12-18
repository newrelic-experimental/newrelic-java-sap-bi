package com.newrelic.instrumentation.labs.sap.jmx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import com.newrelic.agent.HarvestListener;
import com.newrelic.agent.HarvestService;
import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.org.json.simple.JSONArray;
import com.newrelic.agent.deps.org.json.simple.JSONObject;
import com.newrelic.agent.deps.org.json.simple.parser.JSONParser;
import com.newrelic.agent.deps.org.json.simple.parser.ParseException;
import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.agent.stats.StatsEngine;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.services.jmx.MBeanServerConnectionImpl;

public class SAPJMXHarvester extends Thread implements HarvestListener, AgentConfigListener {

	private static SAPJMXHarvester instance = null;
	public static boolean initilized = false;
	private static int frequency = 1;

	private static List<MBeanServer> mBeanServers = new ArrayList<MBeanServer>();
	private static List<MBeanCollectInfo> collections = new ArrayList<SAPJMXHarvester.MBeanCollectInfo>();
	private static List<Pattern> perfBeans = new ArrayList<Pattern>();
	private static final String LOOPFREQ = "SAP.JMX.frequency";
	private static final String COLLECTTHREADS = "SAP.JMX.collectThreads";
	private static File configFile = null;
	private static boolean perfBeansDumped = false;

	private static boolean collectIndivdualThreadInfo = true;

	private int loopCounter = 0;

	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(Map<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		if(instanceId != null && !instanceId.isEmpty()) {
			attributes.put("AgentInstanceId", instanceId);
		}
	}
	

	static {
		processConfiguration();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ConfigChecker(), 1L, 1L, TimeUnit.MINUTES);
	}

	private static void processConfiguration() {
		try {
			if(configFile == null) {
				File agentDirectory = ConfigFileHelper.getNewRelicDirectory();
				configFile = new File(agentDirectory, "sap-jmx.json");
			}
			if(configFile == null || !configFile.exists()) {
				return;
			}
			NewRelic.getAgent().getLogger().log(Level.FINE, "Reading JMX configuration from {0}", configFile.getPath());
			JSONParser parser = new JSONParser();
			FileReader fReader = new FileReader(configFile);
			Object obj = parser.parse(fReader);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Read from sap-jmx.json: {0}", obj);
			// Clear any previously configured item
			collections.clear();
			perfBeans.clear();

			if(obj instanceof JSONObject) {
				JSONObject json = (JSONObject)obj;
				JSONArray jsonArray = (JSONArray) json.get("mbeans");
				NewRelic.getAgent().getLogger().log(Level.FINE, "Configuration contains {0} MBean configs", jsonArray.size());
				if(jsonArray != null) {
					for(int i=0;i<jsonArray.size();i++) {
						JSONObject mbeanJson = (JSONObject)jsonArray.get(i);
						if(mbeanJson != null) {
							MBeanCollectInfo info = new MBeanCollectInfo();
							String domain = (String) mbeanJson.get("domain");
							NewRelic.getAgent().getLogger().log(Level.FINE, "Got domain: {0}", domain);
							if(domain != null) info.domain = domain;
							String objName = (String)mbeanJson.get("objectName");
							NewRelic.getAgent().getLogger().log(Level.FINE, "Got ObjectName: {0}", objName);
							if(objName != null) info.objectName = objName;
							JSONArray attrArray = (JSONArray)mbeanJson.get("attributes");
							if (attrArray != null) {
								List<String> attrs = new ArrayList<String>();
								for (int j = 0; j < attrArray.size(); j++) {
									String attrToAdd = (String) attrArray.get(j);
									attrs.add(attrToAdd);
									NewRelic.getAgent().getLogger().log(Level.FINE, "Added attribute to list: {0}",
											attrToAdd);
								}
								info.attributes = attrs;
							}
							attrArray = (JSONArray)mbeanJson.get("operations");
							if (attrArray != null) {
								List<String> attrs = new ArrayList<String>();
								for (int j = 0; j < attrArray.size(); j++) {
									String attrToAdd = (String) attrArray.get(j);
									attrs.add(attrToAdd);
									NewRelic.getAgent().getLogger().log(Level.FINE, "Added operation to list: {0}",
											attrToAdd);
								}
								info.operations = attrs;
							}

							collections.add(info);
						}
					}
				}

				JSONArray performanceBeans = (JSONArray) json.get("performanceBeans");
				if(performanceBeans != null) {
					for(int i=0;i<performanceBeans.size();i++) {
						String regex = (String)performanceBeans.get(i);
						if(regex.contains("\"")) {
							regex = regex.replace("\"", "");
						}
						perfBeans.add(Pattern.compile(regex));
						NewRelic.getAgent().getLogger().log(Level.FINE, "Will collect Performance monitors that match {0}", regex);
					}
				}
			}
		} catch (FileNotFoundException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to open reader for file {0}","sap-jmx.json");
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to open reader for file {0}","sap-jmx.json");
		} catch (ParseException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to parse JSON from file");
		}

	}

	public static boolean isInitialized() {
		return instance != null;
	}

	protected static SAPJMXHarvester getInstance() {
		if(instance == null) {
			instance = new SAPJMXHarvester();
		}
		return instance;
	}

	public static void addMBeanServer(MBeanServer mbs) {
		if(!mBeanServers.contains(mbs)) {
			mBeanServers.add(mbs);
		}
	}

	public static void init(MBeanServerConnectionImpl conn) {
		initilized = true;
		if(instance == null) instance = new SAPJMXHarvester();
		Config config = NewRelic.getAgent().getConfig();
		ServiceFactory.getConfigService().addIAgentConfigListener(instance);
		instance.checkConfig(config);
		conn.getMBeanCount();
		instance.start();
	}

	private SAPJMXHarvester() {

	}

	@Override
	public void run() {

		HarvestService harvestService = ServiceFactory.getHarvestService();
		while(harvestService == null) {
			try {
				sleep(5000L);
			} catch (InterruptedException e) {
			}
			harvestService = ServiceFactory.getHarvestService();			
		}

		harvestService.addHarvestListener(instance);
	}

	@Override
	public void afterHarvest(String arg0) {
		if(!perfBeansDumped) {
			dumpPerformanceBeans();
		}

	}

	private static void dumpPerformanceBeans() {
		HashSet<String> beans = new HashSet<String>();
		for(MBeanServer mbs : mBeanServers) {
			try {
				ObjectName objName = new ObjectName("*:j2eeType=SAP_MonitorPerNode,*");
				Set<ObjectInstance> mbeans = mbs.queryMBeans(objName , null);
				for(ObjectInstance inst : mbeans) {
					ObjectName instName = inst.getObjectName();

					Hashtable<String, String> propList = instName.getKeyPropertyList();
					String oName = propList.get("name");
					beans.add(oName);
				}
			} catch (MalformedObjectNameException e) {
			}
		}
		if(!beans.isEmpty()) {
			File agentDirectory = ConfigFileHelper.getNewRelicDirectory();
			File output = new File(agentDirectory,"PerformanceBeans.txt");
			try {
				PrintWriter writer = new PrintWriter(output);
				for(String bean : beans) {
					writer.println(bean.replace("\"", ""));
				}
				writer.close();
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to output Performance Beans");
			}
		}
		perfBeansDumped = true;
	}

	@Override
	public void beforeHarvest(String arg0, StatsEngine arg1) {
		// if frequency is zero then JMX is disabled
		if(frequency == 0) return;
		if(loopCounter % frequency != 0) {
			loopCounter++;
			if(loopCounter == 100000) {
				loopCounter = 0;
			}
			return;
		}

		loopCounter++;
		// reset every so often to avoid overflow
		if(loopCounter == 100000) {
			loopCounter = 0;
		}
		for(MBeanServer mbs : mBeanServers) {
			for(MBeanCollectInfo cInfo : collections) {
				String oName = cInfo.objectName;
				try {

					String objNameStr = cInfo.domain != null ? cInfo.domain + ":" + oName : "*:"+oName;
					ObjectName objName = new ObjectName(objNameStr);

					Set<ObjectInstance> mbeans = mbs.queryMBeans(objName, null);

					for(ObjectInstance inst : mbeans) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						ObjectName instName = inst.getObjectName();

						map.put("ObjectName-Domain", instName.getDomain());
						Hashtable<String, String> propList = instName.getKeyPropertyList();
						Set<String> keys = propList.keySet();
						for(String key : keys) {
							String value = propList.get(key).replace("\"", "");
							map.put(key, value);
						}

						boolean reported = false;

						if(cInfo.attributes != null) {
							for(String attr : cInfo.attributes) {
								try {
									Object value = mbs.getAttribute(instName, attr);
									if(value != null) {
										String valueStr = value.toString();
										if(!valueStr.isEmpty()) {
											map.put(attr, value);
										}
										reported = true;
									}
								} catch (Exception e) {
									NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to get value for {0}",attr);
								}
							}
						}

						if(reported) {
							addInstanceName(map);
							NewRelic.getAgent().getInsights().recordCustomEvent("SAPJMX", map);
						}
					}

				} catch (MalformedObjectNameException e) {
					NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to create ObjectName for  {0}",oName);
				}

			}

			try {
				ObjectName objName = new ObjectName("*:j2eeType=SAP_MonitorPerNode,*");
				Set<ObjectInstance> mbeans = mbs.queryMBeans(objName , null);
				for(ObjectInstance inst : mbeans) {
					try {
						ObjectName instName = inst.getObjectName();

						MBeanInfo mBeanInfo = mbs.getMBeanInfo(instName);
						MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
						boolean found = false;
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("ObjectName-Domain", instName.getDomain());
						Hashtable<String, String> propList = instName.getKeyPropertyList();
						String oName = propList.get("name").replace("\"", "");
						boolean skip = true;
						for(int j=0;j<perfBeans.size() && skip; j++) {
							Pattern beanName = perfBeans.get(j);
							boolean match = beanName.matcher(oName).matches();
							if(match) {
								skip = false;
							}
						}
						if(skip) continue;
						Set<String> keys = propList.keySet();
						for(String key : keys) {
							String value = propList.get(key).replace("\"", "");
							map.put(key, value);
						}
						for(int i=0;i<attributes.length; i++) {
							try {
								String name = attributes[i].getName();
								if("Value".equals(name)) {
									Object value = mbs.getAttribute(instName, "Value");
									if(value != null) {
										map.put("Value", value);
										found = true;
									}
								}
								if("value".equals(name)) {
									Object value = mbs.getAttribute(instName, "value");
									if(value != null) {
										map.put("value", value);
										found = true;
									}
								}
								if("AverageQualityRate".equals(name)) {
									Object value = mbs.getAttribute(instName, "AverageQualityRate");
									if(value != null) {
										map.put("AverageQualityRate", value);
										found = true;
									}
								}
								if("CurrentQualityRate".equals(name)) {
									Object value = mbs.getAttribute(instName, "CurrentQualityRate");
									if(value != null) {
										map.put("CurrentQualityRate", value);
										found = true;
									}
								}
								if("CurrentQualityRate".equals(name)) {
									Object value = mbs.getAttribute(instName, "CurrentQualityRate");
									if(value != null) {
										map.put("CurrentQualityRate", value);
										found = true;
									}
								}
								if("TotalTries".equals(name)) {
									Object value = mbs.getAttribute(instName, "TotalTries");
									if(value != null) {
										map.put("TotalTries", value);
										found = true;
									}
								}
								if("SuccessfulTries".equals(name)) {
									Object value = mbs.getAttribute(instName, "SuccessfulTries");
									if(value != null) {
										map.put("SuccessfulTries", value);
										found = true;
									}
								}
								if("AverageDuration".equals(name)) {
									Object value = mbs.getAttribute(instName, "AverageDuration");
									if(value != null) {
										map.put("AverageDuration", value);
										found = true;
									}
								}
								if("CurrentDuration".equals(name)) {
									Object value = mbs.getAttribute(instName, "CurrentDuration");
									if(value != null) {
										map.put("CurrentDuration", value);
										found = true;
									}
								}
								if("TotalTime".equals(name)) {
									Object value = mbs.getAttribute(instName, "TotalTime");
									if(value != null) {
										map.put("TotalTime", value);
										found = true;
									}
								}
								if("TotalNumber".equals(name)) {
									Object value = mbs.getAttribute(instName, "TotalNumber");
									if(value != null) {
										map.put("TotalNumber", value);
										found = true;
									}
								}
								if("Availability".equals(name)) {
									Object value = mbs.getAttribute(instName, "Availability");
									if(value != null) {
										map.put("Availability", value);
										found = true;
									}
								}
								if("TimeOnTrueInMilliseconds".equals(name)) {
									Object value = mbs.getAttribute(instName, "TimeOnTrueInMilliseconds");
									if(value != null) {
										map.put("TimeOnTrueInMilliseconds", value);
										found = true;
									}
								}
								if("TimeOnFalseInMilliseconds".equals(name)) {
									Object value = mbs.getAttribute(instName, "TimeOnFalseInMilliseconds");
									if(value != null) {
										map.put("TimeOnFalseInMilliseconds", value);
										found = true;
									}
								}
								if("Frequency".equals(name)) {
									Object value = mbs.getAttribute(instName, "Frequency");
									if(value != null) {
										map.put("Frequency", value);
										found = true;
									}
								}
							} catch (Exception e) {
							}



						}

						if(found) {
							NewRelic.getAgent().getInsights().recordCustomEvent("SAPJMX", map);
						}
					} catch (Exception e) {
					}

				}
			} catch (MalformedObjectNameException e) {
			}


			collectThreadInfo(mbs);
			collectSessions(mbs);
			collectOSStats(mbs);
		}

	}

	private void collectOSStats(MBeanServer mbs) {
		try {
			ObjectName objName = new ObjectName("*:name=OSMonitorsMBean,*");
			Set<ObjectInstance> mbeans = mbs.queryMBeans(objName , null);
			for(ObjectInstance inst : mbeans) {
				HashMap<String,Object> attrs = new HashMap<String, Object>();
				boolean collected = false;

				Integer cpuUtil = (Integer)mbs.invoke(inst.getObjectName(), "cpuUtilization", null, null);
				if(cpuUtil != null) {
					collected = true;
					attrs.put("CPUUtilization", cpuUtil);
				}

				Integer memUtil = (Integer)mbs.invoke(inst.getObjectName(), "memoryUtilization", null, null);
				if(memUtil != null) {
					collected = true;
					attrs.put("MemoryUtilization", memUtil);
				}
				if(collected) {
					addInstanceName(attrs);
					NewRelic.getAgent().getInsights().recordCustomEvent("OSUtilization", attrs);
				}
			}

		} catch(Exception e) {

		}
	}

	private void collectSessions(MBeanServer mbs) {

		try {
			ObjectName objName = new ObjectName("*:name=session_mgmt_monitor_sessions,*");
			Set<ObjectInstance> mbeans = mbs.queryMBeans(objName , null);
			for(ObjectInstance inst : mbeans) {
				HashMap<String,Object> attrs = new HashMap<String, Object>();
				boolean collected = false;

				Integer users = (Integer) mbs.invoke(inst.getObjectName(), "logged_in_users", null, null);
				if(users != null) {
					attrs.put("LoggedInUsers", users);
					collected = true;
				}

				Integer webSessions = (Integer) mbs.invoke(inst.getObjectName(), "active_web_sessions", null, null);
				if(webSessions != null) {
					attrs.put("ActiveWebSessions", webSessions);
					collected = true;
				}

				webSessions = (Integer) mbs.invoke(inst.getObjectName(), "open_web_sessions", null, null);
				if(webSessions != null) {
					attrs.put("OpenWebSessions", webSessions);
					collected = true;
				}

				Integer ejbSessions = (Integer) mbs.invoke(inst.getObjectName(), "open_ejb_sessions", null, null);
				if(ejbSessions != null) {
					attrs.put("OpenEjbSessions", ejbSessions);
					collected = true;
				}
				if(collected) {
					addInstanceName(attrs);
					NewRelic.getAgent().getInsights().recordCustomEvent("SessionMonitoring", attrs);
				}
			}

		} catch(Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "failed to get session data");

		}
	}

	private void collectThreadInfo(MBeanServer mbs) {

		try {
			ObjectName objName = new ObjectName("*:name=ThreadsBean,*");
			Set<ObjectInstance> mbeans = mbs.queryMBeans(objName , null);
			NewRelic.getAgent().getLogger().log(Level.FINE,"ThreadsBean query returned {0} instances",mbeans.size());
			for(ObjectInstance inst : mbeans) {
				HashMap<String,Object> attrs = new HashMap<String, Object>();
				Integer threads = (Integer)mbs.invoke(inst.getObjectName(), "getThreadCount", null, null);
				putValue(attrs, "ThreadCount", threads);
				Long currentCPUTime = (Long)mbs.invoke(inst.getObjectName(), "getCurrentThreadCpuTime", null, null);
				putValue(attrs, "CurrentCPUTime", currentCPUTime);
				Long userCPUTime = (Long)mbs.invoke(inst.getObjectName(), "getCurrentThreadCpuTime", null, null);
				putValue(attrs, "CurrentUserCPUTime", userCPUTime);
				Integer daemonThreadCount = (Integer)mbs.invoke(inst.getObjectName(), "getDaemonThreadCount", null, null);
				putValue(attrs, "DaemonThreadCount", daemonThreadCount);

				long[] deadlocked = (long[])mbs.invoke(inst.getObjectName(), "findMonitorDeadlockedThreads", null, null);
				putValue(attrs, "Deadlocked", deadlocked == null ? 0 : deadlocked.length);

				if(deadlocked != null && deadlocked.length > 0) {
					int threadCount = 1;
					for(long id : deadlocked) {
						CompositeData data = (CompositeData)mbs.invoke(inst.getObjectName(), "getThreadInfo", new Object[] {id}, new String[] {"long"});
						putValue(attrs,"DeadlockedThread-Name-"+threadCount,data.get("threadName"));
						putValue(attrs,"DeadlockedThread-ID-"+threadCount,data.get("threadId"));
						putValue(attrs,"DeadlockedThread-State-"+threadCount,data.get("threadState"));
					}
				}

				addInstanceName(attrs);
				NewRelic.getAgent().getInsights().recordCustomEvent("SAPThreads", attrs);
				if (collectIndivdualThreadInfo) {
					long[] ids = (long[]) mbs.invoke(inst.getObjectName(), "getAllThreadIds", null, null);
					if (ids.length > 0) {

						for (long id : ids) {
							HashMap<String, Object> attributes = new HashMap<String, Object>();

							boolean report = false;
							Object[] params = { id };
							CompositeData result = (CompositeData) mbs.invoke(inst.getObjectName(), "getThreadInfo",
									params, new String[] { "long" });

							// Check if result is null before accessing (thread may have terminated)
							if (result == null) {
								NewRelic.getAgent().getLogger().log(Level.FINEST,
									"getThreadInfo returned null for thread ID: " + id + " (thread may have terminated)");
								continue; // Skip this thread and move to next
							}

							Long value = (Long) result.get("blockedCount");
							if (value != null && value > 0) {
								report = true;
							}
							putValue(attributes, "Blocked Count", value);
							value = (Long) result.get("blockedTime");
							if (value != null && value > 0) {
								report = true;
							}
							putValue(attributes, "Blocked Time", value);
							Boolean b = (Boolean) result.get("suspended");
							if (b != null && b) {
								report = true;
							}
							putValue(attributes, "Suspended", b);
							putValue(attributes, "Thread ID", result.get("threadId"));
							putValue(attributes, "Thread Name", result.get("threadName"));
							String threadState = (String) result.get("threadState");
							if (threadState != null && threadState.toLowerCase().contains("block")) {
								report = true;
							}
							putValue(attributes, "Thread State", result.get("threadState"));

							putValue(attributes, "Wait Count", result.get("waitedCount"));
							putValue(attributes, "Wait Time", result.get("waitedTime"));

							Long cpuTime = (Long) mbs.invoke(inst.getObjectName(), "getThreadCpuTime", params,
									new String[] { "long" });
							putValue(attributes, "ThreadCPUTime", cpuTime);
							cpuTime = (Long) mbs.invoke(inst.getObjectName(), "getThreadUserTime", params,
									new String[] { "long" });
							putValue(attributes, "ThreadUserTime", cpuTime);

							if (report) {
								addInstanceName(attrs);
								NewRelic.getAgent().getInsights().recordCustomEvent("SAPThread", attributes);
							}
						}
					} 
				}
			}
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to invoke getThreadInfo");
		}
	}

	private static void putValue(HashMap<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty()) {
			if(value != null) {
				attributes.put(key, value);
			}
		}
	}

	protected void stopHarvest() {
		HarvestService harvestService = ServiceFactory.getHarvestService();
		while(harvestService == null) {
			try {
				sleep(5000L);
			} catch (InterruptedException e) {
			}
			harvestService = ServiceFactory.getHarvestService();			
		}

		harvestService.removeHarvestListener(instance);
	}

	static class MBeanCollectInfo {
		@Override
		public String toString() {
			return "MBeanCollectInfo [objectName=" + objectName + ", domain=" + domain + ", attributes=" + attributes
					+ ", operations=" + operations + ", getThreadInfo=" + getThreadInfo + "]";
		}
		protected String objectName;
		protected String domain;
		protected List<String> attributes;
		protected List<String> operations;
		protected boolean getThreadInfo = false;


	}

	private void checkConfig(Config config) {
		Object value = config.getValue(LOOPFREQ);
		if(value instanceof Number) {
			Number n = (Number)value;
			int freq = n.intValue();
			if(frequency != freq) {
				frequency = freq;
				NewRelic.getAgent().getLogger().log(Level.FINE, "SAP JMX metrics will be collected every {0} minutes", frequency);
			}
		} else if(value instanceof String) {
			String s = (String)value;
			try {
				Integer i = Integer.parseInt(s);
				frequency = i;
				NewRelic.getAgent().getLogger().log(Level.FINE, "SAP JMX metrics will be collected every {0} minutes", frequency);
			} catch (NumberFormatException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Unable to parse integer frequency from string {0}", s);
			}
		}

		value = config.getValue(COLLECTTHREADS);
		if(value instanceof Boolean) {
			collectIndivdualThreadInfo = (Boolean)value;
			if(collectIndivdualThreadInfo) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "SAP JMX will start collecting individual thread info");
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "SAP JMX will not collect individual thread info");
			}
		} else if(value instanceof String) {
			try {
				Boolean b = Boolean.parseBoolean((String)value);
				collectIndivdualThreadInfo = b;
				if(collectIndivdualThreadInfo) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "SAP JMX will start collecting individual thread info");
				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE, "SAP JMX will not collect individual thread info");
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void configChanged(String category, AgentConfig config) {
		checkConfig(config);
	}

	private static class ConfigChecker implements Runnable {

		private long lastCheck;

		private ConfigChecker() {
			lastCheck = System.currentTimeMillis();
		}

		@Override
		public void run() {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Running check on modifications to sap-jmx.json");
			long current = System.currentTimeMillis();
			if(configFile == null) {
				File agentDirectory = ConfigFileHelper.getNewRelicDirectory();
				configFile = new File(agentDirectory, "sap-jmx.json");
			}
			if(configFile == null || !configFile.exists()) {
				return;
			}
			long lastMod = configFile.lastModified();
			if(lastMod > lastCheck) {
				processConfiguration();
			}
			lastCheck = current;
		}

	}

}
