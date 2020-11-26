/*
 * Copyright 2017, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.model.security;

import org.openremote.model.Constants;
import org.openremote.model.http.RequestParams;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Manage users in realms and get info of current user.
 */
// TODO Relax permissions to allow regular users to maintain their own realm
@Path("user")
public interface UserResource {

    @GET
    @Produces(APPLICATION_JSON)
    @Path("{realm}")
User[] getAll(@BeanParam RequestParams requestParams, @PathParam("realm") String realm);

    @GET
    @Path("{realm}/{userId}")
    @Produces(APPLICATION_JSON)
User get(@BeanParam RequestParams requestParams, @PathParam("realm") String realm, @PathParam("userId") String userId);

    @GET
    @Produces(APPLICATION_JSON)
User getCurrent(@BeanParam RequestParams requestParams);

    @PUT
    @Path("{realm}/{userId}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
@RolesAllowed(Constants.WRITE_ADMIN_ROLE)
    void update(@BeanParam RequestParams requestParams, @PathParam("realm") String realm, @PathParam("userId") String userId, @Valid User user);

    @POST
    @Path("{realm}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
@RolesAllowed(Constants.WRITE_ADMIN_ROLE)
    void create(@BeanParam RequestParams requestParams, @PathParam("realm") String realm, @Valid User user);

    @DELETE
    @Path("{realm}/{userId}")
    @Produces(APPLICATION_JSON)
@RolesAllowed(Constants.WRITE_ADMIN_ROLE)
    void delete(@BeanParam RequestParams requestParams, @PathParam("realm") String realm, @PathParam("userId") String userId);

    @PUT
    @Path("{realm}/{userId}/reset-password")
@Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed(Constants.WRITE_ADMIN_ROLE)
    void resetPassword(@BeanParam RequestParams requestParams, @PathParam("realm") String realm, @PathParam("userId") String userId, Credential credential);

    @GET
    @Path("{realm}/{userId}/role")
    @Produces(APPLICATION_JSON)
Role[] getRoles(@BeanParam RequestParams requestParams, @PathParam("realm") String realm, @PathParam("userId") String userId);

    @GET
    @Path("role")
    @Produces(APPLICATION_JSON)
Role[] getCurrentUserRoles(@BeanParam RequestParams requestParams);

    @PUT
    @Path("{realm}/role/{userId}")
    @Consumes(APPLICATION_JSON)
@RolesAllowed(Constants.WRITE_ADMIN_ROLE)
    void updateRoles(@BeanParam RequestParams requestParams, @PathParam("realm") String realm, @PathParam("userId") String userId, Role[] roles);
}
