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
package org.openremote.model.asset.agent;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.auth.OAuthGrant;
import org.openremote.model.auth.UsernamePassword;
import org.openremote.model.query.filter.ValuePredicate;
import org.openremote.model.v2.*;
import org.openremote.model.value.ValueFilter;
import org.openremote.model.value.Values;

import java.util.Optional;

/**
 * An agent is a special sub type of {@link Asset} that is associated with a {@link Protocol} and is responsible
 * for providing an instance of the associated {@link Protocol} when requested via {@link #getProtocolInstance}.
 */
public abstract class Agent extends Asset {

    public static final AttributeDescriptor<Boolean> DISABLED = new AttributeDescriptor<>("agentDisabled", true, ValueTypes.BOOLEAN, null);

    public static final AttributeDescriptor<ConnectionStatus> STATUS = new AttributeDescriptor<>("agentStatus", true, ValueTypes.CONNECTION_STATUS, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );

    /**
     * Can be used by protocols that support it to indicate that string values should be converted to/from bytes from/to
     * HEX string representation (e.g. 34FD87)
     */
    public static final AttributeDescriptor<Boolean> MESSAGE_CONVERT_HEX = new AttributeDescriptor<>("messageConvertHex", true, ValueTypes.BOOLEAN, true);

    /**
     * Can be used by protocols that support it to indicate that string values should be converted to/from bytes from/to
     * binary string representation (e.g. 1001010111)
     */
    public static final AttributeDescriptor<Boolean> MESSAGE_CONVERT_BINARY = new AttributeDescriptor<>("messageConvertBinary", true, ValueTypes.BOOLEAN, true);

    /**
     * Charset to use when converting byte[] to a string (should default to UTF8 if not specified); values must be
     * string that matches a charset as defined in {@link java.nio.charset.Charset}
     */
    public static final AttributeDescriptor<String> MESSAGE_CHARSET = new AttributeDescriptor<>("messageCharset", true, ValueTypes.STRING, "UTF8");

    /**
     * Max length of messages received by a {@link Protocol}; what this actually means will be protocol specific i.e.
     * for {@link String} protocols it could be the number of characters but for {@link Byte} protocols it could be the
     * number of bytes. This is typically used for I/O based {@link Protocol}s.
     */
    public static final AttributeDescriptor<Integer> MESSAGE_MAX_LENGTH = new AttributeDescriptor<>("messageMaxLength", true, ValueTypes.POSITIVE_INTEGER, null);

    /**
     * Min length of messages received by a {@link Protocol}; what this actually means will be protocol specific i.e.
     * for {@link String} protocols it could be the number of characters but for {@link Byte} protocols it could be the
     * number of bytes. This is typically used for I/O based {@link Protocol}s.
     */
    public static final AttributeDescriptor<Integer> MESSAGE_MIN_LENGTH = new AttributeDescriptor<>("messageMinLength", true, ValueTypes.POSITIVE_INTEGER, null);

    /**
     * Defines a delimiter for messages received by a {@link Protocol}. Multiples of this {@link MetaItem} can be used
     * to add multiple delimiters (the first matched delimiter should be used to generate the shortest possible match(.
     * This is typically used for I/O based {@link Protocol}s.
     */
    public static final AttributeDescriptor<String> MESSAGE_DELIMITER = new AttributeDescriptor<>("messageDelimiter", true, ValueTypes.STRING, null);

    /**
     * For protocols that use {@link #MESSAGE_DELIMITER}, this indicates whether or not the matched delimiter
     * should be stripped from the message.
     */
    public static final AttributeDescriptor<Boolean> MESSAGE_STRIP_DELIMITER = new AttributeDescriptor<>("messageStripDelimiter", true, ValueTypes.BOOLEAN, true);

    /**
     * {@link OAuthGrant} for connecting to services that require OAuth authentication
     */
    public static final AttributeDescriptor<OAuthGrant> OAUTH_GRANT = new AttributeDescriptor<>("oAuthGrant", true, ValueTypes.OAUTH_GRANT, null);

