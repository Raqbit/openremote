/*
 * Copyright 2019, OpenRemote Inc.
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
package org.openremote.manager.asset;

import org.openremote.model.ContainerProvider;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.asset.AssetModelProvider;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentDescriptorImpl;
import org.openremote.model.asset.agent.ProtocolDescriptor;
import org.openremote.model.attribute.*;
import org.openremote.model.util.EnumUtil;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;
import java.util.logging.Logger;

import static org.openremote.model.util.TextUtil.isNullOrEmpty;

/**
 * This service gathers descriptor information from all registered {@link AssetModelProvider}s via the
 * {@link ServiceLoader} and aggregates them into this single {@link AssetModelProvider} that other backend services
 * can then consume. Other backend services can also register {@link AssetModelProvider}s by calling
 * {@link #addAssetModelProvider}.
 */
public class AssetModelService implements ContainerService, AssetModelProvider {

    /**
     * Built in model provider that extracts descriptors from {@link Asset} classes
     */
    public static class StandardModelProvider implements AssetModelProvider {

        protected ContainerProvider container;

        public StandardModelProvider(ContainerProvider container) {
            this.container = container;

            // Search all classes if this is slow then can look to require a fixed namespace prefix for assets
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(
                    new SubTypesScanner(true)
                ));
            LOG.fine("Scanning classpath for Asset classes");
            Set<Class<? extends Asset>> assetClasses = reflections.getSubTypesOf(Asset.class);

        }

        @Override
        public MetaItemDescriptor[] getMetaDescriptors() {
            return new MetaItemDescriptor[0];
        }

        @Override
        public AssetDescriptor[] getAssetDescriptors() {
            return new AssetDescriptor[0];
        }

        @Override
        public AgentDescriptor[] getAgentDescriptors() {
            return container.getServices(Protocol.class).stream().map((protocol) -> {
                ProtocolDescriptor pd = protocol.getProtocolDescriptor();

                if (pd != null) {
                    // Translate meta item descriptors into asset descriptors
                    AttributeDescriptor[] attributeDescriptors = pd.getProtocolConfigurationMetaItems() == null ? new AttributeDescriptor[0] : pd.getProtocolConfigurationMetaItems().stream()
                        .map((descriptor) -> {
                            AttributeValueDescriptor valueDescriptor = EnumUtil.enumFromString(AttributeValueType.class, descriptor.getValueType().name()).orElse(AttributeValueType.OBJECT);

                            return new AttributeDescriptorImpl(
                                descriptor.getUrn(),
                                valueDescriptor,
                                descriptor.getInitialValue()
                            );
                        }).toArray(AttributeDescriptor[]::new);

                    return new AgentDescriptorImpl(
                        pd.getDisplayName(),
                        pd.getName(),
                        "cogs",
                        null,
                        attributeDescriptors)
                        .setInstanceDiscovery(pd.isConfigurationDiscovery())
                        .setInstanceImport(pd.isConfigurationImport())
                        .setAssetDiscovery(pd.isDeviceDiscovery())
                        .setAssetImport(pd.isDeviceImport());
                }
                return null;
            }).filter(Objects::nonNull).toArray(AgentDescriptor[]::new);
        }

        @Override
        public AttributeDescriptor[] getAttributeDescriptors() {
            return new AttributeDescriptor[0];
        }

