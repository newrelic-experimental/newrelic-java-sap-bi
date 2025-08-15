package com.sap.aii.af.app.mp.ejb;

import java.util.LinkedList;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.AdapterMonitorLogger;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.AttributeProcessor;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.Utils;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave
public class ModuleProcessorBean {

	public ModuleData process(String channelId, ModuleData objectData) {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
		if(objectData != null) {
			Utils.currentModuleData.set(objectData);
		}
		
		ModuleData result = Weaver.callOriginal();
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
}
