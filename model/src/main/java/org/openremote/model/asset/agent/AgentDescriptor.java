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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.protocol.ProtocolAssetDiscovery;
import org.openremote.model.protocol.ProtocolAssetImport;
import org.openremote.model.protocol.ProtocolInstanceDiscovery;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemDescriptor;

/**
 * Special type of {@link AssetDescriptor} that describes an agent {@link Asset}.
 */
@JsonTypeName("agent")
public class AgentDescriptor<T extends Agent, S extends Protocol> extends AssetDescriptor<T> {

    public static class DiscoveryBooleanConverter extends StdConverter<Class<?>, Boolean> {

        @Override
        public Boolean convert(Class<?> value) {
            return value != null;
        }
    }

    protected Class<S> protocolClass;
    @JsonSerialize(converter = DiscoveryBooleanConverter.class)
    protected Class<? extends ProtocolInstanceDiscovery> instanceDiscoveryFactory;
    @JsonSerialize(converter = DiscoveryBooleanConverter.class)
    protected Class<? extends ProtocolAssetDiscovery> assetDiscoveryFactory;
    @JsonSerialize(converter = DiscoveryBooleanConverter.class)
    protected Class<? extends ProtocolAssetImport> assetImportFactory;
    protected MetaItemDescriptor<?>[] linkedAttributeDescriptors;

    public AgentDescriptor(String name, String icon, String colour, Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors, Class<S> protocolClass, Class<? extends ProtocolInstanceDiscovery> instanceDiscoveryFactory, Class<? extends ProtocolAssetDiscovery> assetDiscoveryFactory, Class<? extends ProtocolAssetImport> assetImportFactory, MetaItemDescriptor<?>...linkedAttributeDescriptors) {
        super(name, icon, colour, type, additionalAttributeDescriptors);
        this.protocolClass = protocolClass;
        this.instanceDiscoveryFactory = instanceDiscoveryFactory;
        this.assetDiscoveryFactory = assetDiscoveryFactory;
        this.assetImportFactory = assetImportFactory;
        this.linkedAttributeDescriptors = linkedAttributeDescriptors;
    }

    public AgentDescriptor(String name, String icon, String colour, Class<T> type, Class<S> protocolClass, Class<? extends ProtocolInstanceDiscovery> instanceDiscoveryFactory, Class<? extends ProtocolAssetDiscovery> assetDiscoveryFactory, Class<? extends ProtocolAssetImport> assetImportFactory, MetaItemDescriptor<?>...linkedAttributeDescriptors) {
        this(name, icon, colour, type, null, protocolClass, instanceDiscoveryFactory, assetDiscoveryFactory, assetImportFactory, linkedAttributeDescriptors);
    }

    public Class<? extends ProtocolInstanceDiscovery> getInstanceDiscoveryFactory() {
        return instanceDiscoveryFactory;
    }

    public Class<? extends ProtocolAssetDiscovery> getAssetDiscoveryFactory() {
        return assetDiscoveryFactory;
    }

    public Class<? extends ProtocolAssetImport> getAssetImportFactory() {
        return assetImportFactory;
    }

    public MetaItemDescriptor<?>[] getLinkedAttributeDescriptors() {
        return linkedAttributeDescriptors;
    }

    public Class<S> getProtocolClass() {
        return protocolClass;
    }
}
