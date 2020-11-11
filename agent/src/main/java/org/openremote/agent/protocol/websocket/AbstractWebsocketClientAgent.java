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

import com.fasterxml.jackson.core.type.TypeReference;
import org.openremote.agent.protocol.http.HttpClientProtocol;
import org.openremote.agent.protocol.io.IoAgent;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.value.Values;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static org.openremote.agent.protocol.http.HttpClientProtocol.PROTOCOL_NAME;
import static org.openremote.model.util.TextUtil.REGEXP_PATTERN_STRING_NON_EMPTY;

public abstract class AbstractWebsocketClientAgent<T> extends IoAgent<T, WebsocketIoClient<T>> {


    /*--------------- META ITEMS TO BE USED ON PROTOCOL CONFIGURATIONS ---------------*/

    /**
     * Websocket connect endpoint URI
     */
    public static final MetaItemDescriptor META_PROTOCOL_CONNECT_URI = metaItemString(
        PROTOCOL_NAME + ":uri",
        ACCESS_PRIVATE,
        true,
        REGEXP_PATTERN_STRING_NON_EMPTY,
        PatternFailure.STRING_EMPTY
    );

    /**
     * Headers for websocket connect call (see {@link HttpClientProtocol#META_HEADERS} for details)
     */
    public static final MetaItemDescriptor META_PROTOCOL_CONNECT_HEADERS = metaItemObject(
        PROTOCOL_NAME + ":headers",
        ACCESS_PRIVATE,
        false,
        null);

    /**
     * Array of {@link WebsocketSubscription}s that should be executed either once the websocket connection is
     * established (when used on a linked {@link ProtocolConfiguration}) or when a linked attribute is linked (when
     * used on a linked {@link Attribute}); the subscriptions are executed in the order specified in the array
     */
    public static final MetaItemDescriptor META_SUBSCRIPTIONS = metaItemArray(
        PROTOCOL_NAME + ":subscriptions",
        ACCESS_PRIVATE,
        false,
        null);

    /*--------------- META ITEMS TO BE USED ON LINKED ATTRIBUTES ---------------*/

    public static final List<MetaItemDescriptor> ATTRIBUTE_META_ITEM_DESCRIPTORS = Arrays.asList(
        META_SUBSCRIPTIONS);

    public static final List<MetaItemDescriptor> PROTOCOL_META_ITEM_DESCRIPTORS = Arrays.asList(
        META_PROTOCOL_CONNECT_URI,
        META_PROTOCOL_CONNECT_HEADERS,
        META_PROTOCOL_USERNAME,
        META_PROTOCOL_PASSWORD,
        META_PROTOCOL_OAUTH_GRANT,
        META_SUBSCRIPTIONS);

    public Optional<WebsocketSubscription<T>[]> getSubscriptions(Attribute<?> attribute) {
        return Values.getMetaItemValueOrThrow(
            attribute,
            META_SUBSCRIPTIONS,
            false,
            true)
            .flatMap(Values::getArray)
            .map(arrValue -> {
                try {
                    return Values.JSON.readValue(arrValue.toJson(), new TypeReference<WebsocketSubscription<T>[]>() {});
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Failed to deserialize WebsocketSubscription[]", e);
                    return null;
                }
            });
    }
}
