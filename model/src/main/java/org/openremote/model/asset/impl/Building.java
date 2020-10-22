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

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueTypes;

public class Building extends Asset {

    public static final AttributeDescriptor<String> STREET = new AttributeDescriptor<>("street", true, ValueTypes.STRING, null);
    public static final AttributeDescriptor<String> CITY = new AttributeDescriptor<>("city", true, ValueTypes.STRING, null);
    public static final AttributeDescriptor<String> COUNTRY = new AttributeDescriptor<>("country", true, ValueTypes.STRING, null);
    public static final AttributeDescriptor<String> POSTAL_CODE = new AttributeDescriptor<>("postalCode", true, ValueTypes.STRING, null);

    public static final AssetDescriptor<Building> DESCRIPTOR = new AssetDescriptor<>("Building", "office-building", "4b5966", Building.class);

    public Building(String name) {
        super(name, DESCRIPTOR);
    }

    public String getStreet() {
        return getAttributes().get(STREET).flatMap(Attribute::getValue).orElse(null);
    }

    public String getCity() {
        return getAttributes().get(CITY).flatMap(Attribute::getValue).orElse(null);
    }

    public String getCountry() {
        return getAttributes().get(COUNTRY).flatMap(Attribute::getValue).orElse(null);
    }

    public String getPostalCode() {
        return getAttributes().get(POSTAL_CODE).flatMap(Attribute::getValue).orElse(null);
    }
}
