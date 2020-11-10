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
package org.openremote.agent.protocol.zwave;

import io.netty.channel.ChannelHandler;
import org.openremote.agent.protocol.AbstractProtocol;
import org.openremote.agent.protocol.io.AbstractIoClientProtocol;
import org.openremote.agent.protocol.io.AbstractNettyIoClient;
import org.openremote.agent.protocol.serial.SerialIoClient;
import org.openremote.model.Container;
import org.openremote.model.protocol.ProtocolInstanceDiscovery;
import org.openremote.model.protocol.ProtocolAssetDiscovery;
import org.openremote.model.protocol.ProtocolAssetImport;
import org.openremote.model.AbstractValueHolder;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.asset.AssetTreeNode;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.*;
import org.openremote.model.file.FileInfo;
import org.openremote.model.util.Pair;
import org.openremote.model.value.ArrayValue;
import org.openremote.model.value.Value;
import org.openremote.model.value.ValueType;
import org.openremote.model.value.Values;
import org.openremote.protocol.zwave.port.ZWavePortConfiguration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.openremote.agent.protocol.zwave.ZWConfiguration.getEndpointIdAsString;
import static org.openremote.agent.protocol.zwave.ZWConfiguration.getZWEndpoint;
import static org.openremote.agent.protocol.zwave.ZWConfiguration.getZWLinkName;
import static org.openremote.agent.protocol.zwave.ZWConfiguration.getZWNodeId;
import static org.openremote.agent.protocol.zwave.ZWConfiguration.initProtocolConfiguration;
import static org.openremote.model.Constants.PROTOCOL_NAMESPACE;
import static org.openremote.model.util.TextUtil.REGEXP_PATTERN_INTEGER_POSITIVE;

