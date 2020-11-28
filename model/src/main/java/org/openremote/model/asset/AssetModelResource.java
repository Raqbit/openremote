/*
 * Copyright 2019, OpenRemote Inc.
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
package org.openremote.model.asset;

import org.openremote.model.http.RequestParams;
import org.openremote.model.value.MetaItemDescriptor;
import org.openremote.model.value.ValueDescriptor;

import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Resource for handling model requests and also providing server side validation of {@link Asset}s
 */
// TODO: Implement generic Asset validation for assets and agents
@Path("model")
public interface AssetModelResource {

    /**
     * Retrieve the asset descriptors {@link AssetDescriptor} available in the system
     */
    @GET
    @Path("asset/descriptors/{parentId}")
    @Produces(APPLICATION_JSON)
    AssetDescriptor<?>[] getAssetDescriptors(@BeanParam RequestParams requestParams, @PathParam("parentId") String parentId);

    /**
     * Retrieve value descriptors {@link ValueDescriptor}.
     */
    @GET
    @Path("attribute/valueDescriptors")
    @Produces(APPLICATION_JSON)
    ValueDescriptor<?>[] getValueDescriptors(@BeanParam RequestParams requestParams);

    /**
     * Retrieve meta descriptors {@link MetaItemDescriptor} present.
     */
    @GET
    @Path("metaItem/descriptors")
    @Produces(APPLICATION_JSON)
    MetaItemDescriptor<?>[] getMetaItemDescriptors(@BeanParam RequestParams requestParams);


//    /**
//     * Ask the appropriate protocol on the specified agent to validate the supplied {@link org.openremote.model.asset.agent.ProtocolConfiguration}
//     */
//    @POST
//    @Path("validate/{agentId}")
//    @Consumes(APPLICATION_JSON)
//    @Produces(APPLICATION_JSON)
//    //    AttributeValidationResult validateProtocolConfiguration(
//        @BeanParam RequestParams requestParams,
//        @PathParam("agentId") String agentId,
//        Attribute<?> protocolConfiguration
//    );
}
