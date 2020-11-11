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
public class AgentDescriptor<T extends Agent, S extends Protocol<T>> extends AssetDescriptor<T> {

    public static class DiscoveryBooleanConverter extends StdConverter<Class<?>, Boolean> {

        @Override
        public Boolean convert(Class<?> value) {
            return value != null;
        }
    }

    protected Class<S> protocolClass;
    @JsonSerialize(converter = DiscoveryBooleanConverter.class)
    protected Class<? extends ProtocolInstanceDiscovery> instanceDiscovery;
    protected boolean assetDiscovery;
    protected boolean assetImport;
    protected MetaItemDescriptor<?>[] linkedAttributeDescriptors;

    public AgentDescriptor(String icon, String colour, Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors, Class<S> protocolClass, Class<? extends ProtocolInstanceDiscovery> instanceDiscovery, boolean assetDiscovery, boolean assetImport, MetaItemDescriptor<?>...linkedAttributeDescriptors) {
        super(icon, colour, type, additionalAttributeDescriptors);
        this.protocolClass = protocolClass;
        this.instanceDiscovery = instanceDiscovery;
        this.assetDiscovery = assetDiscovery;
        this.assetImport = assetImport;
        this.linkedAttributeDescriptors = linkedAttributeDescriptors;
    }

    public AgentDescriptor(String icon, String colour, Class<T> type, Class<S> protocolClass, Class<? extends ProtocolInstanceDiscovery> instanceDiscovery, boolean assetDiscovery, boolean assetImport, MetaItemDescriptor<?>...linkedAttributeDescriptors) {
        this(icon, colour, type, null, protocolClass, instanceDiscovery, assetDiscovery, assetImport, linkedAttributeDescriptors);
    }

    public Class<? extends ProtocolInstanceDiscovery> getInstanceDiscovery() {
        return instanceDiscovery;
    }

    public boolean isInstanceDiscovery() {
        return instanceDiscovery != null;
    }

    public boolean isAssetDiscovery() {
        return assetDiscovery;
    }

    public boolean isAssetImport() {
        return assetImport;
    }

    public MetaItemDescriptor<?>[] getLinkedAttributeDescriptors() {
        return linkedAttributeDescriptors;
    }

    public Class<S> getProtocolClass() {
        return protocolClass;
    }
}
