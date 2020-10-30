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
package org.openremote.model.protocol;

import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;
import org.openremote.model.AbstractValueHolder;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeExecuteStatus;
import org.openremote.model.attribute.AttributeRef;
import org.openremote.model.attribute.AttributeState;
import org.openremote.model.auth.OAuthGrant;
import org.openremote.model.query.filter.StringPredicate;
import org.openremote.model.util.Pair;
import org.openremote.model.util.TextUtil;
import org.openremote.model.value.ValueFilter;
import org.openremote.model.value.Values;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

import static org.openremote.model.value.Values.NULL_LITERAL;

public final class ProtocolUtil {

    protected ProtocolUtil() {
    }


    public static String bytesToHexString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    public static byte[] bytesFromHexString(String hex) {
        try {
            return Hex.decodeHex(hex.toCharArray());
        } catch (Exception e) {
            Protocol.LOG.log(Level.WARNING, "Failed to convert hex string to bytes", e);
            return new byte[0];
        }
    }

    public static String bytesToBinaryString(byte[] bytes) {
        return BinaryCodec.toAsciiString(bytes);
    }

    public static byte[] bytesFromBinaryString(String binary) {
        try {
            return BinaryCodec.fromAscii(binary.toCharArray());
        } catch (Exception e) {
            Protocol.LOG.log(Level.WARNING, "Failed to convert hex string to bytes", e);
            return new byte[0];
        }
    }

    /**
     * Extract the {@link ValueFilter}s from the specified {@link Attribute}
     */
    public static Optional<ValueFilter[]> getLinkedAttributeValueFilters(Attribute attribute) {
        if (attribute == null) {
            return Optional.empty();
        }

        Optional<ArrayValue> arrayValueOptional = attribute.getMetaItem(Protocol.META_ATTRIBUTE_VALUE_FILTERS)
            .flatMap(AbstractValueHolder::getValueAsArray);

        if (!arrayValueOptional.isPresent()) {
            return Optional.empty();
        }

        try {
            String json = arrayValueOptional.get().toJson();
            return Optional.of(Values.JSON.readValue(json, ValueFilter[].class));
        } catch (Exception e) {
            Protocol.LOG.log(Level.WARNING, e.getMessage(), e);
        }

        return Optional.empty();
    }

    public static Optional<OAuthGrant> getOAuthGrant(Attribute attribute) throws IllegalArgumentException {
        return !attribute.hasMetaItem(Protocol.META_PROTOCOL_OAUTH_GRANT)
            ? Optional.empty()
            : Optional.of(attribute.getMetaItem(Protocol.META_PROTOCOL_OAUTH_GRANT)
            .flatMap(AbstractValueHolder::getValueAsObject)
            .map(objValue -> {
                String json = objValue.toJson();
                try {
                    return Values.JSON.readValue(json, OAuthGrant.class);
                } catch (IOException e) {
                    throw new IllegalArgumentException("OAuth Grant meta item is not valid", e);
                }
            })
            .orElseThrow(() -> new IllegalArgumentException("OAuth grant meta item must be an ObjectValue")));
    }

    /**
     * Will perform recommended value processing for outbound values (Linked Attribute -> Protocol); the
     * containsDynamicPlaceholder flag is required so that the entire {@link Protocol#META_ATTRIBUTE_WRITE_VALUE} payload is not
     * searched on every single write request (for performance reasons), instead this should be recorded when the
     * attribute is first linked.
     */
    public static Pair<Boolean, Value> doOutboundValueProcessing(Attribute attribute, Value value, boolean containsDynamicPlaceholder) {

        String writeValue = Values.getMetaItemValueOrThrow(attribute, Protocol.META_ATTRIBUTE_WRITE_VALUE, false, true)
            .map(Object::toString).orElse(null);

        if (attribute.isExecutable()) {
            AttributeExecuteStatus status = Values.getString(value)
                .flatMap(AttributeExecuteStatus::fromString)
                .orElse(null);

            if (status == AttributeExecuteStatus.REQUEST_START && writeValue != null) {
                try {
                    value = Values.parse(writeValue).orElse(null);
                } catch (Exception e) {
                    value = null;
                    Protocol.LOG.log(Level.INFO, "Failed to pass attribute write payload generated by META_ATTRIBUTE_WRITE_VALUE", e);
                }
                return new Pair<>(false, value);
            }
        }

        // value conversion
        ObjectValue converter = Values.getMetaItemValueOrThrow(
            attribute,
            Protocol.META_ATTRIBUTE_WRITE_VALUE_CONVERTER,
            false,
            false)
            .flatMap(Values::getObject)
            .orElse(null);

        if (converter != null) {
            Protocol.LOG.fine("Applying attribute value converter to attribute write: " + attribute.getReferenceOrThrow());

            Pair<Boolean, Value> converterResult = applyValueConverter(value, converter);

            if (converterResult.key) {
                return converterResult;
            }

            value = converterResult.value;
        }

        // dynamic value insertion
        boolean hasWriteValue = attribute.hasMetaItem(Protocol.META_ATTRIBUTE_WRITE_VALUE);

        if (hasWriteValue) {
            if (writeValue == null) {
                Protocol.LOG.fine("META_ATTRIBUTE_WRITE_VALUE contains null so sending null to protocol for attribute write on: " + attribute.getReferenceOrThrow());
                return new Pair<>(false, null);
            }

            if (containsDynamicPlaceholder) {
                String valueStr = value == null ? NULL_LITERAL : value.toString();
                writeValue = writeValue.replaceAll(Protocol.DYNAMIC_VALUE_PLACEHOLDER_REGEXP, valueStr);
            }

            try {
                value = Values.parse(writeValue).orElse(null);
            } catch (Exception e) {
                Protocol.LOG.log(Level.INFO, "Failed to pass attribute write payload generated by META_ATTRIBUTE_WRITE_VALUE", e);
            }
        }

        return new Pair<>(false, value);
    }