        @Override
        public AttributeValueDescriptor[] getValueDescriptors() {
            return new AttributeValueDescriptor[0];
        }
    }

    private static final Logger LOG = Logger.getLogger(AssetModelService.class.getName());
    protected final List<AssetModelProvider> assetModelProviders = new ArrayList<>();
    protected final List<AgentDescriptor> agentDescriptors = new ArrayList<>();
    protected final List<AssetDescriptor> assetDescriptors = new ArrayList<>();
    protected final List<AttributeDescriptor> attributeDescriptors = new ArrayList<>();
    protected final List<AttributeValueDescriptor> attributeValueDescriptors = new ArrayList<>();
    protected final List<MetaItemDescriptor> metaItemDescriptors = new ArrayList<>();
    protected boolean started;

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE + 10;
    }

    @Override
    public void init(ContainerProvider container) throws Exception {
        // Add the standard model provider
        addAssetModelProvider(new StandardModelProvider(container));

        // Find all service loader registered model providers
        ServiceLoader.load(AssetModelProvider.class).forEach(this::addAssetModelProvider);
    }

    @Override
    public void start(ContainerProvider container) throws Exception {
        assetModelProviders.forEach(this::addProviderDescriptors);
        started = true;
    }

    @Override
    public void stop(ContainerProvider container) throws Exception {
    }

    public void addAssetModelProvider(AssetModelProvider assetModelProvider) {
        // Model providers should be singletons so make sure no duplicate instances of the same class
        if (assetModelProviders.stream().noneMatch(asm -> asm.getClass() == assetModelProvider.getClass())) {
            this.assetModelProviders.add(assetModelProvider);
            if (started) {
                addProviderDescriptors(assetModelProvider);
            }
        }
    }

    protected void addProviderDescriptors(AssetModelProvider assetModelProvider) {
        LOG.fine("Adding asset model descriptors of provider: " + assetModelProvider.getClass().getName());

        agentDescriptors.addAll(Arrays.asList(assetModelProvider.getAgentDescriptors()));
        assetDescriptors.addAll(Arrays.asList(assetModelProvider.getAssetDescriptors()));
        attributeDescriptors.addAll(Arrays.asList(assetModelProvider.getAttributeDescriptors()));
        attributeValueDescriptors.addAll(Arrays.asList(assetModelProvider.getValueDescriptors()));
        metaItemDescriptors.addAll(Arrays.asList(assetModelProvider.getMetaDescriptors()));
    }

    @Override
    public MetaItemDescriptor[] getMetaDescriptors() {
        return metaItemDescriptors.toArray(new MetaItemDescriptor[0]);
    }

    @Override
    public AssetDescriptor[] getAssetDescriptors() {
        return assetDescriptors.toArray(new AssetDescriptor[0]);
    }

    @Override
    public AgentDescriptor[] getAgentDescriptors() {
        return agentDescriptors.toArray(new AgentDescriptor[0]);
    }

    @Override
    public AttributeDescriptor[] getAttributeDescriptors() {
        return attributeDescriptors.toArray(new AttributeDescriptor[0]);
    }

    @Override
    public AttributeValueDescriptor[] getValueDescriptors() {
        return attributeValueDescriptors.toArray(new AttributeValueDescriptor[0]);
    }

    public Optional<MetaItemDescriptor> getMetaItemDescriptor(String urn) {
        if (isNullOrEmpty(urn))
            return Optional.empty();
        for (MetaItemDescriptor metaItemDescriptor : metaItemDescriptors) {
            if (metaItemDescriptor.getUrn().equalsIgnoreCase(urn))
                return Optional.of(metaItemDescriptor);
        }
        return Optional.empty();
    }

    public Optional<AssetDescriptor> getAssetDescriptor(String assetType) {
        if (assetType == null)
            return Optional.empty();

        for (AssetDescriptor assetDescriptor : assetDescriptors) {
            if (assetType.equals(assetDescriptor.getType()))
                return Optional.of(assetDescriptor);
        }
        return Optional.empty();
    }

    public Optional<AttributeDescriptor> getAttributeDescriptor(String name) {
        if (name == null)
            return Optional.empty();

        for (AttributeDescriptor attributeDescriptor : attributeDescriptors) {
            if (name.equalsIgnoreCase(attributeDescriptor.getAttributeName()))
                return Optional.of(attributeDescriptor);
        }
        return Optional.empty();
    }

    public Optional<AttributeValueDescriptor> getAttributeValueDescriptor(String name) {
        if (name == null)
            return Optional.empty();

        for (AttributeValueDescriptor attributeValueDescriptor : attributeValueDescriptors) {
            if (name.equalsIgnoreCase(attributeValueDescriptor.getName()))
                return Optional.of(attributeValueDescriptor);
        }
        return Optional.empty();
    }

    public boolean isMetaItemRestrictedRead(MetaItem metaItem) {
        return getMetaItemDescriptor(metaItem.getName())
            .map(meta -> meta.getAccess().restrictedRead)
            .orElse(false);
    }

    public boolean isMetaItemRestrictedWrite(MetaItem metaItem) {
        return getMetaItemDescriptor(metaItem.getName())
            .map(meta -> meta.getAccess().restrictedWrite)
            .orElse(false);
    }

    public boolean isMetaItemPublicRead(MetaItem metaItem) {
        return getMetaItemDescriptor(metaItem.getName())
            .map(meta -> meta.getAccess().publicRead)
            .orElse(false);
    }
}
