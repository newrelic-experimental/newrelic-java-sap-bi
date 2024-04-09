package com.newrelic.instrumentation.labs.sap.jmxdumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.services.jmx.MBeanServerConnectionImpl;

public class SAPJMXDumper implements Runnable {
	private static SAPJMXDumper instance = null;

	public static boolean initilized = false;

	private static List<MBeanServer> mBeanServers = new ArrayList<>();

	private static final List<String> ignoredOps = new ArrayList<>();
	private static int numberOfRuns = 0;
	private static int maxRuns = 10;
	private static Map<MBeanServer, List<ObjectName>> objectNames = new HashMap<>();
	private static Map<MBeanServer, Integer> objectCounts = new HashMap<>();
	private static ScheduledExecutorService executor = null;

	static {
		ignoredOps.add("start");
		ignoredOps.add("stop");
		ignoredOps.add("Start");
		ignoredOps.add("Stop");
		//    ignoredOps.add("collectAndStoreMonitoringData");
		//    ignoredOps.add("load");
		//    ignoredOps.add("unload");
		//    ignoredOps.add("ApplyChanges");
		//    ignoredOps.add("Restart");
		//    ignoredOps.add("startRecursive");
		//    ignoredOps.add("UpdateProperties");
		//    ignoredOps.add("UpdateTemplateProperties");
		//    ignoredOps.add("obtainBusinessMethodSettings");
		//    ignoredOps.add("GetDefaultSettings");
		//    ignoredOps.add("GetMatchingResourceAdaptersNames");
		//    ignoredOps.add("GetResourceAdapterPropertiesNames");
	}

	private static final List<String> ignoredReturns = new ArrayList<>();

	static {
		ignoredReturns.add("void");
		//   ignoredReturns.add("java.util.Properties");
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	public static boolean isInitialized() {
		return (instance != null);
	}

	protected static SAPJMXDumper getInstance() {
		if (instance == null)
			instance = new SAPJMXDumper(); 
		return instance;
	}

	public static void addMBeanServer(MBeanServer mbs) {
		if (!mBeanServers.contains(mbs))
			mBeanServers.add(mbs); 
	}

	public static void init(MBeanServerConnectionImpl conn) {
		initilized = true;
		if (instance == null)
			instance = new SAPJMXDumper(); 
		try {
			Integer beanCount = conn.getMBeanCount();
			NewRelic.getAgent().getLogger().log(Level.FINE, "MBeanServerConnection {0} has {1} MBeans",conn,beanCount);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE,e, "MBeanServerConnection {0} returned an error while trying to get MBean count");
		}
		executor.schedule(getInstance(), 5L, TimeUnit.MINUTES);
	}

