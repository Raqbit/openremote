/*
 * Copyright 2016, OpenRemote Inc.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openremote.model.v2.AbstractNameValueProviderImpl;
import org.openremote.model.v2.MetaDescriptor;
import org.openremote.model.v2.ValueDescriptor;

import java.util.Objects;

/**
 * A named value whose name must match the name of a {@link MetaDescriptor} and whose value must match the value type of
 * the {@link MetaDescriptor}.
 */
public class MetaItem<T> extends AbstractNameValueProviderImpl<T> {

    protected MetaItem() {
    }

    public MetaItem(MetaDescriptor<T> metaDescriptor) {
        this(metaDescriptor, metaDescriptor.getDefaultValue());
    }

    public MetaItem(MetaDescriptor<T> metaDescriptor, T value) {
        super(metaDescriptor.getName(), metaDescriptor.getValueDescriptor(), value);
    }

    // Don't serialise type info for meta items as the name is the meta descriptor name
    @JsonIgnore
    @Override
    public ValueDescriptor<T> getValueType() {
        return super.getValueType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MetaItem)) {
            return false;
        }

        MetaItem<?> metaItem = (MetaItem<?>) o;
        return Objects.equals(getName(), metaItem.getName())
            && Objects.equals(getValue(), metaItem.getValue());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
