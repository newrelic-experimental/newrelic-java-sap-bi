package com.newrelic.instrumentation.labs.sap.adapters.ejb;

import java.io.File;
import java.util.logging.Level;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;

public class AdapterLoggingConfig {

	private String contextadapterLog = null;
	private String supplementaadapterLog = null;
	private String principaladapterLog = null;

	public AdapterLoggingConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File logDirectory = new File(newRelicDir, "attributeLogging");
		boolean logDirExists = true;
		if(!logDirectory.exists()) {
			logDirectory.mkdir();
		} else if(!logDirectory.isDirectory()) {
			boolean deleted = logDirectory.delete();
			if(!deleted) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "{0} is not a directory and unable to delete", logDirectory);
				logDirExists = false;
			} else {
				logDirectory.mkdir();
			}
		}
		if(logDirExists) {
			File logFile = new File(logDirectory, EJBAdapterLogger.CONTEXT_LOGFILENAME);
			contextadapterLog = logFile.getAbsolutePath();
			
			File logFile2 = new File(logDirectory,EJBAdapterLogger.DATA_LOGFILENAME);
			supplementaadapterLog = logFile2.getAbsolutePath();
			
			File logFile3 = new File(logDirectory, EJBAdapterLogger.DATA_PRINCIPAL_LOGFILENAME);
			principaladapterLog = logFile3.getAbsolutePath();
		} else {
			File logFile = new File(newRelicDir, EJBAdapterLogger.CONTEXT_LOGFILENAME);
			contextadapterLog = logFile.getAbsolutePath();
			File logFile2 = new File(newRelicDir,EJBAdapterLogger.DATA_LOGFILENAME);
			supplementaadapterLog = logFile2.getAbsolutePath();
			File logFile3 = new File(newRelicDir, EJBAdapterLogger.DATA_PRINCIPAL_LOGFILENAME);
			principaladapterLog = logFile3.getAbsolutePath();
		}
	}

	public String getPrincipaladapterLog() {
		return principaladapterLog;
	}

	public String getContextLog() {
		return contextadapterLog;
	}

	public String getSupplementalLog() {
		return supplementaadapterLog;
	}
}

