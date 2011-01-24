/*
 * FullTextDaoSupport.java created on 2010-03-15
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
package org.brushingbits.jnap.persistence.hsearch;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.brushingbits.jnap.bean.model.IndexedModel;
import org.brushingbits.jnap.bean.paging.PagingDataHolder;
import org.brushingbits.jnap.persistence.FullTextDao;
import org.brushingbits.jnap.persistence.hibernate.DaoSupport;
import org.brushingbits.jnap.util.ReflectionUtils;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.Field;
import org.springframework.beans.BeanUtils;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 * 
 * @param <E>
 * @see IndexedModel
 */
public abstract class FullTextDaoSupport<E extends IndexedModel> extends
		DaoSupport<E> implements FullTextDao<E> {

	protected static ThreadLocal<FullTextSession> fullTextSessionHolder = new ThreadLocal<FullTextSession>();

	protected Version luceneVersion = Version.LUCENE_29;

	private String[] indexedFields;

	/**
	 * 
	 * @return
	 */
	protected FullTextSession getFullTextSession() {
		FullTextSession session = fullTextSessionHolder.get();
		if (session == null || !session.isOpen()) {
			session = Search.getFullTextSession(getSession());
			fullTextSessionHolder.set(session);
		}
		return session;
	}

	/**
	 * Hibernate Search implementation for {@link FullTextDao#searchByDocId(Serializable)}.
	 */
	public E searchByDocId(Serializable docId) {
		return (E) getFullTextSession().get(getEntityClass(), docId);
	}

	/**
	 * Hibernate Search implementation for {@link FullTextDao#searchByKeywords(String)}.
	 */
	public List<E> searchByKeywords(String keywords) {
		return searchByKeywords(keywords, false);
	}

	/**
	 * Hibernate Search implementation for {@link FullTextDao#searchByKeywords(String, boolean)}.
	 */
	public List<E> searchByKeywords(String keywords, boolean leadingWildcard) {
		String query = keywords;
		String[] indexedFields = getIndexedFields();
		boolean containsField = false;
		for (String field : indexedFields) {
			containsField = query.contains(field + ":");
			if (containsField) {
				break;
			}
		}
		if (!containsField) {
			query = createKeywordsQuery(keywords, leadingWildcard, indexedFields);
		}
		return search(query);
	}

	/**
	 * Equivalent to {@code search(queryString, true, false)}.
	 * 
	 * @param queryString The full text query string.
	 * @return the query result.
	 * @see #search(String, boolean, boolean)
	 */
	protected List<E> search(String queryString) {
		return search(queryString, true, false);
	}

	/**
	 * Equivalent to {@code search(queryString, limitEntityType, false)}.
	 * 
	 * @param queryString The full text query string.
	 * @param limitEntityType {@code true} to limit the search to this Dao PersistentModel type ({@link E})
	 * @return the query result.
	 * @see #search(String, boolean, boolean)
	 */
	protected List<E> search(String queryString, boolean limitEntityType) {
		return search(queryString, limitEntityType, false);
	}

	/**
	 * Performs a full text query.
	 * 
	 * @param queryString The full text query string.
	 * @param limitEntityType {@code true} to limit the search to this Dao PersistentModel type ({@link E})
	 * @param leadingWildcard {@code true} to allow the use of leading wildcard on query parameters
	 * (field:*mail, for example). Use it carefully, since it can cause performance issues.
	 * 
	 * @return the query result.
	 */
	protected List<E> search(String queryString, boolean limitEntityType, boolean leadingWildcard) {
		Query query = createLuceneQuery(queryString, limitEntityType, leadingWildcard);
		FullTextQuery fullTextQuery = getFullTextSession().createFullTextQuery(query, getEntityClass());
		if (PagingDataHolder.isPagingSet()) {
			doPaging(fullTextQuery);
		}
		return fullTextQuery.list();
	}

	protected void doPaging(FullTextQuery fullTextQuery) {
		setupPaging(new FullTextQueryPagingSetup(fullTextQuery));
	}

	protected Query createLuceneQuery(String queryString, boolean limitEntityType, boolean leadingWildcard) {
		Analyzer analyzer = limitEntityType ? getAnalyzer() : getDefaultAnalyzer();
		QueryParser parser = new QueryParser(getLuceneVersion(), queryString, analyzer);
		parser.setAllowLeadingWildcard(leadingWildcard);
		Query query = null;
		try {
			query = parser.parse(queryString);
		} catch (ParseException e) {
			throw new HSearchQueryException(e.getMessage(), e);
		}
		return query;
	}

	protected String createKeywordsQuery(String param, boolean leadingWildcard, String... fields) {
		StringBuilder queryStr = new StringBuilder();
		for (int i = 0; i < fields.length; i++) {
			param = leadingWildcard ? addSurroundingWildcard(param) : addTrailingWildcard(param);
			queryStr.append(" ").append(fields[i]).append(": ").append(param);
		}
		return queryStr.toString().trim();
	}

	protected String[] getIndexedFields() {
		if (this.indexedFields == null) {
			PropertyDescriptor[] beanProperties = BeanUtils.getPropertyDescriptors(getEntityClass());
			List<String> fields = new ArrayList<String>();
			for (PropertyDescriptor propertyDescriptor : beanProperties) {
				Field field = ReflectionUtils.getAnnotation(Field.class, propertyDescriptor.getReadMethod());
				if (field == null) {
					java.lang.reflect.Field propertyField = FieldUtils.getField(getEntityClass(),
							propertyDescriptor.getName());
					if (propertyField != null && propertyField.isAnnotationPresent(Field.class)) {
						field = propertyField.getAnnotation(Field.class);
					}
				}
				if (field != null) {
					String fieldName = propertyDescriptor.getName();
					if (StringUtils.isNotBlank(fieldName)) {
						fieldName = field.name();
					}
					fields.add(fieldName);
				}
			}
		}
		return this.indexedFields;
	}

	protected String addSurroundingWildcard(String param) {
		return "*" + param + "*";
	}

	protected String addLeadingWildcard(String param) {
		return "*" + param;
	}

	protected String addTrailingWildcard(String param) {
		return param + "*";
	}

	protected Analyzer getAnalyzer() {
		return getFullTextSession().getSearchFactory().getAnalyzer(getEntityClass());
	}

	protected Analyzer getDefaultAnalyzer() {
		// TODO how to obtain from configuration?
		return new StandardAnalyzer(getLuceneVersion());
	}

	protected Version getLuceneVersion() {
		return luceneVersion;
	}

	public void setLuceneVersion(Version luceneVersion) {
		this.luceneVersion = luceneVersion;
	}

}
