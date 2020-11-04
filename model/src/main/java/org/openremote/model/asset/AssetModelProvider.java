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
package org.openremote.model.asset;

import org.openremote.model.util.AssetModelUtil;
import org.openremote.model.v2.MetaDescriptor;
import org.openremote.model.v2.ValueDescriptor;

import java.util.ServiceLoader;

/**
 * Provides model descriptors that are processed by {@link AssetModelUtil}; implementations can be discovered
 * using the standard {@link ServiceLoader} mechanism or can be manually registered by adding an instance to the
 * {@link AssetModelUtil#getModelProviders()}.
 */
public interface AssetModelProvider {

    AssetDescriptor<?>[] getAssetDescriptors();

    MetaDescriptor<?>[] getMetaDescriptors();

    ValueDescriptor<?>[] getValueDescriptors();
}
