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
package org.openremote.model.asset.impl;

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueTypes;

import java.util.Optional;

public class Gateway extends Asset {

    public static final AttributeDescriptor<String> CLIENT_ID = new AttributeDescriptor<>("clientId", false, ValueTypes.STRING, null);
    public static final AttributeDescriptor<String> CLIENT_SECRET = new AttributeDescriptor<>("clientId", false, ValueTypes.UUID, null);
    public static final AttributeDescriptor<ConnectionStatus> STATUS = new AttributeDescriptor<>("gatewayStatus", false, ValueTypes.CONNECTION_STATUS, null);
    public static final AttributeDescriptor<Boolean> DISABLED = new AttributeDescriptor<>("disabled", false, ValueTypes.BOOLEAN, null);

    public static final AssetDescriptor<Gateway> DESCRIPTOR = new AssetDescriptor<>("Gateway", "router-wireless", null, Gateway.class);

    public Gateway(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getClientId() {
        return getAttributes().getValueOrDefault(CLIENT_ID);
    }

    public Optional<String> getClientSecret() {
        return getAttributes().getValueOrDefault(CLIENT_SECRET);
    }

    public Optional<ConnectionStatus> getGatewayStatus() {
        return getAttributes().getValueOrDefault(STATUS);
    }

    public Optional<Boolean> getDisabled() {
        return getAttributes().getValueOrDefault(DISABLED);
    }
}
