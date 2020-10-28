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

public class ElectricityStorage extends Device {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", true, ValueTypes.STRING, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> LEVELISED_COST_OF_STORAGE = new AttributeDescriptor<>("levelisedCostOfStorage", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );
    public static final AttributeDescriptor<Double> ENERGY_CAPACITY = new AttributeDescriptor<>("energyCapacity", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> ENERGY_AVAILABLE_PERCENTAGE = new AttributeDescriptor<>("energyAvailablePercentage", true, ValueTypes.PERCENTAGE_INTEGER_0_100, null);
    public static final AttributeDescriptor<Double> ENERGY_AVAILABLE = new AttributeDescriptor<>("energyAvailable", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_CAPACITY_REMAINING = new AttributeDescriptor<>("energyCapacityRemaining", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL_IMPORT = new AttributeDescriptor<>("energyTotalImport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL_EXPORT = new AttributeDescriptor<>("energyTotalExport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_CAPACITY_IMPORT = new AttributeDescriptor<>("powerCapacityImport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Double> POWER_CAPACITY_EXPORT = new AttributeDescriptor<>("powerCapacityExport", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Double> POWER_TOTAL = new AttributeDescriptor<>("powerTotal", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_SETPOINT = new AttributeDescriptor<>("powerSetpoint", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Integer> CHARGE_CYCLES = new AttributeDescriptor<>("chargeCycles", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> FINANCIAL_WALLET = new AttributeDescriptor<>("financialWallet", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );
    public static final AttributeDescriptor<Integer> CARBON_WALLET = new AttributeDescriptor<>("carbonWallet", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_MASS_KILOGRAM),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );

    public static final AssetDescriptor<ElectricityStorage> DESCRIPTOR = new AssetDescriptor<>("Electricity storage", "battery-charging", "1B7C89", ElectricityStorage.class);

    public ElectricityStorage(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStatus() {
        return getAttributes().getValueOrDefault(STATUS);
    }

    public Optional<Double> getLevelisedCostOfStorage() {
        return getAttributes().getValueOrDefault(LEVELISED_COST_OF_STORAGE);
    }

    public Optional<Double> getEnergyCapacity() {
        return getAttributes().getValueOrDefault(ENERGY_CAPACITY);
    }

    public Optional<Integer> getEnergyAvailablePercentage() {
        return getAttributes().getValueOrDefault(ENERGY_AVAILABLE_PERCENTAGE);
    }

    public Optional<Double> getEnergyAvailable() {
        return getAttributes().getValueOrDefault(ENERGY_AVAILABLE);
    }

    public Optional<Double> getEnergyCapacityRemaining() {
        return getAttributes().getValueOrDefault(ENERGY_CAPACITY_REMAINING);
    }

    public Optional<Double> getEnergyTotalImport() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL_IMPORT);
    }

    public Optional<Double> getEnergyTotalExport() {
        return getAttributes().getValueOrDefault(ENERGY_TOTAL_EXPORT);
    }

    public Optional<Double> getPowerCapacityImport() {
        return getAttributes().getValueOrDefault(POWER_CAPACITY_IMPORT);
    }

    public Optional<Double> getPowerCapacityExport() {
        return getAttributes().getValueOrDefault(POWER_CAPACITY_EXPORT);
    }

    public Optional<Double> getPowerTotal() {
        return getAttributes().getValueOrDefault(POWER_TOTAL);
    }

    public Optional<Double> getPowerSetpoint() {
        return getAttributes().getValueOrDefault(POWER_SETPOINT);
    }

    public Optional<Integer> getChargeCycles() {
        return getAttributes().getValueOrDefault(CHARGE_CYCLES);
    }

    public Optional<Double> getFinancialWallet() {
        return getAttributes().getValueOrDefault(FINANCIAL_WALLET);
    }

    public Optional<Integer> getCarbonWallet() {
        return getAttributes().getValueOrDefault(CARBON_WALLET);
    }
}
