/*
 * DaoSupport.java created on 15/03/2010
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
import org.brushingbits.jnap.bean.model.PersistentModel;
import org.brushingbits.jnap.bean.paging.PagingDataHolder;
import org.brushingbits.jnap.bean.paging.PagingSetup;
import org.brushingbits.jnap.persistence.Dao;
import org.brushingbits.jnap.util.ReflectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 *
 * @param <E>
 */
public abstract class DaoSupport<E extends PersistentModel> implements Dao<E> {

	protected SessionFactory sessionFactory;
	protected boolean defaultPaging = true;
	protected Order defaultOrder;

	@Autowired
	protected void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	private List<E> doQuery(String hql, boolean paging, Object params) {

		Query query = getSession().createQuery(hql);
		if (params != null) {
			// testing if the query is using 'Named Parameters' or 'Indexed Parameters'
			if (params.getClass().isArray()) {
				setIndexedParameters(query, (Object[]) params);
			} else if (params.getClass().isAssignableFrom(Map.class)) {
				setNamedParameters(query, (Map<String, ?>) params);
			}
		}

		return query.list();
	}

	/**
	 * 
	 * @param query
	 * @param params
	 */
	protected void setNamedParameters(Query query, Map<String, ?> params) {
		if (params != null && !params.isEmpty()) {
			for (String name : params.keySet()) {
				query.setParameter(name, params.get(name));
			}
		}
	}

	/**
	 * 
	 * @param query
	 * @param params
	 */
	protected void setIndexedParameters(Query query, Object[] params) {
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				query.setParameter(i, params[i]);
			}
		}
	}

	protected void configurePaging() {
		
	}

	protected List<E> find(String hql, boolean paging, Object... params) {
		return doQuery(hql, paging, params);
	}

	protected List<E> find(String hql, Object... params) {
		return find(hql, this.defaultPaging, params);
	}

	protected List<E> find(String hql, List<?> params) {
		return find(hql, params.toArray());
	}

	protected List<E> find(String hql) {
		return find(hql, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	protected List<E> find(String hql, boolean paging) {
		return find(hql, paging, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	protected List<E> find(String hql, Map<String, ?> namedParams) {
		return find(hql, this.defaultPaging, namedParams);
	}

	protected List<E> find(String hql, boolean paging, Map<String, ?> namedParams) {
		return doQuery(hql, paging, namedParams);
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#delete(org.brushingbits.jnap.bean.model.PersistentModel)
	 */
	public void delete(E entity) {
		getSession().delete(resolveEntityName(), entity);
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#delete(java.util.List)
	 */
	public void delete(List<E> entities) {
		if (entities != null && !entities.isEmpty()) {
			for (E entity : entities) {
				delete(entity);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#findAll()
	 */
	public List<E> findAll() {
		StringBuilder hql = new StringBuilder("from " + resolveEntityName());
		if (getDefaultOrder() != null) {
			hql.append(" order by ").append(getDefaultOrder().toString());
		}
		return find(hql.toString());
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#findByExample(org.brushingbits.jnap.bean.model.PersistentModel)
	 */
	public List<E> findByExample(E example) {
		Criteria criteria = getSession().createCriteria(resolveEntityName());
		if (getDefaultOrder() != null) {
			criteria.addOrder(getDefaultOrder());
		}
		return criteria.list();
	}

	
	public E findUniqueByExample(E example) {
		List<E> result = findByExample(example);
		E unique = null;
		if (result != null && result.size() == 1) {
			unique = result.get(0);
		}
		return unique;
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#findById(java.io.Serializable)
	 */
	public E findById(Serializable id) {
		return (E) getSession().load(resolveEntityName(), id);
	}

	public E getById(Serializable id) {
		return (E) getSession().get(resolveEntityName(), id);
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#insert(org.brushingbits.jnap.bean.model.PersistentModel)
	 */
	public void insert(E entity) {
		getSession().save(resolveEntityName(), entity);
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#update(org.brushingbits.jnap.bean.model.PersistentModel)
	 */
	public void update(E entity) {
		getSession().update(resolveEntityName(), entity);
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
	 * 
	 * @return
	 */
	protected final String resolveEntityName() {
		return getEntityName() != null ? getEntityName() : getEntityClass().getSimpleName();
	}

	/**
	 * 
	 * @return
	 */
	protected Order getDefaultOrder() {
		return defaultOrder;
	}

	protected void doPaging(Query query) {
		setupPaging(new QueryPagingSetup(query));
	}

	protected void doPaging(Criteria criteria) {
		setupPaging(new CriteriaPagingSetup(criteria));
	}

	private void setupPaging(PagingSetup pagingSetup) {
		pagingSetup.setFirstResult(PagingDataHolder.getCurrentPage());
		pagingSetup.setResultsPerPage(PagingDataHolder.getResultsPerPage());
		PagingDataHolder.setTotal(pagingSetup.countTotal());
	}

}