public class ZWProtocol extends AbstractIoClientProtocol<byte[], SerialIoClient<byte[]>, ZWAgent> implements ProtocolAssetDiscovery,
    ProtocolInstanceDiscovery, ProtocolAssetImport {

    // Constants ------------------------------------------------------------------------------------

    public static final String PROTOCOL_DISPLAY_NAME = "Z-Wave";
    public static final String META_ZWAVE_DEVICE_NODE_ID = PROTOCOL_NAME + ":deviceNodeId";
    public static final String META_ZWAVE_DEVICE_ENDPOINT = PROTOCOL_NAME + ":deviceEndpoint";
    public static final String META_ZWAVE_DEVICE_VALUE_LINK = PROTOCOL_NAME + ":deviceValueLink";

    // Class Members --------------------------------------------------------------------------------

    public static final Logger LOG = Logger.getLogger(ZWProtocol.class.getName());


    // Protected Instance Fields --------------------------------------------------------------------

    protected ZWNetwork network;
    protected Map<AttributeRef, Consumer<org.openremote.protocol.zwave.model.commandclasses.channel.value.Value>> sensorValueConsumerMap;

    // Implements Protocol --------------------------------------------------------------------------

    @Override
    public String getProtocolName() {
        return PROTOCOL_DISPLAY_NAME;
    }

    // Implements AbstractProtocol ------------------------------------------------------------------

    @Override
    protected void doStart(Container container) throws Exception {

        super.doStart(container);
        ZWNetwork network = new ZWNetwork(client);
        network.addConnectionStatusConsumer(this::setConnectionStatus);
        network.connect();
    }

    @Override
    protected void doStop(Container container) throws Exception {
        if (network != null) {
            network.removeConnectionStatusConsumer(this::setConnectionStatus);
            network.disconnect();
        }
    }

    @Override
    protected synchronized void doLinkAttribute(String assetId, Attribute<?> attribute) {

        int nodeId = attribute.getMeta().getValue(ZWAgent.DEVICE_NODE_ID).orElse(0);
        int endpoint = attribute.getMeta().getValue(ZWAgent.DEVICE_ENDPOINT).orElse(0);
        String linkName = attribute.getMeta().getValue(ZWAgent.DEVICE_VALUE).orElse("");
        AttributeRef attributeRef = new AttributeRef(assetId, attribute.getName());

        // TODO: Value must be compatible with the value type of the attribute...for non primitives the object types must match
        Consumer<org.openremote.protocol.zwave.model.commandclasses.channel.value.Value> sensorValueConsumer = value ->
            updateLinkedAttribute(new AttributeState(attributeRef, value));

        sensorValueConsumerMap.put(attributeRef, sensorValueConsumer);
        network.addSensorValueConsumer(nodeId, endpoint, linkName, sensorValueConsumer);
    }

    @Override
    protected synchronized void doUnlinkAttribute(String assetId, Attribute<?> attribute) {
        AttributeRef attributeRef = new AttributeRef(assetId, attribute.getName());
        Consumer<org.openremote.protocol.zwave.model.commandclasses.channel.value.Value> sensorValueConsumer = sensorValueConsumerMap.remove(attributeRef);
        network.removeSensorValueConsumer(sensorValueConsumer);
    }

    @Override
    protected byte[] createWriteMessage(Attribute<?> attribute, AttributeEvent event, Object processedValue) {
        int nodeId = attribute.getMeta().getValue(ZWAgent.DEVICE_NODE_ID).orElse(0);
        int endpoint = attribute.getMeta().getValue(ZWAgent.DEVICE_ENDPOINT).orElse(0);
        String linkName = attribute.getMeta().getValue(ZWAgent.DEVICE_VALUE).orElse("");

        network.writeChannel(nodeId, endpoint, linkName, processedValue);
    }

    @Override
    protected SerialIoClient<byte[]> doCreateIoClient(ZWAgent agent) throws Exception {

        Optional<String> port = agent.getSerialPort();

        if (!port.isPresent()) {
            LOG.severe("No serial port provided for Z-Wave protocol: " + agent);
            throw new IllegalStateException("No serial port provided for Z-Wave protocol: " + agent);
        }

        return new SerialIoClient<>(port.get(), 115200, executorService);
    }

    @Override
    protected Supplier<ChannelHandler[]> getEncoderDecoderProvider(SerialIoClient<byte[]> client, ZWAgent agent) {
        return () -> new ChannelHandler[] {
            new ZWPacketEncoder(),
            new ZWPacketDecoder(),
            new AbstractNettyIoClient.MessageToMessageDecoder<>(byte[].class, client)
        };
    }

    @Override
    protected void onMessageReceived(byte[] message) {

    }

    // Implements ProtocolConfigurationDiscovery --------------------------------------------------

    @Override
    public Attribute[] discoverProtocolConfigurations() {
        return new Attribute[] {
            initProtocolConfiguration(new Attribute(), PROTOCOL_NAME)
        };
    }


    // Implements ProtocolLinkedAttributeDiscovery ------------------------------------------------

    @Override
    public synchronized AssetTreeNode[] discoverLinkedAttributes(Attribute protocolConfiguration) {
        Pair<ZWNetwork, Consumer<ConnectionStatus>> zwNetworkConsumerPair = networkConfigurationMap.get(protocolConfiguration.getReferenceOrThrow());

        if (zwNetworkConsumerPair == null) {
            return new AssetTreeNode[0];
        }

        ZWNetwork zwNetwork = zwNetworkConsumerPair.key;
        try {
            return zwNetwork.discoverDevices(protocolConfiguration);
        } catch(Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            LOG.severe(errors.toString());
            throw e;
        }
    }


    // Implements ProtocolLinkedAttributeImport ---------------------------------------------------

    @Override
    public AssetTreeNode[] discoverLinkedAttributes(Attribute protocolConfiguration, FileInfo fileInfo) throws IllegalStateException {
        // TODO : remove the ProtocolLinkedAttributeImport interface implementation. It has only been added because
        //        the manager GUI doesn't (currently) work at all without it.
        return new AssetTreeNode[0];
    }


    // Protected Instance Methods -----------------------------------------------------------------

    protected ZWControllerFactory createControllerFactory(String serialPort) {
        ZWavePortConfiguration configuration = new ZWavePortConfiguration();
        configuration.setCommLayer(ZWavePortConfiguration.CommLayer.NETTY);
        configuration.setComPort(serialPort);
        ZWControllerFactory factory = new NettyZWControllerFactory(configuration, executorService);
        return factory;
    }
}