	public void run() {
		numberOfRuns++;
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to SAPJMXHarvester.run - {0}",numberOfRuns);

		try {
			File agentDirectory = ConfigFileHelper.getNewRelicDirectory();
			int serverCount = 1;
			File jmxDumperLog = new File(agentDirectory,"jmxdumper.log");
			FileWriter fWriter = new FileWriter(jmxDumperLog, true);
			PrintWriter logger = new PrintWriter(fWriter);

			logger.println("Initiating MBean Query at "+ new Date());

			for (MBeanServer mbs : mBeanServers) {
				int count = mbs.getMBeanCount();
				NewRelic.getAgent().getLogger().log(Level.FINE, "There are {0} MBeans from {1}",count,mbs);
				List<ObjectName> objNames = objectNames.get(mbs);
				if(objNames == null) {
					objNames = new ArrayList<>();
				}
				Integer c = objectCounts.get(mbs);
				int cachedCount = c != null ? c : 0;
				if(count == cachedCount) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "No change in number of MBeans, skipping the query");
					if(numberOfRuns < maxRuns) {
						executor.schedule(getInstance(), 5L, TimeUnit.MINUTES);
						NewRelic.getAgent().getLogger().log(Level.FINE, "Submit next run from skip at {0}",new Date());
						return;
					}
				}
				objectCounts.put(mbs, count);
				logger.println("Querying MBeanServer "+ mbs);
				File attrOutput = new File(agentDirectory, "Mbean-Attributes" + serverCount + ".out");
				FileWriter attrFileWriter = new FileWriter(attrOutput,true);
				PrintWriter attrWriter = new PrintWriter(attrFileWriter);
				File opsOutput = new File(agentDirectory, "Mbean-Operations" + serverCount + ".out");
				FileWriter opsFileWriter = new FileWriter(opsOutput,true);
				serverCount++;
				PrintWriter opsWriter = new PrintWriter(opsFileWriter);
				Set<ObjectInstance> mbeans = mbs.queryMBeans(null, null);
				logger.println("MBeanServer "+ mbs + " has " + mbeans.size() + "MBeans");
				int attributesAdded = 0;
				int operationsAdded = 0;
				int mbeansAdded = 0;
				for (ObjectInstance objInst : mbeans) {
					try {
						ObjectName objectName = objInst.getObjectName();
						if(objNames.contains(objectName)) {
							continue;
						}
						mbeansAdded++;
						objNames.add(objectName);
						MBeanInfo mbeanInfo = mbs.getMBeanInfo(objectName);
						MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
						int attributeCount = attributes != null ? attributes.length : 0;
						Hashtable<String, String> propList = objectName.getKeyPropertyList();
						Set<String> keys = propList.keySet();
						if (attributes != null && attributes.length > 0) {
							attrWriter.println("ObjectName:");
							for (String key : keys)
								attrWriter.println("\t" + key + ":" + (String)propList.get(key)); 
							attrWriter.println("MBeanClass: " + mbeanInfo.getClassName());
							attrWriter.println("Attributes:");
							for (MBeanAttributeInfo ainfo : attributes) {
								attrWriter.println("\tName: " + ainfo.getName());
								attrWriter.println("\tType: " + ainfo.getType());
							} 
							attrWriter.println();
							attributesAdded++;
						} 
						MBeanOperationInfo[] operations = mbeanInfo.getOperations();
						int operationCount = operations != null ? operations.length : 0;
						logger.println("MBean " + objectName + " has " + attributeCount + " attributes and " + operationCount + " operations");
						if (operations != null && operations.length > 0) {
							int ignored = 0;
							int setMethods = 0;
							int ignoredMethods = 0;
							int ignoredReturnTypes = 0;

							boolean writing = false;
							for (MBeanOperationInfo oInfo : operations) {
								String opName = oInfo.getName();
								if(opName.toLowerCase().startsWith("set")) setMethods++;
								if (!ignoredOps.contains(opName) || opName.toLowerCase().startsWith("set")) {
									String opReturnType = oInfo.getReturnType();
									if (!ignoredReturns.contains(opReturnType)) {
										if (!writing) {
											writing = true;
											opsWriter.println("ObjectName:");
											for (String key : keys)
												opsWriter.println("\t" + key + ":" + (String)propList.get(key)); 
											opsWriter.println("Operations:");
										} 
										opsWriter.println("\tName: " + oInfo.getName());
										opsWriter.println("\tReturnType: " + oInfo.getReturnType());
										MBeanParameterInfo[] parameters = oInfo.getSignature();
										if (parameters != null && parameters.length > 0) {
											opsWriter.println("\tSignature");
											for (MBeanParameterInfo pInfo : parameters) {
												opsWriter.println("\t\tName:" + pInfo.getName());
												opsWriter.println("\t\tType:" + pInfo.getType());
											} 
										} 
									} else {
										ignored++;
										ignoredReturnTypes++;
									}
								} else {
									ignored++;
									ignoredMethods++;
								}
							} 
							if (writing) {
								opsWriter.println(); 
								operationsAdded++;
							}
							logger.println("Ignored " + ignored + " operations of which " + setMethods + " were setter methods, " + ignoredMethods + " were ignored methods, and " + ignoredReturnTypes + " were ignored returns");
						} 
					} catch (Exception e) {
						NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to get info for {0}", objInst);
					}
					logger.flush();
				} 
				attrWriter.close();
				opsWriter.close();
				logger.close();
				NewRelic.getAgent().getLogger().log(Level.FINE, "Collected {0} MBeans from {1}",objNames.size(),mbs);
				objectNames.put(mbs, objNames);
				NewRelic.getAgent().getLogger().log(Level.FINE, "For MBeanServer {0}, added {1} Mbeans, {2} attributes, {3} operations",mbs,mbeansAdded,attributesAdded,operationsAdded);
			} 
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to open file");
		} 
		if(numberOfRuns < maxRuns) {
			Executors.newSingleThreadScheduledExecutor().schedule(getInstance(), 5L, TimeUnit.MINUTES);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Submit next run from end of run at {0}",new Date());
		}
	}
}