    /**
     * Basic authentication username and password
     */
    public static final AttributeDescriptor<UsernamePassword> USERNAME_AND_PASSWORD = new AttributeDescriptor<>("usernamePassword", true, ValueTypes.USERNAME_AND_PASSWORD, null);

    /**
     * TCP/IP network host name/IP address
     */
    public static final AttributeDescriptor<String> HOST = new AttributeDescriptor<>("host", true, ValueTypes.HOSTNAME_OR_IP_ADDRESS, null);

    /**
     * TCP/IP network port number
     */
    public static final AttributeDescriptor<Integer> PORT = new AttributeDescriptor<>("port", true, ValueTypes.PORT, null);

    /**
     * Serial port name/address
     */
    public static final AttributeDescriptor<String> SERIAL_PORT = new AttributeDescriptor<>("serialPort", true, ValueTypes.STRING, null);

    /**
     * Serial baudrate to use for connection
     */
    public static final AttributeDescriptor<Integer> SERIAL_BAUDRATE = new AttributeDescriptor<>("serialBaudrate", true, ValueTypes.POSITIVE_INTEGER, null);

    public static final AttributeDescriptor<Integer> POLLING_MILLIS = new AttributeDescriptor<>("pollingMillis", true, ValueTypes.POSITIVE_INTEGER, null);


    /* LINKED ATTRIBUTE META BELOW HERE */

    protected static final ValueDescriptor<ValueFilter> VALUE_FILTER = new ValueDescriptor<>("Value filter", ValueFilter.class);
    protected static final ValueDescriptor<ValuePredicate> VALUE_PREDICATE = new ValueDescriptor<>("Value predicate", ValuePredicate.class);

    /**
     * Defines {@link ValueFilter}s to apply to an incoming value before it is written to a protocol linked attribute;
     * this is particularly useful for generic protocols. The message should pass through the filters in array order.
     */
    public static final MetaDescriptor<ValueFilter[]> META_VALUE_FILTERS = new MetaDescriptor<>("valueFilters", VALUE_FILTER.asArray(), null);

    /**
     * Defines a value converter map to allow for basic value type conversion; the incoming value will be converted to
     * JSON and if this string matches a key in the converter then the value of that key will be pushed through to the
     * attribute. An example use case is an API that returns "ACTIVE"/"DISABLED" strings but you want to connect this to
     * a {@link ValueTypes#BOOLEAN} attribute.
     */
    public static final MetaDescriptor<ObjectNode> META_VALUE_CONVERTER = new MetaDescriptor<>("valueConverter", ValueTypes.OBJECT, null);

    /**
     * Similar to {@link #META_VALUE_CONVERTER} but will applied to outgoing values allowing for the opposite conversion.
     */
    public static final MetaDescriptor<ObjectNode> META_WRITE_VALUE_CONVERTER = new MetaDescriptor<>("writeValueConverter", ValueTypes.OBJECT, null);

    /**
     * JSON string to be used for attribute writes and can contain {@link Protocol#DYNAMIC_VALUE_PLACEHOLDER}s; this allows the
     * written value to be injected into a bigger JSON payload or to even hardcode the value sent to the protocol (i.e.
     * ignore the written value). If this {@link MetaItem} is not defined then the written value is passed through to
     * the protocol as is. The resulting JSON string (after any dynamic value insertion) is then parsed using {@link
     * Values#parse} so it is important the value of this {@link MetaItem} is a valid JSON string so literal strings
     * must be quoted (e.g. '"string value"' not 'string value' otherwise parsing will fail).
     * <p>
     * A value of 'null' will produce a literal null.
     */
    public static final MetaDescriptor<String> META_WRITE_VALUE = new MetaDescriptor<>("writeValue", ValueTypes.STRING, null);

    /**
     * Polling frequency in milliseconds for {@link Attribute}s whose value should be polled.
     */
    public static final MetaDescriptor<Integer> META_POLLING_MILLIS = new MetaDescriptor<>("pollingMillis", ValueTypes.POSITIVE_INTEGER, null);

