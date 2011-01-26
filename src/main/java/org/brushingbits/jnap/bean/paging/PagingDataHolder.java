/*
 * PagingDataHolder.java created on 2010-06-05
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
 * @author Daniel Rochetti
 * @since 1.0
 */
public class PagingDataHolder {

	private static ThreadLocal<Integer> currentPage = new ThreadLocal<Integer>();

	private static ThreadLocal<Integer> resultsPerPage = new ThreadLocal<Integer>();

	private static ThreadLocal<Integer> total = new ThreadLocal<Integer>();


	public static Integer getCurrentPage() {
		return currentPage.get();
	}

	public static void setCurrentPage(Integer currentPage) {
		PagingDataHolder.currentPage.set(currentPage);
	}

	public static Integer getResultsPerPage() {
		return resultsPerPage.get();
	}

	public static void setResultsPerPage(Integer perPage) {
		PagingDataHolder.resultsPerPage.set(perPage);
	}

	public static Integer getTotal() {
		return total.get();
	}

	public static void setTotal(Integer total) {
		PagingDataHolder.total.set(total);
	}

	/**
	 * Clears all pagination parameters.
	 */
	public static void clear() {
		PagingDataHolder.setCurrentPage(null);
		PagingDataHolder.setResultsPerPage(null);
		PagingDataHolder.setTotal(null);
	}

	/**
	 * Verifies if the paging parameters were set.
	 * @return <code>true</code> if the parameters were set or
	 * <code>false</code> otherwise.
	 */
	public static boolean isPagingSet() {
		return getCurrentPage() != null && getResultsPerPage() != null;
	}

	/**
	 * A simple JavaBean that represents a paging state.
	 * 
	 * @author Daniel
	 * @since 1.0
	 */
	public static class PagingData {

		private Integer currentPage;
		private Integer resultsPerPage;
		private Integer totalResults;

		private PagingData() {
		}

		public static PagingData getCurrent() {
			PagingData current = null;
			if (PagingDataHolder.isPagingSet()) {
				current = new PagingData();
				current.currentPage = PagingDataHolder.getCurrentPage();
				current.resultsPerPage = PagingDataHolder.getResultsPerPage();
				current.totalResults = PagingDataHolder.getTotal();
			}
			return current;
		}

		public Integer getCurrentPage() {
			return currentPage;
		}

		public Integer getResultsPerPage() {
			return resultsPerPage;
		}

		public Integer getTotalResults() {
			return totalResults;
		}

	}
}
