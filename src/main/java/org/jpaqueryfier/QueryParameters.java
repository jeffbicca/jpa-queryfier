package org.jpaqueryfier;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import static org.jpaqueryfier.SQLGrammar.PARAMETER_VALUE_PATTERN;

public class QueryParameters {

	private List<QueryParameter> parameters = new LinkedList<QueryParameter>();

	public QueryParameter get(String name) {
		for (QueryParameter parameter : parameters)
			if (name.equals(parameter.getName()))
				return parameter;
		return new QueryParameter(null, null);
	}

	public List<QueryParameter> get() {
		return parameters;
	}

	void add(QueryParameter parameter) {
		parameters.add(parameter);
	}

	void addFrom(String sql) {
		Matcher m = PARAMETER_VALUE_PATTERN.matcher(sql);
		while (m.find())
			parameters.add(new QueryParameter(m.group().replace(":", ""), null));
	}

	void removeIfAlreadyAdded(QueryParameter paramToAdd) {
		Iterator<QueryParameter> parameter = parameters.iterator();
		while (parameter.hasNext())
			if (parameter.next().getName().equals(paramToAdd.getName()))
				parameter.remove();
	}

}
