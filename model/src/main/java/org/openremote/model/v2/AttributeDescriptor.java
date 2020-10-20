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

public class AttributeDescriptor<T> implements MetaDescriptorProvider, NameValueDescriptorProvider<T> {
    protected String name;
    protected boolean optional;
    protected ValueDescriptor<T> valueDescriptor;
    protected T defaultValue;
    protected MetaDescriptor<?>[] meta;

    public AttributeDescriptor(String name, boolean optional, ValueDescriptor<T> valueDescriptor, T defaultValue) {
        this(name, optional, valueDescriptor, defaultValue, EMPTY_META);
    }

    public AttributeDescriptor(String name, boolean optional, ValueDescriptor<T> valueDescriptor, T defaultValue, MetaDescriptor<?>[] meta) {
        this.name = name;
        this.optional = optional;
        this.valueDescriptor = valueDescriptor;
        this.defaultValue = defaultValue;
        this.meta = meta;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getName() {
        return name;
    }

    public ValueDescriptor<T> getValueDescriptor() {
        return valueDescriptor;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public MetaDescriptor<?>[] getMetaDescriptors() {
        return meta;
    }
}
