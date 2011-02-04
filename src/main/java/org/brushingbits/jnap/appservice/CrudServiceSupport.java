package org.brushingbits.jnap.appservice;

import java.io.Serializable;
import java.util.List;

import org.brushingbits.jnap.bean.model.PersistentModel;
import org.brushingbits.jnap.persistence.hibernate.Dao;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * @author Daniel Rochetti
 *
 * @param <E>
 * 
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.stereotype.Service
 */
public abstract class CrudServiceSupport<E extends PersistentModel> {
	

	@Transactional(readOnly = true)
	public List<E> findAll() {
		return getDao().findAll();
	}

	@Transactional(readOnly = true)
	public List<E> findByExample(E example) {
		return getDao().findByExample(example);
	}

	@Transactional(readOnly = true)
	public E findUniqueByExample(E example) {
		return getDao().findUniqueByExample(example);
	}

	@Transactional(readOnly = true)
	public E findById(Serializable id) {
		return getDao().findById(id);
	}

	@Transactional
	public void insert(E entity) {
		getDao().insert(entity);
	}

	@Transactional
	public void update(E entity) {
		getDao().update(entity);
	}

	@Transactional
	public void delete(E entity) {
		getDao().delete(entity);
	}

	protected abstract Dao<E> getDao();

}
