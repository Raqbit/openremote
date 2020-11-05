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
package org.openremote.model.v2;

import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;

/**
 * Describes a {@link MetaItem} that can be added to an {@link Attribute}; the {@link #getName()} must match the
 * {@link MetaItem#getName()}.
 */
public class MetaItemDescriptor<T> implements NameValueDescriptorProvider<T> {
    protected String name;
    protected ValueDescriptor<T> valueDescriptor;
    protected T defaultValue;

    public MetaItemDescriptor(String name, ValueDescriptor<T> valueDescriptor, T defaultValue) {
        this.name = name;
        this.valueDescriptor = valueDescriptor;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueDescriptor<T> getValueDescriptor() {
        return valueDescriptor;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

}
