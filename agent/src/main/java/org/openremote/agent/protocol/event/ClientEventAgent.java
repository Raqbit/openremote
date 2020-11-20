/*
 * Copyright 2020, OpenRemote Inc.
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
package org.openremote.agent.protocol.event;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.security.ClientRole;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class ClientEventAgent extends Agent {

    public static final ValueDescriptor<ClientRole> VALUE_CLIENT_ROLE = new ValueDescriptor<>("Client role", ClientRole.class);

    public static final AttributeDescriptor<String> CLIENT_SECRET = new AttributeDescriptor<>("clientSecret", true, ValueType.STRING);
    public static final AttributeDescriptor<ClientRole[]> CLIENT_ROLES = new AttributeDescriptor<>("clientRoles", true, VALUE_CLIENT_ROLE.asArray());

    public ClientEventAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends ClientEventAgent, S extends Protocol<T>> ClientEventAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    @Override
    public ClientEventProtocol getProtocolInstance() {
        return new ClientEventProtocol(this);
    }

    public Optional<String> getClientSecret() {
        return getAttributes().getValue(CLIENT_SECRET);
    }

    public Optional<ClientRole[]> getClientRoles() {
        return getAttributes().getValue(CLIENT_ROLES);
    }
}
