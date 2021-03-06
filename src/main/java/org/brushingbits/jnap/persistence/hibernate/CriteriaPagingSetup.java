/*
 * QueryPagingSetup.java created on 2010-06-06
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

import org.brushingbits.jnap.bean.paging.PagingSetup;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;


/**
 * A {@link PagingSetup} adapter for {@link Criteria} queries.
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class CriteriaPagingSetup implements PagingSetup {

	private Criteria criteria;

	public CriteriaPagingSetup(Criteria criteria) {
		this.criteria = criteria;
	}

	public void setFirstResult(int first) {
		this.criteria.setFirstResult(first);
	}

	public void setResultsPerPage(int max) {
		this.criteria.setMaxResults(max);
	}

	public int countTotal() {
		// set the count() projection
		criteria.setProjection(Projections.rowCount());
		int total = (Integer) criteria.uniqueResult();

		// reset criteria
		criteria.setProjection(null);
		criteria.setResultTransformer(Criteria.ROOT_ENTITY);
		
		return total;
	}

}
