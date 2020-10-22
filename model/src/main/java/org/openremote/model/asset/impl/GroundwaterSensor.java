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

import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaTypes;
import org.openremote.model.v2.ValueTypes;

public class GroundwaterSensor extends Device {

    public static final AttributeDescriptor<Double> TEMPERATURE = new AttributeDescriptor<>("temperature", true, ValueTypes.TEMPERATURE, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> WATER_LEVEL = new AttributeDescriptor<>("waterLevel", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );

    public static final AssetDescriptor<GroundwaterSensor> DESCRIPTOR = new AssetDescriptor<>("Groundwater Sensor", "water-outline", "95d0df", GroundwaterSensor.class);

    public GroundwaterSensor(String name) {
        super(name, DESCRIPTOR);
    }

    public Double getTemperature() {
        return getAttributes().get(TEMPERATURE).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getWaterLevel() {
        return getAttributes().get(WATER_LEVEL).flatMap(Attribute::getValue).orElse(null);
    }
}
