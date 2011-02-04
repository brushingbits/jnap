/*
 * DynaQueryBuilder.java created on 2011-01-30
 *
 * Created by Brushing Bits Labs
 * http://www.brushingbits.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brushingbits.jnap.persistence.hibernate;

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.util.Assert;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class DynaQueryBuilder {

	private static final String BETWEEN = "Between";
	private static final String EQUAL = "Equal";
	private static final String GREATER_THAN = "GreaterThan";
	private static final String GREATER_THAN_OR_EQUAL = "GreaterThanEqual";
	private static final String IS_EMPTY = "IsEmpty";
	private static final String IS_NOT_EMPTY = "IsNotEmpty";
	private static final String IS_NOT_NULL = "IsNotNull";
	private static final String IS_NULL = "IsNull";
	private static final String LESS_THAN = "LessThan";
	private static final String LESS_THAN_OR_EQUAL = "LessThanEqual";
	private static final String LIKE = "Like";
	private static final String LIKE_IC = "LikeIc";
	private static final String NOT_EQUAL = "NotEqual";

	private static final String[] EXPRESSION_OPERATORS = new String[] {
			BETWEEN, GREATER_THAN_OR_EQUAL, GREATER_THAN, IS_EMPTY,
			IS_NOT_EMPTY, IS_NOT_NULL, IS_NULL, LESS_THAN_OR_EQUAL, LESS_THAN,
			LIKE_IC, LIKE, NOT_EQUAL, EQUAL };

	private static final String LOGICAL_OPERATOR_OR = "Or";

	private static final String LOGICAL_OPERATOR_AND = "And";
	
	private static Pattern DYNA_QUERY_OPERATOR_PATTERN = Pattern.compile(format("({0}|{1})",
			LOGICAL_OPERATOR_OR, LOGICAL_OPERATOR_AND));

	private static Pattern DYNA_QUERY_PATTERN = Pattern.compile("^(findBy|countBy|findUniqueBy)([A-Z]\\w*)");

	private Session session;
	private String entityName;
	private String dynaQuery;
	private Object[] queryParams;
	private int propertyValueIndex = 0;
	private ClassMetadata entityMetadata;
	private Map<String, DynaQueryBuilder.CriterionBuilder> criterionBuilderMap;

	/**
	 * 
	 * @param session
	 * @param entityName
	 * @param dynaQuery
	 * @param queryParams
	 */
	DynaQueryBuilder(Session session, String entityName, String dynaQuery, Object... queryParams) {
		Assert.notNull(session);
		Assert.hasText(entityName);
		Assert.hasText(dynaQuery);
		this.session = session;
		this.entityName = entityName;
		this.dynaQuery = dynaQuery;
		this.queryParams = queryParams == null ? ArrayUtils.EMPTY_OBJECT_ARRAY : queryParams;
		this.entityMetadata = this.session.getSessionFactory().getClassMetadata(this.entityName);
		buildCriterionBuilderStrategy();
	}

	/**
	 * 
	 */
	private void buildCriterionBuilderStrategy() {
		this.criterionBuilderMap = new HashMap<String, DynaQueryBuilder.CriterionBuilder>();
		this.criterionBuilderMap.put(BETWEEN, new BetweenCriterionBuilder());
		this.criterionBuilderMap.put(EQUAL, new EqualCriterionBuilder());
		this.criterionBuilderMap.put(GREATER_THAN, new GreaterThanCriterionBuilder());
		this.criterionBuilderMap.put(GREATER_THAN_OR_EQUAL, new GreaterThanEqualCriterionBuilder());
		this.criterionBuilderMap.put(IS_EMPTY, new IsEmptyCriterionBuilder());
		this.criterionBuilderMap.put(IS_NOT_EMPTY, new IsNotEmptyCriterionBuilder());
		this.criterionBuilderMap.put(IS_NOT_NULL, new IsNotNullCriterionBuilder());
		this.criterionBuilderMap.put(IS_NULL, new IsNullCriterionBuilder());
		this.criterionBuilderMap.put(LESS_THAN, new LessThanCriterionBuilder());
		this.criterionBuilderMap.put(LESS_THAN_OR_EQUAL, new LessThanEqualCriterionBuilder());
		this.criterionBuilderMap.put(LIKE, new LikeCriterionBuilder(false));
		this.criterionBuilderMap.put(LIKE_IC, new LikeCriterionBuilder(true));
		this.criterionBuilderMap.put(NOT_EQUAL, new NotEqualCriterionBuilder());
	}

	public Criteria build() throws QueryException {
		Matcher matcher = DYNA_QUERY_PATTERN.matcher(this.dynaQuery);
		if (!matcher.matches()) {
			throw new QueryException("The DynaQuery syntax is incorrect. It must start with 'findBy' "
					+ ", findUniqueBy or 'countBy' expression followed by property expressions and operators.",
					this.dynaQuery);
		}
		Criteria criteria = this.createCriteria(matcher.group(1).equals("countBy"));
		final String dynaQueryExpression = matcher.group(2);
		String[] properties = DYNA_QUERY_OPERATOR_PATTERN.split(dynaQueryExpression);
		if (properties.length == 0 || properties.length > 2) {
			throw new QueryException(format("The DynaQuery syntax is incorrect. Dynamic queries must have "
					+ "at least one property an no more than two (yours has {0}).\nYou can use one of "
					+ "the following logical operators ({1}) and these expression operators ({2})",
					properties.length, LOGICAL_OPERATOR_AND + ", " + LOGICAL_OPERATOR_OR,
					StringUtils.join(EXPRESSION_OPERATORS, ", ")), this.dynaQuery);
		}
		Matcher logicalOperatorsMatcher = DYNA_QUERY_OPERATOR_PATTERN.matcher(dynaQueryExpression);
		String logicalOperator = logicalOperatorsMatcher.find()
				? logicalOperatorsMatcher.group(1)
				: LOGICAL_OPERATOR_AND;
		Junction junction = LOGICAL_OPERATOR_OR.equals(logicalOperator)
				? Restrictions.disjunction()
				: Restrictions.conjunction();
		for (String property : properties) {
			junction.add(this.createCriterion(property));
		}
		criteria.add(junction);
		return criteria;
	}

	private Criteria createCriteria(boolean countQuery) {
		Criteria criteria = this.session.createCriteria(entityName);
		if (countQuery) {
			criteria.setProjection(Projections.rowCount());
		}
		return criteria;
	}

	private Criterion createCriterion(final String property) {
		String propertyName = property;
		String criterionType = EQUAL;
		for (String expression : EXPRESSION_OPERATORS) {
			if (property.endsWith(expression)) {
				criterionType = expression;
				propertyName = StringUtils.remove(propertyName, criterionType);
				break;
			}
		}
		propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
		return this.criterionBuilderMap.get(criterionType).build(propertyName);
	}

	Object getNextPropertyValue() {
		return getPropertyValues(1)[0];
	}

	Object[] getPropertyValues(int size) {
		if ((propertyValueIndex + size) <= this.queryParams.length) {
			Object[] values = ArrayUtils.subarray(this.queryParams, propertyValueIndex, propertyValueIndex + size);
			propertyValueIndex += size;
			return values;
		} else {
			throw new QueryException(format("The number of parameters ({0}) does not satisfy "
					+ "DynaQuery expression", Integer.toString(this.queryParams.length)), this.dynaQuery);
		}
	}

	void validateQueryProperty(String propertyName, Object value) {
		if (!ArrayUtils.contains(this.entityMetadata.getPropertyNames(), propertyName)) {
			throw new QueryException(format("Error creating a DynaQuery, the property {0} is not "
					+ "mapped for entity {1}.", propertyName, this.entityName), this.dynaQuery);
		}
		if (value != null) {
			Class propertyType = this.entityMetadata.getPropertyType(propertyName).getReturnedClass();
			if (!propertyType.isAssignableFrom(value.getClass())) {
				throw new QueryException(format("Error creating a DynaQuery, the property {0} type "
						+ "({1}) is not compatible with the argument type ({2}).", propertyName,
						propertyType.getName(), value.getClass().getName()), this.dynaQuery);
			}
		}
	}

	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private interface CriterionBuilder {

		/**
		 * 
		 * @param property
		 * @return
		 */
		Criterion build(String property);

	}

	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class BetweenCriterionBuilder implements CriterionBuilder {

		public Criterion build(String propertyName) {
			Object value1 = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value1);
			Object value2 = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value2);
			return Restrictions.between(propertyName, value1, value2);
		}

	}

	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class EqualCriterionBuilder implements CriterionBuilder {

		public Criterion build(String propertyName) {
			Object value = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value);
			return Restrictions.eq(propertyName, value);
		}

	}

	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class GreaterThanCriterionBuilder implements CriterionBuilder {

		public Criterion build(String propertyName) {
			Object value = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value);
			return Restrictions.gt(propertyName, value);
		}

	}

	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class GreaterThanEqualCriterionBuilder implements CriterionBuilder {

		public Criterion build(String propertyName) {
			Object value = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value);
			return Restrictions.ge(propertyName, value);
		}

	}
	
	/**
	 * 
	 * @see Restrictions#isEmpty(String)
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class IsEmptyCriterionBuilder implements CriterionBuilder {
		
		public Criterion build(String propertyName) {
			validateQueryProperty(propertyName, null);
			return Restrictions.isEmpty(propertyName);
		}
		
	}
	
	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class IsNotEmptyCriterionBuilder implements CriterionBuilder {
		
		public Criterion build(String propertyName) {
			validateQueryProperty(propertyName, null);
			return Restrictions.isNotEmpty(propertyName);
		}
		
	}
	
	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class IsNotNullCriterionBuilder implements CriterionBuilder {
		
		public Criterion build(String propertyName) {
			validateQueryProperty(propertyName, null);
			return Restrictions.isNotNull(propertyName);
		}
		
	}
	
	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class IsNullCriterionBuilder implements CriterionBuilder {
		
		public Criterion build(String propertyName) {
			validateQueryProperty(propertyName, null);
			return Restrictions.isNull(propertyName);
		}
		
	}
	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class LessThanCriterionBuilder implements CriterionBuilder {
		
		public Criterion build(String propertyName) {
			Object value = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value);
			return Restrictions.lt(propertyName, value);
		}
		
	}
	
	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class LessThanEqualCriterionBuilder implements CriterionBuilder {
		
		public Criterion build(String propertyName) {
			Object value = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value);
			return Restrictions.le(propertyName, value);
		}
		
	}
	
	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class LikeCriterionBuilder implements CriterionBuilder {

		private boolean ignoreCase;

		public LikeCriterionBuilder(boolean ignoreCase) {
			this.ignoreCase = ignoreCase;
		}

		public Criterion build(String propertyName) {
			Object propertyValue = DynaQueryBuilder.this.getNextPropertyValue();
			if (!(propertyValue instanceof String)) {
				throw new QueryException("Error creating DynaQuery. You can only use 'Like' "
						+ "operator on String properties.",	DynaQueryBuilder.this.dynaQuery);
			}
			validateQueryProperty(propertyName, propertyValue);
			return this.ignoreCase
				? Restrictions.ilike(propertyName, (String) propertyValue)
				: Restrictions.like(propertyName, (String) propertyValue);
		}
		
	}
	
	/**
	 * 
	 * @author Daniel Rochetti
	 * @since 1.0
	 */
	private class NotEqualCriterionBuilder implements CriterionBuilder {
		
		public Criterion build(String propertyName) {
			Object value = DynaQueryBuilder.this.getNextPropertyValue();
			validateQueryProperty(propertyName, value);
			return Restrictions.ne(propertyName, value);
		}
		
	}

}
