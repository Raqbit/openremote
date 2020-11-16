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
package org.openremote.setup;

import org.openremote.agent.protocol.ProtocolExecutorService;
import org.openremote.agent.protocol.tcp.TcpStringServer;
import org.openremote.container.Container;
import org.openremote.container.ContainerService;
import org.openremote.manager.concurrent.ManagerExecutorService;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;

/**
 * This service will start a simple TCP server for string messages; it listens on the port configured in
 * {@link LoadTestManagerSetup}. To use this just change {@link #active} to true.
 */
public class TcpStringServerService implements ContainerService {

    protected TcpStringServer tcpStringServer;
    protected ScheduledFuture<?> messageSender;
    protected boolean active = false; // Change this to true to run the server

    @Override
    public int getPriority() {
        return ContainerService.DEFAULT_PRIORITY;
    }

    @Override
    public void init(Container container) throws Exception {
        if (active) {
            tcpStringServer = new TcpStringServer(container.getService(ProtocolExecutorService.class), new InetSocketAddress("localhost", 12345), ";", Integer.MAX_VALUE, true);
        }
    }

    @Override
    public void start(Container container) throws Exception {

        if (active) {
            ManagerExecutorService executorService = container.getService(ManagerExecutorService.class);

            tcpStringServer.start();

            messageSender = executorService.scheduleAtFixedRate(() -> {
                tcpStringServer.sendMessage("1:Test;");
            }, 10000, 5000);
        }
    }

    @Override
    public void stop(Container container) throws Exception {
        if (active) {
            messageSender.cancel(false);
            tcpStringServer.stop();
        }
    }
}
