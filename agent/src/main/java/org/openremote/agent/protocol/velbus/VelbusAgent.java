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
package org.openremote.agent.protocol.velbus;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueType;

abstract class VelbusAgent extends Agent {

    public static final AttributeDescriptor<Integer> TIME_INJECTION_INTERVAL_SECONDS = new AttributeDescriptor<>("timeInjectionInterval", true, ValueType.POSITIVE_INTEGER);

    public static final MetaItemDescriptor<String> META_DEVICE_VALUE_LINK = new MetaItemDescriptor<>("deviceValueLink", ValueType.STRING, null);
    public static final MetaItemDescriptor<Integer> META_DEVICE_ADDRESS = new MetaItemDescriptor<>("deviceAddress", ValueType.INT_BYTE, null);

    protected <T extends VelbusAgent, S extends AbstractVelbusProtocol<T>> VelbusAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    public Integer getTimeInjectionInterval() {
        return getAttributes().getValueOrDefault(TIME_INJECTION_INTERVAL_SECONDS);
    }

    @Override
    abstract public AbstractVelbusProtocol<? extends VelbusAgent> getProtocolInstance();
}
