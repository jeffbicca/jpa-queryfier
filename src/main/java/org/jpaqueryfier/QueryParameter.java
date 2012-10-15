package org.jpaqueryfier;

public class QueryParameter {

	private String name;
	private Object value;
	private boolean acceptNull;
	private boolean appended;

	public QueryParameter(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public QueryParameter(String name, Object value, boolean acceptNull) {
		this(name, value);
		this.acceptNull = acceptNull;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setValueAndAppend(Object value) {
		this.value = value;
		append();
	}

	public boolean isNotAlreadyAppended() {
		return !appended;
	}

	public void append() {
		this.appended = true;
	}

	public boolean valueIsNull() {
		return value == null;
	}

	public boolean acceptsNull() {
		return acceptNull;
	}

}
