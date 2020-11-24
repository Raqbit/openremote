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
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.MetaItemType;
import org.openremote.model.value.ValueType;

import java.util.Optional;

public class ParkingAsset extends DeviceAsset {

    public static final AttributeDescriptor<Integer> SPACES_TOTAL = new AttributeDescriptor<>("spacesTotal", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> SPACES_OCCUPIED = new AttributeDescriptor<>("spacesOccupied", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> SPACES_OPEN = new AttributeDescriptor<>("spacesOpen", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> SPACES_BUFFER = new AttributeDescriptor<>("spacesBuffer", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Double> PRICE_HOURLY = new AttributeDescriptor<>("priceHourly", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );
    public static final AttributeDescriptor<Double> PRICE_DAILY = new AttributeDescriptor<>("priceDaily", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );

    public static final AssetDescriptor<ParkingAsset> DESCRIPTOR = new AssetDescriptor<>("parking", "0260ae", ParkingAsset.class);

    public ParkingAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Integer> getSpacesTotal() {
        return getAttributes().getValue(SPACES_TOTAL);
    }

    public Optional<Integer> getSpacesOccupied() {
        return getAttributes().getValue(SPACES_OCCUPIED);
    }

    public Optional<Integer> getSpacesOpen() {
        return getAttributes().getValue(SPACES_OPEN);
    }

    public Optional<Integer> getSpacesBuffer() {
        return getAttributes().getValue(SPACES_BUFFER);
    }

    public Optional<Double> getPriceHourly() {
        return getAttributes().getValue(PRICE_HOURLY);
    }

    public Optional<Double> getPriceDaily() {
        return getAttributes().getValue(PRICE_DAILY);
    }
}
