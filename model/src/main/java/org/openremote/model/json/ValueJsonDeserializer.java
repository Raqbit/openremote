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
package org.openremote.model.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.openremote.model.value.Value;
import org.openremote.model.value.ValueException;
import org.openremote.model.value.Values;

import java.io.IOException;

public class ValueJsonDeserializer<T extends Value> extends StdDeserializer<T> {

    public ValueJsonDeserializer() {
        super(Value.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        try {
            return (T) Values.instance().parse(jsonParser.getCodec().readTree(jsonParser).toString()).
                orElseThrow(() -> new IOException("Empty JSON data"));
        } catch (ValueException ex) {
            throw new IOException(ex);
        }
    }
}
