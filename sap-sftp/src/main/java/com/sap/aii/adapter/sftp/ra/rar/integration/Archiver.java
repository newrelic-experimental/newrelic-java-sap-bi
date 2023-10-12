package com.sap.aii.adapter.sftp.ra.rar.integration;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.sftp.SFTPUtils;
import com.sap.engine.interfaces.messaging.api.Message;

@Weave
public abstract class Archiver {

	@Trace(dispatcher=true)
	public String archive(Message msg, byte[] archiveMe, String[] additionalParameters, String[] parameterReplacements, String file, SftpAdapterConfiguration cfg) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","Archiver","archive");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addMessage(attributes,msg);
		SFTPUtils.addValue(attributes, "File", file);
		String result = Weaver.callOriginal();
		SFTPUtils.addValue(attributes, "ReturnedFileURL", result);
		traced.addCustomAttributes(attributes);
		return result;
	}
}
