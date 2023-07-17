## SAP JMX Dumper Install.  
   
To find the MBeans available on your SAP instance follow these steps:    
1.   Download sap-jmxdumper.jar from this directory.  
2.   Place the jar file into the extensions directory of the New Relic Java Agent (create if it does not exist).  
3.   Copy sap-jmxdumper.jar into the extensions directory.   
4.   Restart the application.  
   
# Output
After restarting there should be two files that show up in the New Relic Java Agent directory.   
Mbean-Attributes1.out - contains a list of MBeans and their attributes.  
Mbean-Operations1.out - contains a list of MBeans and their operations (typically empty) and currently not implemented.    
   
Use the contents of Mbean-Attributes1.out to populate sap-jmx.json.   
