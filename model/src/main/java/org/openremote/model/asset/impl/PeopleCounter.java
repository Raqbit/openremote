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

import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class PeopleCounter extends Device {

    public static final AttributeDescriptor<Integer> COUNT_IN = new AttributeDescriptor<>("countIn", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_OUT = new AttributeDescriptor<>("countOut", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_TOTAL = new AttributeDescriptor<>("countTotal", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_IN_PER_MINUTE = new AttributeDescriptor<>("countInMinute", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_OUT_PER_MINUTE = new AttributeDescriptor<>("countOutMinute", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Double> COUNT_GROWTH_PER_MINUTE = new AttributeDescriptor<>("countGrowthMinute", true, ValueType.NUMBER, null);

    public static final AssetDescriptor<PeopleCounter> DESCRIPTOR = new AssetDescriptor<>("account-multiple", "4b5966", PeopleCounter.class);

    public PeopleCounter(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Integer> getCountIn() {
        return getAttributes().getValueOrDefault(COUNT_IN);
    }

    public Optional<Integer> getCountOut() {
        return getAttributes().getValueOrDefault(COUNT_OUT);
    }

    public Optional<Integer> getCountInMinute() {
        return getAttributes().getValueOrDefault(COUNT_IN_PER_MINUTE);
    }

    public Optional<Integer> getCountOutMinute() {
        return getAttributes().getValueOrDefault(COUNT_OUT_PER_MINUTE);
    }

    public Optional<Integer> getCountTotal() {
        return getAttributes().getValueOrDefault(COUNT_TOTAL);
    }

    public Optional<Double> getCountGrowthMinute() {
        return getAttributes().getValueOrDefault(COUNT_GROWTH_PER_MINUTE);
    }
}
