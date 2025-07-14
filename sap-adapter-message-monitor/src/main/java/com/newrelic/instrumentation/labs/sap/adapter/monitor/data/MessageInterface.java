package com.newrelic.instrumentation.labs.sap.adapter.monitor.data;

import java.io.Serializable;

@SuppressWarnings("rawtypes")
public class MessageInterface implements  Serializable, Comparable, Cloneable {
	private static final long serialVersionUID = -4683423725273067770L;
	private String name;
	private String namespace;
	private String senderParty;
	private String senderComponent;
	private String receiverParty;
	private String receiverComponent;

	public MessageInterface() {
		this.name = "";
		this.namespace = "";
	}

	public MessageInterface(String namespace, String name) {
		this.name = name;
		this.namespace = namespace;
	}

	public String getName() {
		return this.name;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public void setName(String name) {
		this.name = name != null ? name : "";
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace != null ? namespace : "";
	}

	public String getSenderParty() {
		return this.senderParty;
	}

	public void setSenderParty(String senderParty) {
		this.senderParty = senderParty;
	}

	public String getSenderComponent() {
		return this.senderComponent;
	}

	public void setSenderComponent(String senderComponent) {
		this.senderComponent = senderComponent;
	}

	public String getReceiverParty() {
		return this.receiverParty;
	}

	public void setReceiverParty(String receiverParty) {
		this.receiverParty = receiverParty;
	}

	public String getReceiverComponent() {
		return this.receiverComponent;
	}

	public void setReceiverComponent(String receiverComponent) {
		this.receiverComponent = receiverComponent;
	}

	public String toString() {
		return this.namespace + " " + this.name;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof MessageInterface) {
			MessageInterface interf = (MessageInterface) obj;
			return this.name.equals(interf.name) && this.namespace.equals(interf.namespace);
		} else {
			return false;
		}
	}

	public int compareTo(Object o) {
		MessageInterface int2 = (MessageInterface) o;
		int comp = this.namespace.compareTo(int2.namespace);
		if (comp == 0) {
			comp = this.name.compareTo(int2.name);
		}

		return comp;
	}

	public int hashCode() {
		return this.namespace.hashCode() ^ this.name.hashCode();
	}

	public boolean isEmpty() {
		return (this.name == null || this.name.length() == 0)
				&& (this.namespace == null || this.namespace.length() == 0);
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}