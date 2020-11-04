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
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openremote.model.v2.NameHolder;
import org.openremote.model.v2.NameValueDescriptorProvider;
import org.openremote.model.v2.ValueHolder;

import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Special list for {@link NameHolder} items where items with duplicate names are not allowed; this is serialised as
 * a JSON object where the name becomes the key.
 */
@JsonSerialize(using = NamedList.NamedListSerializer.class)
@JsonDeserialize(using = NamedList.NamedListDeserializer.class)
public class NamedList<T extends NameHolder & ValueHolder<?>> extends ArrayList<T> {

    public static class NamedListSerializer extends StdSerializer<NamedList<?>> {

        public NamedListSerializer(Class<NamedList<?>> t) {
            super(t);
        }

        @Override
        public void serialize(NamedList<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            for (NameHolder model : value) {
                gen.writeObjectField(model.getName(), model);
            }

            gen.writeEndObject();
        }
    }

    /**
     * Expects an {@link ObjectNode}; for each key-value entry the value is checked to make sure it is also an
     * {@link ObjectNode} if it isn't then an {@link ObjectNode} is constructed and the value entry is assigned to the
     * value field. The key of the entry is then assigned to the name field and then this {@link ObjectNode} is
     * deserialized as an instance of the inner type which is determined using the {@link ContextualDeserializer}.
     */
    public static class NamedListDeserializer extends StdDeserializer<NamedList<?>> implements ContextualDeserializer {

        public NamedListDeserializer() {
            super(NamedList.class);
        }

        public NamedListDeserializer(JavaType valueType) {
            super(valueType);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            JavaType wrapperType = property.getType();
            JavaType valueType = wrapperType.containedType(0);
            return new NamedListDeserializer(wrapperType);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public NamedList<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);

            if (node.getNodeType() != JsonNodeType.OBJECT) {
                throw new InvalidFormatException(jp, "Expected an object but got type: " + node.getNodeType(), node, NamedList.class);
            }

            JavaType innerType = getValueType().containedType(0);
            NamedList list = new NamedList<>();
            ObjectNode namedListObj = (ObjectNode) node;

            for (Iterator<Map.Entry<String, JsonNode>> it = namedListObj.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> item = it.next();

                String name = item.getKey();
                JsonNode itemNode = item.getValue();

                if (itemNode.getNodeType() != JsonNodeType.OBJECT) {
                    ObjectNode newItemNode = namedListObj.objectNode();
                    newItemNode.set("value", itemNode);
                    item.setValue(newItemNode);
                    itemNode = newItemNode;
                }

                ((ObjectNode)itemNode).put("name", name);
                NameHolder itemObj = ctxt.readValue(itemNode.traverse(), innerType);
                list.add(itemObj);
            }
            return list;
        }
    }

    public NamedList() {
    }

    public NamedList(Collection<? extends T> c) {
        super();
        // Use addAll to do duplicate checking
        addAll(c);
    }

    protected static void throwIfHas(NamedList<?> list, String name) {
        if (list.has(name)) {
            throw new IllegalStateException("List already contains an item with this name: " + name);
        }
    }

    protected static <T extends NameHolder> void throwIfDuplicates(Collection<T> c) {
        c.stream().map(NameHolder::getName).filter(name ->
            Collections.frequency(c, name) > 1
        ).findAny().ifPresent(duplicateName -> {
            throw new IllegalStateException("Detected multiple items with the same name: " + duplicateName);
        });
    }

    @Override
    public T set(int index, T element) {
        throw new IllegalStateException("set by index not supported for named lists");
    }

    @Override
    public boolean add(T t) {
        throwIfHas(this, t.getName());
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        throwIfHas(this, element.getName());
        super.add(index, element);
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

    public Optional<T> get(NameHolder nameHolder) {
        return get(nameHolder.getName());
    }

    public Optional<T> get(String name) {
        return this.stream().filter(item -> item.getName().equals(name)).findFirst();
    }

    @SuppressWarnings("unchecked")
    protected <S, U extends ValueHolder<S>> Optional<U> getInternal(NameValueDescriptorProvider<S> nameValueDescriptorProvider) {
        Optional<T> valueProvider = get(nameValueDescriptorProvider);
        return valueProvider.map(item -> {
            Class<?> itemType = item.getValueType().getType();
            Class<S> expectedType = nameValueDescriptorProvider.getValueDescriptor().getType();
            if (itemType == expectedType) {
                return (U)item;
            }
            return null;
        });
    }

    public <S> Optional<S> getValue(NameValueDescriptorProvider<S> nameValueDescriptorProvider) {
        return getInternal(nameValueDescriptorProvider).flatMap(ValueHolder::getValue);
    }

    public <S> Optional<S> getValueOrDefault(NameValueDescriptorProvider<S> nameValueDescriptorProvider) {
        return Optional.ofNullable(getInternal(nameValueDescriptorProvider).flatMap(ValueHolder::getValue).orElse(nameValueDescriptorProvider.getDefaultValue()));
    }

    @SafeVarargs
    public final void addAll(T... items) {
        this.addAll(Arrays.asList(items));
    }

    /**
     * Add or replace the specified items by name
     */
    @SafeVarargs
    public final void addOrReplace(T...items) {
        addOrReplace(Arrays.asList(items));
    }
    public final void addOrReplace(Collection<T> items) {
        items.forEach(item -> this.removeIf(i -> i.getName().equals(item.getName())));
        addAll(items);
    }

    public boolean has(NameHolder nameHolder) {
        return has(nameHolder.getName());
    }

    public boolean has(String name) {
        return this.stream().anyMatch(item -> item.getName().equals(name));
    }
}
