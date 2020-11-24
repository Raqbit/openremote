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

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.openremote.agent.protocol.io.IoAgent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.asset.agent.GenericAgentLink;
import org.openremote.model.query.filter.ValuePredicate;
import org.openremote.model.value.*;

import java.util.Optional;

public class WebsocketClientAgent extends IoAgent<WebsocketClientAgent, WebsocketClientProtocol, WebsocketClientAgent.WebsocketClientAgentLink> {

    public static class WebsocketClientAgentLink extends AgentLink {

        protected WebsocketSubscription[] websocketSubscriptions;
        protected ValuePredicate messageMatchPredicate;
        protected ValueFilter[] messageMatchFilters;

        public WebsocketClientAgentLink(String id) {
            super(id);
        }

        @JsonPropertyDescription("Array of WebsocketSubscriptions that should be executed when the linked attribute is linked; the subscriptions are executed in the order specified in the array.")
        public WebsocketSubscription[] getWebsocketSubscriptions() {
            return websocketSubscriptions;
        }

        @JsonPropertyDescription("The predicate to apply to incoming messages to determine if the message is intended for the" +
            " linked attribute")
        Optional<ValuePredicate> getMessageMatchPredicate() {
            return Optional.ofNullable(messageMatchPredicate);
        }

        @JsonPropertyDescription("ValueFilters to apply to incoming messages prior to comparison with the messageMatchPredicate")
        Optional<ValueFilter[]> getMessageMatchFilters() {
            return Optional.ofNullable(messageMatchFilters);
        }

        public void setWebsocketSubscriptions(WebsocketSubscription[] websocketSubscriptions) {
            this.websocketSubscriptions = websocketSubscriptions;
        }

        public void setMessageMatchPredicate(ValuePredicate messageMatchPredicate) {
            this.messageMatchPredicate = messageMatchPredicate;
        }

        public void setMessageMatchFilters(ValueFilter[] messageMatchFilters) {
            this.messageMatchFilters = messageMatchFilters;
        }
    }


    /*--------------- META ITEMS TO BE USED ON PROTOCOL CONFIGURATIONS ---------------*/

    public static final ValueDescriptor<WebsocketSubscription> WEBSOCKET_SUBSCRIPTION_VALUE_DESCRIPTOR = new ValueDescriptor<>("Websocket subscription", WebsocketSubscription.class);

    /**
     * Websocket connect endpoint URI
     */
    public static final AttributeDescriptor<String> CONNECT_URI = new AttributeDescriptor<>("connectUri", ValueType.STRING);

    /**
     * Headers for websocket connect call
     */
    public static final AttributeDescriptor<ValueType.MultivaluedStringMap> CONNECT_HEADERS = new AttributeDescriptor<>("connectHeaders", ValueType.MULTIVALUED_STRING_MAP);

    /**
     * Array of {@link WebsocketSubscription}s that should be executed once the websocket connection is established; the
     * subscriptions are executed in the order specified in the array.
     */
    public static final AttributeDescriptor<WebsocketSubscription[]> CONNECT_SUBSCRIPTIONS = new AttributeDescriptor<>("connectSubscriptions", WEBSOCKET_SUBSCRIPTION_VALUE_DESCRIPTOR.asArray());

    /*--------------- META ITEMS TO BE USED ON LINKED ATTRIBUTES ---------------*/

    public static final AgentDescriptor<WebsocketClientAgent, WebsocketClientProtocol, WebsocketClientAgentLink> DESCRIPTOR = new AgentDescriptor<>(
        WebsocketClientAgent.class, WebsocketClientProtocol.class, WebsocketClientAgentLink.class, null
    );

    public WebsocketClientAgent(String name) {
        super(name, DESCRIPTOR);
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
