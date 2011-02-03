/*
 * GenericDao.java created on 2011-02-02
 *
 * Copyright 2011 Brushing Bits, Inc.
 * http://www.brushingbits.com
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
package org.brushingbits.jnap.persistence.factory;

import org.brushingbits.jnap.bean.model.PersistentModel;
import org.brushingbits.jnap.persistence.hibernate.Dao;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
@Repository
public class GenericDao<E extends PersistentModel> extends Dao<E> {

	public GenericDao(Class<E> entityClass) {
		this.entityClass = entityClass;
	}

	protected GenericDao() {
	}

	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}
	
}
