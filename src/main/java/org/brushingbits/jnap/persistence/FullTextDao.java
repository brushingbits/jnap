/*
 * FullTextDao.java created on 2010-03-15
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

import org.brushingbits.jnap.bean.model.IndexedModel;


/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 *
 * @param <E>
 */
public interface FullTextDao<E extends IndexedModel> extends Dao<E> {

	/**
	 * 
	 * @param docId
	 * @return
	 */
	public E searchByDocId(Serializable docId);

	/**
	 * 
	 * @param keywords
	 * @return
	 */
	public List<E> searchByKeywords(String keywords);

	/**
	 * 
	 * @param keywords
	 * @param leadingWildcard
	 * @return
	 */
	public List<E> searchByKeywords(String keywords, boolean leadingWildcard);

}
