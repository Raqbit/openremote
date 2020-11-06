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
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaType;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class Ship extends Asset {

    public static final AttributeDescriptor<Integer> MSSI_NUMBER = new AttributeDescriptor<>("mSSINumber", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> IMO_NUMBER = new AttributeDescriptor<>("iMONumber", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> DIRECTION = new AttributeDescriptor<>("direction", true, ValueType.DIRECTION, null);
    public static final AttributeDescriptor<Integer> LENGTH = new AttributeDescriptor<>("length", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_DISTANCE_METRES)
    );
    public static final AttributeDescriptor<Double> SPEED = new AttributeDescriptor<>("speed", true, ValueType.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_SPEED_KNOTS)
    );
    public static final AttributeDescriptor<String> SHIP_TYPE = new AttributeDescriptor<>("shipType", true, ValueType.STRING, null);

    public static final AssetDescriptor<Ship> DESCRIPTOR = new AssetDescriptor<>("ferry", "000080", Ship.class);

    public <T extends Ship> Ship(String name, AssetDescriptor<T> descriptor) {
        super(name, descriptor);
    }

    public Ship(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Integer> getMSSINumber() {
        return getAttributes().getValueOrDefault(MSSI_NUMBER);
    }

    public Optional<Integer> getIMONumber() {
        return getAttributes().getValueOrDefault(IMO_NUMBER);
    }

    public Optional<Integer> getDirection() {
        return getAttributes().getValueOrDefault(DIRECTION);
    }

    public Optional<Integer> getLength() {
        return getAttributes().getValueOrDefault(LENGTH);
    }

    public Optional<Double> getSpeed() {
        return getAttributes().getValueOrDefault(SPEED);
    }

    public Optional<String> getShipType() {
        return getAttributes().getValueOrDefault(SHIP_TYPE);
    }
}
