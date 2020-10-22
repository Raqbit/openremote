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
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.MetaTypes;
import org.openremote.model.v2.ValueTypes;

public class Weather extends Asset {

    public static final AttributeDescriptor<Double> TEMPERATURE = new AttributeDescriptor<>("temperature", true, ValueTypes.TEMPERATURE, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> UV_INDEX = new AttributeDescriptor<>("uVIndex", false, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.LABEL, "UV index"),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> SUN_IRRADIANCE = new AttributeDescriptor<>("sunIrradiance", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> SUN_AZIMUTH = new AttributeDescriptor<>("sunAzimuth", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> SUN_ZENITH = new AttributeDescriptor<>("sunZenith", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> SUN_ALTITUDE = new AttributeDescriptor<>("sunAltitude", true, ValueTypes.POSITIVE_INTEGER, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> WIND_SPEED = new AttributeDescriptor<>("windSpeed", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_SPEED_KILOMETERS_HOUR),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> WIND_DIRECTION = new AttributeDescriptor<>("sunAltitude", true, ValueTypes.DIRECTION, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Double> RAINFALL = new AttributeDescriptor<>("rainfall", true, ValueTypes.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaTypes.UNIT_TYPE, Constants.UNITS_DISTANCE_MILLIMETRES),
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Integer> HUMIDITY = new AttributeDescriptor<>("humidity", true, ValueTypes.PERCENTAGE_INTEGER_0_100, null,
        new MetaItem<>(MetaTypes.READ_ONLY, true)
    );

    public static final AssetDescriptor<Weather> DESCRIPTOR = new AssetDescriptor<>("Weather", "weather-partly-cloudy", "49B0D8", Weather.class);

    public Weather(String name) {
        super(name, DESCRIPTOR);
    }

    public Double getTemperature() {
        return getAttributes().get(TEMPERATURE).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getUVIndex() {
        return getAttributes().get(UV_INDEX).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getSunIrradiance() {
        return getAttributes().get(SUN_IRRADIANCE).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getSunAzimuth() {
        return getAttributes().get(SUN_AZIMUTH).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getSunZenith() {
        return getAttributes().get(SUN_ZENITH).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getSunAltitude() {
        return getAttributes().get(SUN_ALTITUDE).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getRainfall() {
        return getAttributes().get(RAINFALL).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getHumidity() {
        return getAttributes().get(HUMIDITY).flatMap(Attribute::getValue).orElse(null);
    }
}
