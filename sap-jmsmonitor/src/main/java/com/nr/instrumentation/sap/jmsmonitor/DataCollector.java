package com.nr.instrumentation.sap.jmsmonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

import com.newrelic.agent.HarvestListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.agent.stats.StatsEngine;
import com.newrelic.api.agent.NewRelic;
import com.sap.jms.configuration.JMSServerInstanceMBean;
import com.sap.jms.server.dc.ConfigurationAdapter;
import com.sap.jms.server.dc.MessageMonitoringFacade;
import com.sap.jms.server.dc.ServiceEnvironment;
import com.sap.jms.server.dc.cache.CacheInfo;
import com.sap.jms.server.dc.consumer.DestinationManager;

public class DataCollector implements HarvestListener {

	private static final Map<MessageMonitoringFacade,ServiceEnvironment> jmsMonitors = new HashMap<MessageMonitoringFacade, ServiceEnvironment>();
	
	public static boolean initialized = false;
	
	public static void addMessageMonitoringFacade(MessageMonitoringFacade facade, ServiceEnvironment env) {
		Set<MessageMonitoringFacade> keys = jmsMonitors.keySet();
		boolean add = true;
		for(MessageMonitoringFacade monitor : keys) {
			ServiceEnvironment environ = jmsMonitors.get(monitor);
			JMSServerInstanceMBean monitorServer = environ.getServerInstance();
			JMSServerInstanceMBean facadeServer = env.getServerInstance();
			try {
				String monitorInst = monitorServer.getInstanceName();
				String facadeInst = facadeServer.getInstanceName();
				if(monitorInst != null && facadeInst != null) {
					if(monitorInst.equalsIgnoreCase(facadeInst)) {
						add = false;
					}
				}
			} catch (JMSException e) {
			}
		}
		if(add) {
			jmsMonitors.put(facade,env);
		}
	}
	
	public static void init() {
		initialized = true;
		ServiceFactory.getHarvestService().addHarvestListener(new DataCollector());
	}

