/*
 * Copyright 2016, OpenRemote Inc.
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

import org.openremote.model.ContainerProvider;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.*;
import org.openremote.model.auth.OAuthGrant;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.query.filter.StringPredicate;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.util.TextUtil;
import org.openremote.model.value.*;

import java.nio.charset.Charset;
import java.util.logging.Logger;

import static org.openremote.model.Constants.PROTOCOL_NAMESPACE;
import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;
import static org.openremote.model.util.TextUtil.REGEXP_PATTERN_STRING_NON_EMPTY;

/**
 * A protocol instance is responsible for connecting devices and services to the context broker. A protocol instance
 * has a one-to-one mapping with an {@link Agent} and should get its' configuration parameters from the {@link Attribute}s
 * of this {@link Agent}, how this is done is up to the {@link Agent}/{@link Protocol} creator to determine.
 * <h2>Lifecycle</h2>
 * When the system is started or a CRUD operation is performed on an {@link Agent} then the following calls are made:
 * <h3>Destroy existing protocol instance</h3>
 * If a protocol instance already exists for the {@link Agent} then the following calls are made on that instance:
 * <ol>
 * <li>{@link #unlinkAttribute} - Called for each attribute linked to the {@link Agent}</li>
 * <li>{@link #disconnect}</li>
 * </ol>
 * <h3>Create/initialise protocol instance</h3>
 * If the {@link Agent} was deleted or {@link Agent#isDisabled} then nothing happens otherwise:
 * <ol>
 * <li>{@link #connect} - If this call returns false then it is assumed that a permanent failure has occurred (i.e.
 *  the {@link Agent} is incorrectly configured) and this Agent's  status will be marked as e attribute linking will not occur.</li>
 * <li>{@link #linkAttribute} - Called for each attribute linked to the {@link Agent}</li>
 * </ol>
 * <h3>Configuring protocol instances</h3>
 * A protocol implementation must support multiple protocol configurations and therefore support multiple logical
 * instances. A protocol 'instance' can be defined by an attribute on an {@link Asset} that has a type of {@link
 * AssetType#AGENT}; the attribute must conform to {@link ProtocolConfiguration}.
 * <p>
 * When a protocol configuration is loaded/created for a protocol then the {@link #connect} method
 * will be called. The protocol should check the {@link Attribute#isEnabled} status of the protocol configuration
 * to determine whether or not the logical instance should be running or stopped.
 * <p>
 * The protocol is responsible for calling the provided consumer whenever the status of the logical instance changes
 * (e.g. if the configuration is not valid then the protocol should call the consumer with a value of {@link
 * ConnectionStatus#ERROR} and it should provide sensible logging to allow fault finding).
 * <h3>Connecting attributes to actuators and sensors</h3>
 * {@link Attribute}s of {@link Asset}s can be linked to a protocol configuration instance by creating an {@link
 * MetaItemType#AGENT_LINK} {@link MetaItem} on an attribute. Besides the {@link MetaItemType#AGENT_LINK}, other
 * protocol-specific meta items may also be required when an asset attribute is linked to a protocol configuration
 * instance. Attributes linked to a protocol configuration instance will get passed to the protocol via a call to {@link
 * #linkAttribute}.
 * <p>
 * The protocol handles read and write of linked attributes:
 * <p>
 * If the actual state of the device (or service) changes, the linked protocol writes the new state into the attribute
 * value and notifies the context broker of the change. A protocol updates a linked attributes' value by sending  an
 * {@link AttributeEvent} messages on the {@link #SENSOR_QUEUE}, including the source protocol name in header {@link
 * #SENSOR_QUEUE_SOURCE_PROTOCOL}.
 * <p>
 * If the user writes a new value into the linked attribute, the protocol translates this value change into a device (or
 * service) action. Write operations on attributes linked to a protocol configuration can be consumed by the protocol on
 * the {@link #ACTUATOR_TOPIC} where the message body will be an {@link AttributeEvent}. Each message also contains the
 * target protocol name in header {@link #ACTUATOR_TOPIC_TARGET_PROTOCOL}.
 * <p>
 * To simplify protocol development some common protocol behaviour is recommended:
 * <h1>Inbound value conversion (Protocol -> Linked Attribute)</h1>
 * <p>
 * Standard value filtering and/or conversion should be performed in the following order, this is encapsulated in {@link
 * Util#doInboundValueProcessing}:
 * <ol>
 * <li>Configurable value filtering which allows the value produced by the protocol to be filtered through any
 * number of {@link ValueFilter}s before being written to the linked attribute
 * (see {@link #META_ATTRIBUTE_VALUE_FILTERS})</li>
 * <li>Configurable value conversion which allows the value produced by the protocol to be converted in a configurable
 * way before being written to the linked attribute (see {@link #META_ATTRIBUTE_VALUE_CONVERTER})</li>
 * <li>Automatic basic value conversion should be performed when the {@link ValueType} of the value produced by the
 * protocol and any configured value conversion does not match the linked attributes underlying {@link ValueType}; this
 * basic conversion should use the {@link Values#convertToValue} method</li>
 * </ol>
 * <h1>Outbound value conversion (Linked Attribute -> Protocol)</h1>
 * Standard value conversion should be performed in the following order, this is encapsulated in
 * {@link Util#doOutboundValueProcessing}:
 * <ol>
 * <li>Configurable value conversion which allows the value sent from the linked attribute to be converted in a
 * configurable way before being sent to the protocol for processing (see {@link #META_ATTRIBUTE_WRITE_VALUE_CONVERTER})
 * <li>Configurable dynamic value insertion (replacement of {@link #DYNAMIC_VALUE_PLACEHOLDER} strings within a
 * pre-defined JSON string with the value sent from the linked attribute (this allows for attribute values to be inserted
 * into a larger payload before processing by the protocol; it also allows the written value to be fixed or statically
 * converted.
 * </ol>
 * When sending the converted value onto the actual protocol implementation for processing the original
 * {@link AttributeEvent} as well as the converted value should be made available.
 * <p>
 * NOTE: That {@link #connect} will always be called
 * before {@link #linkAttribute} and {@link #unlinkAttribute} will always be called before
 * {@link #disconnect}.
 * <p>
 * The following summarises the method calls protocols should expect:
 * <p>
 * Protocol configuration (logical instance) is created/loaded:
 * <ol>
 * <li>{@link #connect}</li>
 * <li>{@link #linkAttribute}</li>
 * </ol>
 * <p>
 * Protocol configuration (logical instance) is modified:
 * <ol>
 * <li>{@link #unlinkAttribute}</li>
 * <li>{@link #disconnect}</li>
 * <li>{@link #connect}</li>
 * <li>{@link #linkAttribute}</li>
 * </ol>
 * <p>
 * Protocol configuration (logical instance) is removed:
 * <ol>
 * <li>{@link #unlinkAttribute}</li>
 * <li>{@link #disconnect}</li>
 * </ol>
 * <p>
 * Attribute linked to protocol configuration is created/loaded:
 * <ol>
 * <li>{@link #linkAttribute}</li>
 * </ol>
 * <p>
 * Attribute linked to protocol configuration is modified:
 * <ol>
 * <li>{@link #unlinkAttribute}</li>
 * <li>{@link #linkAttribute}</li>
 * </ol>
 * <p>
 * Attribute link to protocol configuration is removed:
 * <ol>
 * <li>{@link #unlinkAttribute}</li>
 * </ol>
 */
