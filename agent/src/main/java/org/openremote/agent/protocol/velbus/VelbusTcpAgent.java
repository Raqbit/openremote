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

import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;

public class VelbusTcpAgent extends VelbusAgent {

    public VelbusTcpAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends VelbusTcpAgent, S extends Protocol<T>> VelbusTcpAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    @Override
    public VelbusTcpProtocol getProtocolInstance() {
        return new VelbusTcpProtocol(this);
    }
}
