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
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.v2.MetaTypes;
import org.openremote.model.value.ValueFilter;
import org.openremote.model.value.Values;

import java.util.logging.Logger;

import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;

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
 *  the {@link Agent} is incorrectly configured) and this Agent's status will be marked as e attribute linking will not occur.</li>
 * <li>{@link #linkAttribute} - Called for each attribute linked to the {@link Agent}</li>
 * </ol>
 * <h3>Configuring protocol instances</h3>
 * Each {@link Agent} asset has its' own {@link Protocol} instance and this instance is responsible for managing only
 * the attributes linked to that specific instance.
 * <p>
 * <h3>Connection status</h3>
 * The protocol instance is responsible for calling the provided {@link ConnectionStatus} consumer whenever the status
 * of the logical (e.g. if the configuration is not valid then the protocol should call the consumer with a value of
 * {@link ConnectionStatus#ERROR} and it should provide sensible logging to allow fault finding).
 * <h3>Connecting attributes to actuators and sensors</h3>
 * {@link Attribute}s of {@link Asset}s can be linked to a protocol configuration instance by creating an {@link
 * MetaTypes#AGENT_LINK} {@link MetaItem} on an attribute. Besides the {@link MetaTypes#AGENT_LINK}, other
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
 * To simplify protocol development some common protocol behaviour is recommended for generic protocols:
 * <h1>Inbound value conversion (Protocol -> Linked Attribute)</h1>
 * <p>
 * Standard value filtering and/or conversion should be performed in the following order:
 * <ol>
 * <li>Configurable value filtering which allows the value produced by the protocol to be filtered through any
 * number of {@link ValueFilter}s before being written to the linked attribute
 * (see {@link Agent#META_VALUE_FILTERS})</li>
 * <li>Configurable value conversion which allows the value produced by the protocol to be converted in a configurable
 * way before being written to the linked attribute (see {@link Agent#META_VALUE_CONVERTER})</li>
 * <li>Automatic basic value conversion should be performed when the type of the value produced by the
 * protocol and any configured value conversion does not match the linked attributes underlying value type; this
 * basic conversion should use the {@link Values#convert} method</li>
 * </ol>
 * <h1>Outbound value conversion (Linked Attribute -> Protocol)</h1>
 * Standard value conversion should be performed in the following order:
 * <ol>
 * <li>Configurable value conversion which allows the value sent from the linked attribute to be converted in a
 * configurable way before being sent to the protocol for processing (see {@link Agent#META_WRITE_VALUE_CONVERTER})
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
 * {@link Agent} asset is created/loaded:
 * <ol>
 * <li>{@link #connect}</li>
 * <li>{@link #linkAttribute}</li>
 * </ol>
 * <p>
 * {@link Agent} is modified:
 * <ol>
 * <li>{@link #unlinkAttribute}</li>
 * <li>{@link #disconnect}</li>
 * <li>{@link #connect}</li>
 * <li>{@link #linkAttribute}</li>
 * </ol>
 * <p>
 * {@link Agent} is removed:
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
     * Attributes are linked to an agent via an {@link MetaTypes#AGENT_LINK} meta item.
     * @return True if successful, false otherwise
     */
    boolean linkAttribute(Asset asset, Attribute<?> attribute);

    /**
     * Un-links an {@link Attribute} from its' agent; the agent will still be connected during this call. This is called
     * whenever the attribute is modified or removed or when the agent is modified or removed.
     */
    void unlinkAttribute(Asset asset, Attribute<?> attribute);

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
     * Get the {@link Asset#getId} of the associated {@link Agent}; should be used in logging for instance identification
     */
    String getAgentId();

    /**
     *  Get the {@link Asset#getName} of the associated {@link Agent}; should be used in logging for instance identification
     */
    String getAgentName();
}
