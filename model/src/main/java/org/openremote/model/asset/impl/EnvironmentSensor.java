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

public class EnvironmentSensor extends Device {

    public static final AttributeDescriptor<Double> TEMPERATURE = new AttributeDescriptor<>("temperature", true, ValueTypes.TEMPERATURE, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> RELATIVE_HUMIDITY = new AttributeDescriptor<>("relativeHumidity", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> NO2 = new AttributeDescriptor<>("nO2Level", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.LABEL, "NO2 level"),
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAMS_CUBIC_M),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> OZONE = new AttributeDescriptor<>("ozoneLevel", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAMS_CUBIC_M),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> PM1 = new AttributeDescriptor<>("particlesPM1", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.LABEL, "Particles PM1"),
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAMS_CUBIC_M),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> PM2_5 = new AttributeDescriptor<>("particlesPM2_5", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.LABEL, "Particles PM2.5"),
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAMS_CUBIC_M),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> PM10 = new AttributeDescriptor<>("particlesPM10", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.LABEL, "Particles PM10"),
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAMS_CUBIC_M),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );

    public static final AssetDescriptor<EnvironmentSensor> DESCRIPTOR = new AssetDescriptor<>("Environment Sensor", "periodic-table-co2", "f18546", EnvironmentSensor.class);

    public EnvironmentSensor(String name) {
        super(name, DESCRIPTOR);
    }

    public Double getTemperature() {
        return getAttributes().get(TEMPERATURE).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getRelativeHumidity() {
        return getAttributes().get(RELATIVE_HUMIDITY).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getOzone() {
        return getAttributes().get(OZONE).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getNO2() {
        return getAttributes().get(NO2).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getParticlesPM1() {
        return getAttributes().get(PM1).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getParticlesPM2_5() {
        return getAttributes().get(PM2_5).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getParticlesPM10() {
        return getAttributes().get(PM10).flatMap(Attribute::getValue).orElse(null);
    }
}
