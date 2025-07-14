# SAP Logging #
In order to help monitor SAP events that happen rapidly and result in dropped custom events, this instrumentation package provides the ability to log these events to a log file.  These log files can then be monitored by New Relic Logging and reported to New Relic.   
Each of these log files can be configured via the newrelic.yml configuration file and using the configuration that is described for each below.   It allows for enabling and disabling the logging, file name and path, and rolling attributes.   
  
## Configuration ##
The configuration for each of the log files is contained in an SAP stanza in the newrelic.yml file.  If the all or any of the settings is not present then the default value will apply.  All of the settings are dynamic meaning that if the value is changed or created in newrelic.yml and newrelic.yml is saved, then the change will take effect within 2 mintues. In the following, reference to the New Relic Agent directory refer to the directory that contains newrelic.jar.    
Note that you may adjust your values according to how fast that particular log fills up and available disk space.   
### Notes on File Size ###
The logs can be rolled according to file size of the log.   The configuration value has the format "N size_indentifier" where N is an integer and the size_identifier is one of the following: KB, MB, GB, TB 
### Notes on File Count ###
You can keep previous logs around and the file count is the number of archived logs to keep.  When the log file is rolled the oldest file is deleted if the max number of files has been reached.   Archived log files are appended with ".n".   
### Configuration Stanza ###
The configuration stanza will look like this:   
&nbsp;&nbsp;SAP:   
&nbsp;&nbsp;&nbsp;&nbsp;*loggingcomponent*:   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*setting*: *value*

## Example Configuration ##
The following screenshot shows an example configuration in newrelic.yml    
<img width="1254" alt="image" src="https://github.com/newrelic-experimental/newrelic-java-sap-bi/assets/8822859/9fc2176a-00f1-478e-9312-fd2b00f50438">

## Audit Log Component ##
This component will replicate the entries that SAP collects in the Audit Log.  
### Settings ###
| Setting | Description | Default Value |
| ------- | ----------- | ------------- |
| enabled | Boolean value - whether to generate the audit log or not | true |
| log_file_name | String - full path that is used to create and write that log file | audit.log in the New Relic Agent directory |
| log_file_interval | Integer - number of minutes between rolling the log file | 60 minutes (1 hour) |
| log_size_limit | String - Size indentifer decrribed above | 100 KB |
| log_file_count | Integer - Number of archive log files to keep | 3 |
| ignores | String - comma seperated list of text keys to ignore (not report to the log) | None |

## Adapter Message Log Component ##
This component monitors and logs adapter message by querying on a regular basis for messages that have been active since the last query.    
For details on what is collected see https://github.com/newrelic-experimental/newrelic-java-sap-bi/blob/add_adapter_logging/Logs/Adapter-Message-Logging.md   
### Settings ###
| Setting | Description | Default Value |
| ------- | ----------- | ------------- |
| enabled | Boolean value - whether to generate the adapter message log or not | true |
| log_file_name | String - full path that is used to create and write that log file | adapter_message.log in the New Relic Agent directory |
| log_file_interval | Integer - number of minutes between rolling the log file | 60 minutes (1 hour) |
| log_size_limit | String - Size indentifer decrribed above | 100 KB |
| log_file_count | Integer - Number of archive log files to keep | 3 |
| frequency | Integer - Number of minutes between queries | 3 |
| delay | Integer - Number of minutes before running the first query | 1 |

## Message Log Component ##
This component will replicate the entries that SAP collects in the Message Log.  
### Settings ###
| Setting | Description | Default Value |
| ------- | ----------- | ------------- |
| enabled | Boolean value - whether to generate the message log or not | true |
| log_file_name | String - full path that is used to create and write that log file | sap-messages.log in the New Relic Agent directory |
| log_file_interval | Integer - number of minutes between rolling the log file | 60 minutes (1 hour) |
| log_size_limit | String - Size indentifer decrribed above | 100 KB |
| log_file_count | Integer - Number of archive log files to keep | 3 |

### Message Fields Configuration ###
The message fields that are written to the message log file can be configured by placing a JSON file named saploggingconfig.json in the New Relic Java Agent directory.   If the file is not present and message logging is enabled then a default set of fields is reported.  For more information on configuring the fields to collect see    

## Communication Channels Log Component ##
This component will produce two different logs regarding information on the communication channels.  The first log will report detailed channel details whenever the timestamp on the process data is between the last collection and the current time.  We will refer to this log as the detailed log and only the current entries are written.  The collection process will run every 2 mintues.   The second log is a summary log and will report summary entries every 5 minutes for every channel.  

### Settings ###
| Setting | Description | Default Value |
| ------- | ----------- | ------------- |
| enabled | Boolean value - whether to generate the detailed communication channel log or not | true |
| log_file_name | String - full path that is used to create and write that log file | communicationchannels.log in the New Relic Agent directory |
| summarylog_enabled | Boolean value - whether to generate the summary communication channel log or not | true |
| summarylog_file_name | String - full path that is used to create and write that log file | channelsummary.log in the New Relic Agent directory |
| log_file_interval | Integer - number of minutes between rolling the log file | 60 minutes (1 hour) |
| log_size_limit | String - Size indentifer decrribed above | 100 KB |
| log_file_count | Integer - Number of archive log files to keep | 3 |

#### Notes ####
The rolling file attributes are common to both the detailed and summary logs   

## Gateway Logs ##
This consists of three components to produce logs related to making calls via the SAP Gateway framework.  It produces two gateway logs with one being more detailed than the other.  The third is the trace message log which will produce a log of trace data that is produced if tracing is enabled.  The component name is gatewaylog.   

### Settings ###
| Setting | Description | Default Value |
| ------- | ----------- | ------------- |
| log_file_name | String - full path that is used to create and write the gateway log | gateway.log in the New Relic Agent directory |
| mpllog_file_name |String - full path that is used to create and write the message processing log | messageprocessing.log in the New Relic Agent directory |
| tracelog_file_name | String - full path that is used to create and write to the trace message log file | tracemessage.log in the New Relic Agent directory |
| log_file_interval | Integer - number of minutes between rolling the log file | 60 minutes (1 hour) |
| log_size_limit | String - Size indentifer decrribed above | 100 KB |
| log_file_count | Integer - Number of archive log files to keep | 3 |
   
Each of the logs can be enabled or disabled via a setting under the SAP stanza.  If not defined the default value is true.   
&nbsp;&nbsp;SAP:   
&nbsp;&nbsp;&nbsp;&nbsp;gatewaylog:   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;enabled: *true or false*   
&nbsp;&nbsp;&nbsp;&nbsp;messageprocessinglog:   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;enabled: *true or false*   
&nbsp;&nbsp;&nbsp;&nbsp;tracemessagelog:   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;enabled: *true or false*   
