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
package org.openremote.agent.protocol.zwave;

import org.openremote.agent.protocol.io.IoAgent;
import org.openremote.agent.protocol.serial.SerialIoClient;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeValidationFailure;
import org.openremote.model.attribute.AttributeValidationResult;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.util.TextUtil;
import org.openremote.model.v2.MetaItemDescriptor;
import org.openremote.model.v2.ValueType;
import org.openremote.model.value.Values;

import java.util.Arrays;
import java.util.Optional;

import static org.openremote.agent.protocol.zwave.ZWProtocol.*;
import static org.openremote.model.util.TextUtil.REGEXP_PATTERN_INTEGER_POSITIVE;
import static org.openremote.model.util.TextUtil.isNullOrEmpty;

public class ZWAgent extends IoAgent<byte[], SerialIoClient<byte[]>> {

    public static MetaItemDescriptor<Integer> DEVICE_NODE_ID = new MetaItemDescriptor<>("deviceNodeId", ValueType.POSITIVE_INTEGER, 0);
    public static MetaItemDescriptor<Integer> DEVICE_ENDPOINT = new MetaItemDescriptor<>("deviceEndpoint", ValueType.POSITIVE_INTEGER, 0);
    public static MetaItemDescriptor<String> DEVICE_VALUE = new MetaItemDescriptor<>("deviceValue", ValueType.STRING, "");


    public static String getEndpointIdAsString(Attribute assetAttribute)
    {
        return getZWNodeId(assetAttribute) + ":" + getZWEndpoint(assetAttribute);
    }

    public static boolean validateSerialConfiguration(Attribute protocolConfiguration, AttributeValidationResult result) {
        boolean failure = false;

        if (!isSerialConfiguration(protocolConfiguration)) {
            failure = true;
            if (result != null) {
                result.addAttributeFailure(
                    new AttributeValidationFailure(
                        ValueHolder.ValueFailureReason.VALUE_MISMATCH,
                        ZWProtocol.PROTOCOL_NAME));
            }
        }

        boolean portFound = false;

        if (protocolConfiguration.getMeta() != null && !protocolConfiguration.getMeta().isEmpty()) {
            for (int i = 0; i < protocolConfiguration.getMeta().size(); i++) {
                MetaItem metaItem = protocolConfiguration.getMeta().get(i);
                if (isMetaNameEqualTo(metaItem, ZWProtocol.META_ZWAVE_SERIAL_PORT)) {
                    portFound = true;
                    if (isNullOrEmpty(metaItem.getValueAsString().orElse(null))) {
                        failure = true;
                        if (result == null) {
                            break;
                        }
                        result.addMetaFailure(i,
                            new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_VALUE_IS_REQUIRED, ValueType.STRING.name()));
                    }
                }
            }
        }

        if (!portFound) {
            failure = true;
            if (result != null) {
                result.addMetaFailure(
                    new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_MISSING, META_ZWAVE_SERIAL_PORT)
                );
            }
        }

        return !failure;
    }

    public static boolean isSerialConfiguration(Attribute attribute) {
        return attribute != null && attribute.getValueAsString().map(value -> value.equals(ZWProtocol.PROTOCOL_NAME)).orElse(false);
    }

    public static boolean isValidProtocolName(String protocolName) {
        return TextUtil.isValidURN(protocolName);
    }

    public static void isValidProtocolNameOrThrow(String protocolName) throws IllegalArgumentException {
        if (!isValidProtocolName(protocolName)) {
            throw new IllegalArgumentException("Protocol name must start with 'urn:' but is: " + protocolName);
        }
    }

    public static Attribute initProtocolConfiguration(Attribute attribute, String protocolName) throws IllegalArgumentException {
        if (attribute == null) {
            return null;
        }
        isValidProtocolNameOrThrow(protocolName);
        attribute.setReadOnly(true);
        attribute.setType(AttributeValueType.STRING);
        attribute.setValue(Values.create(protocolName));
        attribute.getMeta().add(new MetaItem(PROTOCOL_CONFIGURATION, Values.create(true)));
        attribute.getMeta().add(new MetaItem(META_ZWAVE_SERIAL_PORT, Values.create("/dev/ttyACM0")));
        return attribute;
    }

    public static final List<MetaItemDescriptor> ATTRIBUTE_META_ITEM_DESCRIPTORS = Arrays.asList(
        new MetaItemDescriptorImpl(
            META_ZWAVE_DEVICE_NODE_ID,
            ValueType.NUMBER,
            true,
            "^([1-9]\\d{0,1}|1[0-9][0-9]|2[0-2][0-9]|23[0-2])$", //1-232
            "1-232",
            1,
            null,
            false,
            null, null, null
        ),
        new MetaItemDescriptorImpl(
            META_ZWAVE_DEVICE_ENDPOINT,
            ValueType.NUMBER,
            true,
            REGEXP_PATTERN_INTEGER_POSITIVE,
            MetaItemDescriptor.PatternFailure.INTEGER_POSITIVE_NON_ZERO.name(),
            1,
            Values.create(DEFAULT_ENDPOINT),
            false,
            null, null, null
        ),
        new MetaItemDescriptorImpl(
            META_ZWAVE_DEVICE_VALUE_LINK,
            ValueType.STRING,
            true,
            null,
            null,
            1,
            null,
            false,
            null, null, null
        )
    );

    public static AgentDescriptor<ZWAgent, ZWProtocol> DESCRIPTOR = new AgentDescriptor();

    protected <T extends Agent, S extends Protocol<T>> ZWAgent(String name) {
        super(name, DESCRIPTOR);
    }

    @Override
    public Protocol getProtocolInstance() {
        return null;
    }
}
