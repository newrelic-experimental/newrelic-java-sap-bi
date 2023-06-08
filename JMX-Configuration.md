### SAP JMX Configuration
  
The New Relic SAP instrumentation provides the ability to collect JMX metrics from your SAP instance.
It can be configured via newrelic.yml and by including a sap-jmx.json configuration file.  
  
## Configuration via newrelic.yml

There are two configuration items that can be configured in newrelic.ynl.  The first is the frequency at which the metrics are collected (in number of minutes).  The second is whether to collect individual thread information.  Please note that these two items are dynamic so they can be changed without having to restart the application.  The values should change within one minute of saving newrelic.yml.   
  
# Frequency  
The configuration item is an integer and represents the number of minutes between collection of JMX metrics.  The default is 1 minute.  To disable collection set the value to 0.   
To configure the agent to collect the metrics at an interval other than the current period or to disable collection add the following if it is not already present otherwise change the value:  
  SAP:  
    JMX:  
      frequency: 2  
    
# Thread JMX Information  
Individual Thread information will be collected on any thread that meets any of the follow conditions:  
blockCount is positive   
blockTime is positive  
suspended is true   
thread state is block  
  
By default, collection of the individual threads is enabled.  To disable it or to reenable it use the following configuration:  
  SAP:
    JMX:  
      collectThreads: false   
  
# Note
The two above items use a common stanza so if you configure both items they will use a common SAP and JMX stanza:  
  SAP:
    JMX:
      frequency: 3
      collectThreads: false
  
## JMX MBeanCollection Configuration  
The instrumentation will monitor the New Relic Agent directory for a file named sap-jmx.json and will proecess it if present.   It checks every minute to see if the file has been modified or added and prcesses it if it has.  A sample JSON file can be found here:  
  
# Configuration  
The configuration consists of a single JSON element that contains a JSON array of MBean Objects.   
Each MBean object consists of the following attributes:   
domain - MBean domain which is typically 'com.sap.default'.    Can be omitted.
objectName - the object name of the MBean to collect.  Can be a regular expression.  Typically can just use the name attribute of the object name followed by ",*".
attributes - JSON array of string values which represent the names of the attributes to collect.
  
# Notes  
The conifguration is dynamic so you can change the configuration and the collected MBeans and attributes will change within a minute or two.   
