/*
 * Groups.java created on 2010-03-26
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
package org.brushingbits.jnap.validation;


/**
 * 
 * @see javax.validation.groups.Default
 * @author Daniel Rochetti
 * @since 1.0
 */
public final class Groups {

	/**
	 * This class has only <code>inner interfaces</code> and cannot be directly instantiated.
	 */
	private Groups() {}

	/**
	 * A tag interface for grouping validation. Should be used as the first group being validated
	 * in a wizard like validation flow.
	 */
	public static interface FirstStep {}

	/**
	 * A tag interface for grouping validation. Should be used as the second group being validated
	 * in a wizard like validation flow.
	 */
	public static interface SecondStep {}

	/**
	 * A tag interface for grouping validation. Should be used as the third group being validated
	 * in a wizard like validation flow.
	 */
	public static interface ThirdStep {}

	/**
	 * A tag interface for grouping validation. Should be used as the fourth group being validated
	 * in a wizard like validation flow.
	 */
	public static interface FourthStep {}

	/**
	 * A tag interface for grouping validation. Should be used during a CRUD operation of type "Create"
	 * when grouping validation is needed.
	 */
	public static interface CreateOp {}

	/**
	 * A tag interface for grouping validation. Should be used during a CRUD operation of type "Update"
	 * when grouping validation is needed.
	 */
	public static interface UpdateOp {}

	/**
	 * A tag interface for grouping validation. Should be used during a CRUD operation of type "Delete"
	 * when grouping validation is needed.
	 */
	public static interface DeleteOp {}
}
