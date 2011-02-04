/*
 * RestCrudControllerSupport.java created on 2010-03-20
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
package org.brushingbits.jnap.web;

import javax.validation.groups.Default;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.brushingbits.jnap.appservice.CrudServiceSupport;
import org.brushingbits.jnap.bean.model.PersistentModel;
import org.brushingbits.jnap.validation.Groups;
import org.brushingbits.jnap.validation.ValidationConfig;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * A base REST controller that is capable of CRUD (Create, Read, Update, Delete) operations for a
 * {@link PersistentModel} out of the box.
 * 
 * @author Daniel Rochetti
 *
 * @param <E> type of the {@code PersistentModel} that will be handled by CRUD operations.
 */
public abstract class RestCrudControllerSupport<E extends PersistentModel> extends RestControllerSupport<E> {

	@Autowired
	protected CrudServiceSupport<E> crudService;

	@GET
	@Path("/")
	public Response index() {
		return Response.ok(INDEX);
	}

	@GET
	@Path("/list")
	public Response list() {
		this.modelList = crudService.findAll();
		return Response.ok(INDEX);
	}

	@GET
	@Path("/{model.id}")
	public Response show() {
		refreshModel();
		return Response.ok(SHOW);
	}

	@GET
	@Path("/new")
	public Response editNew() {
		resetModel();
		return Response.ok(EDIT_NEW);
	}

	@POST
	@Path("/")
	@ValidationConfig(groups = { Default.class, Groups.CreateOp.class })
	public Response create() {
		crudService.insert(model);
		return Response.ok(EDIT_NEW);
	}

	@GET
	@Path("/{model.id}/edit")
	public Response edit() {
		refreshModel();
		return Response.ok(EDIT);
	}

	@PUT
	@Path("/{model.id}")
	@ValidationConfig(groups = { Default.class, Groups.UpdateOp.class })
	public Response update() {
		crudService.update(model);
		return Response.ok(EDIT);
	}

	@DELETE
	@Path("/{model.id}")
	@ValidationConfig(groups = { Default.class, Groups.DeleteOp.class })
	public Response delete() {
		crudService.delete(model);
		return Response.ok(INDEX);
	}

	/**
	 * TODO
	 */
	@Override
	protected void refreshModel() {
		this.model = crudService.findById(model.getId());
	}

}