public interface Protocol<T extends Agent> {

    Logger LOG = SyslogCategory.getLogger(PROTOCOL, Protocol.class);
    String ACTUATOR_TOPIC_TARGET_PROTOCOL = "Protocol";
    String SENSOR_QUEUE_SOURCE_PROTOCOL = "Protocol";

    /**
     * Can be used by protocols that support it to indicate that string values should be converted to/from bytes from/to
     * HEX string representation (e.g. 34FD87)
     */
    MetaItemDescriptor META_PROTOCOL_CONVERT_HEX = metaItemFixedBoolean(PROTOCOL_NAMESPACE + ":convertHex", ACCESS_PRIVATE, false);

    /**
     * Can be used by protocols that support it to indicate that string values should be converted to/from bytes from/to
     * binary string representation (e.g. 1001010111)
     */
    MetaItemDescriptor META_PROTOCOL_CONVERT_BINARY = metaItemFixedBoolean(PROTOCOL_NAMESPACE + ":convertBinary", ACCESS_PRIVATE, false);

    /**
     * Charset to use when converting byte[] to a string (should default to UTF8 if not specified); values must be
     * string that matches a charset as defined in {@link java.nio.charset.Charset}
     */
    MetaItemDescriptor META_PROTOCOL_CHARSET = metaItemString(
        PROTOCOL_NAMESPACE + ":charset",
        ACCESS_PRIVATE,
        false,
        Charset.availableCharsets().keySet().toArray(new String[0])
    );

