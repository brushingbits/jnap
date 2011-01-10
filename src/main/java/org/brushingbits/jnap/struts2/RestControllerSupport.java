/*
 * RestControllerSupport.java created on 19/03/2010
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
package org.brushingbits.jnap.struts2;

import java.util.List;

import javax.validation.Valid;

import org.brushingbits.jnap.bean.model.Model;
import org.brushingbits.jnap.util.ReflectionUtils;

import com.opensymphony.xwork2.ActionSupport;

/**
 * 
 * @author Daniel Rochetti
 *
 * @param <M>
 */
public class RestControllerSupport<M extends Model> extends ActionSupport {

	/**
	 * 
	 */
	public static final String INDEX = "index";

	/**
	 * 
	 */
	public static final String SHOW = "show";

	/**
	 * 
	 */
	public static final String EDIT_NEW = "editNew";

	/**
	 * 
	 */
	public static final String EDIT = "edit";


	@Valid
	protected M model;
	protected M searchModel;
	protected List<M> modelList;

	/**
	 * 
	 */
	protected void refreshModel() {
		resetModel();
	}

	/**
	 * 
	 */
	protected void resetModel() {
		setModel((M) ReflectionUtils.newInstance(ReflectionUtils.getParametrizedType(getClass())));
	}

	public M getModel() {
		if (model == null) {
			resetModel();
		}
		return model;
	}

	public void setModel(M model) {
		this.model = model;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>searchModel</code>.
	 */
	public M getSearchModel() {
		return searchModel;
	}

	/**
	 * <code>Mutator</code> ("setter") method for property <code>searchModel</code>.
	 */
	public void setSearchModel(M searchModel) {
		this.searchModel = searchModel;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>modelList</code>.
	 */
	public List<M> getModelList() {
		return modelList;
	}

	/**
	 * <code>Mutator</code> ("setter") method for property <code>modelList</code>.
	 */
	public void setModelList(List<M> result) {
		this.modelList = result;
	}

}
