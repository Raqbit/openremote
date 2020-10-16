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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openremote.model.v2.NameProvider;
import org.openremote.model.v2.NameValueDescriptorProvider;
import org.openremote.model.v2.ValueProvider;

import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Special list for {@link NameProvider} items where items with duplicate names are not allowed; this is serialised as
 * a JSON object where the name becomes the key.
 */
@JsonSerialize(using = NamedList.NamedListSerializer.class)
@JsonDeserialize(using = NamedList.NamedListDeserializer.class)
public class NamedList<T extends NameProvider & ValueProvider<?>> extends ArrayList<T> {

    public static class NamedListSerializer extends StdSerializer<NamedList<?>> {

        public NamedListSerializer(Class<NamedList<?>> t) {
            super(t);
        }

        @Override
        public void serialize(NamedList<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            for (NameProvider model : value) {
                gen.writeObjectField(model.getName(), model);
            }

            gen.writeEndObject();
        }
    }

    public static class NamedListDeserializer extends StdDeserializer<NamedList<?>> {

        public NamedListDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public NamedList<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);

            if (node.getNodeType() != JsonNodeType.OBJECT) {
                throw new InvalidFormatException(jp, "Expected an object but got type: " + node.getNodeType(), node, NamedList.class);
            }
            return null;
        }
    }

    protected static void throwIfHas(NamedList<?> list, String name) {
        if (list.has(name)) {
            throw new IllegalStateException("List already contains an item with this name: " + name);
        }
    };

    protected static <T extends NameProvider> void throwIfDuplicates(Collection<T> c) {
        c.stream().map(NameProvider::getName).filter(name ->
            Collections.frequency(c, name) > 1
        ).findAny().ifPresent(duplicateName -> {
            throw new IllegalStateException("Detected multiple items with the same name: " + duplicateName);
        });
    }

    @Override
    public T set(int index, T element) {
        T result = super.set(index, element);
        throwIfHas(this, element.getName());
        return result;
    }

    @Override
    public boolean add(T t) {
        boolean result = super.add(t);
        throwIfHas(this, t.getName());
        return result;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        throwIfHas(this, element.getName());
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throwIfDuplicates(c);
        c.forEach(item -> throwIfHas(this, item.getName()));
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throwIfDuplicates(c);
        c.forEach(item -> throwIfHas(this, item.getName()));
        return super.addAll(index, c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        super.replaceAll(operator);
        throwIfDuplicates(this);
    }

    public Optional<T> get(NameProvider nameHolder) {
        return get(nameHolder.getName());
    }

    public Optional<T> get(String name) {
        return this.stream().filter(item -> item.getName().equals(name)).findFirst();
    }

    public <S, U extends ValueProvider<S>> Optional<U> getInternal(NameValueDescriptorProvider<S> nameValueDescriptorProvider) {
        Optional<T> valueProvider = get(nameValueDescriptorProvider);
        return valueProvider.map(item -> {
            Class<?> itemType = item.getType();
            Class<S> expectedType = nameValueDescriptorProvider.getValueDescriptor().getType();
            if (itemType == expectedType) {
                return (U)item;
            }
            return null;
        });
    }

    @SafeVarargs
    public final void addAll(T... items) {
        this.addAll(Arrays.asList(items));
    }

    /**
     * Add or replace the specified items by name
     */
    public final void set(T...items) {
        set(Arrays.asList(items));
    }
    public final void set(Collection<T> items) {
        items.forEach(item -> this.removeIf(i -> i.getName().equals(item.getName())));
        addAll(items);
    }

    public boolean has(NameProvider nameHolder) {
        return has(nameHolder.getName());
    }

    public boolean has(String name) {
        return this.stream().anyMatch(item -> item.getName().equals(name));
    }
}
