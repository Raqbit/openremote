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
package org.openremote.model.util;

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.asset.AssetModelProvider;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.v2.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;

/**
 * Utility class for retrieving asset model descriptors
 */
public class AssetModelUtil {

    protected static boolean initialised;
    protected static final List<AssetModelProvider> assetModelProviders = new ArrayList<>(Collections.singletonList(new StandardModelProvider()));

    static {
        // Find all service loader registered model providers
        ServiceLoader.load(AssetModelProvider.class).forEach(assetModelProviders::add);
    }

    /**
     * Built in model provider that dynamically extracts descriptors at runtime as follows:
     * <h2>{@link AssetDescriptor}s/{@link AgentDescriptor}s</h2>
     * The {@link Asset} class and classes that extend it are searched for public static fields of type
     * {@link AssetDescriptor} ({@link AgentDescriptor} for {@link Agent}s) with the {@link ModelDescriptor} annotation.
     * <p>
     * For a given class only one annotated {@link AssetDescriptor}/{@link AgentDescriptor} should be present otherwise
     * an {@link IllegalStateException} will be thrown. An {@link Agent} class must declare an {@link AgentDescriptor}
     * rather than an {@link AssetDescriptor} otherwise an {@link IllegalStateException} will be thrown. 
     * <p>
     * Each {@link AssetDescriptor} will discover its' own {@link AttributeDescriptor}s also using reflection (see
     * {@link AssetDescriptor#getAttributeDescriptors}) by looking for static public fields of type
     * {@link AttributeDescriptor} with the {@link ModelDescriptor} on its' own class and on all super classes up to
     * {@link Asset}, this way asset types inherit {@link AttributeDescriptor}s, and an inherited
     * {@link AttributeDescriptor} cannot be overridden, any attempt to override will result in an
     * {@link IllegalStateException}
     * <h2>{@link MetaDescriptor}s from {@link MetaType}</h2>
     * <h2>{@link ValueDescriptor}s from {@link ValueType}</h2>
     *
     */
    public static class StandardModelProvider implements AssetModelProvider {

        public StandardModelProvider() {
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

    protected AssetModelUtil() {}

    public static AssetDescriptor<?>[] getAssetDescriptors(Class<? extends Asset> parentAssetType) {
        if (!initialised) {
            initialise();
        }
    }

    public static AttributeDescriptor<?>[] getAttributeDescriptors(Class<? extends Asset> assetType) {
        if (!initialised) {
            initialise();
        }
    }

    public static MetaDescriptor<?>[] getMetaDescriptors(Class<? extends Asset> assetType) {
        if (!initialised) {
            initialise();
        }
    }

    public static ValueDescriptor<?>[] getValueDescriptors(Class<? extends Asset> assetType) {
        if (!initialised) {
            initialise();
        }
    }

    public static void refresh() {
        initialised = false;
    }

    public static List<AssetModelProvider> getModelProviders() {
        return assetModelProviders;
    }

    protected static void initialise() {

    }
}
