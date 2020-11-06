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
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;

public class Building extends Asset {

    public static final AttributeDescriptor<String> STREET = new AttributeDescriptor<>("street", true, ValueType.STRING, null);
    public static final AttributeDescriptor<String> CITY = new AttributeDescriptor<>("city", true, ValueType.STRING, null);
    public static final AttributeDescriptor<String> COUNTRY = new AttributeDescriptor<>("country", true, ValueType.STRING, null);
    public static final AttributeDescriptor<String> POSTAL_CODE = new AttributeDescriptor<>("postalCode", true, ValueType.STRING, null);

    public static final AssetDescriptor<Building> DESCRIPTOR = new AssetDescriptor<>("office-building", "4b5966", Building.class);

    public Building(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStreet() {
        return getAttributes().getValueOrDefault(STREET);
    }

    public Optional<String> getCity() {
        return getAttributes().getValueOrDefault(CITY);
    }

    public Optional<String> getCountry() {
        return getAttributes().getValueOrDefault(COUNTRY);
    }

    public Optional<String> getPostalCode() {
        return getAttributes().getValueOrDefault(POSTAL_CODE);
    }
}
