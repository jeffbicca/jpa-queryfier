package org.jpaqueryfier;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class JpaQueryfier {

	private static final String PARAMETER_WITH_CLAUSE_REGEX = "(where|WHERE|and|AND|or|OR) [a-zA-Z0-9_]* = :([a-zA-Z][a-zA-Z0-9_]*)";
	private static final String PARAMETER_REGEX = ":([a-zA-Z][a-zA-Z0-9_]*)";

	@Inject
	private EntityManager em;

	private String sql;
	private List<QueryParameter> parameters = new LinkedList<QueryParameter>();
	private boolean allowNulls = false;

	public JpaQueryfier(String sql) {
		this.sql = sql;
		fillParametersFromSql();
	}

	public JpaQueryfier(String sql, EntityManager em) {
		this(sql);
		this.em = em;
	}

	public Query queryfy() {
		removeNullParametersFromSql();
		Query query = em.createQuery(sql);
		for (QueryParameter parameter : parameters)
			query.setParameter(parameter.getName(), parameter.getValue());

		return query;
	}

	public JpaQueryfier allowingNulls() {
		this.allowNulls = true;
		return this;
	}

	public JpaQueryfier with(Object value) {
		for (QueryParameter parameter : parameters)
			if (parameter.valueIsNull() && parameter.isNotAlreadyAppended()) {
				parameter.setValue(value);
				parameter.append();
				break;
			}
		return this;
	}

	public List<QueryParameter> getParameters() {
		return parameters;
	}

	public String getSql() {
		return sql;
	}

	private void fillParametersFromSql() {
		if (!parameters.isEmpty())
			return;

		Matcher m = Pattern.compile(PARAMETER_REGEX).matcher(sql);
		while (m.find())
			parameters.add(new QueryParameter(m.group().replace(":", ""), null));
	}

	private void removeNullParametersFromSql() {
		if (allowNulls)
			return;

		Matcher m = Pattern.compile(PARAMETER_WITH_CLAUSE_REGEX).matcher(sql);
		boolean whereRemoved = false;
		while (m.find()) {
			String parameterWithClause = m.group();
			if (doesNotContainValue(getParameterNameFrom(parameterWithClause))) {
				sql = sql.replace(parameterWithClause, "");
				if (parameterWithClause.contains("WHERE") || parameterWithClause.contains("where"))
					whereRemoved = true;
			} else if (whereRemoved)
				sql = sql.replaceFirst("and|AND|or|OR", "WHERE");
		}
		sql = sql.trim();
	}

	private boolean doesNotContainValue(String parameterName) {
		for (QueryParameter parameter : parameters)
			if (parameterName.equals(parameter.getName()))
				return parameter.getValue() == null;
		return false;
	}

	private String getParameterNameFrom(String parameterWithClause) {
		Matcher m = Pattern.compile(PARAMETER_REGEX).matcher(parameterWithClause);
		while (m.find())
			return m.group().replace(":", "");
		return null;
	}

}
