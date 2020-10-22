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

public class LightColored extends Light {

    public static final AttributeDescriptor<ValueTypes.IntegerList> COLOR_RGB = new AttributeDescriptor<>("colorRGB", true, ValueTypes.COLOUR_RGB, null);
    public static final AttributeDescriptor<ValueTypes.IntegerList> COLOR_RGBW = new AttributeDescriptor<>("colorRGBW", true, ValueTypes.COLOUR_RGB, null);
    public static final AttributeDescriptor<ValueTypes.IntegerList> COLOR_ARGB = new AttributeDescriptor<>("colorARGB", true, ValueTypes.COLOUR_RGB, null);
    // TODO: Re-evaluate the following these seem like protocol related attributes
    public static final AttributeDescriptor<Integer> GROUP_NUMBER = new AttributeDescriptor<>("groupNumber", true, ValueTypes.POSITIVE_INTEGER, null);
    public static final AttributeDescriptor<String> SCENARIO = new AttributeDescriptor<>("scenario", true, ValueTypes.STRING, null);

    public static final AssetDescriptor<LightColored> DESCRIPTOR = new AssetDescriptor<>("Light Colored", "lightbulb", "706CE6", LightColored.class);

    public LightColored(String name) {
        super(name, DESCRIPTOR);
    }

    public ValueTypes.IntegerList getColorRGB() {
        return getAttributes().get(COLOR_RGB).flatMap(Attribute::getValue).orElse(null);
    }

    public ValueTypes.IntegerList getColorRGBW() {
        return getAttributes().get(COLOR_RGBW).flatMap(Attribute::getValue).orElse(null);
    }

    public ValueTypes.IntegerList getColorARGB() {
        return getAttributes().get(COLOR_ARGB).flatMap(Attribute::getValue).orElse(null);
    }
}
