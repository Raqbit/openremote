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

import java.util.Objects;
import java.util.Optional;

/**
 * The desired or current or past state of an {@link AttributeRef}.
 * <p>
 * <code>null</code> is a valid {@link #value}.
 * </p>
 */
public class AttributeState<T> {

    protected AttributeRef attributeRef;
    protected T value;
    protected boolean deleted;

    protected AttributeState() {
    }

    public AttributeState(String entityId, Attribute<T> attribute) {
        this(entityId, attribute.getName(), attribute.getValue().orElse(null));
    }

    public AttributeState(String entityId, String attributeName, T value) {
        this(new AttributeRef(entityId, attributeName), value);
    }

    /**
     * @param value can be <code>null</code> if the attribute has no value.
     */
    public AttributeState(AttributeRef attributeRef, T value) {
        this.attributeRef = Objects.requireNonNull(attributeRef);
        this.value = value;
    }

    /**
     * Sets the {@link #value} to <code>null</code>.
     */
    public AttributeState(AttributeRef attributeRef) {
        this(attributeRef, null);
    }

    public AttributeRef getAttributeRef() {
        return attributeRef;
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "attributeRef=" + attributeRef +
            ", value=" + value +
            ", deleted=" + deleted +
            '}';
    }
}
