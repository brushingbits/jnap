/*
 * PagingSetup.java created on 2010-06-05
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
package org.brushingbits.jnap.bean.paging;

/**
 * A interface to abstract the behavior of paging (range and total) across different query needs,
 * such as {@link org.hibernate.Query}, {@link org.hibernate.Criteria}
 * and {@link org.hibernate.search.FullTextQuery}.
 * The implementation is responsible to set the first result number ((current - 1) * perpage),
 * the max results (or results per page) and count the total number of results (without paging).
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public interface PagingSetup {

	/**
	 * Sets the number of the first row.
	 * @param first the row number, starting at <tt>0</tt>.
	 */
	public void setFirstResult(int first);

	/**
	 * Set the maximum number of rows to retrieve per page.
	 * @param max the maximum number of rows.
	 */
	public void setResultsPerPage(int max);

	/**
	 * Count the total number of results the database query would return without the
	 * paging parameters.
	 * @return the total number of results.
	 */
	public int countTotal();

}
