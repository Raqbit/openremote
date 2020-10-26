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
package org.openremote.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import static org.openremote.model.value.RegexValueFilter.NAME;

/**
 * This filter works on any type of data; when applying the filter the data should be converted to JSON representation
 * using a tool like Jackson and then the JSON path expression should be applied to this JSON string.
 */
@JsonTypeName(NAME)
public class JsonPathFilter extends ValueFilter<Object> {

    public static final String NAME = "jsonPath";

    @JsonProperty
    public String path;

    @JsonProperty
    public boolean returnFirst;

    @JsonProperty
    public boolean returnLast;

    @JsonCreator
    public JsonPathFilter(@JsonProperty("path") String path,
                          @JsonProperty("returnFirst") boolean returnFirst,
                          @JsonProperty("returnLast") boolean returnLast) {
        this.path = path;
        this.returnFirst = returnFirst;
        this.returnLast = returnLast;
    }

    @Override
    public Class<Object> getValueType() {
        return Object.class;
    }
}
