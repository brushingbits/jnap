/*
 * Dao.java created on 15/03/2010
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
package org.brushingbits.jnap.persistence;

import java.io.Serializable;
import java.util.List;

import org.brushingbits.jnap.bean.model.PersistentModel;


/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 *
 * @param <E>
 */
public interface Dao<E extends PersistentModel> {

	public void insert(E entity);

	public void update(E entity);

	public void delete(E entity);

	public void delete(List<E> entities);

	public E getById(Serializable id);

	public E findById(Serializable id);

	public List<E> findAll();

	public List<E> findByExample(E example);

	public E findUniqueByExample(E example);

}
