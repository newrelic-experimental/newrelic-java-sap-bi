package com.newrelic.instrumentation.labs.sap.adapter.monitor.data;

import java.io.Serializable;

@SuppressWarnings("rawtypes")
public class MessageParty implements Serializable, Comparable{

	private static final long serialVersionUID = -1653748736456141604L;
	private String name = "";
	private String schema = "";
	private String agency = "";
	private static final String DEFAULT_SCHEMA = "XIParty";
	private static final String DEFAULT_AGENCY = "http://sap.com/xi/XI";

	public MessageParty() {
	}

	public MessageParty(String name) {
		this.name = name;
		this.schema = DEFAULT_SCHEMA;
		this.agency = DEFAULT_AGENCY;
	}

	public MessageParty(String name, String schema, String agency) {
		this.name = name;
		this.schema = schema;
		this.agency = agency;
	}

	public String getAgency() {
		return this.agency;
	}

	public String getName() {
		return this.name;
	}

	public String getSchema() {
		return this.schema;
	}

	public void setAgency(String string) {
		this.agency = string != null ? string : "";
	}

	public void setName(String string) {
		this.name = string != null ? string : "";
	}

	public void setSchema(String string) {
		this.schema = string != null ? string : "";
	}

	public String toHTMLString() {
		if (this.name.length() == 0) {
			return "";
		} else {
			StringBuffer string = new StringBuffer(this.name);
			if (this.agency.length() != 0 || this.schema.length() != 0) {
				string.append("<BR>(");
				string.append(this.agency);
				string.append(',');
				string.append(this.schema);
				string.append(')');
			}

			return string.toString();
		}
	}

	public String toString() {
		return this.name + " " + this.agency + " " + this.schema;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof MessageParty) {
			MessageParty party = (MessageParty) obj;
			return this.name.equals(party.name) && this.agency.equals(party.agency) && this.schema.equals(party.schema);
		} else {
			return false;
		}
	}

	public int compareTo(Object o) {
		MessageParty party2 = (MessageParty) o;
		int comp = this.name.compareTo(party2.name);
		if (comp == 0) {
			comp = this.agency.compareTo(party2.agency);
		}

		if (comp == 0) {
			comp = this.schema.compareTo(party2.schema);
		}

		return comp;
	}

	public int hashCode() {
		return this.name.hashCode() ^ this.agency.hashCode() ^ this.schema.hashCode();
	}

}
