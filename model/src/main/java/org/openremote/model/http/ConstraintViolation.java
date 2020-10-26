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
package org.openremote.model.http;

public class ConstraintViolation {

    public enum Type {CONFLICT, CLASS, FIELD, PROPERTY, PARAMETER, RETURN_VALUE}

    protected Type constraintType;

    protected String path;

    protected String message;

    protected String value;

    final public Type getConstraintType() {
        return constraintType;
    }

    final public void setConstraintType(Type constraintType) {
        this.constraintType = constraintType;
    }

    final public String getPath() {
        return path;
    }

    final public void setPath(String path) {
        this.path = path;
    }

    final public String getMessage() {
        return message;
    }

    final public void setMessage(String message) {
        this.message = message;
    }

    final public String getValue() {
        return value;
    }

    final public void setValue(String value) {
        this.value = value;
    }

    @Override
    final public String toString() {
        return getClass().getSimpleName() + "{" +
            "constraintType=" + constraintType +
            ", path='" + path + '\'' +
            ", message='" + message + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
