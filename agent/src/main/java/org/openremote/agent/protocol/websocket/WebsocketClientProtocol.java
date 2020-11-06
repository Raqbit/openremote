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
package org.openremote.agent.protocol.websocket;

import io.netty.channel.ChannelHandler;
import org.openremote.model.protocol.ProtocolUtil;
import org.openremote.model.attribute.AttributeValidationFailure;
import org.openremote.model.ValueHolder;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.asset.agent.ProtocolConfiguration;
import org.openremote.model.attribute.*;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.util.Pair;
import org.openremote.model.value.Value;
import org.openremote.model.value.Values;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.openremote.agent.protocol.http.HttpClientProtocol.getUsernameAndPassword;
import static org.openremote.model.value.Values.joinCollections;
import static org.openremote.model.Constants.PROTOCOL_NAMESPACE;
import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;

/**
 * This is a generic implementation of {@link AbstractWebsocketClientProtocol} for communicating with a Websocket server
 * using {@link String} based messages; messages coming from the server are written through to {@link Attribute}s by using
 * the {@link #META_ATTRIBUTE_MATCH_FILTERS} and {@link #META_ATTRIBUTE_MATCH_PREDICATE} {@link MetaItem}s.
 * <p>
 * For supported {@link ProtocolConfiguration} {@link MetaItem}s see {@link #PROTOCOL_META_ITEM_DESCRIPTORS}
 * <p>
 * For supported linked {@link Attribute} {@link MetaItem}s see {@link #ATTRIBUTE_META_ITEM_DESCRIPTORS}
 */
public class WebsocketClientProtocol extends AbstractWebsocketClientProtocol<String> {

    public static final String PROTOCOL_NAME = PROTOCOL_NAMESPACE + ":websocketClient";
    public static final String PROTOCOL_DISPLAY_NAME = "Websocket Client";
    public static final String PROTOCOL_VERSION = "1.0";
    private static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, WebsocketClientProtocol.class);
    protected final Map<AttributeRef, List<Pair<AttributeRef, Consumer<String>>>> protocolMessageConsumers = new HashMap<>();

    public static final List<MetaItemDescriptor> PROTOCOL_META_ITEM_DESCRIPTORS = joinCollections(
        AbstractWebsocketClientProtocol.PROTOCOL_META_ITEM_DESCRIPTORS,
        PROTOCOL_GENERIC_META_ITEM_DESCRIPTORS);

    public static final List<MetaItemDescriptor> ATTRIBUTE_META_ITEM_DESCRIPTORS = joinCollections(
        Arrays.asList(
            META_ATTRIBUTE_MATCH_FILTERS,
            META_ATTRIBUTE_MATCH_PREDICATE),
        AbstractWebsocketClientProtocol.ATTRIBUTE_META_ITEM_DESCRIPTORS);

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public String getProtocolDisplayName() {
        return PROTOCOL_DISPLAY_NAME;
    }

    @Override
    protected List<MetaItemDescriptor> getProtocolConfigurationMetaItemDescriptors() {
        return new ArrayList<>(PROTOCOL_META_ITEM_DESCRIPTORS);
    }

    @Override
    protected List<MetaItemDescriptor> getLinkedAttributeMetaItemDescriptors() {
        return new ArrayList<>(ATTRIBUTE_META_ITEM_DESCRIPTORS);
    }

    @Override
    public Attribute getProtocolConfigurationTemplate() {
        return super.getProtocolConfigurationTemplate()
                .addMeta(
                        new MetaItem(META_PROTOCOL_CONNECT_URI, null)
                );
    }

    @Override
    public AttributeValidationResult validateProtocolConfiguration(Attribute protocolConfiguration) {
        AttributeValidationResult result = super.validateProtocolConfiguration(protocolConfiguration);
        if (result.isValid()) {
            try {
                ProtocolUtil.getOAuthGrant(protocolConfiguration);
                getUsernameAndPassword(protocolConfiguration);
            } catch (IllegalArgumentException e) {
                result.addAttributeFailure(
                        new AttributeValidationFailure(ValueHolder.ValueFailureReason.VALUE_MISMATCH, PROTOCOL_NAME)
                );
            }
        }
        return result;
    }

    @Override
    protected void doDisconnect() {
        synchronized (protocolMessageConsumers) {
            protocolMessageConsumers.remove(protocolConfiguration.getReferenceOrThrow());
        }
        super.doDisconnect();
    }

    @Override
    protected void doLinkAttribute(Asset asset, Attribute attribute) {
        super.doLinkAttribute(asset, attribute);

        AttributeRef protocolRef = agent.getReferenceOrThrow();
        Consumer<String> messageConsumer = ProtocolUtil.createGenericAttributeMessageConsumer(attribute, assetService, this::updateLinkedAttribute);

        if (messageConsumer != null) {
            synchronized (protocolMessageConsumers) {
                protocolMessageConsumers.compute(protocolRef, (ref, consumers) -> {
                    if (consumers == null) {
                        consumers = new ArrayList<>();
                    }
                    consumers.add(new Pair<>(
                        attribute.getReferenceOrThrow(),
                        messageConsumer
                    ));
                    return consumers;
                });
            }
        }
    }

    @Override
    protected void doUnlinkAttribute(Asset asset, Attribute attribute) {
        AttributeRef attributeRef = attribute.getReferenceOrThrow();
        synchronized (protocolMessageConsumers) {
            protocolMessageConsumers.compute(agent.getReferenceOrThrow(), (ref, consumers) -> {
                if (consumers != null) {
                    consumers.removeIf((attrRefConsumer) -> attrRefConsumer.key.equals(attributeRef));
                }
                return consumers;
            });
        }
        super.doUnlinkAttribute(asset, attribute);
    }

    @Override
    protected Supplier<ChannelHandler[]> getEncoderDecoderProvider(WebsocketIoClient<String> client, Attribute protocolConfiguration) {
        return getGenericStringEncodersAndDecoders(client, protocolConfiguration);
    }

    @Override
    protected void onMessageReceived(AttributeRef protocolRef, String message) {
        List<Pair<AttributeRef, Consumer<String>>> consumers;

        synchronized (protocolMessageConsumers) {
            consumers = protocolMessageConsumers.get(protocolRef);

            if (consumers != null) {
                consumers.forEach(c -> {
                    if (c.value != null) {
                        c.value.accept(message);
                    }
                });
            }
        }
    }

    @Override
    protected String createWriteMessage(Attribute protocolConfiguration, Attribute attribute, AttributeEvent event, Value processedValue) {
        if (attribute.isReadOnly()) {
            LOG.fine("Attempt to write to an attribute that doesn't support writes: " + event.getAttributeRef());
            return null;
        }

        if (attribute.isExecutable()) {
            AttributeExecuteStatus status = event.getValue()
                .flatMap(Values::getString)
                .flatMap(AttributeExecuteStatus::fromString)
                .orElse(null);

            if (status != null && status != AttributeExecuteStatus.REQUEST_START) {
                LOG.fine("Unsupported execution status: " + status);
                return null;
            }
        }

        return processedValue != null ? processedValue.toString() : null;
    }
}
