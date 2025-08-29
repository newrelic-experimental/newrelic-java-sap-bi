package com.sap.aii.af.app.mp.ejb;

import java.util.LinkedList;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.AdapterMonitorLogger;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.AttributeProcessor;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageToProcess;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.Utils;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public class ModuleProcessorBean {

	public ModuleData process(String channelId, ModuleData objectData) {
		AdapterMonitorLogger.logMessage("Call to ModuleProcessorBean.process: channelId: " + channelId + ", moduleData: " + objectData);
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
		if(objectData != null) {
			Utils.currentModuleData.set(objectData);
			AdapterMonitorLogger.logMessage("in ModuleProcessorBean.process setting currentModuleData to  moduleData: " + objectData);
		}
		
		ModuleData result = Weaver.callOriginal();
		MessageKey outboundKey = getMessageKey(result);
		if(outboundKey != null) {
			MessageMonitor.messageKeyToProcessDelayed(new MessageToProcess(outboundKey));
		}
		Utils.currentModuleData.set(null);
		return result;
	}
	
	@SuppressWarnings({ "unused", "rawtypes" })
	private ModuleContext createModuleContext(String channelId, String moduleNS, LinkedList moduleConfigList) {
		AdapterMonitorLogger.logMessage("Call to ModuleProcessorBean.createModuleContext: channelId: " + channelId + ", moduleNS: " + moduleNS + ", moduleConfigList: " + moduleConfigList);
		ModuleContext ctx = Weaver.callOriginal();
		ModuleData data = Utils.currentModuleData.get();
		AttributeProcessor.record(ctx, data);
		return ctx;
	}
	
	private MessageKey getMessageKey(ModuleData data) {
		if(data != null) {
			Object principal = data.getPrincipalData();
			if(principal != null && principal instanceof Message) {
				return ((Message)principal).getMessageKey();
			}
		}
		return null;
	}
}
