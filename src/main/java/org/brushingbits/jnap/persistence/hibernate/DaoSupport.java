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
import org.hibernate.criterion.Order;
import org.hibernate.hql.ast.QuerySyntaxException;
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
	protected boolean defaultPaging = false;
	protected Order defaultOrder;

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
		setIndexedParameters(query, params);
		return count(query);
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#delete(org.brushingbits.jnap.bean.model.PersistentModel)
	 */
	public void delete(E entity) {
		if (entity instanceof LogicalDelete) {
			((LogicalDelete) entity).delete();
			update(entity);
		} else {
			getSession().delete(resolveEntityName(), entity);
		}
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

	protected void doPaging(Criteria criteria) {
		setupPaging(new CriteriaPagingSetup(criteria));
	}

	protected void doPaging(Query query) {
		setupPaging(new QueryPagingSetup(query));
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
		if (paging) {
			doPaging(query);
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

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#findById(java.io.Serializable)
	 */
	public E findById(Serializable id) {
		return (E) getSession().load(resolveEntityName(), id);
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

	
	public E getById(Serializable id) {
		return (E) getSession().get(resolveEntityName(), id);
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
		getSession().save(resolveEntityName(), entity);
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

	@Autowired
	protected void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private void setupPaging(PagingSetup pagingSetup) {
		pagingSetup.setFirstResult(PagingDataHolder.getCurrentPage());
		pagingSetup.setResultsPerPage(PagingDataHolder.getResultsPerPage());
		PagingDataHolder.setTotal(pagingSetup.countTotal());
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.persistence.Dao#update(org.brushingbits.jnap.bean.model.PersistentModel)
	 */
	public void update(E entity) {
		getSession().update(resolveEntityName(), entity);
	}

}
