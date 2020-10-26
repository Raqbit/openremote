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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openremote.model.util.TextUtil;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractNameValueHolderImpl<T> implements ValueHolder<T>, NameProvider {

    protected ValueDescriptor<T> type;
    protected T value;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    protected String name;

    protected AbstractNameValueHolderImpl() {
    }

    public AbstractNameValueHolderImpl(@NotNull String name, @NotNull ValueDescriptor<T> type, T value) {
        if (TextUtil.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public ValueDescriptor<T> getValueType() {
        return type;
    }

    @Override
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public void setValue(T value) {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNameValueHolderImpl<?> that = (AbstractNameValueHolderImpl<?>) o;
        return Objects.equals(value, that.value) &&
            name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name);
    }
}