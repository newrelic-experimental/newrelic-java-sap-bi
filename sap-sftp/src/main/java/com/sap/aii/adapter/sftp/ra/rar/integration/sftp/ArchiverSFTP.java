package com.sap.aii.adapter.sftp.ra.rar.integration.sftp;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.sftp.SFTPUtils;
import com.sap.aii.adapter.sftp.ra.rar.integration.SftpAdapterConfiguration;
import com.sap.engine.interfaces.messaging.api.Message;

@Weave
public abstract class ArchiverSFTP {

	@Trace(dispatcher=true)
	public String archive(Message msg, byte[] archiveMe, String[] additionalParameters, String[] parameterReplacements,
			String file, String home, SSHConnection connection, MessageInformation messageInformation,
			SftpAdapterConfiguration cfg) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","ArchiverSFTP","archive");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addMessage(attributes,msg);
		SFTPUtils.addValue(attributes, "File", file);
		SFTPUtils.addValue(attributes, "Home", home);
		SFTPUtils.addSSHConnection(attributes, connection);
		String result = Weaver.callOriginal();
		SFTPUtils.addValue(attributes, "ReturnedFileURL", result);
		traced.addCustomAttributes(attributes);
		return result;
	}
}
