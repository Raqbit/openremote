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

import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaDescriptor;

/**
 * Special type of {@link AssetDescriptor} that describes an agent {@link org.openremote.model.asset.Asset}
 */
public class AgentDescriptor<T extends Agent, S extends Protocol> extends AssetDescriptor<T> {

    protected Class<S> protocolClass;
    protected boolean instanceDiscovery;
    protected boolean instanceImport;
    protected boolean assetDiscovery;
    protected boolean assetImport;
    protected MetaDescriptor<?>[] linkedAttributeDescriptors;

    public AgentDescriptor(String name, String icon, String colour, Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors, Class<S> protocolClass, boolean instanceDiscovery, boolean instanceImport, boolean assetDiscovery, boolean assetImport, MetaDescriptor<?>...linkedAttributeDescriptors) {
        super(name, icon, colour, type, additionalAttributeDescriptors);
        this.protocolClass = protocolClass;
        this.instanceDiscovery = instanceDiscovery;
        this.instanceImport = instanceImport;
        this.assetDiscovery = assetDiscovery;
        this.assetImport = assetImport;
        this.linkedAttributeDescriptors = linkedAttributeDescriptors;
    }

    public AgentDescriptor(String name, String icon, String colour, Class<T> type, Class<S> protocolClass, boolean instanceDiscovery, boolean instanceImport, boolean assetDiscovery, boolean assetImport, MetaDescriptor<?>...linkedAttributeDescriptors) {
        this(name, icon, colour, type, null, protocolClass, instanceDiscovery, instanceImport, assetDiscovery, assetImport, linkedAttributeDescriptors);
    }

    public boolean isInstanceDiscovery() {
        return instanceDiscovery;
    }

    public boolean isInstanceImport() {
        return instanceImport;
    }

    public boolean isAssetDiscovery() {
        return assetDiscovery;
    }

    public boolean isAssetImport() {
        return assetImport;
    }

    public MetaDescriptor<?>[] getLinkedAttributeDescriptors() {
        return linkedAttributeDescriptors;
    }

    public Class<S> getProtocolClass() {
        return protocolClass;
    }
}
