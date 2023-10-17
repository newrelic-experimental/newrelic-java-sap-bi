package com.sap.aii.adapter.sftp.ra.rar.integration.sftp;

import java.util.HashMap;

import com.jcraft.jsch.SftpProgressMonitor;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.sftp.SFTPUtils;

@Weave
public abstract class SSHConnection {

	
	public abstract String getHost();
	
	@Trace
	public void send(byte[] file, String absoluteFilePath, SftpProgressMonitor progressMonitor, Mode mode,boolean skipRemoteDirCheck) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","SSHConnection","send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addValue(attributes, "AbsoluteFilePath", absoluteFilePath);
		SFTPUtils.addValue(attributes, "Mode", mode);
		SFTPUtils.addValue(attributes, "Host", getHost());
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	public void copy(String srcFileName, String dstFileName, SftpProgressMonitor progressMonitor, int bufferSize) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","SSHConnection","copy");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addValue(attributes, "SourceFile", srcFileName);
		SFTPUtils.addValue(attributes, "DestinationFile", dstFileName);
		SFTPUtils.addValue(attributes, "Host", getHost());
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	public void delete(String fileName) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","SSHConnection","delete");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addValue(attributes, "File", fileName);
		SFTPUtils.addValue(attributes, "Host", getHost());
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	public byte[] get(String fileName, SftpProgressMonitor progressMonitor, int receiveBufferSize) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","SSHConnection","get");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addValue(attributes, "File", fileName);
		SFTPUtils.addValue(attributes, "Host", getHost());
		traced.addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}
	
	public void get(String fileName, String localFileLocation, SftpProgressMonitor progressMonitor, int bufferSize) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","SSHConnection","get");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addValue(attributes, "Host", getHost());
		SFTPUtils.addValue(attributes, "File", fileName);
		SFTPUtils.addValue(attributes, "LocalFile", localFileLocation);
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
