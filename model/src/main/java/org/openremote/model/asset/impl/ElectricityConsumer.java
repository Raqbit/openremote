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

public class ElectricityConsumer extends Device {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", true, ValueTypes.STRING, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<String> DEMAND_RESPONSE_TYPE = new AttributeDescriptor<>("demandResponseType", true, ValueTypes.STRING, null);
    public static final AttributeDescriptor<Double> TARIFF_IMPORT = new AttributeDescriptor<>("tariffImport", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> TARIFF_EXPORT = new AttributeDescriptor<>("tariffExport", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Integer> CARBON_IMPORT = new AttributeDescriptor<>("carbonImport", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_KILOGRAM_CARBON_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Integer> CARBON_EXPORT = new AttributeDescriptor<>("carbonExport", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_KILOGRAM_CARBON_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> POWER_TOTAL = new AttributeDescriptor<>("powerTotal", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> POWER_FORECAST_DEVIATION = new AttributeDescriptor<>("powerForecastDeviation", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> POWER_SETPOINT = new AttributeDescriptor<>("powerSetpoint", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> POWER_AVAILABLE_MAX = new AttributeDescriptor<>("powerAvailableMax", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> POWER_AVAILABLE_MIN = new AttributeDescriptor<>("powerAvailableMin", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL = new AttributeDescriptor<>("energyTotal", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );

    public static final AssetDescriptor<ElectricityConsumer> DESCRIPTOR = new AssetDescriptor<>("Electricity consumer", "power-plug", "8A293D", ElectricityConsumer.class);

    public ElectricityConsumer(String name) {
        super(name, DESCRIPTOR);
    }

    public String getStatus() {
        return getAttributes().get(STATUS).flatMap(Attribute::getValue).orElse(null);
    }

    public String getDemandResponseType() {
        return getAttributes().get(DEMAND_RESPONSE_TYPE).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getTariffImport() {
        return getAttributes().get(TARIFF_IMPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getTariffExport() {
        return getAttributes().get(TARIFF_EXPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getCarbonImport() {
        return getAttributes().get(CARBON_IMPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getCarbonExport() {
        return getAttributes().get(CARBON_EXPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerTotal() {
        return getAttributes().get(POWER_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerForecastDeviation() {
        return getAttributes().get(POWER_FORECAST_DEVIATION).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerSetpoint() {
        return getAttributes().get(POWER_SETPOINT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerAvailableMax() {
        return getAttributes().get(POWER_AVAILABLE_MAX).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerAvailableMin() {
        return getAttributes().get(POWER_AVAILABLE_MIN).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTotal() {
        return getAttributes().get(ENERGY_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }
}
