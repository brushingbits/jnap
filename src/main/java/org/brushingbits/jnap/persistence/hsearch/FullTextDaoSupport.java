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

import java.io.Serializable;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.brushingbits.jnap.bean.model.IndexedModel;
import org.brushingbits.jnap.persistence.FullTextDao;
import org.brushingbits.jnap.persistence.hibernate.DaoSupport;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;


/**
 * 
 * @author Daniel Rochetti
 *
 * @param <E>
 * @see IndexedModel
 */
public abstract class FullTextDaoSupport<E extends IndexedModel>
		extends DaoSupport<E> implements FullTextDao<E> {

	protected static ThreadLocal<FullTextSession> fullTextSessionHolder = new ThreadLocal<FullTextSession>();

	protected Version luceneVersion = Version.LUCENE_29;

	protected FullTextSession getFullTextSession() {
		FullTextSession session = fullTextSessionHolder.get();
		if (session == null || !session.isOpen()) {
			session = Search.getFullTextSession(getSession());
			fullTextSessionHolder.set(session);
		}
		return session;
	}

	public static List<? extends IndexedModel<? extends Serializable>> searchEntities(String keywords,
			Class<? extends IndexedModel<? extends Serializable>>... entities) {
		return null;
	}

	public E searchByDocId(Serializable docId) {
		return (E) getFullTextSession().get(getEntityClass(), docId);
	}

	public List<E> searchByKeywords(String keywords) {
		// TODO Auto-generated method stub
		return null;
	}

	protected List<E> search(String queryString) {
		return search(queryString, true, false);
	}

	protected List<E> search(String queryString, boolean limitEntityType) {
		return search(queryString, limitEntityType, false);
	}

	protected List<E> search(String queryString, boolean limitEntityType, boolean leadingWildcard) {
		Query query = createLuceneQuery(queryString, limitEntityType, leadingWildcard);
		FullTextQuery fullTextQuery = getFullTextSession().createFullTextQuery(query, getEntityClass());
		doPaging(fullTextQuery);
		return fullTextQuery.list();
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

	protected String createKeywordsQuery(String param, String... fields) {
		StringBuilder queryStr = new StringBuilder();
		for (int i = 0; i < fields.length; i++) {
			queryStr.append(" ").append(fields[i]).append(": ").append(addSurroundingWildcard(param));
		}
		return queryStr.toString().trim();
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

	/**
	 * <code>Accessor</code> ("getter") method for property <code>luceneVersion</code>.
	 */
	protected Version getLuceneVersion() {
		return luceneVersion;
	}

	/**
	 * <code>Mutator</code> ("setter") method for property <code>luceneVersion</code>.
	 */
	public void setLuceneVersion(Version luceneVersion) {
		this.luceneVersion = luceneVersion;
	}

}
