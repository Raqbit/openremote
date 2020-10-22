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
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaTypes;
import org.openremote.model.v2.ValueTypes;

public class Parking extends Device {

    public static final AttributeDescriptor<Integer> SPACES_TOTAL = new AttributeDescriptor<>("spacesTotal", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> SPACES_OCCUPIED = new AttributeDescriptor<>("spacesOccupied", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> SPACES_OPEN = new AttributeDescriptor<>("spacesOpen", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> SPACES_BUFFER = new AttributeDescriptor<>("spacesBuffer", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Double> PRICE_HOURLY = new AttributeDescriptor<>("priceHourly", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );
    public static final AttributeDescriptor<Double> PRICE_DAILY = new AttributeDescriptor<>("priceDaily", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );

    public static final AssetDescriptor<Parking> DESCRIPTOR = new AssetDescriptor<>("Parking", "parking", "0260ae", Parking.class);

    public Parking(String name) {
        super(name, DESCRIPTOR);
    }

    public Integer getSpacesTotal() {
        return getAttributes().get(SPACES_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getSpacesOccupied() {
        return getAttributes().get(SPACES_OCCUPIED).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getSpacesOpen() {
        return getAttributes().get(SPACES_OPEN).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getSpacesBuffer() {
        return getAttributes().get(SPACES_BUFFER).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPriceHourly() {
        return getAttributes().get(PRICE_HOURLY).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPriceDaily() {
        return getAttributes().get(PRICE_DAILY).flatMap(Attribute::getValue).orElse(null);
    }
}