    /**
     * The predicate to use on incoming messages to determine if the message is intended for the {@link Attribute} that
     * has this {@link MetaItem}; it is particularly useful for pub-sub based {@link Protocol}s.
     */
    public static final MetaDescriptor<ValuePredicate> META_MESSAGE_MATCH_PREDICATE = new MetaDescriptor<>("messageMatchPredicate", VALUE_PREDICATE, null);

    /**
     * {@link ValueFilter}s to apply to incoming messages prior to comparison with the {@link
     * #META_MESSAGE_MATCH_PREDICATE}, if the predicate matches then the original un-filtered message is
     * intended for this linked {@link Attribute} and the message should be written to the {@link Attribute} where the
     * actual value written can be filtered using the {@link #META_VALUE_FILTERS}.
     */
    public static final MetaDescriptor<ValueFilter[]> META_MESSAGE_MATCH_FILTERS = new MetaDescriptor<>("messageMatchFilters", VALUE_FILTER.asArray(), null);

    protected MetaDescriptor<?>[] LINKED_ATTRIBUTE_META_DESCRIPTORS = new MetaDescriptor<?>[] {
        META_VALUE_FILTERS,
        META_VALUE_CONVERTER,
        META_WRITE_VALUE_CONVERTER,
        META_WRITE_VALUE,
        META_POLLING_MILLIS,
        META_MESSAGE_MATCH_PREDICATE,
        META_MESSAGE_MATCH_FILTERS
    };

    protected <T extends Agent> Agent(String name, AssetDescriptor<T> descriptor) {
        super(name, descriptor);
    }

    /**
     * Get the protocol instance for this Agent.
     */
    public abstract Protocol getProtocolInstance();

    public Optional<Boolean> isDisabled() {
        return getAttributes().getValueOrDefault(DISABLED);
    }

    public Agent setDisabled(boolean disabled) {
        getAttributes().addOrReplace(new Attribute<>(DISABLED, disabled));
        return this;
    }

    public Optional<ConnectionStatus> getAgentStatus() {
        return getAttributes().getValueOrDefault(STATUS);
    }
    public Optional<Boolean> getMessageConvertHex() {
        return getAttributes().getValueOrDefault(MESSAGE_CONVERT_HEX);
    }

    public Optional<Boolean> getMessageConvertBinary() {
        return getAttributes().getValueOrDefault(MESSAGE_CONVERT_BINARY);
    }

    public Optional<String> getMessageCharset() {
        return getAttributes().getValueOrDefault(MESSAGE_CHARSET);
    }

    public Optional<Integer> getMessageMaxLength() {
        return getAttributes().getValueOrDefault(MESSAGE_MAX_LENGTH);
    }

    public Optional<Integer> getMessageMinLength() {
        return getAttributes().getValueOrDefault(MESSAGE_MIN_LENGTH);
    }

    public Optional<String> getMessageDelimiter() {
        return getAttributes().getValueOrDefault(MESSAGE_DELIMITER);
    }

    public Optional<Boolean> getMessageStripDelimiter() {
        return getAttributes().getValueOrDefault(MESSAGE_STRIP_DELIMITER);
    }

    public Optional<OAuthGrant> getOAuthGrant() {
        return getAttributes().getValueOrDefault(OAUTH_GRANT);
    }

    public Optional<UsernamePassword> getUsernamePassword() {
        return getAttributes().getValueOrDefault(USERNAME_AND_PASSWORD);
    }

    public Optional<String> getHost() {
        return getAttributes().getValueOrDefault(HOST);
    }

    public Optional<Integer> getPort() {
        return getAttributes().getValueOrDefault(PORT);
    }

    public Optional<String> getSerialPort() {
        return getAttributes().getValueOrDefault(SERIAL_PORT);
    }

    public Optional<Integer> getSerialBaudrate() {
        return getAttributes().getValueOrDefault(SERIAL_BAUDRATE);
    }
}
