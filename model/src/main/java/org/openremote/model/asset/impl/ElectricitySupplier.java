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

    
    public static final AttributeDescriptor<Integer> EFFICIENCY = new AttributeDescriptor<>("efficiency", true, ValueTypes.PERCENTAGE_INTEGER_0_100, null);
    public static final AttributeDescriptor<Double> POWER_TOTAL = new AttributeDescriptor<>("powerTotal", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> POWER_FORECAST_DEVIATION = new AttributeDescriptor<>("powerForecastDeviation", true, ValueTypes.NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL = new AttributeDescriptor<>("energyTotal", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> PANEL_ORIENTATION = new AttributeDescriptor<>("panelOrientation", true, ValueTypes.DIRECTION, null);

    public static final AssetDescriptor<ElectricitySupplier> DESCRIPTOR = new AssetDescriptor<>("Electricity supplier", "upload-network", "9257A9", ElectricitySupplier.class);

    public ElectricitySupplier(String name) {
        super(name, DESCRIPTOR);
    }

    public String getStatus() {
        return getAttributes().get(STATUS).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerTotal() {
        return getAttributes().get(POWER_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerForecastDeviation() {
        return getAttributes().get(POWER_FORECAST_DEVIATION).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getPowerCapacity() {
        return getAttributes().get(POWER_CAPACITY).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getEfficiency() {
        return getAttributes().get(EFFICIENCY).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getEnergyTotal() {
        return getAttributes().get(ENERGY_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getPanelOrientation() {
        return getAttributes().get(PANEL_ORIENTATION).flatMap(Attribute::getValue).orElse(null);
    }
}
