# SPAN Attributes
The SAP instrumentation will populate the spans/traces in the distributed trace/transaction with various values relative to the method being invoked.   These include things like Message Keys, Message direction, etc.  Most are populated via the instrumentation automatically.  The other way that they can show up is by configuring the agent to collect various module related values related to the processing of a message.  The subsequent sections will detail both of these ways of reporting this data.    
## Module Message Processing  
Via a JSON configuration file, you can configure attributes to collect via both the Module context data and Module supplemental data to spans of the following metric spans:


## Span Data via Instrumentation   
### Data Collected 
| Span (Metric) Name | Module Context | Module Data (Supplemental) |
| ----------- | -------------- | -------------------------- |
| Custom/SAP/Module/***ClassName***/process | X | X |
| Custom/SAP/ModuleLocal/***ClassName***/process | X | X |
| Custom/SAP/ModuleRemote/*ClassName*/process | X | X |
| Reported Fault Errors | X | X |
|Custom/SAP/Adapters/Channel/process_receiver/***AdapterType*** | X | |
|Custom/SAP/Adapters/Channel/process_sender | X | |
| Custom/SAP/Adapters/ModuleProcessorBean/process/***AdapterType*** |  | X |
### Finding Attributes to Report
The instrumentation produces some log files that output the available attributes that can be captured.  These log files are placed in a directory named attributeLogging in the New Relic Java Agent directory (the directory containing newrelic.jar).   It will generate four log files in this directory. Each log file will contain an availble attribute on each line.   Note that currently it does contain duplicates (but usually only two).  
The log files are named:   
context-data-attributes-ejb.log   
context-data-attributes.log   
moduledata-supplemental-attributes-ejb.log   
moduledata-supplemental-attributes.log   

### Configuration
To configure the attributes to collect, create a file named **attribute-config.json** in the New Relic Java Agent directory.  The file should contain a JSON object as described below.   
Note that the configuration file is dynamic and will pick up changes (including deletion or creation of the file) to attribute-config.json.  The changes will take effect within approximately one minute.   
#### Contents
{   
&nbsp;&nbsp;&nbsp;"ModuleContext": {   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"collectChannelId": false,    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"attributesToCollect": [    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"module.key"    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;]    
&nbsp;&nbsp;&nbsp;},   
&nbsp;&nbsp;&nbsp;"ModuleData": {   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"supplementalData": {   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"enabled": true,   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"attributesToCollect": [   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"module.parameters",   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"bindingId"   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;]   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;},   
&nbsp;&nbsp;&nbsp;}   
    
}   

