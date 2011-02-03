/*
 * Dao.java created on 2010-03-15
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

import javax.annotation.PostConstruct;

import org.apache.commons.lang.ArrayUtils;
import org.brushingbits.jnap.bean.model.LogicalDelete;
import org.brushingbits.jnap.bean.model.PersistentModel;
import org.brushingbits.jnap.bean.paging.PagingDataHolder;
import org.brushingbits.jnap.bean.paging.PagingSetup;
import org.brushingbits.jnap.util.ReflectionUtils;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.hql.ast.QuerySyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


/**
 * <p>A base class for Hibernate powered data access objects (Dao).</p>
 * 
 * <p>Requires a {@link org.hibernate.SessionFactory} to be set. If only one is available on
 * the {@link org.springframework.context.ApplicationContext} it will be autowired. If you
 * have more than one {@code SessionFactory} then you must override the
 * {@link #setSessionFactory(SessionFactory)} to inject the proper factory for each 
 * {@code Dao} implementation or you can set it via Spring XML.</p>
 * 
 * @author Daniel Rochetti
 * @since 1.0
 *
 * @param <E> the {@code Entity} type handled by this {@code Dao}. It must implement {@link PersistentModel}.
 */
public abstract class Dao<E extends PersistentModel> {

	protected SessionFactory sessionFactory;
	protected boolean defaultPaging = true;
	protected Order defaultOrder;
	protected Class<E> entityClass;

	@Autowired
	protected void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@PostConstruct
	public void validateState() {
		Assert.notNull(this.sessionFactory);
	}

	/**
	 * Delete the given {@code PersistentModel} instance.
	 * @param entity the model to delete.
	 */
	public void delete(E entity) {
		if (entity instanceof LogicalDelete) {
			((LogicalDelete) entity).delete();
			update(entity);
		} else {
			getSession().delete(resolveEntityName(), entity);
		}
	}

	/**
	 * 
	 * @return
	 */
	public Integer countAll() {
		Criteria countAllCriteria = createCriteria();
		countAllCriteria.setProjection(Projections.rowCount());
		return ((Number) countAllCriteria.uniqueResult()).intValue();
	}

	/**
	 * Checks whether an instance of the model exists for the specified id.
	 * @param id The id of the instance.
	 * @return {@code true} if a persistent instance is found with the specified id
	 * or {@code false} otherwise.
	 */
	public boolean exists(Serializable id) {
		return countBy("Id", id) != 0;
	}

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	public List<E> findBy(String dynaQuery, Object... params) {
		return findByCriteria(createDynaQuery("findBy" + dynaQuery, params));
	}

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	public E findUniqueBy(String dynaQuery, Object... params) {
		return handleUniqueResult(findByCriteria(createDynaQuery("findUniqueBy" + dynaQuery, params)));
	}

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	public Integer countBy(String dynaQuery, Object... params) {
		return ((Number) createDynaQuery("countBy" + dynaQuery, params).uniqueResult()).intValue();
	}

