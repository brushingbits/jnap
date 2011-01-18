/*
 * DynaModel.java created on 2010-03-02
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
package org.brushingbits.jnap.bean.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public final class DynaModel implements Model {

	private Map<String, Object> values;

	public DynaModel() {
		this.values = new HashMap<String, Object>();
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void clear() {
		values.clear();
	}

	public boolean containsKey(Object key) {
		return values.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return values.containsValue(value);
	}

	public Object get(Object key) {
		return values.get(key);
	}

	public Object set(String key, Object value) {
		return values.put(key, value);
	}

	public Object remove(Object key) {
		return values.remove(key);
	}

	public int size() {
		return values.size();
	}

	
}