    public static Pair<Boolean, Value> doInboundValueProcessing(Attribute attribute, Value value, ProtocolAssetService assetService) {

        // filtering
        ValueFilter[] filters = getLinkedAttributeValueFilters(attribute).orElse(null);
        if (filters != null) {
            value = assetService.applyValueFilters(value, filters);
        }

        // value conversion
        ObjectValue converter = Values.getMetaItemValueOrThrow(
            attribute,
            Protocol.META_ATTRIBUTE_VALUE_CONVERTER,
            false,
            false)
            .flatMap(Values::getObject)
            .orElse(null);

        if (converter != null) {
            Protocol.LOG.fine("Applying attribute value converter to attribute: " + attribute.getReferenceOrThrow());

            Pair<Boolean, Value> convertedValue = applyValueConverter(value, converter);

            if (convertedValue.key) {
                return convertedValue;
            }

            value = convertedValue.value;
        }

        // built in value conversion
        Optional<ValueType> attributeValueType = attribute.getValueType().map(AttributeValueDescriptor::getValueType);

        if (value != null && attributeValueType.isPresent()) {
            if (attributeValueType.get() != value.getType()) {
                Protocol.LOG.fine("Trying to convert value: " + value.getType() + " -> " + attributeValueType.get());
                Optional<Value> convertedValue = Values.convertToValue(value, attributeValueType.get());

                if (!convertedValue.isPresent()) {
                    Protocol.LOG.warning("Failed to convert value: " + value.getType() + " -> " + attributeValueType.get());
                    Protocol.LOG.warning("Cannot send linked attribute update");
                    return new Pair<>(true, null);
                }

                value = convertedValue.get();
            }
        }

        return new Pair<>(false, value);
    }

    public static Pair<Boolean, Object> applyValueConverter(Value value, ObjectValue converter) {

        if (converter == null) {
            return new Pair<>(false, value);
        }

        String converterKey = value == null ? NULL_LITERAL.toUpperCase() : value.toString().toUpperCase(Locale.ROOT);
        return converter.get(converterKey)
            .map(v -> {
                if (v.getType() == ValueType.STRING) {
                    String valStr = v.toString();
                    if ("@IGNORE".equalsIgnoreCase(valStr)) {
                        return new Pair<>(true, (Value) null);
                    }

                    if ("@NULL".equalsIgnoreCase(valStr)) {
                        return new Pair<>(false, (Value) null);
                    }
                }

                return new Pair<>(false, v);
            })
            .orElse(new Pair<>(true, value));
    }

    public static Consumer<String> createGenericAttributeMessageConsumer(Attribute attribute, ProtocolAssetService assetService, Consumer<AttributeState> stateConsumer) {

        ValueFilter[] matchFilters = Values.getMetaItemValueOrThrow(
            attribute,
            Protocol.META_ATTRIBUTE_MATCH_FILTERS,
            false,
            true)
            .map(Value::toJson)
            .map(json -> {
                try {
                    return Values.JSON.readValue(json, ValueFilter[].class);
                } catch (IOException e) {
                    Protocol.LOG.log(Level.WARNING, "Failed to deserialize ValueFilter[]", e);
                    return null;
                }
            }).orElse(null);

        StringPredicate matchPredicate = Values.getMetaItemValueOrThrow(
            attribute,
            Protocol.META_ATTRIBUTE_MATCH_PREDICATE,
            false,
            true)
            .map(Value::toJson)
            .map(s -> {
                try {
                    return Values.JSON.readValue(s, StringPredicate.class);
                } catch (IOException e) {
                    Protocol.LOG.log(Level.WARNING, "Failed to deserialise StringPredicate", e);
                    return null;
                }
            })
            .orElse(null);

        if (matchPredicate == null) {
            return null;
        }

        AttributeRef attributeRef = attribute.getReferenceOrThrow();

        return message -> {
            if (!TextUtil.isNullOrEmpty(message)) {
                StringValue stringValue = Values.create(message);
                Value val = assetService.applyValueFilters(stringValue, matchFilters);
                if (val != null) {
                    if (StringPredicate.asPredicate(matchPredicate).test(message)) {
                        Protocol.LOG.finest("Message matches attribute so writing state to state consumer for attribute: " + attributeRef);
                        stateConsumer.accept(new AttributeState(attributeRef, stringValue));
                    }
                }
            }
        };
    }
}
