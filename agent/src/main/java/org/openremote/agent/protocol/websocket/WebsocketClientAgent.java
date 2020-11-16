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
package org.openremote.agent.protocol.websocket;

import org.openremote.agent.protocol.http.HttpClientProtocol;
import org.openremote.agent.protocol.io.AbstractIoClientProtocol;
import org.openremote.agent.protocol.io.IoAgent;
import org.openremote.agent.protocol.tcp.TcpIoClient;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class WebsocketClientAgent extends IoAgent<String, WebsocketIoClient<String>> {


    /*--------------- META ITEMS TO BE USED ON PROTOCOL CONFIGURATIONS ---------------*/

    public static final ValueDescriptor<WebsocketSubscription> WEBSOCKET_SUBSCRIPTION_VALUE_DESCRIPTOR = new ValueDescriptor<>("Websocket subscription", WebsocketSubscription.class);

    /**
     * Websocket connect endpoint URI
     */
    public static final AttributeDescriptor<String> CONNECT_URI = new AttributeDescriptor<>("connectUri", false, ValueType.STRING, null);

    /**
     * Headers for websocket connect call (see {@link HttpClientProtocol#META_HEADERS} for details)
     */
    public static final AttributeDescriptor<ValueType.MultivaluedStringMap> CONNECT_HEADERS = new AttributeDescriptor<>("connectHeaders", true, ValueType.MULTIVALUED_STRING_MAP, null);

    /**
     * Array of {@link WebsocketSubscription}s that should be executed once the websocket connection is established; the
     * subscriptions are executed in the order specified in the array.
     */
    public static final AttributeDescriptor<WebsocketSubscription[]> CONNECT_SUBSCRIPTIONS = new AttributeDescriptor<>("connectSubscriptions", true, WEBSOCKET_SUBSCRIPTION_VALUE_DESCRIPTOR.asArray(), null);

    /*--------------- META ITEMS TO BE USED ON LINKED ATTRIBUTES ---------------*/

    /**
     * Array of {@link WebsocketSubscription}s that should be executed when the linked attribute is linked; the
     * subscriptions are executed in the order specified in the array.
     */
    public static final MetaItemDescriptor<WebsocketSubscription[]> META_SUBSCRIPTIONS = new MetaItemDescriptor<>("websocketSubscriptions", WEBSOCKET_SUBSCRIPTION_VALUE_DESCRIPTOR.asArray(), null);

    public WebsocketClientAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <V extends IoAgent<String, WebsocketIoClient<String>>, W extends AbstractIoClientProtocol<String, WebsocketIoClient<String>, V>> WebsocketClientAgent(String name, AgentDescriptor<V, W> descriptor) {
        super(name, descriptor);
    }

    public Optional<String> getConnectUri() {
        return getAttributes().getValue(CONNECT_URI);
    }

    public Optional<ValueType.MultivaluedStringMap> getConnectHeaders() {
        return getAttributes().getValue(CONNECT_HEADERS);
    }

    public Optional<WebsocketSubscription[]> getConnectSubscriptions() {
        return getAttributes().getValue(CONNECT_SUBSCRIPTIONS);
    }

    @Override
    public WebsocketClientProtocol getProtocolInstance() {
        return new WebsocketClientProtocol(this);
    }
}