    /**
     * Max length of messages received by a {@link Protocol}; what this actually means will be protocol specific i.e.
     * for {@link String} protocols it could be the number of characters but for {@link Byte} protocols it could be the
     * number of bytes. This is typically used for I/O based {@link Protocol}s.
     */
    MetaItemDescriptor META_PROTOCOL_MAX_LENGTH = metaItemInteger(
        PROTOCOL_NAMESPACE + ":maxLength",
        ACCESS_PRIVATE,
        false,
        0,
        Integer.MAX_VALUE
    );

    /**
     * Defines a delimiter for messages received by a {@link Protocol}. Multiples of this {@link MetaItem} can be used
     * to add multiple delimiters (the first matched delimiter should be used to generate the shortest possible match(.
     * This is typically used for I/O based {@link Protocol}s.
     */
    MetaItemDescriptor META_PROTOCOL_DELIMITER = new MetaItemDescriptorImpl(
        PROTOCOL_NAMESPACE + ":delimiter",
        ValueType.STRING,
        ACCESS_PRIVATE,
        false,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        false
    );

    /**
     * For protocols that use {@link #META_PROTOCOL_DELIMITER}, this indicates whether or not the matched delimiter
     * should be stripped from the message.
     */
    MetaItemDescriptor META_PROTOCOL_STRIP_DELIMITER = metaItemFixedBoolean(
        PROTOCOL_NAMESPACE + ":stripDelimiter",
        ACCESS_PRIVATE,
        false
    );

    /**
     * OAuth grant ({@link OAuthGrant} stored as {@link ObjectValue})
     */
    MetaItemDescriptor META_PROTOCOL_OAUTH_GRANT = metaItemObject(
        PROTOCOL_NAMESPACE + ":oAuthGrant",
        ACCESS_PRIVATE,
        false,
        null);


    /**
     * Basic authentication username (string)
     */
    MetaItemDescriptor META_PROTOCOL_USERNAME = metaItemString(
        PROTOCOL_NAMESPACE + ":username",
        ACCESS_PRIVATE,
        false,
        REGEXP_PATTERN_STRING_NON_EMPTY,
        PatternFailure.STRING_EMPTY);

    /**
     * Basic authentication password (string)
     */
    MetaItemDescriptor META_PROTOCOL_PASSWORD = metaItemString(
        PROTOCOL_NAMESPACE + ":password",
        ACCESS_PRIVATE,
        false,
        true,
        REGEXP_PATTERN_STRING_NON_EMPTY,
        PatternFailure.STRING_EMPTY);


    /**
     * TCP/IP network host name/IP address
     */
    MetaItemDescriptor META_PROTOCOL_HOST = metaItemString(
        PROTOCOL_NAMESPACE + ":host",
        ACCESS_PRIVATE,
        true,
        TextUtil.REGEXP_PATTERN_STRING_NON_EMPTY_NO_WHITESPACE,
        PatternFailure.STRING_EMPTY_OR_CONTAINS_WHITESPACE);

    /**
     * TCP/IP network port number
     */
    MetaItemDescriptor META_PROTOCOL_PORT = metaItemInteger(
        PROTOCOL_NAMESPACE + ":port",
        ACCESS_PRIVATE,
        true,
        1,
        65536);

    /**
     * Serial port name/address
     */
    MetaItemDescriptor META_PROTOCOL_SERIAL_PORT = metaItemString(
        PROTOCOL_NAMESPACE + ":serialPort",
        ACCESS_PRIVATE,
        true,
        REGEXP_PATTERN_STRING_NON_EMPTY,
        PatternFailure.STRING_EMPTY);

    MetaItemDescriptor META_PROTOCOL_SERIAL_BAUDRATE = metaItemInteger(
        PROTOCOL_NAMESPACE + ":baudrate",
        ACCESS_PRIVATE,
        true,
        1,
        Integer.MAX_VALUE);

