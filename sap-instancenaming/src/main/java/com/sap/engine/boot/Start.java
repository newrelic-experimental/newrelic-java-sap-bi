package com.sap.engine.boot;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public class Start {
	
	@Trace
	private static void ebcdicConvert(String[] args) {
		Weaver.callOriginal();
	}

}
