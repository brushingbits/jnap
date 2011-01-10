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
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;


/**
 * @author Daniel Rochetti
 *
 */
public class QueryPagingSetup implements PagingSetup {

	private Query query;

	public QueryPagingSetup(Query query) {
		this.query = query;
	}

	public void setFirstResult(int first) {
		this.query.setFirstResult(first);
	}

	public void setResultsPerPage(int max) {
		this.query.setMaxResults(max);

	}

	public int countTotal() {
		ScrollableResults cursor = query.scroll(ScrollMode.FORWARD_ONLY);
		cursor.last();
		int total = cursor.getRowNumber() + 1;
		cursor.close();
		return total;
	}

}
