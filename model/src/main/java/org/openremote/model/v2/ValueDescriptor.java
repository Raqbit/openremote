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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.attribute.MetaItemList;

import java.util.Arrays;
import java.util.Collection;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, defaultImpl = ValueDescriptor.class)
public class ValueDescriptor<T> implements MetaProvider {

    public static class ValueProviderStringConverter extends StdConverter<ValueDescriptor<?>, String> {

        @Override
        public String convert(ValueDescriptor<?> value) {
            return value.getName();
        }
    }

    protected String name;
    protected Class<T> type;
    protected MetaItemList meta;

    public ValueDescriptor(String name, Class<T> type) {
        this(name, type, (MetaItemList)null);
    }

    public ValueDescriptor(String name, Class<T> type, MetaItem<?>...meta) {
        this(name, type, new MetaItemList(Arrays.asList(meta)));
    }

    public ValueDescriptor(String name, Class<T> type, Collection<MetaItem<?>> meta) {
        this(name, type, new MetaItemList(meta));
    }

    public ValueDescriptor(String name, Class<T> type, MetaItemList meta) {
        this.name = name;
        this.type = type;
        this.meta = meta;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public Collection<MetaItem<?>> getMeta() {
        return meta;
    }
}
