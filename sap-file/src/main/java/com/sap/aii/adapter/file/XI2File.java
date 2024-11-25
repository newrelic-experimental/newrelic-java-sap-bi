package com.sap.aii.adapter.file;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumenation.sap.file.FileUtils;
import com.sap.aii.adapter.file.configuration.XI2FileConfiguration;
import com.sap.aii.adapter.file.ftp.FTPCl;
import com.sap.aii.af.sdk.xi.lang.Binary;
import com.sap.aii.af.sdk.xi.mo.Message;
import com.sap.aii.af.sdk.xi.mo.xmb.DynamicConfiguration;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public class XI2File {

	
	@Trace(dispatcher=true)
	public Binary processMessage(Message message) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","File","XI2File","processMessage");
		return Weaver.callOriginal();
	}

	@Trace
	public void put2FTPServer(MessageKey pmk, String qos, FTPCl ftpClient, String fiNameTarget, Binary toReceive,
			XI2FileConfiguration cfg, DynamicConfiguration dynCfg, Message message) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","File","XI2File","put2FTPServer");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		FileUtils.addMessageKey(attributes, pmk);
		FileUtils.addValue(attributes, "FileNameTarget", fiNameTarget);
		FileUtils.addValue(attributes, "FTP_Host", ftpClient.getHost());
		FileUtils.addValue(attributes, "FTP_Port", ftpClient.getPort());
		
		Weaver.callOriginal();
	}
}
