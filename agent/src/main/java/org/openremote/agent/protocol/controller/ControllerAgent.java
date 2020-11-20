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
package org.openremote.agent.protocol.controller;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class ControllerAgent extends Agent {

    public static final AttributeDescriptor<String> CONTROLLER_URI = new AttributeDescriptor<>("controllerURI", false, ValueType.STRING);

    public static final MetaItemDescriptor<String> META_DEVICE_NAME = new MetaItemDescriptor<>("deviceName", ValueType.STRING, null);
    public static final MetaItemDescriptor<String> META_SENSOR_NAME = new MetaItemDescriptor<>("sensorName", ValueType.STRING, null);
    public static final MetaItemDescriptor<String> META_COMMAND_DEVICE_NAME = new MetaItemDescriptor<>("commandDeviceName", ValueType.STRING, null);
    public static final MetaItemDescriptor<String> META_COMMAND_NAME = new MetaItemDescriptor<>("commandName", ValueType.STRING, null);
    public static final MetaItemDescriptor<ValueType.MultivaluedStringMap> META_COMMANDS_MAP = new MetaItemDescriptor<>("commandsMap", ValueType.MULTIVALUED_STRING_MAP, null);

    public ControllerAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends ControllerAgent, S extends Protocol<T>> ControllerAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    @Override
    public Protocol getProtocolInstance() {
        return null;
    }

    public Optional<String> getControllerURI() {
        return getAttributes().getValue(CONTROLLER_URI);
    }
}
