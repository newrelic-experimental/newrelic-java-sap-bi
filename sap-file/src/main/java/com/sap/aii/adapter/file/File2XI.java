package com.sap.aii.adapter.file;

import java.util.Hashtable;
import java.util.List;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.adapter.file.configuration.File2XIConfiguration;
import com.sap.aii.adapter.file.configuration.File2XIConfiguration.FileSource;
import com.sap.aii.adapter.file.ftp.FTPCl;
import com.sap.aii.adapter.file.io.FileHandle;
import com.sap.aii.af.lib.scheduler.Job;
import com.sap.aii.af.lib.scheduler.Task;
import com.sap.aii.af.sdk.xi.lang.Binary;
import com.sap.aii.af.service.util.transaction.api.TxManager;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class File2XI {

	private File2XIConfiguration cfg_ = Weaver.callOriginal();
	protected Job myJob_ = Weaver.callOriginal();

	@SuppressWarnings("rawtypes")
	@Trace
	byte[] send(Binary toSend, String eoGuid, String qos, FileHandle fileHandle, Hashtable hashtable, MessageKey messageKey, String eoKey) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttribute("Service", cfg_.getServiceName());
		traced.addCustomAttribute("Channel", cfg_.getChannelName());
		traced.addCustomAttribute("Party", cfg_.getPartyName());

		traced.setMetricName("Custom","SAP","File","File2XI","send");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher = true)
	public void invoke() {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","File","File2XI","invoke");
		traced.addCustomAttribute("Service", cfg_.getServiceName());
		traced.addCustomAttribute("Channel", cfg_.getChannelName());
		traced.addCustomAttribute("Party", cfg_.getPartyName());

		Weaver.callOriginal();
	}
	
	@SuppressWarnings("unused")
	private FileHandle[] getFileList(FileSource[] fileSources) {
		FileHandle[] handles = Weaver.callOriginal();
		if(handles.length == 0) {
			NewRelic.getAgent().getTransaction().ignore();
		} else {
			NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.CUSTOM_LOW, true, "FileAdapter", "File","ProcessFile");
		}
		return handles;
	}

	FileHandle[] getFtpList(FTPCl ftpCl, FileSource[] fileSources) {

		FileHandle[]  handles = Weaver.callOriginal();
		if(handles.length == 0) {
			NewRelic.getAgent().getTransaction().ignore();
		} else {
			NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.CUSTOM_LOW, true, "FileAdapter", "File","ProcessFTP");
		}
		return handles;
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
		String jobName = myJob_ != null ? myJob_.getName() : "NullJob";
		Task task = myJob_ != null ? myJob_.getTask() : null;
		String taskName = task != null ? task.getClass().getSimpleName() : "UnknownTask";
		traced.addCustomAttribute("Task-Name", taskName);
		traced.addCustomAttribute("Job", jobName);
		Weaver.callOriginal();
	}

	@Trace
	private int processFileList(FileHandle[] fileHandles, LockWrapper lockWrapper,TxManager transactionManager) {
		return Weaver.callOriginal();
	}

	@Trace
	private int processFtpList(FileHandle[] fileHandles, LockWrapper lockWrapper, TxManager transactionManager) {
		return Weaver.callOriginal();
	}

	@Trace(dispatcher = true)
	public List<FileHandle> getFileHandles() {
		return Weaver.callOriginal();
	}

	@Weave
	private static class LockWrapper {

	}
}
