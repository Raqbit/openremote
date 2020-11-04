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
import org.openremote.model.v2.MetaType;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class Microphone extends Device {

    public static final AttributeDescriptor<Double> SOUND_LEVEL = new AttributeDescriptor<>("soundLevel", true, ValueType.POSITIVE_NUMBER, null,
        new MetaItem<>(MetaType.UNIT_TYPE, Constants.UNITS_SOUND_DECIBELS)
    );

    public static final AssetDescriptor<Microphone> DESCRIPTOR = new AssetDescriptor<>("Microphone", "microphone", "47A5FF", Microphone.class);

    public Microphone(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Double> getSoundLevel() {
        return getAttributes().getValueOrDefault(SOUND_LEVEL);
    }
}