	/**
	 * 
	 * @param entities
	 */
	public void delete(List<E> entities) {
		if (entities != null && !entities.isEmpty()) {
			for (E entity : entities) {
				delete(entity);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<E> findAll() {
		Criteria criteria = createCriteria();
		if (getDefaultOrder() != null) {
			criteria.addOrder(getDefaultOrder());
		}
		return findByCriteria(criteria);
	}

	/**
	 * 
	 * @param example
	 * @return
	 */
	public List<E> findByExample(E example) {
		Criteria criteria = createCriteria();
		criteria.add(Example.create(example).ignoreCase());
		if (getDefaultOrder() != null) {
			criteria.addOrder(getDefaultOrder());
		}
		return findByCriteria(criteria);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public E findById(Serializable id) {
		return (E) getSession().load(resolveEntityName(), id);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public E getById(Serializable id) {
		return (E) getSession().get(resolveEntityName(), id);
	}

	/**
	 * 
	 * @param entity
	 */
	public void insert(E entity) {
		getSession().save(resolveEntityName(), entity);
	}

	/**
	 * 
	 * @param entity
	 */
	public void update(E entity) {
		getSession().update(resolveEntityName(), entity);
	}

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	protected Criteria createDynaQuery(String dynaQuery, Object... params) {
		DynaQueryBuilder dynaQueryBuilder = new DynaQueryBuilder(getSession(), resolveEntityName(),
				dynaQuery, params);
		return dynaQueryBuilder.build();
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	protected Integer count(Query query) {
		if (!query.getQueryString().toLowerCase().startsWith("select count(")) {
			throw new QuerySyntaxException("The count query must start with a 'select count clause'",
					query.getQueryString());
		}
		Number quantity = (Number) query.uniqueResult();
		return quantity == null ? 0 : quantity.intValue();
	}

	/**
	 * 
	 * @param hql
	 * @param params
	 * @return
	 */
	protected Integer count(String hql, Object... params) {
		Query query = getSession().createQuery(hql);
		QueryUtils.setIndexedParameters(query, params);
		return count(query);
	}

	/**
	 * 
	 * @param hql
	 * @param params
	 * @return
	 */
	protected Integer count(String hql, Map<String, ?> params) {
		Query query = getSession().createQuery(hql);
		QueryUtils.setNamedParameters(query, params);
		return count(query);
	}

	/**
	 * 
	 * @param hql
	 * @param paging
	 * @param params
	 * @return
	 */
	private List<E> doQuery(String hql, boolean paging, Object params) {
		Query query = getSession().createQuery(hql);
		QueryUtils.setParameters(query, params);
		if (paging && PagingDataHolder.isPagingSet()) {
			doPaging(query, params);
		}
		return query.list();
	}

	/**
	 * 
	 * @param hql
	 * @return
	 */
	protected List<E> find(String hql) {
		return find(hql, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	/**
	 * 
	 * @param hql
	 * @param paging
	 * @return
	 */
	protected List<E> find(String hql, boolean paging) {
		return find(hql, paging, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	/**
	 * 
	 * @param hql
	 * @param paging
	 * @param namedParams
	 * @return
	 */
	protected List<E> find(String hql, boolean paging, Map<String, ?> namedParams) {
		return doQuery(hql, paging, namedParams);
	}

	/**
	 * 
	 * @param hql
	 * @param paging
	 * @param params
	 * @return
	 */
	protected List<E> find(String hql, boolean paging, Object... params) {
		return doQuery(hql, paging, params);
	}

	/**
	 * 
	 * @param hql
	 * @param params
	 * @return
	 */
	protected List<E> find(String hql, List<?> params) {
		return find(hql, params.toArray());
	}

	/**
	 * 
	 * @param hql
	 * @param namedParams
	 * @return
	 */
	protected List<E> find(String hql, Map<String, ?> namedParams) {
		return find(hql, this.defaultPaging, namedParams);
	}

	/**
	 * 
	 * @param hql
	 * @param params
	 * @return
	 */
	protected List<E> find(String hql, Object... params) {
		return find(hql, this.defaultPaging, params);
	}

	/**
	 * 
	 * @return
	 */
	protected Criteria createCriteria() {
		return getSession().createCriteria(resolveEntityName());
	}

	/**
	 * 
	 * @param criteria
	 * @return
	 */
	protected List<E> findByCriteria(Criteria criteria) {
		return findByCriteria(criteria, this.defaultPaging);
	}

	/**
	 * 
	 * @param criteria
	 * @param paging
	 * @return
	 */
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

	/**
	 * 
	 * @param example
	 * @return
	 */
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
	public Class<E> getEntityClass() {
		if (this.entityClass == null) {
			this.entityClass = (Class<E>) ReflectionUtils.getParametrizedType(getClass());
		}
		return this.entityClass;
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

	/**
	 * 
	 * @param result
	 * @return
	 * @throws QueryException
	 */
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

	/**
	 * 
	 * @return
	 */
	protected final String resolveEntityName() {
		return getEntityName() != null ? getEntityName() : getEntityClass().getName();
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
	 * 
	 * @param criteria The criteria that will be configured for pagination.
	 */
	protected void doPaging(Criteria criteria) {
		setupPaging(new CriteriaPagingSetup(criteria));
	}

	/**
	 * Setup paging for the {@link Query}.
	 * 
	 * @param query The query that will be configured for pagination.
	 * @param queryParams The query parameters.
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
