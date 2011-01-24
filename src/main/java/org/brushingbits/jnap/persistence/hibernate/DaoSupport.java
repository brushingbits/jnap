/*
 * DaoSupport.java created on 2010-03-15
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.brushingbits.jnap.bean.model.LogicalDelete;
import org.brushingbits.jnap.bean.model.PersistentModel;
import org.brushingbits.jnap.bean.paging.PagingDataHolder;
import org.brushingbits.jnap.bean.paging.PagingSetup;
import org.brushingbits.jnap.persistence.Dao;
import org.brushingbits.jnap.util.ReflectionUtils;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.hql.ast.QuerySyntaxException;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * <p>A base class for Hibernate powered data access objects (Dao).</p>
 * <p>Requires a {@link org.hibernate.SessionFactory} to be set. If only one is available on
 * the {@link org.springframework.context.ApplicationContext} it will be autowired. If you
 * have more than {@code SessionFactory} then override the {@link #setSessionFactory(SessionFactory)}
 * to inject the proper factory for each {@code Dao} implementation.</p>
 * 
 * @author Daniel Rochetti
 * @since 1.0
 *
 * @param <E> the {@code Entity} type handled by this {@code Dao}. It must implements {@link PersistentModel}.
 */
public abstract class DaoSupport<E extends PersistentModel> implements Dao<E> {

	protected SessionFactory sessionFactory;
	protected boolean defaultPaging = true;
	protected Order defaultOrder;

	@Autowired
	protected void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void delete(E entity) {
		if (entity instanceof LogicalDelete) {
			((LogicalDelete) entity).delete();
			update(entity);
		} else {
			if (StringUtils.isNotBlank(getEntityName())) {
				getSession().delete(getEntityName(), entity);
			} else {
				getSession().delete(entity);
			}
		}
	}
	
	public void delete(List<E> entities) {
		if (entities != null && !entities.isEmpty()) {
			for (E entity : entities) {
				delete(entity);
			}
		}
	}

	public List<E> findAll() {
		StringBuilder hql = new StringBuilder("from " + resolveEntityName());
		if (getDefaultOrder() != null) {
			hql.append(" order by ").append(getDefaultOrder().toString());
		}
		return find(hql.toString());
	}

	public List<E> findByExample(E example) {
		Criteria criteria = createCriteria();
		criteria.add(Example.create(example).ignoreCase());
		if (getDefaultOrder() != null) {
			criteria.addOrder(getDefaultOrder());
		}
		return findByCriteria(criteria);
	}

	public E findById(Serializable id) {
		E result = null;
		if (StringUtils.isNotBlank(getEntityName())) {
			result = (E) getSession().load(getEntityName(), id);
		} else {
			result = (E) getSession().load(getEntityClass(), id);
		}
		return result;
	}

	public E getById(Serializable id) {
		E result = null;
		if (StringUtils.isNotBlank(getEntityName())) {
			result = (E) getSession().get(getEntityName(), id);
		} else {
			result = (E) getSession().get(getEntityClass(), id);
		}
		return result;
	}

	public void update(E entity) {
		if (StringUtils.isNotBlank(getEntityName())) {
			getSession().update(getEntityName(), entity);
		} else {
			getSession().update(entity);
		}
	}

	protected Integer count(Query query) {
		if (!query.getQueryString().toLowerCase().startsWith("select count(")) {
			throw new QuerySyntaxException("The count query must start with a 'select count clause'",
					query.getQueryString());
		}
		Number quantity = (Number) query.uniqueResult();
		return quantity == null ? 0 : quantity.intValue();
	}

	protected Integer count(String hql, Object... params) {
		Query query = getSession().createQuery(hql);
		QueryUtils.setIndexedParameters(query, params);
		return count(query);
	}
	
	protected Integer count(String hql, Map<String, ?> params) {
		Query query = getSession().createQuery(hql);
		QueryUtils.setNamedParameters(query, params);
		return count(query);
	}

	private List<E> doQuery(String hql, boolean paging, Object params) {
		Query query = getSession().createQuery(hql);
		QueryUtils.setParameters(query, params);
		if (paging && PagingDataHolder.isPagingSet()) {
			doPaging(query, params);
		}
		return query.list();
	}

