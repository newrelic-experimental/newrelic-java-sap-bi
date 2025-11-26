# SAP Message Logging Configuration #
The feature that allows for logging each message in SAP PI/PO to a log file is contained in the extension sap-audit-log.jar.   In addition it will also log audit messages to another log.
The fields that can be written to the message log file is configurable via the saploggingconfig.json.    The details on the configuration are in this document.  The configuration file needs to be placed in the New Relic Java Agent directory (the directory containing the newrelic.jar).   The configuration file is dynamic, so any changes to the file will be picked up within a minute or so without having to restart the application.
The messages that are written to the log are based on three different types, Message, QueueMessage (internal) and FinalMessageStatus.  When the message is written to the log it will write the type as Logging Type.
## Message Fields Configuration ##
The configuration for which fields to log is contained in the file saploggingconfig.json provided and needs to be placed in the New Relic Java Agent directory (this is the directory containing newrelic.jar).  This configuration is JSON and contains what appears to be all available fields related to the message written to the log.  Most are turned off (value false).  To enable a field simply change the value to true and save the configuration file.
The configuration file contains five different JSON configuration items.  Some of the configuration item names end with _all.   These represent items which can produce multiple items.  They are followed in the configuration file by the fields that it can produce.  If the _all item is set to true then the other items are ignored and all of them are reported.  To only report selected items, set the _all item to false and set the items you want to true.   For example, there are to and from for both Party and Service available for messages.  Both have both a name and a type associated with them.   If _all is set to true then both the name and the type will be written to the log.  To only write the name, set _all to false and name to true.
The five different types of configurations are: MessageStatus, ErrorCode, Message, TransportableMessage and QueueMessage.  Each of these has a JSON Object associated with it that ends with -Config.
#### Note ####
If the value of the item is not defined (null) or empty (zero length string), it will not be written to the log.   
**Note that a template of saploggingconfig.json is included in this directory. https://github.com/newrelic-experimental/newrelic-java-sap-bi/blob/main/Logs/saplogginconfig.json**
  
### MessageStatus ###
status - set to true to write message status to the log for all messages.  Default value is true  
### ErrorCode ###
Errorcode - set to true to write the error code to the log if populated for a message. Default value is true
### Message ###

| Name | Description | Default value if not found |
| ---- | ---- | ---- |
| action_all | if set to true will write action name and type to the log, values for name and type are ignored | false |
| action_name | if action_all is false and it is true write name to log | false | 
| action_type | if action_all is false and it is true write type to log | false | 
| correlationId | if set to true write correlation id to the log | false | 
| deliverySemantics | if set to true write delivery semantics to log | false | 
| description | if set to true write message description to log  | false | 
| xmlpayload_all | if set to true then write all of the next items through to xmlpayload_schema | false | 
| xmlpayload_payload_name | if xmlpayload_all is false and it is set to true then write payload name to log | false | 
| xmlpayload_payload_desc | if xmlpayload_all is false and it is set to true then write payload description to log | false | 
| xmlpayload_payload_contenttype | if xmlpayload_all is false and it is set to true then write payload content type to log | false | 
| textpayload_encoding | if xmlpayload_all is false and it is set to true then write payload encoding to log | false | 
| xmlpayload_version | if xmlpayload_all is false and it is set to true then write payload version to log | false | 
| xmlpayload_schema | if xmlpayload_all is false and it is set to true then write payload schema to log | false | 
| mainpayload_all | if set to true then write all of the next items through to mainpayload_contenttype | false | 
| mainpayload_name | if mainpayload_all is false and it is set to true then write main payload name to log | false | 
| mainpayload_desc | if mainpayload_all is false and it is set to true then write main payload description to log | false | 
| mainpayload_contenttype | if mainpayload_all is false and it is set to true then write main payload content type to log | false
| fromparty_all | if set to true then write both the name and the type of the party to the log | false | 
| fromparty_name | if fromparty_all is false and it is set to true then write from party name to log | false | 
| fromparty_type | if fromparty_all is false and it is set to true then write from party type to log | false | 
| fromservice_all | if set to true then write both the name and the type of the service to the log | false | 
| fromservice_name | if fromservice_all is false and it is set to true then write from service name to log | false | 
| fromservice_type | if fromservice_all is false and it is set to true then write from service type to log | false | 
| messageclass | if set to true, write the Java class of the message to the log | false | 
| messagedirection | if set to true, write the message direction to the log | true | 
| messageid | if set to true, write the message id to the log | true | 
| messagekey_all | if set to true then write both the message id and the message direction  of the messagekey to the log | true | 
| messagekey_messageid | if messagekey_all is false and it is set to true then write message key message id to log | false | 
| messagekey_direction | if messagekey_all is false and it is set to true then write message key message direction to log | false | 
| protocol | if set to true, write the message protocol to the log | true | 
| refToMessageId | if set to true, write the ref to message id to the log | false | 
| sequenceid | if set to true, write the sequence id to the log | false | 
| toparty_all | if set to true then write both the name and the type of the party to the log | false | 
| toparty_name | if toparty_all is false and it is set to true then write from party name to log | false | 
| toparty_type | if toparty_all is false and it is set to true then write from party type to log | false | 
| toservice_all | if set to true then write both the name and the type of the service to the log | false | 
| toservice_name | if toservice_all is false and it is set to true then write from service name to log | false | 
| toservice_type | if toservice_all is false and it is set to true then write from service type to log | false | 
| serializationcontext | if set to true, write the serialization context to the log | false | 
| timereceived | if set to true, write the time received to the log | true | 
| timesent | if set to true, write the time sent to the log | false | 

### TranportableMessage ###

| Name | Description | Default value if not found | 
| ---- | ---- | ---- |
| endpoint_all | if set to true then write both the address and transport to the log | false | 
| endpoint_address | if endpoint_all is set to false and it is set to true write address to log | false | 
| endpoint_transport | if endpoint_all is set to false and it is set to true write transport to log | false | 
| messagepriority | if it is set to true write message priority to log | false | 
| parentid | if it is set to true write parent id to log | false | 
| retries | if it is set to true write number of retries to log | false | 
| retryinterval | if it is set to true write the retry interval to log | false | 
| sequencenumber | if it is set to true write sequence number to log | false | 
| persistuntil | if it is set to true write persist until value to log | false | 
| transportbodysize | if it is set to true write transport body size to log | false | 
| validuntil | if it is set to true write valid until value to log | false | 
| versionnumber | if it is set to true write version number to log | false | 