    /**
     * Defines {@link ValueFilter}s to apply to an incoming value before it is written to a protocol linked attribute;
     * this is particularly useful for generic protocols. The {@link MetaItem} value should be an {@link ArrayValue} of
     * {@link ObjectValue}s where each {@link ObjectValue} represents a serialised {@link ValueFilter}. The message
     * should pass through the filters in array order.
     */
    MetaItemDescriptor META_ATTRIBUTE_VALUE_FILTERS = metaItemArray(
        PROTOCOL_NAMESPACE + ":valueFilters",
        ACCESS_PRIVATE,
        false,
        null);

    /**
     * Defines a value converter map to allow for basic value type conversion; the incoming value will be converted to
     * JSON and if this string matches a key in the converter then the value of that key will be pushed through to the
     * attribute. An example use case is an API that returns "ACTIVE"/"DISABLED" strings but you want to connect this to
     * a {@link AttributeValueType#BOOLEAN} attribute.
     */
    MetaItemDescriptor META_ATTRIBUTE_VALUE_CONVERTER = metaItemObject(
        PROTOCOL_NAMESPACE + ":valueConverter",
        ACCESS_PRIVATE,
        false,
        null);

    /**
     * Similar to {@link #META_ATTRIBUTE_VALUE_CONVERTER} but will applied to outgoing values allowing for the opposite
     * conversion.
     */
    MetaItemDescriptor META_ATTRIBUTE_WRITE_VALUE_CONVERTER = metaItemObject(
        PROTOCOL_NAMESPACE + ":writeValueConverter",
        ACCESS_PRIVATE,
        false,
        null);

    /**
     * JSON string to be used for attribute writes and can contain {@link #DYNAMIC_VALUE_PLACEHOLDER}s; this allows the
     * written value to be injected into a bigger JSON payload or to even hardcode the value sent to the protocol (i.e.
     * ignore the written value). If this {@link MetaItem} is not defined then the written value is passed through to
     * the protocol as is. The resulting JSON string (after any dynamic value insertion) is then parsed using {@link
     * Values#parse} so it is important the value of this {@link MetaItem} is a valid JSON string so literal strings
     * must be quoted (e.g. '"string value"' not 'string value' otherwise parsing will fail).
     * <p>
     * A value of 'null' will produce a literal null.
     */
    MetaItemDescriptor META_ATTRIBUTE_WRITE_VALUE = metaItemAny(
        PROTOCOL_NAMESPACE + ":writeValue",
        ACCESS_PRIVATE,
        false,
        null,
        TextUtil.REGEXP_PATTERN_STRING_NON_EMPTY,
        PatternFailure.STRING_EMPTY.name());

    /**
     * Polling frequency in milliseconds for {@link Attribute}s whose value should be polled; can be set on the {@link
     * ProtocolConfiguration} or the {@link Attribute} (the latter takes precedence).
     */
    MetaItemDescriptor META_ATTRIBUTE_POLLING_MILLIS = metaItemInteger(
        PROTOCOL_NAMESPACE + ":pollingMillis",
        ACCESS_PRIVATE,
        false,
        1000,
        null);

    /**
     * The predicate to use on incoming messages to determine if the message is intended for the {@link Attribute} that
     * has this {@link MetaItem}; it is particularly useful for pub-sub based {@link Protocol}s.
     */
    MetaItemDescriptor META_ATTRIBUTE_MATCH_PREDICATE = metaItemObject(
        PROTOCOL_NAMESPACE + ":matchPredicate",
        ACCESS_PRIVATE,
        false,
        new StringPredicate(AssetQuery.Match.EXACT, false, "").toModelValue());

    /**
     * {@link ValueFilter}s to apply to incoming messages prior to comparison with the {@link
     * Protocol#META_ATTRIBUTE_MATCH_PREDICATE}, if the predicate matches then the original un-filtered message is
     * intended for this linked {@link Attribute} and the message should be written to the {@link Attribute} where the
     * actual {@link Value} written can be filtered using the {@link Protocol#META_ATTRIBUTE_VALUE_FILTERS}.
     * <p>
     * The {@link Value} of this {@link MetaItem} must be an {@link ArrayValue} of {@link ObjectValue}s where each
     * {@link ObjectValue} represents a serialised {@link ValueFilter}. The message will pass through the filters in
     * array order and the resulting final {@link Value} should be written to the {@link Attribute}
     */
    MetaItemDescriptor META_ATTRIBUTE_MATCH_FILTERS = metaItemArray(
        PROTOCOL_NAMESPACE + ":matchFilters",
        ACCESS_PRIVATE,
        false,
        null);

