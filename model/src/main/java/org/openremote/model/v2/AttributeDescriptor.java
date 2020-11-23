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
import org.openremote.model.attribute.MetaList;

import java.util.Arrays;
import java.util.Collection;

/**
 * Describes an {@link Attribute} in terms of what the value type will be and also optionally provides default
 * {@link MetaItem}s that will be added to new instances of the {@link Attribute}.
 */
public class AttributeDescriptor<T> extends AbstractNameValueDescriptorHolder<T> implements MetaHolder {

    protected MetaList meta;

    public AttributeDescriptor(String name, ValueDescriptor<T> valueDescriptor) {
        this(name, valueDescriptor, (MetaList)null);
    }

    public AttributeDescriptor(String name, ValueDescriptor<T> valueDescriptor, MetaItem<?>... meta) {
        this(name, valueDescriptor, Arrays.asList(meta));
    }

    public AttributeDescriptor(String name, ValueDescriptor<T> valueDescriptor, Collection<MetaItem<?>> meta) {
        this(name, valueDescriptor, meta instanceof MetaList ? (MetaList)meta : new MetaList(meta));
    }

    public AttributeDescriptor(String name, ValueDescriptor<T> valueDescriptor, MetaList meta) {
        super(name, valueDescriptor);
        this.meta = meta;
    }

    @Override
    public Collection<MetaItem<?>> getMeta() {
        return meta;
    }
}
