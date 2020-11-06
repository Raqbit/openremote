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
import org.openremote.model.v2.MetaType;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class ElectricityConsumer extends Device {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", true, ValueType.STRING, null,
        new MetaItem<>(MetaType.READ_ONLY)
    );
    public static final AttributeDescriptor<String> DEMAND_RESPONSE_TYPE = new AttributeDescriptor<>("demandResponseType", true, ValueType.STRING, null);
    public static final AttributeDescriptor<Double> TARIFF_IMPORT = new AttributeDescriptor<>("tariffImport", true, ValueType.NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> TARIFF_EXPORT = new AttributeDescriptor<>("tariffExport", true, ValueType.NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Integer> CARBON_IMPORT = new AttributeDescriptor<>("carbonImport", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_KILOGRAM_CARBON_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Integer> CARBON_EXPORT = new AttributeDescriptor<>("carbonExport", true, ValueType.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_KILOGRAM_CARBON_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> POWER_TOTAL = new AttributeDescriptor<>("powerTotal", true, ValueType.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_FORECAST_DEVIATION = new AttributeDescriptor<>("powerForecastDeviation", true, ValueType.NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_SETPOINT = new AttributeDescriptor<>("powerSetpoint", true, ValueType.NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_AVAILABLE_MAX = new AttributeDescriptor<>("powerAvailableMax", true, ValueType.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_AVAILABLE_MIN = new AttributeDescriptor<>("powerAvailableMin", true, ValueType.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL = new AttributeDescriptor<>("energyTotal", true, ValueType.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaType.READ_ONLY)
    );

    public static final AssetDescriptor<ElectricityConsumer> DESCRIPTOR = new AssetDescriptor<>("power-plug", "8A293D", ElectricityConsumer.class);

    public ElectricityConsumer(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStatus() {
        return getAttributes().getValueOrDefault(STATUS);
    }

    public Optional<String> getDemandResponseType() {
        return getAttributes().getValueOrDefault(DEMAND_RESPONSE_TYPE);
    }

    public Optional<Double> getTariffImport() {
        return getAttributes().getValueOrDefault(TARIFF_IMPORT);
    }

    public Optional<Double> getTariffExport() {
        return getAttributes().getValueOrDefault(TARIFF_EXPORT);
    }

    public Optional<Integer> getCarbonImport() {
        return getAttributes().getValueOrDefault(CARBON_IMPORT);
    }

    public Optional<Integer> getCarbonExport() {
        return getAttributes().getValueOrDefault(CARBON_EXPORT);
    }

    public Optional<Double> getPowerTotal() {
        return getAttributes().getValueOrDefault(POWER_TOTAL);
    }

    public Optional<Double> getPowerForecastDeviation() {
        return getAttributes().getValueOrDefault(POWER_FORECAST_DEVIATION);
    }

    public Optional<Double> getPowerSetpoint() {
        return getAttributes().getValueOrDefault(POWER_SETPOINT);
    }

    public Optional<Double> getPowerAvailableMax() {
        return getAttributes().getValueOrDefault(POWER_AVAILABLE_MAX);
    }

    public Optional<Double> getPowerAvailableMin() {
        return getAttributes().getValueOrDefault(POWER_AVAILABLE_MIN);
    }

    public Optional<Double> getEnergyTotal() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL);
    }
}
