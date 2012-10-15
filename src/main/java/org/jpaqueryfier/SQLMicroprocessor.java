package org.jpaqueryfier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jpaqueryfier.SQLGrammar.PARAMETER_VALUE_PATTERN;
import static org.jpaqueryfier.SQLGrammar.PARAMETER_VALUE_WITH_CLAUSE_PATTERN;
import static org.jpaqueryfier.SQLGrammar.PARAMETER_WITH_CLAUSE_PATTERN;

class SQLMicroprocessor {

	private String sql;
	private QueryParameters parameters;
	private boolean isWhereRemoved;
	private boolean allowNulls;

	SQLMicroprocessor(String sql, QueryParameters parameters, boolean allowNulls) {
		this.sql = sql;
		this.parameters = parameters;
		this.allowNulls = allowNulls;
	}

	String removeNullParameters() {
		return removeNullParameters(PARAMETER_WITH_CLAUSE_PATTERN);
	}

	private String removeNullParameters(Pattern p) {
		if (allowNulls)
			return sql.trim();

		Matcher m = p.matcher(sql);
		while (m.find()) {
			String parameterWithClause = m.group();
			sql = removeParameterIfIsNullAndDontAcceptNulls(parameterWithClause,
					parameters.get(getParameterNameFrom(parameterWithClause)));
		}
		return sql.trim();
	}

	private String removeParameterIfIsNullAndDontAcceptNulls(String parameterWithClause, QueryParameter parameter) {
		if (parameter.valueIsNull() && !parameter.acceptsNull()) {
			sql = sql.replace(parameterWithClause, "");
			isWhereRemoved = parameterWithClause.contains("WHERE") || parameterWithClause.contains("where");
			if (parameterWithClause.contains("BETWEEN") || parameterWithClause.contains("between"))
				removeNullParameters(PARAMETER_VALUE_WITH_CLAUSE_PATTERN);
		} else if (isWhereRemoved)
			sql = sql.replaceFirst("and|AND|or|OR", "WHERE");
		return sql;
	}

	private String getParameterNameFrom(String parameterWithClause) {
		Matcher m = PARAMETER_VALUE_PATTERN.matcher(parameterWithClause);
		while (m.find())
			return m.group().replace(":", "");
		return "";
	}

}
