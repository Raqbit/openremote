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
package org.openremote.model.asset.agent;

import org.openremote.model.Constants;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.asset.AssetResource;
import org.openremote.model.asset.AssetTreeNode;
import org.openremote.model.attribute.AttributeValidationResult;
import org.openremote.model.file.FileInfo;
import org.openremote.model.http.RequestParams;
import org.openremote.model.http.SuccessStatusCode;
import org.openremote.model.protocol.ProtocolInstanceDiscovery;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * This resource is for Agent specific tasks; normal asset/attribute CRUD operations should still use
 * {@link AssetResource}.
 */
@Path("agent")
public interface AgentResource {

    /**
     * Get discovered protocol instances for the specified named agent {@link AssetDescriptor}; the associated
     * {@link Protocol} must implement {@link ProtocolInstanceDiscovery} otherwise an empty set of results will be
     * returned. The {@link Asset} parent where the {@link Agent} will be added must be specified so the backend
     * can determine if the {@link Agent} is being created on an Edge gateway instance or on this
     *
     * @return A map of logical instance name and corresponding {@link Attribute}s that should be added to an
     * {@link Agent} instance to configure it so that it connects to the discovered {@link Protocol} instance.
     */
    @GET
    @Path("configuration/{agentId}/{protocolName}")
    @Produces(APPLICATION_JSON)
    @SuccessStatusCode(200)
    @RolesAllowed({Constants.READ_ASSETS_ROLE})
    @SuppressWarnings("unusable-by-js")
    Attribute[] getDiscoveredProtocolConfigurations(
        @BeanParam RequestParams requestParams,
        @PathParam("agentId") String agentId,
        @PathParam("protocolName") String protocolName
    );

    /**
     * Ask the appropriate protocol on the specified agent to validate the supplied {@link org.openremote.model.asset.agent.ProtocolConfiguration}
     */
    @POST
    @Path("validate/{agentId}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @SuccessStatusCode(200)
    @SuppressWarnings("unusable-by-js")
    AttributeValidationResult validateProtocolConfiguration(
        @BeanParam RequestParams requestParams,
        @PathParam("agentId") String agentId,
        Attribute<?> protocolConfiguration
    );

    /**
     * Get discovered linked attributes for the specified Agent
     * and {@link org.openremote.model.asset.agent.ProtocolConfiguration}.
     * <p>
     * Currently this request will automatically add the found {@link Asset}s to the DB as well as returning
     * them in the response. The {@param parentId} is used to set where in the {@link Asset} tree the new assets are
     * inserted.
     */
    @GET
    @Path("search/{agentId}/{protocolConfigurationName}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @SuccessStatusCode(200)
    @SuppressWarnings("unusable-by-js")
    AssetTreeNode[] searchForLinkedAttributes(
        @BeanParam RequestParams requestParams,
        @PathParam("agentId") String agentId,
        @PathParam("protocolConfigurationName") String protocolConfigurationName,
        @QueryParam("parentId") String parentId,
        @QueryParam("realm") String realm
    );

    /**
     * Get discovered linked attributes for the specified Agent
     * and {@link org.openremote.model.asset.agent.ProtocolConfiguration} using the supplied {@link FileInfo}.
     * <p>
     * Currently this request will automatically add the found {@link Asset}s to the DB as well as returning
     * them in the response. The {@param parentId} is used to set where in the {@link Asset} tree the new assets are
     * inserted.
     * <b>NOTE:</b> The {@link FileInfo} should be a file that the protocol understands.
     */
    @POST
    @Path("import/{agentId}/{protocolConfigurationName}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @SuccessStatusCode(200)
    @SuppressWarnings("unusable-by-js")
    // TODO: File upload should use standard multipart mechanism
    AssetTreeNode[] importLinkedAttributes(
        @BeanParam RequestParams requestParams,
        @PathParam("agentId") String agentId,
        @PathParam("protocolConfigurationName") String protocolConfigurationName,
        @QueryParam("parentId") String parentId,
        @QueryParam("realm") String realm,
        FileInfo fileInfo
    );
}