	protected List<E> find(String hql) {
		return find(hql, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	protected List<E> find(String hql, boolean paging) {
		return find(hql, paging, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	protected List<E> find(String hql, boolean paging, Map<String, ?> namedParams) {
		return doQuery(hql, paging, namedParams);
	}

	protected List<E> find(String hql, boolean paging, Object... params) {
		return doQuery(hql, paging, params);
	}

	protected List<E> find(String hql, List<?> params) {
		return find(hql, params.toArray());
	}

	protected List<E> find(String hql, Map<String, ?> namedParams) {
		return find(hql, this.defaultPaging, namedParams);
	}

	protected List<E> find(String hql, Object... params) {
		return find(hql, this.defaultPaging, params);
	}

	/**
	 * @return
	 */
	protected Criteria createCriteria() {
		Criteria criteria = null;
		if (StringUtils.isNotBlank(getEntityName())) {
			criteria = getSession().createCriteria(getEntityName());
		} else {
			criteria = getSession().createCriteria(getEntityClass());
		}
		return criteria;
	}

	protected List<E> findByCriteria(Criteria criteria) {
		return findByCriteria(criteria, this.defaultPaging);
	}

	protected List<E> findByCriteria(Criteria criteria, boolean paging) {
		if (paging && PagingDataHolder.isPagingSet()) {
			doPaging(criteria);
		}
		return criteria.list();
	}

	/**
	 * 
	 * @param hql
	 * @param params
	 * @return
	 * @throws QueryException
	 */
	protected E findUnique(String hql, Object... params) throws QueryException {
		return handleUniqueResult(find(hql, false, params));
	}

	public E findUniqueByExample(E example) {
		return handleUniqueResult(findByExample(example));
	}

	/**
	 * 
	 * @return
	 */
	protected Order getDefaultOrder() {
		return defaultOrder;
	}

	/**
	 * 
	 * @return
	 */
	protected Class<E> getEntityClass() {
		return (Class<E>) ReflectionUtils.getParametrizedType(getClass());
	}

	/**
	 * 
	 * @return
	 */
	protected String getEntityName() {
		return null;
	}

	/**
	 * Gets the current session.
	 * 
	 * @return the current Hibernate {@link Session}.
	 * @see SessionFactory#getCurrentSession()
	 */
	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	protected E handleUniqueResult(List<E> result) throws QueryException {
		E uniqueResult = null;
		if (result != null && result.size() > 0) {
			if (result.size() == 1) {
				uniqueResult = result.get(0);
			} else {
				throw new NonUniqueResultException(result.size());
			}
		}
		return uniqueResult;
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#insert(org.brushingbits.jnap.bean.model.PersistentModel)
	 */
	public void insert(E entity) {
		if (StringUtils.isNotBlank(getEntityName())) {
			getSession().save(getEntityName(), entity);
		} else {
			getSession().save(entity);
		}
	}

	/**
	 * 
	 * @return
	 */
	protected final String resolveEntityName() {
		return getEntityName() != null ? getEntityName() : getEntityClass().getSimpleName();
	}

	protected String addSurroundingLikeChar(String param) {
		return "%" + param + "%";
	}

	protected String addLeadingLikeChar(String param) {
		return "%" + param;
	}
	
	protected String addTrailingLikeChar(String param) {
		return param + "%";
	}

	/**
	 * Setup paging for the {@link Criteria}.
	 * @param criteria
	 */
	protected void doPaging(Criteria criteria) {
		setupPaging(new CriteriaPagingSetup(criteria));
	}

	/**
	 * Setup paging for the {@link Query}.
	 * @param query
	 * @param queryParams
	 */
	protected void doPaging(Query query, Object queryParams) {
		setupPaging(new QueryPagingSetup(query, getSession(), queryParams));
	}

	/**
	 * 
	 * @param pagingSetup
	 */
	protected void setupPaging(PagingSetup pagingSetup) {
		PagingDataHolder.setTotal(pagingSetup.countTotal());
		final int resultsPerPage = PagingDataHolder.getResultsPerPage();
		int first = (PagingDataHolder.getCurrentPage() - 1) * resultsPerPage;
		pagingSetup.setFirstResult(first);
		pagingSetup.setResultsPerPage(resultsPerPage);
	}

}
