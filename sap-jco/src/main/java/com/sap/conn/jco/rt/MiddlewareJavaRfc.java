package com.sap.conn.jco.rt;

import java.net.URI;

import com.newrelic.api.agent.GenericParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.JCoAttributes;
import com.sap.conn.jco.JCoException;

@Weave
public abstract class MiddlewareJavaRfc {

	public abstract String getName();

	@Weave
	static abstract class JavaRfcClient {

		@Trace(dispatcher=true)
		public void connect(ClientConnection client) throws JCoException {

			try {
				Weaver.callOriginal();
			} catch (Exception e) {
				if(e instanceof JCoException) {
					JCoException jce = (JCoException)e;
					NewRelic.noticeError(jce);
					throw jce;
				}
			}
		}

		@Trace
		public void execute(ClientConnection client, String name, DefaultParameterList imp,DefaultParameterList imptab, DefaultParameterList chn, DefaultParameterList exp,
				boolean supportsASXML, AbapClassException.Mode classExceptionMode) throws JCoException {

			ConnectionAttributes attrs = client.getAttributes();
			URI uri = URI.create("abap://"+attrs.getHost()+"/"+attrs.getDestination()+"/"+name);
			GenericParameters params = GenericParameters.library("SAP-ABAP").uri(uri).procedure(name).build();
			NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
			try {
				Weaver.callOriginal();
			} catch (Exception e) {
				if(e instanceof JCoException) {
					JCoException jce = (JCoException)e;
					NewRelic.noticeError(jce);
					throw jce;
				}
				throw e;
			}

		}
	}

	@Weave
	static abstract class JavaRfcServer {


		@Trace(dispatcher = true)
		protected void dispatchRequest(ServerWorker server, String functionModule) {
			try {
				JCoAttributes attrs = server.getAttributes();
				if(attrs != null) {
					String host = attrs.getHost();
					String dest = attrs.getDestination();

					StringBuffer sb = new StringBuffer("Custom/SAP/JCO/JavaRFC/dispatchRequest");
					if(host != null && !host.isEmpty()) {
						sb.append('/');
						sb.append(host);
					}
					if(dest != null && !dest.isEmpty()) {
						sb.append('/');
						sb.append(dest);
					}
					if(functionModule != null && !functionModule.isEmpty()) {
						sb.append('/');
						sb.append(functionModule);
					}
					NewRelic.getAgent().getTracedMethod().setMetricName(sb.toString());
				}
			} catch (JCoException e) {
			}
			Weaver.callOriginal();
		}
	}

}
