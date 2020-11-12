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

public class EnvironmentSensor extends Device {

    public static final AttributeDescriptor<Double> TEMPERATURE = new AttributeDescriptor<>("temperature", true, ValueType.NUMBER, null,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_TEMPERATURE_CELSIUS),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> RELATIVE_HUMIDITY = new AttributeDescriptor<>("relativeHumidity", true, ValueType.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> NO2 = new AttributeDescriptor<>("nO2Level", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaItemType.LABEL, "NO2 level"),
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAM_CUBIC_M),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> OZONE = new AttributeDescriptor<>("ozoneLevel", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAM_CUBIC_M),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> PM1 = new AttributeDescriptor<>("particlesPM1", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaItemType.LABEL, "Particles PM1"),
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAM_CUBIC_M),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> PM2_5 = new AttributeDescriptor<>("particlesPM2_5", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaItemType.LABEL, "Particles PM2.5"),
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAM_CUBIC_M),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> PM10 = new AttributeDescriptor<>("particlesPM10", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaItemType.LABEL, "Particles PM10"),
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_DENSITY_MICROGRAM_CUBIC_M),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );

    public static final AssetDescriptor<EnvironmentSensor> DESCRIPTOR = new AssetDescriptor<>("periodic-table-co2", "f18546", EnvironmentSensor.class);

    public EnvironmentSensor(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Double> getTemperature() {
        return getAttributes().getValue(TEMPERATURE);
    }

    public Optional<Double> getRelativeHumidity() {
        return getAttributes().getValue(RELATIVE_HUMIDITY);
    }

    public Optional<Integer> getOzone() {
        return getAttributes().getValue(OZONE);
    }

    public Optional<Integer> getNO2() {
        return getAttributes().getValue(NO2);
    }

    public Optional<Integer> getParticlesPM1() {
        return getAttributes().getValue(PM1);
    }

    public Optional<Integer> getParticlesPM2_5() {
        return getAttributes().getValue(PM2_5);
    }

    public Optional<Integer> getParticlesPM10() {
        return getAttributes().getValue(PM10);
    }
}
