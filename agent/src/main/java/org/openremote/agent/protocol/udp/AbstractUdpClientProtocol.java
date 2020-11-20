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
package org.openremote.agent.protocol.udp;

import org.openremote.agent.protocol.io.AbstractIoClientProtocol;
import org.openremote.agent.protocol.io.IoAgent;

/**
 * This is an abstract UDP client protocol for communicating with UDP servers; concrete implementations must implement
 * {@link #getEncoderDecoderProvider} to provide encoders/decoders for messages of type &lt;T&gt;.
 */
public abstract class AbstractUdpClientProtocol<T, U extends IoAgent<T, UdpIoClient<T>>> extends AbstractIoClientProtocol<T, UdpIoClient<T>, U> {

    protected AbstractUdpClientProtocol(U agent) {
        super(agent);
    }

    @Override
    protected UdpIoClient<T> doCreateIoClient() throws Exception {

        String host = agent.getHost().orElse(null);
        Integer port = agent.getPort().orElse(null);
        Integer bindPort = agent.getBindPort().orElse(null);

        return new UdpIoClient<>(host, port, bindPort, executorService);
    }
}
