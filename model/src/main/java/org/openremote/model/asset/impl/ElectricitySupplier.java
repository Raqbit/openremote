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

public class ElectricitySupplier extends Device {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", true, ValueTypes.STRING, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
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
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> ENERGY_TARIFF_EXPORT_FORECAST_DEVIATION = new AttributeDescriptor<>("energyTariffExportForecastDeviation", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
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

    public String getStatus() {
        return getAttributes().get(STATUS).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerCapacity() {
        return getAttributes().get(POWER_CAPACITY).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerEDRSetpoint() {
        return getAttributes().get(POWER_EDR_SETPOINT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerEDRReserve() {
        return getAttributes().get(POWER_EDR_RESERVE).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getPowerEDRMinPeriod() {
        return getAttributes().get(POWER_EDR_MIN_PERIOD).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getTariffImport() {
        return getAttributes().get(TARIFF_IMPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getTariffExport() {
        return getAttributes().get(TARIFF_EXPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTariffImportForecastDeviation() {
        return getAttributes().get(ENERGY_TARIFF_IMPORT_FORECAST_DEVIATION).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTariffExportForecastDeviation() {
        return getAttributes().get(ENERGY_TARIFF_EXPORT_FORECAST_DEVIATION).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTax() {
        return getAttributes().get(ENERGY_TAX).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getGridCost() {
        return getAttributes().get(GRID_COST).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getGridConnectionCost() {
        return getAttributes().get(GRID_CONNECTION_COST).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getCarbonImport() {
        return getAttributes().get(CARBON_IMPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getCarbonExport() {
        return getAttributes().get(CARBON_EXPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerTotal() {
        return getAttributes().get(POWER_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerForecastDeviation() {
        return getAttributes().get(POWER_FORECAST_DEVIATION).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTotalImport() {
        return getAttributes().get(ENERGY_TOTAL_IMPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTotalExport() {
        return getAttributes().get(ENERGY_TOTAL_EXPORT).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTotalImportCost() {
        return getAttributes().get(ENERGY_TOTAL_IMPORT_COST).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTotalExportIncome() {
        return getAttributes().get(ENERGY_TOTAL_EXPORT_INCOME).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getCarbonTotal() {
        return getAttributes().get(CARBON_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }
}
