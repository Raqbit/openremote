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
package org.openremote.model.attribute;

import org.openremote.model.value.MetaItemDescriptor;

import java.util.Collection;
import java.util.Optional;

public class MetaList extends NamedList<MetaItem<?>> {

    public MetaList() {
    }

    public MetaList(Collection<MetaItem<?>> meta) {
        super(meta);
    }

    // This works around the crappy type system
    public <S> Optional<MetaItem<S>> get(MetaItemDescriptor<S> metaDescriptor) {
        return super.getInternal(metaDescriptor);
    }

    public <S> MetaItem<S> getOrCreate(MetaItemDescriptor<S> metaDescriptor) {
        MetaItem<S> metaItem = get(metaDescriptor).orElse(new MetaItem<>(metaDescriptor));
        addOrReplace(metaItem);
        return metaItem;
    }

    public <T> void set(MetaItemDescriptor<T> descriptor, T value) {
        MetaItem<T> metaItem = get(descriptor).orElse(new MetaItem<>(descriptor, null));
        metaItem.setValue(value);
    }
}
