package com.sap.engine.interfaces.messaging.api.pmi;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@SuppressWarnings("deprecation")
@Weave(type=MatchType.Interface)
public abstract class PMIAccess {
	
	@Trace(dispatcher=true)
	public void invokeAFStatusAgent(MessageKey messageKey, String status, String errorCode, String errorCategory, String errorText) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeAFStatusAgent(MessageKey messageKey, String status, String errorCode, String errorCategory,
			String errorText, String adapterType, String fromParty, String toParty, String fromService,
			String toService, String interfaceName, String interfaceNamespace, String[] errorParamNames,
			String[] errorParamValues) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeAdapterInboundAgent(MessageKey messageKey, String adapterType, ProcessingMode processingMode, String qos, String queueName, String adapterData) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeAdapterOutboundAgent(MessageKey messageKey, String adapterType, ProcessingMode processingMode, String adapterData) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeChannelDeterminationAgent(MessageKey messageKey, String channelId, String channelName, String adapterType) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeCPALookupAgent(MessageKey messageKey, String var2, String var3, String var4, String var5, String var6, String var7) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeIDMappingAgent(MessageKey messageKey, MessageKey var2) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeXIRAInboundAgent(MessageKey messageKey) {
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void invokeXIRAOutboundAgent(MessageKey messageKey) {
		Weaver.callOriginal();
	}

}
