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

import org.openremote.model.Constants;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaItemType;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class PlugAsset extends DeviceAsset {

    public static final AttributeDescriptor<Boolean> ON_OFF = new AttributeDescriptor<>("onOff", false, ValueType.BOOLEAN,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_ON_OFF)
    );

    public static final AssetDescriptor<PlugAsset> DESCRIPTOR = new AssetDescriptor<>("plug", "e6688a", PlugAsset.class);

    public <T extends PlugAsset> PlugAsset(String name, AssetDescriptor<T> descriptor) {
        super(name, descriptor);
    }

    public PlugAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Boolean> getOnOff() {
        return getAttributes().getValue(ON_OFF);
    }
}