    // TODO: Some of these options should be configurable depending on expected load etc.
    // Message topic for communicating from asset/thing to protocol layer (asset attribute changed, trigger actuator)
    String ACTUATOR_TOPIC = "seda://ActuatorTopic?multipleConsumers=true&concurrentConsumers=1&waitForTaskToComplete=NEVER&purgeWhenStopping=true&discardIfNoConsumers=true&limitConcurrentConsumers=false&size=1000";

    // Message queue for communicating from protocol to asset/thing layer (sensor changed, trigger asset attribute update)
    String SENSOR_QUEUE = "seda://SensorQueue?waitForTaskToComplete=NEVER&purgeWhenStopping=true&discardIfNoConsumers=false&size=25000";

    String DYNAMIC_VALUE_PLACEHOLDER = "{$value}";

    String DYNAMIC_VALUE_PLACEHOLDER_REGEXP = "\"?\\{\\$value}\"?";

    //Specify to use time. First # delimiter used to specify format. Second # delimiter used to add or subtract millis
    String DYNAMIC_TIME_PLACEHOLDER_REGEXP = "\"?\\{\\$time#?([a-z,A-Z,\\-,\\s,:]*)#?(-?\\d*)?}\"?";

    /**
     * Get the name for this protocol
     */
    String getProtocolName();

    /**
     * Get the display friendly name for this protocol
     */
    String getProtocolDisplayName();

    /**
     * Links an {@link Attribute} to its' agent; the agent would have been connected before this call. This is called
     * when the agent is connected or when the attribute has been modified and re-linked.
     * <p>
     * If the attribute is not valid for this protocol then it is up to the protocol to log the issue and return false.
     * <p>
     * Attributes are linked to an agent via an {@link MetaItemType#AGENT_LINK} meta item.
     * @return True if successful, false otherwise
     */
    boolean linkAttribute(Asset asset, Attribute attribute);

    /**
     * Un-links an {@link Attribute} from its' agent; the agent will still be connected during this call. This is called
     * whenever the attribute is modified or removed or when the agent is modified or removed.
     */
    void unlinkAttribute(Asset asset, Attribute attribute);

    /**
     * Called before {@link #connect} to allow the protocol to perform required tasks with {@link ContainerService}s e.g.
     * register Camel routes and to store the associated {@link Agent} reference.
     */
    void start(T agent, ContainerProvider container) throws Exception;

    /**
     * Called after {@link #disconnect} to allow the protocol to perform required tasks with {@link ContainerService}s e.g.
     * remove Camel routes.
     */
    void stop(ContainerProvider container) throws Exception;

    /**
     * Connect/initialise the protocol instance using the settings defined in the {@link Agent};
     * the {@link Agent} is responsible for instantiating the protocol instance and passing any
     * required configuration to the protocol via the {@link Agent#getProtocolInstance}.
     * @return true if the instance is usable and attributes should be linked, false otherwise
     */
    boolean connect();

    /**
     * Disconnect/destroy the protocol instance, implementors should cleanup any resources associated with the instance.
     */
    void disconnect();

    /**
     * Get the {@link Asset#getId} of the associated {@link Agent}
     */
    String getAgentId();

    /**
     *  Get the {@link Asset#getName} of the associated {@link Agent}
     */
    String getAgentName();

    /**
     * Get a {@link ProtocolDescriptor} for this protocol.
     */
    ProtocolDescriptor getProtocolDescriptor();

    /**
     * Create an empty {@link ProtocolConfiguration} attribute that contains the required meta items needed by the
     * protocol. The purpose of this is to populate the UI when adding a new protocol configuration for this protocol.
     */
    Attribute getProtocolConfigurationTemplate();

    /**
     * Validate the supplied {@link ProtocolConfiguration} attribute against this protocol (should indicate that the
     * protocol configuration is well formed but not necessarily that it connects to a working system).
     */
    AttributeValidationResult validateProtocolConfiguration(Attribute protocolConfiguration);
}
