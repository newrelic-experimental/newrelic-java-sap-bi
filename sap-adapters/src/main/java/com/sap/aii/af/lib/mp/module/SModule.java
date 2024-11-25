package com.sap.aii.af.lib.mp.module;

import java.util.Enumeration;
import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapters.DataUtils;

@Weave(type=MatchType.Interface)
public abstract class SModule {
	
	@SuppressWarnings("rawtypes")
	public ModuleData onFault(ModuleContext context, ModuleData var2, Exception var3) {
		
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		DataUtils.addAttributes(var2, attributes);
		Enumeration keys = context.getContextDataKeys();
		while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String value = context.getContextData(key);
			if(value != null) {
				attributes.put(key, value);
			}
		}
		NewRelic.noticeError(var3,attributes);
		return Weaver.callOriginal();
	}

}
