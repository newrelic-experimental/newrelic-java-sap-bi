# SAP Adapter Message Logging Configuration #
This feature allows the user to collect attributes related to Adapter messages.  If enabled it will write the configured attributes to a log file in JSON format.    

## Configuration ##
The configuration is stored in a file which contains a single JSON object which contains all of the attributes listed below and a boolean value as to whether to report it or not.  You can find the configuration file under config/adapter-message-config.json.   
Copy adapter-message-config.json into the New Relic Java Agent directory (directory which contains newrelic.jar).  If the configuration file is not present in the agent directory, a default set of values will be used (see Default Value in table below).   
The configuration is dynamic which means that you do not need to restart the application for the changes to take effect.  The instrumentation monitors the file every minute to see if it has been added, deleted or modified.  If added or modified it loads the new configuration from the file.   If deleted it will replace the existing configuration with the default configuration.   

### Multivalue Items ###
Some of the components below have mulitple attributes assoicated with them.  These are the three Interface items (Interface, ReceiverInterface, SenderInterface) and the two Party items (ReceiverParty, SenderParty). Each of them have a _all setting and a _*attribute* setting.
If the _all setting is set to true, it will report all of the attributes.  If _all is set to false and one or more of the _*attribute* settings is set to true then only the _*attribute* attribute is reported. If _all is set to true and one or more of the _*attribute* settings is set to true then the _*attributes* are essentailly ignored.   
### Business Attributes ###
If any business attributes are included with a message then they will be reported as well. They will be reported with a key value of BusinessAttribute-*attributeName*    
## Logging ##
All attributes that are configured to report will always be reported and in the same order to make for easier parsing.  If the attribute value that is retieved is null then it will be reported by the string Not_Reported.  If the value is an empty string then it will be reported as Empty_String    

## Attributes ##  
| Name | Default Value |
| ---- | ---- |
| ApplicationComponent| false |
| BusinessMessage| false |
| Cancelable| false|
| ConnectionName| true|
| CorrelationID|  false |
| Direction|  true |
| Duration|  true |
| Editable|  false |
| Endpoint|  true |
| EndTime|  true |
| ErrorCategory|  true |
| ErrorCode|  true |
| ErrorLabel|  true |
| Headers|  false |
| Interface_all |  false |
| Interface_name|  true |
| Interface_namespace|  false |
| Interface_receiverComponent|  false |
| Interface_receiverParty|  false |
| Interface_senderComponent|  false |
| Interface_senderParty|  false |
| IsPersistent|  false |
| MessageId|  true |
| MessageKey|  true |
| MessagePriority|  false |
| MessageType|  true |
| NodeId|  true |
| ParentId|  false |
| Passport|  false |
| PassportConnectionCounter|  false |
| PassportConnectionID|  false |
| PassportPreviousComponent|  false |
| PassportRootContextID|  false |
| PassportTID|  false |
| PayloadPermissionWarning|  false |
| PersistUntil|  true |
| Protocol|  true |
| QualityOfService|  true |
| ReceiverInterface_all|  false |
| ReceiverInterface_name|  true |
| ReceiverInterface_namespace|  true |
| ReceiverInterface_receiverComponent|  false |
| ReceiverInterface_receiverParty|  false |
| ReceiverInterface_senderComponent|  false |
| ReceiverInterface_senderParty|  false |
| ReceiverName|  true |
| ReceiverParty_all|  false |
| ReceiverParty_name|  false |
| ReceiverParty_schema|  false |
| ReceiverParty_agency|  false |
| ReferenceID|  false |
| Retries|  true |
| RetryInterval|  false |
| RootID|  false |
| ScenarioIdentifier|  true |
| ScheduleTime|  false |
| SenderInterface_all|  false |
| SenderInterface_name|  true |
| SenderInterface_namespace|  false |
| SenderInterface_receiverComponent|  false |
| SenderInterface_receiverParty|  false |
| SenderInterface_senderComponent|  false |
| SenderInterface_senderParty|  false |
| SenderName|  true |
| SenderParty_all|  false |
| SenderParty_name|  true |
| SenderParty_schema|  false |
| SenderParty_agency|  false |
| SequenceID|  false |
| SequenceNumber|  true |
| SerializationContext|  false |
| ServiceDefinition|  false |
| Size|  true |
| SoftwareComponent|  false |
| StartTime|  false |
| Status|  true |
| TimesFailed|  true |
| Transport|  false |
| ValidUntil|  false |
| Version|  false |
| WasEdited | true|