	@Override
	public void afterHarvest(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeHarvest(String arg0, StatsEngine arg1) {
		
		Set<MessageMonitoringFacade> keys = jmsMonitors.keySet();
		
		for(MessageMonitoringFacade facade : keys) {
			ServiceEnvironment env = jmsMonitors.get(facade);
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			JMSServerInstanceMBean serverInst = env.getServerInstance();
			String serverName = null;
			if(serverInst != null) {
				try {
					serverName = serverInst.getInstanceName();
					putValue(attributes, "InstanceName", serverName);
				} catch (JMSException e) {
				}
			}
			try {
				putValue(attributes,"CacheCount",facade.getCacheCount());
			} catch (JMSException e1) {
			}
			try {
				putValue(attributes,"CacheSize",facade.getCacheSize());
			} catch (JMSException e1) {
			}
			Map<Integer, CacheInfo> destStats = null;
			
			try {
				destStats = facade.getDestinationStatistics();
			} catch (JMSException e1) {
			}
			
			Set<Integer> activeQueues = env.getDestinationContainer().getActiveQueues();
			putValue(attributes,"ActiveQueues", activeQueues != null ? activeQueues.size() : 0);
			int tmpConsumers = 0;
			int tmpDead = 0;
			long tmpPending = 0;
			long tmpInCache = 0;
			long tmpInDB = 0;
			long tmpSucc = 0;
			long tmpUnNecessary = 0;
			long tmpUnSucc = 0L;
			boolean haveTemp = false;
			ConfigurationAdapter configAdapter = env.getConfigurationAdapter();
		
			for(Integer queue : activeQueues) {
				try {
					DestinationManager destMgr = env.getDestinationContainer().getDestinationManager(queue);
					String destName = configAdapter.getDestinationName(queue);
					HashMap<String, Object> queueattributes = new HashMap<String, Object>();
					putValue(queueattributes,"JMSServer",serverName);
					boolean isTmp = configAdapter.isDestinationTemporary(queue);
					if(isTmp) haveTemp = true;
					if(!isTmp) {
						putValue(queueattributes,"Name", destName);
					}
					if(destStats != null) {
						CacheInfo cInfo = destStats.get(queue);
						if(cInfo != null) {
							putValue(queueattributes,"CacheCount",cInfo.getCacheCount());
							putValue(queueattributes,"CacheSize",cInfo.getCacheSize());
						}
					}
					int consumers = destMgr.getConsumersCount();
					if(isTmp) {
						tmpConsumers += consumers;
					} else {
						putValue(queueattributes,"Consumers", consumers);
					}
					int deadMsgs = facade.getDeadMessagesCount(queue);
					if(isTmp) {
						tmpDead += deadMsgs;
					} else {
						putValue(queueattributes,"DeadMessages", deadMsgs);
					}
					long pending = facade.getPendingMessagesCounter(queue);
					if(isTmp) {
						tmpPending += pending;
					} else {
						putValue(queueattributes,"PendingMessages", pending);
					}
					long inCache = facade.getMessagesCountInCache(queue);
					if(isTmp) {
						tmpInCache += inCache;
					} else {
						putValue(queueattributes,"MessagesCountInCache", inCache);
					}
					long inDB = facade.getMessageCountInDB(queue);
					if(isTmp) {
						tmpInDB  += inDB;
					} else {
						putValue(queueattributes,"MessageCountInDB", inDB);
					}
					long cacheHits = facade.getSuccessfullCacheHits(queue);
					if(isTmp) {
						tmpSucc += cacheHits;
					} else {
						putValue(queueattributes,"SuccessfullCacheHits", cacheHits);
					}
					long unnecc = facade.getUnnecessaryStores(queue);
					if(isTmp) {
						tmpUnNecessary += unnecc;
					} else {
						putValue(queueattributes,"UnnecessaryStores", unnecc);
					}
					long unsucc = facade.getUnsuccessfullCacheHits(queue);
					if(isTmp) {
						tmpUnSucc += unsucc;
					} else {
						putValue(queueattributes,"UnsuccessfullCacheHits", unsucc);
					}
					
					if(!isTmp) {
						NewRelic.getAgent().getInsights().recordCustomEvent("JMSQueue", queueattributes);
					}
					
				} catch (Exception e) {
				}
				
			}
			
			if(haveTemp) {
				HashMap<String, Object> queueattributes = new HashMap<String, Object>();
				putValue(queueattributes,"Name", "TempQueue");
				putValue(queueattributes,"Consumers", tmpConsumers);
				putValue(queueattributes,"DeadMessages", tmpDead);
				putValue(queueattributes,"PendingMessages", tmpPending);
				putValue(queueattributes,"MessagesCountInCache", tmpInCache);
				putValue(queueattributes,"MessageCountInDB", tmpInDB);
				putValue(queueattributes,"SuccessfullCacheHits", tmpSucc);
				putValue(queueattributes,"UnnecessaryStores", tmpUnNecessary);			
				putValue(queueattributes,"UnsuccessfullCacheHits", tmpUnSucc);		
				NewRelic.getAgent().getInsights().recordCustomEvent("JMSQueue", queueattributes);
				tmpConsumers = 0;
				tmpDead = 0;
				tmpPending = 0;
				tmpInCache = 0;
				tmpInDB = 0;
				tmpSucc = 0;
				tmpUnNecessary = 0;
				tmpUnSucc = 0;
				haveTemp = false;
			}

			Set<Integer> activeTopics = env.getDestinationContainer().getActiveTopics();
			putValue(attributes,"ActiveTopics", activeTopics != null ? activeTopics.size() : 0);
			for(Integer topic : activeTopics) {
				try {
					DestinationManager destMgr = env.getDestinationContainer().getDestinationManager(topic);
					HashMap<String, Object> topicattributes = new HashMap<String, Object>();
					putValue(topicattributes,"JMSServer",serverName);
					String destName = configAdapter.getDestinationName(topic);
					boolean isTmp = configAdapter.isDestinationTemporary(topic);
					if(isTmp) haveTemp = true;
					if(!isTmp) {
						putValue(topicattributes,"Name", destName);
					}
					if(destStats != null) {
						CacheInfo cInfo = destStats.get(topic);
						if(cInfo != null) {
							putValue(topicattributes,"CacheCount",cInfo.getCacheCount());
							putValue(topicattributes,"CacheSize",cInfo.getCacheSize());
						}
					}
					int consumers = destMgr.getConsumersCount();
					if(isTmp) {
						tmpConsumers += consumers;
					} else {
						putValue(topicattributes,"Consumers", consumers);
					}
					int deadMsgs = facade.getDeadMessagesCount(topic);
					if(isTmp) {
						tmpDead += deadMsgs;
					} else {
						putValue(topicattributes,"DeadMessages", deadMsgs);
					}
					long pending = facade.getPendingMessagesCounter(topic);
					if(isTmp) {
						tmpPending += pending;
					} else {
						putValue(topicattributes,"PendingMessages", pending);
					}
					long inCache = facade.getMessagesCountInCache(topic);
					if(isTmp) {
						tmpInCache += inCache;
					} else {
						putValue(topicattributes,"MessagesCountInCache", inCache);
					}
					long inDB = facade.getMessageCountInDB(topic);
					if(isTmp) {
						tmpInDB  += inDB;
					} else {
						putValue(topicattributes,"MessageCountInDB", inDB);
					}
					long cacheHits = facade.getSuccessfullCacheHits(topic);
					if(isTmp) {
						tmpSucc += cacheHits;
					} else {
						putValue(topicattributes,"SuccessfullCacheHits", cacheHits);
					}
					long unnecc = facade.getUnnecessaryStores(topic);
					if(isTmp) {
						tmpUnNecessary += unnecc;
					} else {
						putValue(topicattributes,"UnnecessaryStores", unnecc);
					}
					long unsucc = facade.getUnsuccessfullCacheHits(topic);
					if(isTmp) {
						tmpUnSucc += unsucc;
					} else {
						putValue(topicattributes,"UnsuccessfullCacheHits", unsucc);
					}
					
					if(!isTmp) {
						NewRelic.getAgent().getInsights().recordCustomEvent("JMSTopic", topicattributes);
					}
					
				} catch (Exception e) {
				}
				
			}

			if(haveTemp) {
				HashMap<String, Object> topicattributes = new HashMap<String, Object>();
				putValue(topicattributes,"Name", "TempTopic");
				putValue(topicattributes,"Consumers", tmpConsumers);
				putValue(topicattributes,"DeadMessages", tmpDead);
				putValue(topicattributes,"PendingMessages", tmpPending);
				putValue(topicattributes,"MessagesCountInCache", tmpInCache);
				putValue(topicattributes,"MessageCountInDB", tmpInDB);
				putValue(topicattributes,"SuccessfullCacheHits", tmpSucc);
				putValue(topicattributes,"UnnecessaryStores", tmpUnNecessary);			
				putValue(topicattributes,"UnsuccessfullCacheHits", tmpUnSucc);			
				NewRelic.getAgent().getInsights().recordCustomEvent("JMSTopic", topicattributes);
			}
			NewRelic.getAgent().getInsights().recordCustomEvent("JMSMonitor", attributes);
		}
		
	}
	
	private static void putValue(HashMap<String, Object> attributes, String key, Object value) {
		if(value != null) {
			attributes.put(key, value);
		}
	}
}
