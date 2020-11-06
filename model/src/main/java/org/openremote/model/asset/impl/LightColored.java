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
import org.openremote.model.value.ColorRGB;

import java.util.Optional;

public class LightColored extends Light {

    public static final AttributeDescriptor<ColorRGB> COLOR_RGB = new AttributeDescriptor<>("colorRGB", true, ValueType.COLOUR_RGB, null);
    // TODO: Re-evaluate the following these seem like protocol related attributes
    public static final AttributeDescriptor<Integer> GROUP_NUMBER = new AttributeDescriptor<>("groupNumber", true, ValueType.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<String> SCENARIO = new AttributeDescriptor<>("scenario", true, ValueType.STRING, null);

    public static final AssetDescriptor<LightColored> DESCRIPTOR = new AssetDescriptor<>("lightbulb", "706CE6", LightColored.class);

    public LightColored(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<ColorRGB> getColorRGB() {
        return getAttributes().getValueOrDefault(COLOR_RGB);
    }
}
