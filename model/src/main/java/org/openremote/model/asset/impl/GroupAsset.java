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
package org.openremote.model.asset.impl;

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

/**
 * An asset for grouping other assets together; if the {@link #CHILD_ASSET_TYPE} {@link Attribute} is specified then
 * all children must be of the specified type.
 */
public class GroupAsset extends Asset {

    public static final AttributeDescriptor<String> CHILD_ASSET_TYPE = new AttributeDescriptor<>("childAssetType", true, ValueType.STRING);
public static final String t = GroupAsset.class.getSimpleName();
    public static final AssetDescriptor<GroupAsset> DESCRIPTOR = new AssetDescriptor<>("folder", "B3B3B3", GroupAsset.class);

    public GroupAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getChildAssetType() {
        return getAttributes().getValue(CHILD_ASSET_TYPE);
    }
}
