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
import org.openremote.model.attribute.Attribute;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueTypes;

public class PeopleCounter extends Device {

    public static final AttributeDescriptor<Integer> COUNT_IN = new AttributeDescriptor<>("countIn", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_OUT = new AttributeDescriptor<>("countOut", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_TOTAL = new AttributeDescriptor<>("countTotal", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_IN_PER_MINUTE = new AttributeDescriptor<>("countInMinute", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Integer> COUNT_OUT_PER_MINUTE = new AttributeDescriptor<>("countOutMinute", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<Double> COUNT_GROWTH_PER_MINUTE = new AttributeDescriptor<>("countGrowthMinute", true, ValueTypes.NUMBER, null);

    public static final AssetDescriptor<PeopleCounter> DESCRIPTOR = new AssetDescriptor<>("People counter", "account-multiple", "4b5966", PeopleCounter.class);

    public PeopleCounter(String name) {
        super(name, DESCRIPTOR);
    }

    public Integer getCountIn() {
        return getAttributes().get(COUNT_IN).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getCountOut() {
        return getAttributes().get(COUNT_OUT).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getCountInMinute() {
        return getAttributes().get(COUNT_IN_PER_MINUTE).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getCountOutMinute() {
        return getAttributes().get(COUNT_OUT_PER_MINUTE).flatMap(Attribute::getValue).orElse(null);
    }

    public Integer getCountTotal() {
        return getAttributes().get(COUNT_TOTAL).flatMap(Attribute::getValue).orElse(null);
    }

    public Double getCountGrowthMinute() {
        return getAttributes().get(COUNT_GROWTH_PER_MINUTE).flatMap(Attribute::getValue).orElse(null);
    }
}
