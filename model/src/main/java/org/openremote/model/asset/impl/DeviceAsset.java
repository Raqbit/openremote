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
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class DeviceAsset extends Asset {

    public static final AttributeDescriptor<String> MANUFACTURER = new AttributeDescriptor<>("manufacturer", true, ValueType.STRING);
    public static final AttributeDescriptor<String> MODEL = new AttributeDescriptor<>("model", true, ValueType.STRING);

    public static final AssetDescriptor<DeviceAsset> DESCRIPTOR = new AssetDescriptor<>("cube-outline", null, DeviceAsset.class);

    public <T extends DeviceAsset> DeviceAsset(String name, AssetDescriptor<T> descriptor) {
        super(name, descriptor);
    }

    public DeviceAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getManufacturer() {
        return getAttributes().getValue(MANUFACTURER);
    }

    public Optional<String> getModel() {
        return getAttributes().getValue(MODEL);
    }
}
