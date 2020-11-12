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
package org.openremote.agent.protocol.velbus;

import org.openremote.agent.protocol.AbstractProtocol;
import org.openremote.agent.protocol.io.IoClient;
import org.openremote.agent.protocol.velbus.device.VelbusDeviceType;
import org.openremote.model.Container;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetTreeNode;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.*;
import org.openremote.model.protocol.ProtocolAssetDiscovery;
import org.openremote.model.protocol.ProtocolAssetImport;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.util.EnumUtil;
import org.openremote.model.v2.MetaItemType;
import org.openremote.model.v2.ValueType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openremote.agent.protocol.velbus.VelbusConfiguration.getVelbusDeviceAddress;
import static org.openremote.agent.protocol.velbus.VelbusConfiguration.getVelbusDevicePropertyLink;
import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;
import static org.openremote.model.util.TextUtil.isNullOrEmpty;

abstract class AbstractVelbusProtocol<T extends VelbusAgent> extends AbstractProtocol<T> implements
    ProtocolAssetDiscovery,
    ProtocolAssetImport {

    public static final int DEFAULT_TIME_INJECTION_INTERVAL_SECONDS = 3600 * 6;
    public static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, AbstractVelbusProtocol.class);
    protected VelbusNetwork network;
    protected Future<Void> assetImportTask;
    protected final Map<AttributeRef, Consumer<Object>> attributePropertyValueConsumers = new HashMap<>();

    protected AbstractVelbusProtocol(T agent) {
        super(agent);
    }

    @Override
    protected void doStart(Container container) throws Exception {

        try {

            IoClient<VelbusPacket> messageProcessor = createIoClient(agent);
            int timeInjectionSeconds = agent.getTimeInjectionInterval();

            LOG.fine("Creating new VELBUS network instance for protocol instance: " + agent);
            network = new VelbusNetwork(messageProcessor, executorService, timeInjectionSeconds);
            network.connect();
            network.addConnectionStatusConsumer(this::setConnectionStatus);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create IO client for protocol instance: " + agent, e);
            setConnectionStatus(ConnectionStatus.ERROR);
            throw e;
        }
    }

    @Override
    protected void doStop(Container container) throws Exception {
        if (network != null) {
            network.disconnect();
            network.close();
            network.removeConnectionStatusConsumer(this::setConnectionStatus);
        }
    }

    @Override
    protected void doLinkAttribute(String assetId, Attribute<?> attribute) {

        // Get the device that this attribute is linked to
        int deviceAddress = getVelbusDeviceAddress(attribute);

        // Get the property that this attribute is linked to
        String property = getVelbusDevicePropertyLink(attribute);
        AttributeRef attributeRef = new AttributeRef(assetId, attribute.getName());
        LOG.fine("Linking attribute to device '" + deviceAddress + "' and property '" + property + "': " + attributeRef);

        Consumer<Object> propertyValueConsumer = propertyValue ->
            updateLinkedAttribute(new AttributeState(attributeRef, propertyValue));

        attributePropertyValueConsumers.put(attributeRef, propertyValueConsumer);
        network.addPropertyValueConsumer(deviceAddress, property, propertyValueConsumer);
    }

    @Override
    protected void doUnlinkAttribute(String assetId, Attribute<?> attribute) {
        // Get the device that this attribute is linked to
        int deviceAddress = getVelbusDeviceAddress(attribute);

        // Get the property that this attribute is linked to
        String property = getVelbusDevicePropertyLink(attribute);

        AttributeRef attributeRef = new AttributeRef(assetId, attribute.getName());
        Consumer<Object> propertyValueConsumer = attributePropertyValueConsumers.remove(attributeRef);
        network.removePropertyValueConsumer(deviceAddress, property, propertyValueConsumer);
    }

    @Override
    protected void doLinkedAttributeWrite(Attribute<?> attribute, AttributeEvent event, Object processedValue) {

        // Get the device that this attribute is linked to
        int deviceAddress = getVelbusDeviceAddress(attribute);

        // Get the property that this attribute is linked to
        String property = getVelbusDevicePropertyLink(attribute);

        network.writeProperty(deviceAddress, property, event.getValue().orElse(null));
    }

    @Override
    public boolean startAssetImport(byte[] fileData, Consumer<AssetTreeNode[]> assetConsumer, Runnable stoppedCallback) {
        if (assetImportTask != null) {
            LOG.info("Asset import already running");
            return false;
        }

        assetImportTask = executorService.submit(() -> {
            Document xmlDoc;
            try {
                String xmlStr = new String(fileData);
                LOG.info("Parsing VELBUS project file");

                xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlStr)));
            } catch (Exception e) {
                stoppedCallback.run();

                LOG.log(Level.WARNING, "Failed to convert VELBUS project file into XML", e);
                return;
            }

            xmlDoc.getDocumentElement().normalize();

            NodeList modules = xmlDoc.getElementsByTagName("Module");
            LOG.info("Found " + modules.getLength() + " module(s)");

            List<Asset> devices = new ArrayList<>(modules.getLength());
            MetaItem<String> agentLink = new MetaItem<>(MetaItemType.AGENT_LINK, agent.getId());

            for (int i = 0; i < modules.getLength(); i++) {
                Element module = (Element) modules.item(i);

                // TODO: Process memory map and add
                Optional<VelbusDeviceType> deviceType = EnumUtil.enumFromString(VelbusDeviceType.class, module.getAttribute("type").replaceAll("-", ""));

                if (!deviceType.isPresent()) {
                    LOG.info("Module device type '" + module.getAttribute("type") + "' is not supported so ignoring");
                    continue;
                }

                String[] addresses = module.getAttribute("address").split(",");
                int baseAddress = Integer.parseInt(addresses[0], 16);
                String build = module.getAttribute("build");
                String serial = module.getAttribute("serial");
                String name = module.getElementsByTagName("Caption").item(0).getTextContent();
                name = isNullOrEmpty(name) ? deviceType.toString() : name;

                // TODO: Use device specific asset types
                Asset device = new Asset(name);

                device.setAttributes(
                    new Attribute<>("build", ValueType.STRING, build)
                        .addMeta(
                            new MetaItem<>(MetaItemType.LABEL, "Build"),
                            new MetaItem<>(MetaItemType.READ_ONLY, true)
                        ),
                    new Attribute<>("serialNumber", ValueType.STRING, serial)
                        .addMeta(
                            new MetaItem<>(MetaItemType.LABEL, "Serial No"),
                            new MetaItem<>(MetaItemType.READ_ONLY, true)
                        )
                );

                device.getAttributes().addAll(
                    deviceType.flatMap(type -> Optional.ofNullable(type.getFeatureProcessors())
                        .map(processors ->
                            Arrays.stream(processors).flatMap(processor ->
                                processor.getPropertyDescriptors(type).stream().map(descriptor -> {

                                    Attribute<?> attribute = new Attribute<>(descriptor.getName(), descriptor.getAttributeValueDescriptor())
                                        .addMeta(
                                            agentLink,
                                            new MetaItem<>(MetaItemType.LABEL, descriptor.getDisplayName())
                                        )
                                        .addMeta(
                                            createLinkedAttributeMetaItems(
                                                baseAddress,
                                                descriptor.getLinkName()
                                            )
                                        );

                                    if (descriptor.isReadOnly()) {
                                        attribute.addMeta(new MetaItem<>(MetaItemType.READ_ONLY, true));
                                    }
                                    return attribute;
                                })
                            ).toArray(Attribute[]::new)
                        ))
                        .orElse(new Attribute[0])
                );

                devices.add(device);
            }

            assetConsumer.accept(devices.stream().map(AssetTreeNode::new).toArray(AssetTreeNode[]::new));
        }, null);

        return true;
    }

    @Override
    public void stopAssetImport() {
        if (assetImportTask != null) {
            assetImportTask.cancel(true);
        }
        assetImportTask = null;
    }

    /**
     * Should return an instance of {@link IoClient} for the supplied protocolConfiguration
     */
    protected abstract IoClient<VelbusPacket> createIoClient(T agent) throws RuntimeException;

    public static MetaItem<?>[] createLinkedAttributeMetaItems(int address, String deviceLink) {
        return new MetaItem[] {
            new MetaItem<>(VelbusAgent.META_DEVICE_ADDRESS, address),
            new MetaItem<>(VelbusAgent.META_DEVICE_VALUE_LINK, deviceLink)
        };
    }
}
