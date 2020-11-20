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
package org.openremote.agent.protocol.knx;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueDescriptor;
import org.openremote.model.v2.ValueType;

import javax.validation.constraints.Pattern;
import java.util.Optional;

public class KNXAgent extends Agent {

    @Pattern(regexp = "^\\d{1,3}/\\d{1,3}/\\d{1,3}$")
    public static final ValueDescriptor<String> GROUP_ADDRESS_VALUE = new ValueDescriptor<>("KNX group address", String.class);

    @Pattern(regexp = "^\\d\\.\\d\\.\\d$")
    public static final ValueDescriptor<String> SOURCE_ADDRESS_VALUE = new ValueDescriptor<>("KNX message source address", String.class);

    @Pattern(regexp = "^\\d{1,3}\\.\\d{1,3}$")
    public static final ValueDescriptor<String> DPT_VALUE = new ValueDescriptor<>("KNX datapoint type", String.class);

    public static final AttributeDescriptor<Boolean> NAT_MODE = new AttributeDescriptor<>("nATMode", true, ValueType.BOOLEAN);
    public static final AttributeDescriptor<Boolean> ROUTING_MODE = new AttributeDescriptor<>("routingMode", true, ValueType.BOOLEAN);
    public static final AttributeDescriptor<String> MESSAGE_SOURCE_ADDRESS = new AttributeDescriptor<>("messageSourceAddress", true, SOURCE_ADDRESS_VALUE);


    public static final MetaItemDescriptor<String> META_DPT = new MetaItemDescriptor<>("dpt", DPT_VALUE, null);
    public static final MetaItemDescriptor<String> META_STATUS_GROUP_ADDRESS = new MetaItemDescriptor<>("statusGA", GROUP_ADDRESS_VALUE, null);
    public static final MetaItemDescriptor<String> META_ACTION_GROUP_ADDRESS = new MetaItemDescriptor<>("actionGA", GROUP_ADDRESS_VALUE, null);

    public KNXAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends KNXAgent, S extends Protocol<T>> KNXAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    public Optional<String> getMessageSourceAddress() {
        return getAttributes().getValue(MESSAGE_SOURCE_ADDRESS);
    }

    public Optional<Boolean> isNATMode() {
        return getAttributes().getValue(NAT_MODE);
    }

    public Optional<Boolean> isRoutingMode() {
        return getAttributes().getValue(ROUTING_MODE);
    }

    @Override
    public Protocol getProtocolInstance() {
        return new KNXProtocol(this);
    }
}
