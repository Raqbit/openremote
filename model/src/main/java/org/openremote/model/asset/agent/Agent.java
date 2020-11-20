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

    @ModelDescriptor
    public static final AttributeDescriptor<Boolean> DISABLED = new AttributeDescriptor<>("agentDisabled", true, ValueType.BOOLEAN);

    @ModelDescriptor
    public static final AttributeDescriptor<ConnectionStatus> STATUS = new AttributeDescriptor<>("agentStatus", true, ValueType.CONNECTION_STATUS,
        new MetaItem<>(MetaItemType.READ_ONLY)
    );

    /**
     * Can be used by protocols that support it to indicate that string values should be converted to/from bytes from/to
     * HEX string representation (e.g. 34FD87)
     */
    public static final AttributeDescriptor<Boolean> MESSAGE_CONVERT_HEX = new AttributeDescriptor<>("messageConvertHex", true, ValueType.BOOLEAN);

    /**
     * Can be used by protocols that support it to indicate that string values should be converted to/from bytes from/to
     * binary string representation (e.g. 1001010111)
     */
    public static final AttributeDescriptor<Boolean> MESSAGE_CONVERT_BINARY = new AttributeDescriptor<>("messageConvertBinary", true, ValueType.BOOLEAN);

    /**
     * Charset to use when converting byte[] to a string (should default to UTF8 if not specified); values must be
     * string that matches a charset as defined in {@link java.nio.charset.Charset}
     */
    public static final AttributeDescriptor<String> MESSAGE_CHARSET = new AttributeDescriptor<>("messageCharset", true, ValueType.STRING);

    /**
     * Max length of messages received by a {@link Protocol}; what this actually means will be protocol specific i.e.
     * for {@link String} protocols it could be the number of characters but for {@link Byte} protocols it could be the
     * number of bytes. This is typically used for I/O based {@link Protocol}s.
     */
    public static final AttributeDescriptor<Integer> MESSAGE_MAX_LENGTH = new AttributeDescriptor<>("messageMaxLength", true, ValueType.POSITIVE_INTEGER);

    /**
     * Defines a set of delimiters for messages received by a {@link Protocol}; the first matched delimiter should be
     * used to generate the shortest possible match(This is typically used for I/O based {@link Protocol}s.
     */
    public static final AttributeDescriptor<String[]> MESSAGE_DELIMITERS = new AttributeDescriptor<>("messageDelimiters", true, ValueType.STRING.asArray());

    /**
     * For protocols that use {@link #MESSAGE_DELIMITERS}, this indicates whether or not the matched delimiter
     * should be stripped from the message.
     */
    public static final AttributeDescriptor<Boolean> MESSAGE_STRIP_DELIMITER = new AttributeDescriptor<>("messageStripDelimiter", true, ValueType.BOOLEAN);

    /**
     * {@link OAuthGrant} for connecting to services that require OAuth authentication
     */
    public static final AttributeDescriptor<OAuthGrant> OAUTH_GRANT = new AttributeDescriptor<>("oAuthGrant", true, ValueType.OAUTH_GRANT);

    /**
     * Basic authentication username and password
     */
    public static final AttributeDescriptor<UsernamePassword> USERNAME_AND_PASSWORD = new AttributeDescriptor<>("usernamePassword", true, ValueType.USERNAME_AND_PASSWORD);

    /**
     * TCP/IP network host name/IP address
     */
    public static final AttributeDescriptor<String> HOST = new AttributeDescriptor<>("host", true, ValueType.HOSTNAME_OR_IP_ADDRESS);

    /**
     * TCP/IP network port number
     */
    public static final AttributeDescriptor<Integer> PORT = new AttributeDescriptor<>("port", true, ValueType.PORT);

    /**
     * Local TCP/IP network port number to bind to
     */
    public static final AttributeDescriptor<Integer> BIND_PORT = new AttributeDescriptor<>("bindPort", true, ValueType.PORT);

    /**
     * Local TCP/IP network host name/IP address to bind to
     */
    public static final AttributeDescriptor<String> BIND_HOST = new AttributeDescriptor<>("bindHost", true, ValueType.HOSTNAME_OR_IP_ADDRESS);

    /**
     * Serial port name/address
     */
    public static final AttributeDescriptor<String> SERIAL_PORT = new AttributeDescriptor<>("serialPort", true, ValueType.STRING);

    /**
     * Serial baudrate to use for connection
     */
    public static final AttributeDescriptor<Integer> SERIAL_BAUDRATE = new AttributeDescriptor<>("serialBaudrate", true, ValueType.POSITIVE_INTEGER);

    /**
     * Default polling frequency (milliseconds)
     */
    public static final AttributeDescriptor<Integer> POLLING_MILLIS = new AttributeDescriptor<>("pollingMillis", true, ValueType.POSITIVE_INTEGER);

    @ModelDescriptor
    protected static final ValueDescriptor<ValueFilter> VALUE_FILTER = new ValueDescriptor<>("Value filter", ValueFilter.class);

    @ModelDescriptor
    protected static final ValueDescriptor<ValuePredicate> VALUE_PREDICATE = new ValueDescriptor<>("Value predicate", ValuePredicate.class);

    /* LINKED ATTRIBUTE META BELOW HERE */

    /**
     * Defines {@link ValueFilter}s to apply to an incoming value before it is written to a protocol linked attribute;
     * this is particularly useful for generic protocols. The message should pass through the filters in array order.
     */
    public static final MetaItemDescriptor<ValueFilter[]> META_VALUE_FILTERS = new MetaItemDescriptor<>("valueFilters", VALUE_FILTER.asArray(), null);

    /**
     * Defines a value converter map to allow for basic value type conversion; the incoming value will be converted to
     * JSON and if this string matches a key in the converter then the value of that key will be pushed through to the
     * attribute. An example use case is an API that returns "ACTIVE"/"DISABLED" strings but you want to connect this to
     * a {@link ValueType#BOOLEAN} attribute.
     */
    public static final MetaItemDescriptor<ObjectNode> META_VALUE_CONVERTER = new MetaItemDescriptor<>("valueConverter", ValueType.JSON_OBJECT, null);

    /**
     * Similar to {@link #META_VALUE_CONVERTER} but will applied to outgoing values allowing for the opposite conversion.
     */
    public static final MetaItemDescriptor<ObjectNode> META_WRITE_VALUE_CONVERTER = new MetaItemDescriptor<>("writeValueConverter", ValueType.JSON_OBJECT, null);

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
    public static final MetaItemDescriptor<String> META_WRITE_VALUE = new MetaItemDescriptor<>("writeValue", ValueType.STRING, null);

    /**
     * Polling frequency in milliseconds for {@link Attribute}s whose value should be polled.
     */
    public static final MetaItemDescriptor<Integer> META_POLLING_MILLIS = new MetaItemDescriptor<>("pollingMillis", ValueType.POSITIVE_INTEGER, null);

    /**
     * The predicate to use on incoming messages to determine if the message is intended for the {@link Attribute} that
     * has this {@link MetaItem}; it is particularly useful for pub-sub based {@link Protocol}s.
     */
    public static final MetaItemDescriptor<ValuePredicate> META_MESSAGE_MATCH_PREDICATE = new MetaItemDescriptor<>("messageMatchPredicate", VALUE_PREDICATE, null);

    /**
     * {@link ValueFilter}s to apply to incoming messages prior to comparison with the {@link
     * #META_MESSAGE_MATCH_PREDICATE}, if the predicate matches then the original un-filtered message is
     * intended for this linked {@link Attribute} and the message should be written to the {@link Attribute} where the
     * actual value written can be filtered using the {@link #META_VALUE_FILTERS}.
     */
    public static final MetaItemDescriptor<ValueFilter[]> META_MESSAGE_MATCH_FILTERS = new MetaItemDescriptor<>("messageMatchFilters", VALUE_FILTER.asArray(), null);

    protected static MetaItemDescriptor<?>[] GENERIC_PROTOCOL_LINKED_ATTRIBUTE_META_DESCRIPTORS = new MetaItemDescriptor<?>[] {
        META_VALUE_FILTERS,
        META_VALUE_CONVERTER,
        META_WRITE_VALUE_CONVERTER,
        META_WRITE_VALUE,
        META_POLLING_MILLIS,
        META_MESSAGE_MATCH_PREDICATE,
        META_MESSAGE_MATCH_FILTERS
    };

    protected static AttributeDescriptor<?>[] GENERIC_PROTOCOL_ATTRIBUTE_DESCRIPTORS = new AttributeDescriptor<?>[] {
        MESSAGE_CHARSET,
        MESSAGE_CONVERT_BINARY,
        MESSAGE_CONVERT_HEX,
        MESSAGE_MAX_LENGTH,
        MESSAGE_STRIP_DELIMITER,
        MESSAGE_DELIMITERS
    };

    protected <T extends Agent, S extends Protocol<T>> Agent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    /**
     * Get the protocol instance for this Agent.
     */
    public abstract Protocol getProtocolInstance();

    public Optional<Boolean> isDisabled() {
        return getAttributes().getValue(DISABLED);
    }

    public Agent setDisabled(boolean disabled) {
        getAttributes().addOrReplace(new Attribute<>(DISABLED, disabled));
        return this;
    }

    public Optional<ConnectionStatus> getAgentStatus() {
        return getAttributes().getValue(STATUS);
    }

    public Optional<Boolean> getMessageConvertHex() {
        return getAttributes().getValue(MESSAGE_CONVERT_HEX);
    }

    public Optional<Boolean> getMessageConvertBinary() {
        return getAttributes().getValue(MESSAGE_CONVERT_BINARY);
    }

    public Optional<String> getMessageCharset() {
        return getAttributes().getValue(MESSAGE_CHARSET);
    }

    public Optional<Integer> getMessageMaxLength() {
        return getAttributes().getValue(MESSAGE_MAX_LENGTH);
    }

    public Optional<String[]> getMessageDelimiters() {
        return getAttributes().getValue(MESSAGE_DELIMITERS);
    }

    public Optional<Boolean> getMessageStripDelimiter() {
        return getAttributes().getValue(MESSAGE_STRIP_DELIMITER);
    }

    public Optional<OAuthGrant> getOAuthGrant() {
        return getAttributes().getValue(OAUTH_GRANT);
    }

    public Optional<UsernamePassword> getUsernamePassword() {
        return getAttributes().getValue(USERNAME_AND_PASSWORD);
    }

    public Optional<String> getHost() {
        return getAttributes().getValue(HOST);
    }

    public Optional<Integer> getPort() {
        return getAttributes().getValue(PORT);
    }

    public Optional<Integer> getBindPort() {
        return getAttributes().getValue(BIND_PORT);
    }

    public Optional<String> getBindHost() {
        return getAttributes().getValue(BIND_HOST);
    }

    public Optional<String> getSerialPort() {
        return getAttributes().getValue(SERIAL_PORT);
    }

    public Optional<Integer> getSerialBaudrate() {
        return getAttributes().getValue(SERIAL_BAUDRATE);
    }

    public Optional<Integer> getPollingMillis() {
        return getAttributes().getValue(POLLING_MILLIS);
    }
}
