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
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaTypes;
import org.openremote.model.v2.ValueTypes;

public class Ship extends Asset {

    public static final AttributeDescriptor<Integer> MSSI_NUMBER = new AttributeDescriptor<>("mSSINumber", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> IMO_NUMBER = new AttributeDescriptor<>("iMONumber", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> DIRECTION = new AttributeDescriptor<>("direction", true, ValueTypes.DIRECTION, null);
    public static final AttributeDescriptor<Integer> LENGTH = new AttributeDescriptor<>("length", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_DISTANCE_METRES)
    );
    public static final AttributeDescriptor<Double> SPEED = new AttributeDescriptor<>("speed", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_SPEED_KNOTS)
    );
    public static final AttributeDescriptor<String> SHIP_TYPE = new AttributeDescriptor<>("shipType", true, ValueTypes.STRING, null);

    public static final AssetDescriptor<Ship> DESCRIPTOR = new AssetDescriptor<>("Ship", "ferry", "000080", Ship.class);

    public <T extends Ship> Ship(String name, AssetDescriptor<T> descriptor) {
        super(name, descriptor);
    }

    public Ship(String name) {
        super(name, DESCRIPTOR);
    }

    public Integer getMSSINumber() {
        return getAttributes().get(MSSI_NUMBER).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getIMONumber() {
        return getAttributes().get(IMO_NUMBER).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getDirection() {
        return getAttributes().get(DIRECTION).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getLength() {
        return getAttributes().get(LENGTH).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getSpeed() {
        return getAttributes().get(SPEED).flatMap(Attribute::getValue).orElse(null);
    }

    public String getShipType() {
        return getAttributes().get(SHIP_TYPE).flatMap(Attribute::getValue).orElse(null);
    }
}
