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
package org.openremote.agent.protocol.simulator;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.simulator.SimulatorReplayDatapoint;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueDescriptor;

public class SimulatorAgent extends Agent {

    public static final ValueDescriptor<SimulatorReplayDatapoint> REPLAY_DATAPOINT_VALUE = new ValueDescriptor<>("Replay datapoint", SimulatorReplayDatapoint.class);

    /**
     * Used to store 24h dataset of values that should be replayed (i.e. written to the linked attribute) in a continuous
     * loop.
     */
    public static final MetaItemDescriptor<SimulatorReplayDatapoint[]> SIMULATOR_REPLAY_DATA = new MetaItemDescriptor<>("simulatorReplayData", REPLAY_DATAPOINT_VALUE.asArray(), null);

    public SimulatorAgent(String name) {
        super(name, DESCRIPTOR);
    }

    protected <T extends SimulatorAgent, S extends Protocol<T>> SimulatorAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    @Override
    public Protocol getProtocolInstance() {
        return new SimulatorProtocol(this);
    }
}
