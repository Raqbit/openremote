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
import org.openremote.model.v2.MetaTypes;
import org.openremote.model.v2.ValueTypes;

import java.util.Optional;

public class ElectricityProducer extends Device {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", true, ValueTypes.STRING, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_CAPACITY = new AttributeDescriptor<>("powerCapacity", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Integer> EFFICIENCY = new AttributeDescriptor<>("efficiency", true, ValueTypes.PERCENTAGE_INTEGER_0_100, null);
    public static final AttributeDescriptor<Double> POWER_TOTAL = new AttributeDescriptor<>("powerTotal", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_FORECAST_DEVIATION = new AttributeDescriptor<>("powerForecastDeviation", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL = new AttributeDescriptor<>("energyTotal", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> PANEL_ORIENTATION = new AttributeDescriptor<>("panelOrientation", true, ValueTypes.DIRECTION, null);

    public static final AssetDescriptor<ElectricityProducer> DESCRIPTOR = new AssetDescriptor<>("Electricity producer", "ev-station", "8A293D", ElectricityProducer.class);

    public ElectricityProducer(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStatus() {
        return getAttributes().getValueOrDefault(STATUS);
    }

    public Optional<Double> getPowerTotal() {
        return getAttributes().getValueOrDefault(POWER_TOTAL);
    }

    public Optional<Double> getPowerForecastDeviation() {
        return getAttributes().getValueOrDefault(POWER_FORECAST_DEVIATION);
    }

    public Optional<Double> getPowerCapacity() {
        return getAttributes().getValueOrDefault(POWER_CAPACITY);
    }

    public Optional<Integer> getEfficiency() {
        return getAttributes().getValueOrDefault(EFFICIENCY);
    }

    public Optional<Double> getEnergyTotal() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL);
    }

    public Optional<Integer> getPanelOrientation() {
        return getAttributes().getValueOrDefault(PANEL_ORIENTATION);
    }
}
