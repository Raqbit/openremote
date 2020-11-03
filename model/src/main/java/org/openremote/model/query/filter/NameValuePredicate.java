/*
 * Copyright 2017, OpenRemote Inc.
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
package org.openremote.model.query.filter;

/**
 * There is an implicit AND condition between the name and the value; the name is required and {@link #mustExist} or
 * {@link #mustNotExist} can be set to control whether the {@link org.openremote.model.v2.AbstractNameValueHolderImpl}
 * exists or doesn't; if neither are specified then it is assumed to be optional and then the {@link #value} can be
 * used to predicate on the value of the {@link org.openremote.model.v2.AbstractNameValueHolderImpl} if it is present.
 */
public class NameValuePredicate {

    public StringPredicate name;
    public boolean mustNotExist;
    public boolean mustExist;
    public ValuePredicate value;

    public NameValuePredicate() {
    }

    public NameValuePredicate(String name) {
        this(new StringPredicate(name));
    }

    public NameValuePredicate(StringPredicate name) {
        this.name = name;
    }

    public NameValuePredicate(ValuePredicate value) {
        this.value = value;
    }

    public NameValuePredicate(StringPredicate name, ValuePredicate value) {
        this.name = name;
        this.value = value;
    }

    public NameValuePredicate name(StringPredicate name) {
        this.name = name;
        return this;
    }

    public NameValuePredicate value(ValuePredicate value) {
        this.value = value;
        return this;
    }

    public NameValuePredicate mustNotExist() {
        this.mustNotExist = true;
        return this;
    }

    public NameValuePredicate mustExist() {
        this.mustExist = true;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name=" + name +
            ", mustExist=" + mustExist +
            ", mustNotExist=" + mustNotExist +
            ", value=" + value +
            '}';
    }
}
