# SPAN Attributes
The SAP instrumentation will populate the spans/traces in the distributed trace/transaction with various values relative to the method being invoked.   These include things like Message Keys, Message direction, etc.  Most are populated via the instrumentation automatically.  The other way that they can show up is by configuring the agent to collect various module related values related to the processing of a message.  The subsequent sections will detail both of these ways of reporting this data.    
## Module Message Processing  
Via a JSON configuration file, you can configure attributes to collect via both the Module context data and Module supplemental data to spans of the following metric spans:

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


## Span Data via Instrumentation   

### Attributes Collected 
| Span/Tracer Name | Attribute or Data Group (**bold**) |
| ---------------- | ---------------------------------- |
| Custom/SAP/AdapterAccess/call/***msgFromService***,***msgInterface*** | MessageFromService, MessageInterface, MessageInterfaceNamespace |
| Custom/SAP/AdapterAccess/execute/***msgFromService***,***msgInterface*** | MessageFromService, MessageInterface, MessageInterfaceNamespace |
| Custom/SAP/AdapterAccess/send/***msgFromService***,***msgInterface*** | MessageFromService, MessageInterface, MessageInterfaceNamespace |
| Custom/SAP/XIAccess/***connection***/call | FromService, InterfaceNamespace, FromParty, ToService, ToParty, Interface |
| Custom/SAP/XIAccess/***connection***/send | FromService, InterfaceNamespace, FromParty, ToService, ToParty, Interface |
| Custom/SAP/AS2/Deliverer/deliver |  **Channel**, **MessageKey**, **Message**, **AS2Message** |
| Custom/SAP/AS2/Deliverer/deliverMDN |  **Channel**, MessageID, URL, AuthUser, ProxyHost, ProxyPort, ProxyUser |
| Custom/SAP/AS2/ChannelDispatcher/receive |  **Channel**, **MessageKey** |
| Custom/SAP/EJB3/***ClassName***/proceedFinal/***ejbclassName***/***ejbmethodName*** | **InstanceIdentity** |
| Custom/SAP/MessageListener/***ClassName***/onMessage | **Message**, **MessageKey** |
| Custom/ProcessingBlock/***ClassName***/process | **Message**, **MessageKey** |
| Custom/Processor/***ClassName***/processMessage | **Message** |
| Custom/SAP/QueueConsumer/***ClassName***/onMessage | **MessageKey**, QueueName |
| Java/com.sap.engine.messaging.impl.core.service.Call/execute | **TransportMessage**, ConnectionName |
| Java/com.sap.engine.messaging.impl.core.service.Receive/execute | **TransportMessage**, ConnectionName |
| Java/com.sap.engine.messaging.impl.core.service.Request/execute | **TransportMessage**, ConnectionName |
| Java/com.sap.engine.messaging.impl.core.service.Store/execute | **TransportMessage**, ConnectionName |
| Java/com.sap.engine.messaging.impl.core.service.Trigger/execute | **TransportMessage**, ConnectionName |
| Custom/SAP/Services/(call, deliver, receive, request, send, store, transmit, trigger) | **TransportMessage**, ConnectionName |
| Custom/File/SapAdapterServiceFrameImpl/callSapAdapter | Channel, **MessageKey**, **Action**, **Party**, **Service** |
| Java/***child class of com.sap.conn.idoc.jco.JCoIDoc***/send | QueueName, TID, **JCODestination** |
| Custom/SAP/JDBC/JDBC2XI/send | **MessageKey** |
| Custom/JDBC/SapAdapterServiceFrameImpl/callSapAdapter | Channel, **MessageKey**, **Action**, **Party**, **Service** |
| Java/com.sap.engine.messaging.impl.api.collector.MessageCollector/onMessage | **QueueMessage** |
| Java/com.sap.engine.messaging.impl.core.MessageController/putMessageInQueue | MessageKey, ConnectionName, MessageType |
| Java/com.sap.engine.messaging.impl.core.MessageController/putMessageInStore | **QueueMessage** |
| Java/com.sap.engine.messaging.impl.core.MessageController/scheduleMessage | MessageKey, ConnectionName, MessageType |
| Custom/SAP/REST/AbstractReceiverChannel/***ClassName***/receive | **MessageKey**, **Action**, **Party**, **Service**,  CorrelationId, Protocol, InterfaceName |
| Custom/SAP/REST/RESTSenderChannel/service/***endpoint*** | Method, Path, RequestURL, Query, Endpoint |
| Custom/Scheduler/JobExecutionRuntimeImpl/executeJob | Job-Name, Job-Node, Job-ID |
| Custom/Scheduler/JobExecutor/onJob | Job-Name, Job-Node, Job-ID |
| Custom/SAP/SFTP/Archiver/archive | **Message**, File, ReturnedFileURL |
| Custom/SAP/SFTP/ArchiverSFTP/archive | **Message**, File, ReturnedFileURL, Home |
| Custom/SAP/SFTP/SSHConnection/send | AbsoluteFilePath, Mode, Host |
| Custom/SAP/SFTP/SSHConnection/copy | SourceFile, Host, DestinationFile |
| Custom/SAP/SFTP/SSHConnection/delete | File, Host |
| Custom/SAP/SFTP/SSHConnection/get | File, Host, LocalFile |
| Custom/SAP/SFTP/CCIInteraction/execute | **InteractionSpec**, InputRecord, ReturnedRecord, OutputRecord |
| Custom/WebServices/TransportBinding/***ClassName***/sendResponseMessage | **ProviderContextHelper**, Action |
| Custom/WebServices/TransportBinding/***ClassName***/sendServerError | **ProviderContextHelper**, Action |
| Custom/WebServices/TransportBinding/***ClassName***/sendAsynchronousResponse | **ProviderContextHelper**, Action |
| Custom/WebServices/TransportBinding/***ClassName***/sendMessageOneWay | **ProviderContextHelper**, Action |
| Custom/WebServices/ImplementationContainer/invokeMethod | MethodName, ConfigContext-Name, ConfigContext-Path |
   
### Data Groups   
| Data Group | Attributes |
| ---------- | ---------- |
| Channel | Channel-Name, Channel-AdapterType, Channel-Direction, Channel-Party, Channel-Service |
| Message | Message-Action, Message-Id, Message-CorreleationId, Message-SequenceId, Message-FromParty, Message-ToParty, Message-FromService, Message-ToService |
| MessageKey | MessageKey-ID, MessageKey-Direction |
| AS2Message | AS2Message-ContentType, AS2Message-FileName, AS2Message-FromEmain, AS2Message-FromName, AS2Message-MessageId, AS2Message-Subject, AS2Message-ToName |
| InstanceIdentity | ApplicationName, ModuleName, BeanName, ElementUniqueName |
| TransportMessage | TransportMessage-CorrelationID, TransportMessage-MessageId, Action, TransportMessage-FromParty, TransportMessage-ToParty, TransportMessage-FromService, TransportMessage-ToService |
| Action | Action-Name, Action-Type |
| Party | ToParty-Name, ToParty-Type, FromParty-Name, FromParty-Type |
| Service | ToService-Name, ToService-Type, FromService-Name, FromService-Type |
| JCODestination |  ApplicationServerHost,DestinationName, Client, GatewayService, GatewayHost |
| QueueMessage | ConnectionName, MessageKey, MessageType, Protocol |
| InteractionSpec | XIInteractionSpec-FunctionName, XIInteractionSpec-Class |
| ProviderContextHelper | Path, SessionId, Name, Operation-HTTPLocation, Operation-JavaMethodName, Operation-WSDLOperationName |
