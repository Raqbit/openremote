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

public class ElectricityCharger extends Device {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", true, ValueTypes.STRING, null,
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<String> CHARGER_TYPE = new AttributeDescriptor<>("chargerType", true, ValueTypes.STRING, null);
    public static final AttributeDescriptor<Double> POWER_CAPACITY = new AttributeDescriptor<>("powerCapacity", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Double> POWER_CONSUMPTION = new AttributeDescriptor<>("powerConsumption", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> TARIFF_IMPORT = new AttributeDescriptor<>("tariffImport", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> TARIFF_EXPORT = new AttributeDescriptor<>("tariffExport", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> TARIFF_START = new AttributeDescriptor<>("tariffStart", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );

    public static final AssetDescriptor<ElectricityCharger> DESCRIPTOR = new AssetDescriptor<>("Electricity charger", "ev-station", "8A293D", ElectricityCharger.class);

    public ElectricityCharger(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStatus() {
        return getAttributes().getValueOrDefault(STATUS);
    }

    public Optional<String> getChargerType() {
        return getAttributes().getValueOrDefault(CHARGER_TYPE);
    }

    public Optional<Double> getPowerCapacity() {
        return getAttributes().getValueOrDefault(POWER_CAPACITY);
    }

    public Optional<Double> getPowerConsumption() {
        return getAttributes().getValueOrDefault(POWER_CONSUMPTION);
    }

    public Optional<Double> getTariffImport() {
        return getAttributes().getValueOrDefault(TARIFF_IMPORT);
    }

    public Optional<Double> getTariffExport() {
        return getAttributes().getValueOrDefault(TARIFF_EXPORT);
    }

    public Optional<Double> getTariffStart() {
        return getAttributes().getValueOrDefault(TARIFF_START);
    }
}
