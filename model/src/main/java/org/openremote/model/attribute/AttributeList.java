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

import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueDescriptor;

import java.util.Collection;
import java.util.Optional;

public class AttributeList extends NamedList<Attribute<?>> {

    public AttributeList() {
    }

    public AttributeList(Collection<? extends Attribute<?>> c) {
        super(c);
    }

    // This works around the crappy type system and avoids the need for a type witness
    public <S> Optional<Attribute<S>> get(AttributeDescriptor<S> attributeDescriptor) {
        return super.get(attributeDescriptor);
    }

    public <S> Attribute<S> getOrCreate(AttributeDescriptor<S> attributeDescriptor) {
        Attribute<S> attribute = get(attributeDescriptor).orElse(new Attribute<>(attributeDescriptor));
        addOrReplace(attribute);
        return attribute;
    }

    @SuppressWarnings("unchecked")
    public <S> Attribute<S> getOrCreate(String attributeName, ValueDescriptor<S> valueDescriptor) {
        Attribute<S> attribute = (Attribute<S>) get(attributeName).orElse(new Attribute<>(attributeName, valueDescriptor));
        addOrReplace(attribute);
        return attribute;
    }

    public <T> void set(AttributeDescriptor<T> descriptor, T value) {
        Attribute<T> attribute = get(descriptor).orElse(new Attribute<>(descriptor, null));
        attribute.setValue(value);
    }
}
