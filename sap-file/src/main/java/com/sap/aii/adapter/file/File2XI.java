package com.sap.aii.adapter.file;

import java.util.Hashtable;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.adapter.file.configuration.File2XIConfiguration;
import com.sap.aii.adapter.file.configuration.File2XIConfiguration.FileSource;
import com.sap.aii.adapter.file.ftp.FTPCl;
import com.sap.aii.adapter.file.io.FileHandle;
import com.sap.aii.af.sdk.xi.lang.Binary;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class File2XI {
	
	private File2XIConfiguration cfg_ = Weaver.callOriginal();

	@SuppressWarnings("rawtypes")
	@Trace
	byte[] send(Binary toSend, String eoGuid, String qos, FileHandle fileHandle, Hashtable hashtable,
			MessageKey messageKey, String eoKey) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","File","File2XI","send");
		return Weaver.callOriginal();
	}
	
	@Trace(dispatcher=true)
	public void invoke() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","File","File2XI","invoke");
		Weaver.callOriginal();
	}
	
	@Trace
	FileHandle[] getFtpList(FTPCl ftpCl, FileSource[] fileSources) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","File","File2XI","getFtpList");
		return Weaver.callOriginal();
	}
	
	@Trace
	private void reconnect2FTPServer(MessageKey pmk) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","File","File2XI","reconnect2FTPServer");
		String ftpHost = cfg_.getFtpHost();
		if(ftpHost != null && !ftpHost.isEmpty()) {
			traced.addCustomAttribute("FTPHost", ftpHost);
		}
		int ftpPort = cfg_.getFtpPort();
		if(ftpPort > 0) {
			traced.addCustomAttribute("FTPPort", ftpPort);
		}
		Weaver.callOriginal();
	}
	
}
