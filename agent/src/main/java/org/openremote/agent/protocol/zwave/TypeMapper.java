/*
 * Copyright 2018, OpenRemote Inc.
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
package org.openremote.agent.protocol.zwave;

import org.openremote.model.attribute.MetaItem;
import org.openremote.model.util.Pair;
import org.openremote.model.v2.MetaItemType;
import org.openremote.model.v2.ValueDescriptor;
import org.openremote.model.v2.ValueType;
import org.openremote.protocol.zwave.model.commandclasses.channel.ChannelType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.openremote.model.Constants.*;

public class TypeMapper {

    private static final Logger LOG = Logger.getLogger(TypeMapper.class.getName());

    static private Map<ChannelType, Pair<ValueDescriptor<?>, List<MetaItem<?>>>> typeMap = new HashMap<>();

    static {

        // Basic types

        typeMap.put(ChannelType.INTEGER, new Pair<>(ValueType.INTEGER, null));
        typeMap.put(ChannelType.NUMBER, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.STRING, new Pair<>(ValueType.STRING, null));
        typeMap.put(ChannelType.BOOLEAN, new Pair<>(ValueType.BOOLEAN, null));
        typeMap.put(ChannelType.ARRAY, new Pair<>(ValueType.ARRAY, null));

        // COMMAND_CLASS_SENSOR_MULTILEVEL

        typeMap.put(ChannelType.TEMPERATURE_CELSIUS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS))));
        typeMap.put(ChannelType.TEMPERATURE_FAHRENHEIT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT))));
        typeMap.put(ChannelType.PERCENTAGE, new Pair<>(ValueType.PERCENTAGE_INTEGER_0_100, null));
        typeMap.put(ChannelType.LUMINANCE_PERCENTAGE, new Pair<>(ValueType.PERCENTAGE_INTEGER_0_100, null));
        typeMap.put(ChannelType.LUMINANCE_LUX, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_BRIGHTNESS_LUX))));
        typeMap.put(ChannelType.POWER_WATT, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_WATT))));
        typeMap.put(ChannelType.POWER_BTU_H, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_BTU_PER_HOUR))));
        typeMap.put(ChannelType.HUMIDITY_PERCENTAGE, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE))));
        typeMap.put(ChannelType.HUMIDITY_ABSOLUTE, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DENSITY_GRAM_CUBIC_M))));
        typeMap.put(ChannelType.SPEED_MS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_METRES_SECOND))));
        typeMap.put(ChannelType.SPEED_MPH, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_MILES_HOUR))));
        typeMap.put(ChannelType.DIRECTION_DECIMAL_DEGREES, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ANGLE_DEGREE))));
        typeMap.put(ChannelType.PRESSURE_KPA, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PRESSURE_KPA))));
        typeMap.put(ChannelType.PRESSURE_IN_HG, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PRESSURE_IN_HG))));
        typeMap.put(ChannelType.SOLAR_RADIATION_WATT_M2, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_WATT_SQUARE_M))));
        typeMap.put(ChannelType.DEW_POINT_CELSIUS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS))));
        typeMap.put(ChannelType.DEW_POINT_FAHRENHEIT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT))));
        typeMap.put(ChannelType.RAINFALL_MMPH, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_MILLIMETRES_HOUR))));
        typeMap.put(ChannelType.RAINFALL_INPH, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_MILLIMETRES_HOUR))));
        typeMap.put(ChannelType.TIDE_LEVEL_M, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_METRE))));
        typeMap.put(ChannelType.TIDE_LEVEL_FT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_FOOT))));
        typeMap.put(ChannelType.WEIGHT_KG, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM))));
        typeMap.put(ChannelType.WEIGHT_LB, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_POUND))));
        typeMap.put(ChannelType.VOLTAGE_V, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLTAGE_VOLT))));
        typeMap.put(ChannelType.VOLTAGE_MV, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLTAGE_MILLIVOLT))));
        typeMap.put(ChannelType.CURRENT_A, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_CURRENT_AMP))));
        typeMap.put(ChannelType.CURRENT_MA, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_CURRENT_MILLIAMP))));
        typeMap.put(ChannelType.CO2_PPM, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DENSITY_PARTS_MILLION))));
        typeMap.put(ChannelType.AIR_FLOW_CMPH, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FLOW_CUBIC_M_MINUTE))));
        typeMap.put(ChannelType.AIR_FLOW_CFTPM, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FLOW_CUBIC_FOOT_MINUTE))));
        typeMap.put(ChannelType.TANK_CAPACITY_L, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_LITRE))));
        typeMap.put(ChannelType.TANK_CAPACITY_CBM, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_METRE))));
        typeMap.put(ChannelType.TANK_CAPACITY_GAL, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_GALLON))));
        typeMap.put(ChannelType.DISTANCE_M, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_METRE))));
        typeMap.put(ChannelType.DISTANCE_CM, new Pair<>(ValueType.POSITIVE_NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_CENTIMETRE))));
        typeMap.put(ChannelType.DISTANCE_FT, new Pair<>(ValueType.POSITIVE_NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_FOOT))));
        typeMap.put(ChannelType.ANGLE_POSITION_PERCENT, new Pair<>(ValueType.PERCENTAGE_INTEGER_0_100, null));
        typeMap.put(ChannelType.ANGLE_POSITION_DEGREE_NORTH_POLE, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ANGLE_DEGREE))));
        typeMap.put(ChannelType.ANGLE_POSITION_DEGREE_SOUTH_POLE, new Pair<>(ValueType.INTEGER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ANGLE_DEGREE))));
        typeMap.put(ChannelType.ROTATION_HZ, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_HERTZ))));
        typeMap.put(ChannelType.ROTATION_RPM, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_RPM))));
        typeMap.put(ChannelType.WATER_TEMPERATURE_CELSIUS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS))));
        typeMap.put(ChannelType.WATER_TEMPERATURE_FAHRENHEIT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT))));
        typeMap.put(ChannelType.SOIL_TEMPERATURE_CELSIUS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS))));
        typeMap.put(ChannelType.SOIL_TEMPERATURE_FAHRENHEIT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT))));
        typeMap.put(ChannelType.SEISMIC_INTENSITY_MERCALLI, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SEISMIC_INTENSITY_EU_MACROSEISMIC, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SEISMIC_INTENSITY_LIEDU, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SEISMIC_INTENSITY_SHINDO, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_LOCAL, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_MOMENT, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_SURFACE_WAVE, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_BODY_WAVE, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.ULTRAVIOLET_UV_INDEX, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.RESISTIVITY_OHM, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_RESISTANCE_OHM))));
        typeMap.put(ChannelType.CONDUCTIVITY_SPM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.LOUDNESS_DB, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SOUND_DECIBELS))));
        typeMap.put(ChannelType.LOUDNESS_DBA, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SOUND_DECIBELS_ATTENUATED))));
        typeMap.put(ChannelType.MOISTURE_PERCENTAGE, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE))));
        typeMap.put(ChannelType.MOISTURE_VOLUME_WATER_CONTENT, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.MOISTURE_IMPEDANCE, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.MOISTURE_WATER_ACTIVITY, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.FREQUENCY_HZ, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_HERTZ))));
        typeMap.put(ChannelType.FREQUENCY_KHZ, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_KILOHERTZ))));
        typeMap.put(ChannelType.TIME_SECONDS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TIME_SECOND))));
        typeMap.put(ChannelType.TARGET_TEMPERATUE_CELSIUS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS))));
        typeMap.put(ChannelType.TARGET_TEMPERATUE_FAHRENHEIT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT))));
        typeMap.put(ChannelType.PARTICULATE_MATTER_2_5_MOLPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.PARTICULATE_MATTER_2_5_MCGPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.FORMALDEHYDE_LEVEL_MOLPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.RADON_CONCENTRATION_BQPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.RADON_CONCENTRATION_PCIPL, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.METHANE_DENSITY_MOLPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.VOLATILE_ORGANIC_COMPOUND_MOLPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.VOLATILE_ORGANIC_COMPOUND_PPM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.CO_MOLPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.CO_PPM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SOIL_HUMIDITY_PERCENTAGE, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE))));
        typeMap.put(ChannelType.SOIL_REACTIVITY_PH, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.SOIL_SALINITY_MOLPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.HEART_RATE_BPM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.BLOOD_PRESSURE_SYSTOLIC, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.BLOOD_PRESSURE_DIASTOLIC, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.MUSCLE_MASS_KG, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM))));
        typeMap.put(ChannelType.FAT_MASS_KG, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM))));
        typeMap.put(ChannelType.BONE_MASS_KG, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM))));
        typeMap.put(ChannelType.TOTAL_BODY_WATER_KG, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM))));
        typeMap.put(ChannelType.BASIC_METABOLIC_RATE_JOULE, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_JOULE))));
        typeMap.put(ChannelType.BODY_MASS_INDEX, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.ACCELERATION_X_MPSS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ACCELERATION_METRE_SECOND))));
        typeMap.put(ChannelType.ACCELERATION_Y_MPSS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ACCELERATION_METRE_SECOND))));
        typeMap.put(ChannelType.ACCELERATION_Z_MPSS, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ACCELERATION_METRE_SECOND))));
        typeMap.put(ChannelType.SMOKE_DENSITY_PERCENTAGE, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE))));
        typeMap.put(ChannelType.WATER_FLOW_LPH, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FLOW_LITRE_HOUR))));
        typeMap.put(ChannelType.WATER_PRESSURE_KPA, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PRESSURE_KPA))));
        typeMap.put(ChannelType.RF_SIGNAL_STRENGTH_RSSI, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.RF_SIGNAL_STRENGTH_DBM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.PARTICULATE_MATTER_MOLPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.PARTICULATE_MATTER_MCGPCM, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.RESPIRATORY_RATE_BPM, new Pair<>(ValueType.NUMBER, null));

        // COMMAND_CLASS_METER

        // Electric Meter
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KWH, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_KILOWATT_HOUR))));
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KVAH, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_KILOWATT_HOUR))));
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_W, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_WATT))));
        typeMap.put(ChannelType.ELECTRIC_METER_PULSE_COUNT, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.ELECTRIC_METER_VOLTAGE_V, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLTAGE_VOLT))));
        typeMap.put(ChannelType.ELECTRIC_METER_CURRENT_A, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_CURRENT_AMP))));
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_FACTOR, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_KVAR, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_KILOWATT))));
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KVARH, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_KILOWATT_HOUR))));

        // Gas Meter
        typeMap.put(ChannelType.GAS_METER_VOLUME_CM, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_METRE))));
        typeMap.put(ChannelType.GAS_METER_VOLUME_CFT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_FOOT))));
        typeMap.put(ChannelType.GAS_METER_PULSE_COUNT, new Pair<>(ValueType.NUMBER, null));

        // Water Meter
        typeMap.put(ChannelType.WATER_METER_VOLUME_CM, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_METRE))));
        typeMap.put(ChannelType.WATER_METER_VOLUME_CFT, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_FOOT))));
        typeMap.put(ChannelType.WATER_METER_VOLUME_GAL, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_GALLON))));
        typeMap.put(ChannelType.WATER_METER_PULSE_COUNT, new Pair<>(ValueType.NUMBER, null));

        // COMMAND_CLASS_COLOR_CONTROL

        typeMap.put(ChannelType.COLOR_WARM_WHITE, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_COLD_WHITE, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_RED, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_GREEN, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_BLUE, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_AMBER, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_CYAN, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_PURPLE, new Pair<>(ValueType.BYTE, null));
        typeMap.put(ChannelType.COLOR_INDEXED, new Pair<>(ValueType.NUMBER, null));
        typeMap.put(ChannelType.COLOR_RGB, new Pair<>(ValueType.COLOUR_RGB, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_COLOR_RGB))));
        typeMap.put(ChannelType.COLOR_ARGB, new Pair<>(ValueType.COLOUR_RGB, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_COLOR_ARGB))));

        // COMMAND_CLASS_SENSOR_ALARM

        typeMap.put(ChannelType.GENERAL_PURPOSE_ALARM, new Pair<>(ValueType.BOOLEAN, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF))));
        typeMap.put(ChannelType.SMOKE_ALARM, new Pair<>(ValueType.BOOLEAN, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF))));
        typeMap.put(ChannelType.CO_ALARM, new Pair<>(ValueType.BOOLEAN, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF))));
        typeMap.put(ChannelType.CO2_ALARM, new Pair<>(ValueType.BOOLEAN, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF))));
        typeMap.put(ChannelType.HEAT_ALARM, new Pair<>(ValueType.BOOLEAN, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF))));
        typeMap.put(ChannelType.WATER_LEAK_ALARM, new Pair<>(ValueType.BOOLEAN, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF))));
        typeMap.put(ChannelType.FIRST_SUPPORTED_ALARM, new Pair<>(ValueType.BOOLEAN, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF))));

        // COMMAND_CLASS_BATTERY

        typeMap.put(ChannelType.CHARGE_PERCENTAGE, new Pair<>(ValueType.NUMBER, Collections.singletonList(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE))));

        // COMMAND_CLASS_CLOCK

        typeMap.put(ChannelType.DATETIME, new Pair<>(ValueType.TIMESTAMP_ISO8601, null));
    }

    public static ValueDescriptor<?> toAttributeType(ChannelType channelType) {

        ValueDescriptor<?> valueType = ValueType.STRING;

        if (typeMap.containsKey(channelType)) {
            Pair<ValueDescriptor<?>, List<MetaItem<?>>> valueTypeAndMeta = typeMap.get(channelType);
            if (valueTypeAndMeta != null) {
                valueType = valueTypeAndMeta.key;
            }
        } else {
            switch(channelType.getValueType()) {
                case INTEGER:
                    valueType = ValueType.INTEGER;
                    break;
                case NUMBER:
                    valueType = ValueType.NUMBER;
                    break;
                case BOOLEAN:
                    valueType = ValueType.BOOLEAN;
                    break;
                case STRING:
                    valueType = ValueType.STRING;
                    break;
                case ARRAY:
                    valueType = ValueType.ARRAY;
                    break;
            }
        }
        return valueType;
    }
}
