package com.sap.aii.af.idoc.outbound;

import java.util.List;
import java.util.Map;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.idoc.store.IDocOutboundMessage;
import com.sap.aii.af.idoc.util.IDocBulk;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import javax.resource.cci.Connection;


@Weave
public abstract class IDocOutboundProcessor {

	@Trace(dispatcher = true)
	public Object[] processXIMessage(Message requestMessage, String cid, boolean junitFlag, IDocBulk iDocBulk) {
		
		return Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public Map<MessageKey, List<IDocOutboundMessage>> processXIMessages(List<Message> aggregateMessage, String cid, boolean junitFlag, IDocBulk iDocBulk) {
		return Weaver.callOriginal();
	}
	
	@Trace
	private void sendIDocList(Message requestMessage, IDocOutboundProcessor.IDocOutboundProcessorParams params,
			IDocBulk iDocBulk, Connection conn) {
		Weaver.callOriginal();
	}
	
	@Weave
	private static class IDocOutboundProcessorParams {
		
	}
}
