package com.sap.engine.interfaces.messaging.api.logger;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.auditlogging.Logger;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageStatus;

@Weave(type = MatchType.Interface)
public abstract class MessageLogger {

	   public void log(Message message) {
		   if(!Logger.LOGGED.get()) {
			   Logger.LOGGED.set(Boolean.TRUE);
			   Logger.log(message, null, null, null);
		   }
		   Weaver.callOriginal();
		   Logger.LOGGED.set(Boolean.FALSE);
	   }

	   public void log(Message message, String connName) {
		   if(!Logger.LOGGED.get()) {
			   Logger.LOGGED.set(Boolean.TRUE);
			   Logger.log(message, null, null, connName);
		   }
		   Weaver.callOriginal();
		   Logger.LOGGED.set(Boolean.FALSE);
	   }

	   public void log(Message message, boolean triggerEvent) {
		   if(!Logger.LOGGED.get()) {
			   Logger.LOGGED.set(Boolean.TRUE);
			   Logger.log(message, null, null, null);
		   }
		   Weaver.callOriginal();
		   Logger.LOGGED.set(Boolean.FALSE);
	   }

	   public void log(Message message, boolean triggerEvent, String connName) {
		   if(!Logger.LOGGED.get()) {
			   Logger.LOGGED.set(Boolean.TRUE);
			   Logger.log(message, null, null, connName);
		   }
		   Weaver.callOriginal();
		   Logger.LOGGED.set(Boolean.FALSE);
	   }

	   public void log(Message message, MessageStatus status, String errorCode, boolean triggerEvent) {
		   if(!Logger.LOGGED.get()) {
			   Logger.LOGGED.set(Boolean.TRUE);
			   Logger.log(message, status, errorCode, null);
		   }
		   Weaver.callOriginal();
		   Logger.LOGGED.set(Boolean.FALSE);
	   }

	   public void log(Message message, MessageStatus status, String errorCode, String connName, boolean triggerEvent) {
		   if(!Logger.LOGGED.get()) {
			   Logger.LOGGED.set(Boolean.TRUE);
			   Logger.log(message, status, errorCode, connName);
		   }
		   Weaver.callOriginal();
		   Logger.LOGGED.set(Boolean.FALSE);
	   }

	   public void log(Message message, int retainTime, ProcessingState processingState) {
		   
	   }

	   public void logStatus(Message message, MessageStatus status, String errorCode, boolean triggerEvent) {
		   if(!Logger.LOGGED.get()) {
			   Logger.LOGGED.set(Boolean.TRUE);
			   Logger.log(message, status, errorCode, null);
		   }
		   Weaver.callOriginal();
		   Logger.LOGGED.set(Boolean.FALSE);
	   }

	   public abstract String getName();

}
