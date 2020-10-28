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

public class ElectricitySupplier extends Device {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", true, ValueTypes.STRING, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_CAPACITY = new AttributeDescriptor<>("powerCapacity", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Double> POWER_EDR_SETPOINT = new AttributeDescriptor<>("powerEDRSetpoint", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Double> POWER_EDR_RESERVE = new AttributeDescriptor<>("powerEDRReserve", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Integer> POWER_EDR_MIN_PERIOD = new AttributeDescriptor<>("powerEDRMinPeriod", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_TIME_SECONDS)
    );
    public static final AttributeDescriptor<Double> TARIFF_IMPORT = new AttributeDescriptor<>("energyTariffImport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> TARIFF_EXPORT = new AttributeDescriptor<>("energyTariffExport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> ENERGY_TARIFF_IMPORT_FORECAST_DEVIATION = new AttributeDescriptor<>("energyTariffImportForecastDeviation", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_TARIFF_EXPORT_FORECAST_DEVIATION = new AttributeDescriptor<>("energyTariffExportForecastDeviation", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_TAX = new AttributeDescriptor<>("energyTax", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> GRID_COST = new AttributeDescriptor<>("gridCost", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );
    public static final AttributeDescriptor<Double> GRID_CONNECTION_COST = new AttributeDescriptor<>("gridConnectionCost", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );
    public static final AttributeDescriptor<Double> CARBON_IMPORT = new AttributeDescriptor<>("carbonImport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_KILOGRAM_CARBON_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> CARBON_EXPORT = new AttributeDescriptor<>("carbonExport", true, ValueTypes.POSITIVE_NUMBER, null,
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
    public static final AttributeDescriptor<Double> ENERGY_TOTAL_IMPORT = new AttributeDescriptor<>("energyTotalImport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL_EXPORT = new AttributeDescriptor<>("energyTotalExport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL_IMPORT_COST = new AttributeDescriptor<>("energyTotalImportCost", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL_EXPORT_INCOME = new AttributeDescriptor<>("energyTotalExportIncome", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> CARBON_TOTAL = new AttributeDescriptor<>("carbonTotal", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_MASS_KILOGRAM),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );

    public static final AssetDescriptor<ElectricitySupplier> DESCRIPTOR = new AssetDescriptor<>("Electricity supplier", "upload-network", "9257A9", ElectricitySupplier.class);

    public ElectricitySupplier(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStatus() {
        return getAttributes().getValueOrDefault(STATUS);
    }

    public Optional<Double> getPowerCapacity() {
        return getAttributes().getValueOrDefault(POWER_CAPACITY);
    }

    public Optional<Double> getPowerEDRSetpoint() {
        return getAttributes().getValueOrDefault(POWER_EDR_SETPOINT);
    }

    public Optional<Double> getPowerEDRReserve() {
        return getAttributes().getValueOrDefault(POWER_EDR_RESERVE);
    }

    public Optional<Integer> getPowerEDRMinPeriod() {
        return getAttributes().getValueOrDefault(POWER_EDR_MIN_PERIOD);
    }

    public Optional<Double> getTariffImport() {
        return getAttributes().getValueOrDefault(TARIFF_IMPORT);
    }

    public Optional<Double> getTariffExport() {
        return getAttributes().getValueOrDefault(TARIFF_EXPORT);
    }

    public Optional<Double> getEnergyTariffImportForecastDeviation() {
        return getAttributes().getValueOrDefault(ENERGY_TARIFF_IMPORT_FORECAST_DEVIATION);
    }

    public Optional<Double> getEnergyTariffExportForecastDeviation() {
        return getAttributes().getValueOrDefault(ENERGY_TARIFF_EXPORT_FORECAST_DEVIATION);
    }

    public Optional<Double> getEnergyTax() {
        return getAttributes().getValueOrDefault(ENERGY_TAX);
    }

    public Optional<Double> getGridCost() {
        return getAttributes().getValueOrDefault(GRID_COST);
    }

    public Optional<Double> getGridConnectionCost() {
        return getAttributes().getValueOrDefault(GRID_CONNECTION_COST);
    }

    public Optional<Double> getCarbonImport() {
        return getAttributes().getValueOrDefault(CARBON_IMPORT);
    }

    public Optional<Double> getCarbonExport() {
        return getAttributes().getValueOrDefault(CARBON_EXPORT);
    }

    public Optional<Double> getPowerTotal() {
        return getAttributes().getValueOrDefault(POWER_TOTAL);
    }

    public Optional<Double> getPowerForecastDeviation() {
        return getAttributes().getValueOrDefault(POWER_FORECAST_DEVIATION);
    }

    public Optional<Double> getEnergyTotalImport() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL_IMPORT);
    }

    public Optional<Double> getEnergyTotalExport() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL_EXPORT);
    }

    public Optional<Double> getEnergyTotalImportCost() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL_IMPORT_COST);
    }

    public Optional<Double> getEnergyTotalExportIncome() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL_EXPORT_INCOME);
    }

    public Optional<Integer> getCarbonTotal() {
        return getAttributes().getValueOrDefault(CARBON_TOTAL);
    }
}
