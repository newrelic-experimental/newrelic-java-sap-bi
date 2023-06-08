### Custom Events for SAP

The SAP instrumentation collects a number of custom events that can help to monitor your SAP instance via NRQL and dashboards.   
  
## Database and Datastore   
  
1. DatabaseStatistics - Database related stats.  Only collected if the database cloeses.  
2. DataStoreStatistics - regularly collected performance metrics on each datasource.   Attribute DataSourceName is the name of the datasource.   
3. QueryStatistics - statistics related to individual queries.  These events are off by default since this will generate a number of events.  See the configuration section on enabling. he attribute SQL is the query that is run.  
4. DMLStatistics - Statistics related to individual queries. These events are off by default due to the high number of events generated. The attribute SQL is the query that is run. See the configuration section on how to enable these statistics.   
   
# Configuration    
To enable Queury and/or DML stats, edit the SAP stanza of newrelic.yml (create if necessary)   
&nbsp;&nbsp;SAP:  
&nbsp;&nbsp;&nbsp;&nbsp;DataMonitor:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Queries:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Report: true
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DML:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Report: true
   
## JMS   
  
1. JMSMonitor - JMS Server level metrics.    
2. JMSQueue - Queue level metrics.   Name attribute is the queue name.   
3. JMSTopic - Topic level metrics.  Name attribute is the topic name.    
  
## Performance   
  
1. SAPModuleData - reports attributes related to each hash.   
2. PeriodPerformance - Reports information about what is collected each period.    
3. SAP_ITSAMXIPerfDataType - module related information.  ApplicationName is the associated hash and ModuleName is the name of the module.
4. AggregPerformanceData - Reports aggregated data for each hash.   
  
## SAP Statistics   
  
1. PerformanceCollectorData - Message level information    
2. MessageStats - Contains information about the message processing.   Information includes the number of messages processed (number in cache), how many messages were skipped because they were reported in a previous cycle and how many messages were reported.    
  
## HTTP   
   
1. WebMonitoring - Reports web application related metrics   
2. HttpMonitoring - Reports HTTP related metrics   
   
## JCO   
   
1. JCoServer - Reports JCO Server related metrics   
2. JCoDestination - Reports JCO Destination related metrics   
3. JCoRepositoryMonitor - Reports JCO Repository related metrics   
   
## JMX   

1. OSUtilization - Reports memory and CPU utilization.   
2. SessionMonitoring - Reports logged in users, active web sessions, open web sessions and open ejb sessions.   
3. SAPThreads - Reports overall Thread status.  
4. SAPThread - Reports individual Thread status.  Only collected if certain values are set.   Reported if blocked count and/or blocked time are positive, the thread is suspended or the thread state is blocked.  Collection can be disabled via configuration.   
5. SAPJMX - see    
   
## Adapter Monitor   
  
1. AuditLog - Provides message key information, text key and auditlogstatus.   
2. ProcessStatus - Provides adapter, channel, message and process information.   
3. ChannelStatus - Provides adapter, channel direction and state, message information.   
4. ChannelsStatus - Aggregated channel status information   
   
## Adapter   
   
1. AdapterMessage - Collects channel information, adapter type, message information, and endpoint information   
  
##  Alerting   
   
1. SAPAlert - Reports information related to values populated in the alert.    



