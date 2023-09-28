package com.newrelic.instrumentation.labs.sap.channel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelFilter;
import com.sap.engine.services.jmx.MBeanServerConnectionImpl;

public class JMXDumper extends Thread {

	private static JMXDumper INSTANCE = null;

	public static boolean initilized = false;
	
	private static boolean found = false;

	private static List<MBeanServer> mBeanServers = new ArrayList<>();
	
	private static List<String> reported = new ArrayList<>();
	
	private static final String filename = "channel-jmx.out";
	
	private static ObjectInstance adapterChannelService  = null;
	
	private static boolean checkInitialized = false;
	

	public static void addMBeanServer(MBeanServer mbs) {
		if(!mBeanServers.contains(mbs)) {
			mBeanServers.add(mbs);
		}
	}
	
	public static void init(MBeanServerConnectionImpl conn) {
		if(INSTANCE == null) {
			INSTANCE = new JMXDumper();
		}
		conn.getMBeanCount();
		INSTANCE.start();
		initilized = true;
	}

	@Override
	public void run() {
		while(!found) {

			File agentDirectory = ConfigFileHelper.getNewRelicDirectory();
			File outputFile = new File(agentDirectory, filename);

			try {
				FileWriter fileWriter = new FileWriter(outputFile, true);
				PrintWriter writer = new PrintWriter(fileWriter);
				

				for(MBeanServer mbs : mBeanServers) {
					
					Set<ObjectInstance> mbeans = mbs.queryMBeans(null, null);
					for(ObjectInstance objInst : mbeans) {
						ObjectName objectName = objInst.getObjectName();
						try {
							MBeanInfo info = mbs.getMBeanInfo(objectName);
							String classname = info.getClassName();
							if(classname.toLowerCase().contains("adapterchannel")) {
								if (!reported.contains(classname)) {
									if(classname.equals("SAP_ITSAMXIAdapterChannelService")) {
										adapterChannelService = objInst;
										RunCheck checker = new RunCheck(adapterChannelService);
										Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(checker, 1L, 3L, TimeUnit.MINUTES);
									}
									writer.println("MBeanName: " + classname);
									writer.println("\tCanonical Name: " + objectName.getCanonicalName());
									Hashtable<String, String> props = objectName.getKeyPropertyList();
									if(props != null && !props.isEmpty()) {
										Set<String> keys = props.keySet();
										if(keys != null && !keys.isEmpty()) {
											writer.println("\tProperties: ");
											for(String key : keys) {
												String propValue  = props.get(key);
												writer.println("\t\tName - " + key + ", value - " + propValue);
											}
										} else {
											writer.println("\tProperties: ");
										}
									} else {
										writer.println("\tProperties: ");
									}
									MBeanAttributeInfo[] attributeInfos = info.getAttributes();
									writer.println("Attributes:");
									for (MBeanAttributeInfo aInfo : attributeInfos) {
										String attrName = aInfo.getName();
										Object value = mbs.getAttribute(objectName, attrName);
										
										writer.println("\tAttribute: name - " + attrName + ", type - " + aInfo.getType() + ", value: "+value);  
									}
									MBeanOperationInfo[] mbeanOperations = info.getOperations();
									writer.println("Operations:");
									for (MBeanOperationInfo opInfo : mbeanOperations) {
										writer.println("\tOperation: name - " + opInfo.getName() + ", description - "
												+ opInfo.getDescription() + ", return type: " + opInfo.getReturnType());
									}
									MBeanNotificationInfo[] mbeanNotifications = info.getNotifications();
									for (MBeanNotificationInfo nInfo : mbeanNotifications) {
										writer.print("\tNotification: " + nInfo.getName() + ", description - "
												+ nInfo.getDescription() + ", ");
										String[] notifyTypes = nInfo.getNotifTypes();
										writer.print(", Notify Types: ");
										if (notifyTypes != null && notifyTypes.length > 0) {
											int length = notifyTypes.length;
											for (int i = 0; i < length; i++) {
												writer.print(notifyTypes[i]);
												if (i < length - 1) {
													writer.print(',');
												}
											}
											writer.println();
										} else {
											writer.println("No notify types");
										}
									} 
									writer.flush();
									reported.add(classname);
								}
							}
						} catch (Exception e) {
							NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to retrieve MBean Info for objectname {0}", objectName);
						}
					}
				}
				writer.close();
			} catch (IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to open output file: {0}",outputFile);
				return;
			}
			
			try {
				Thread.sleep(10000L);
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "JMX Checker thread interruppted");
			}
		}
	}
	
	private static class RunCheck implements Runnable {
		
		private ObjectInstance instance = null;
		
		public RunCheck(ObjectInstance inst) {
			instance = inst;
		}
		
		public void run() {
			if(instance != null) {
				int count = 1;
				for(MBeanServer mbs : mBeanServers) {
					ObjectName objName = instance.getObjectName();
					Set<ObjectName> objInstances = mbs.queryNames(objName, null);
					int objCount = 1;
					for(ObjectName inst : objInstances) {
						
						try {
							Object[] params = {};
							String[] signature = {};
							Object result = mbs.invoke(inst, "RetrieveTotalChannelCount", params, signature);
							if(result != null) {
								NewRelic.getAgent().getLogger().log(Level.FINE, "Retrieved Channel count of {0}", result);
							}
							
							SAP_ITSAMXIAdapterChannelFilter filter = new SAP_ITSAMXIAdapterChannelFilter();
							Locale locale = Locale.getDefault();
							String localeStr = locale.getLanguage();
							params = new Object[] {filter,localeStr};
							signature = new String[] {"com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelFilter","java.lang.String"};
							
							result = mbs.invoke(inst, "RetrieveChannelList", params, signature);
							if(result != null) {
								NewRelic.getAgent().getLogger().log(Level.FINE, "Retrieved Channel count of {0}", result);
							}
							
							
						} catch (Exception e) {
							NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to invoke RetrieveTotalChannelCount");
						}
					}
				}
			}
		}
	}
}
