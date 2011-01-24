/*
 * FullTextQueryPagingSetup.java created on 2011-01-23
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

import org.brushingbits.jnap.bean.paging.PagingSetup;
import org.hibernate.search.FullTextQuery;

/**
 * A {@link PagingSetup} adapter for {@code full text (e.g. Lucene)} queries.
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class FullTextQueryPagingSetup implements PagingSetup {

	private FullTextQuery fullTextQuery;

	public FullTextQueryPagingSetup(FullTextQuery fullTextQuery) {
		this.fullTextQuery = fullTextQuery;
	}

	public void setFirstResult(int first) {
		this.fullTextQuery.setFirstResult(first);
	}

	public void setResultsPerPage(int max) {
		this.fullTextQuery.setMaxResults(max);
	}

	public int countTotal() {
		return this.fullTextQuery.getResultSize();
	}

}
