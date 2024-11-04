package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
abstract class ServerRequestQueue {

	public void addRequest(AbstractServerConnection connection) {
		if(connection.token == null) {
			Token token = NewRelic.getAgent().getTransaction().getToken();
			if(token != null && token.isActive()) {
				connection.token = token;
			} else if(token != null) {
				token.expire();
				token = null;
			}
		}
		Weaver.callOriginal();
	}
}
