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
package org.openremote.agent.protocol.zwave;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueType;

public class ZWAgent extends Agent {

    public static MetaItemDescriptor<Integer> DEVICE_NODE_ID = new MetaItemDescriptor<>("deviceNodeId", ValueType.POSITIVE_INTEGER, 0);
    public static MetaItemDescriptor<Integer> DEVICE_ENDPOINT = new MetaItemDescriptor<>("deviceEndpoint", ValueType.POSITIVE_INTEGER, 0);
    public static MetaItemDescriptor<String> DEVICE_VALUE = new MetaItemDescriptor<>("deviceValue", ValueType.STRING, "");


    public static AgentDescriptor<ZWAgent, ZWProtocol> DESCRIPTOR = new AgentDescriptor(

    );

    protected <T extends Agent, S extends Protocol<T>> ZWAgent(String name) {
        super(name, DESCRIPTOR);
    }

    @Override
    public Protocol<ZWAgent> getProtocolInstance() {
        return new ZWProtocol(this);
    }
}
