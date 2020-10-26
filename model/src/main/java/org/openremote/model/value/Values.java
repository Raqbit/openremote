/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openremote.model.value;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.jackson.ORModelModule;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for working with values
 */
public class Values {

    public static final ObjectMapper JSON = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
        .registerModule(new ORModelModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule())
        .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

    public static final String NULL_LITERAL = "null";

    public static Value parseOrNull(String jsonString) {
        try {
            return parse(jsonString).orElse(null);
        }
        catch (Exception ignored) {}
        return null;
    }

    public static <T extends Value> Optional<T> parse(String jsonString) throws ValueException {
        return instance().parse(jsonString);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Value> Optional<T> cast(Class<T> type, Value value) {
        return value != null && value.getType().getModelType() == type
            ? Optional.of((T) value)
            : Optional.empty();
    }

    public static Optional<String> getString(Value value) {
        return cast(StringValue.class, value).map(StringValue::getString);
    }

    public static Optional<Double> getNumber(Value value) {
        return cast(NumberValue.class, value).map(NumberValue::getNumber);
    }

    public static Optional<Boolean> getBoolean(Value value) {
        return cast(BooleanValue.class, value).map(BooleanValue::getBoolean);
    }

    /**
     * Will attempt to coerce the value into a boolean (where it makes sense)
     */
    public static Optional<Boolean> getBooleanCoerced(Value value) {

        return convertToValue(value, BooleanValue.class)
            .map(BooleanValue::getBoolean);
    }

    /**
     * Attempts to coerce the value into an integer (where it makes sense)
     */
    public static Optional<Integer> getIntegerCoerced(Value value) {

        return convertToValue(value, NumberValue.class)
            .map(NumberValue::getNumber)
            .map(Double::intValue);
    }

    /**
     * Attempts to coerce the value into a long (where it makes sense)
     */
    public static Optional<Long> getLongCoerced(Value value) {

        return convertToValue(value, NumberValue.class)
            .map(NumberValue::getNumber)
            .map(Double::longValue);
    }

    public static Optional<ObjectValue> getObject(Value value) {
        return cast(ObjectValue.class, value);
    }

    public static Optional<ArrayValue> getArray(Value value) {
        return cast(ArrayValue.class, value);
    }

    public static <T extends Value> Optional<List<T>> getArrayElements(ArrayValue arrayValue,
                                                                       Class<T> elementType,
                                                                       boolean throwOnError,
                                                                       boolean includeNulls) throws ClassCastException {
        return getArrayElements(arrayValue, elementType, throwOnError, includeNulls, value -> value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Value, U> Optional<List<U>> getArrayElements(ArrayValue arrayValue,
                                                                          Class<T> elementType,
                                                                          boolean throwOnError,
                                                                          boolean includeNulls,
                                                                          Function<T, U> converter)
        throws ClassCastException, IllegalArgumentException {

        if (arrayValue == null || arrayValue.isEmpty() || elementType == null) {
            return Optional.empty();
        }

        if (converter == null) {
            if (throwOnError) {
                throw new IllegalArgumentException("Converter cannot be null");
            }
            return Optional.empty();
        }

        Stream<Value> values = arrayValue.stream();
        if (!throwOnError) {
            values = values.filter(value -> value != null && value.getType().getModelType() == elementType);
        }

        Stream<U> stream = values.map(value -> (T)value).map(converter);

        if (!includeNulls) {
            stream = stream.filter(Objects::nonNull);
        }

        return Optional.of(stream.collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Value> Optional<T> getMetaItemValueOrThrow(Attribute attribute,
                                                                        MetaItemDescriptor metaItemDescriptor,
                                                                        boolean throwIfMetaMissing,
                                                                        boolean throwIfValueMissing)
        throws IllegalArgumentException {
        return getMetaItemValueOrThrow(
                attribute,
                metaItemDescriptor.getUrn(),
                metaItemDescriptor.getValueType().getModelType(),
                throwIfMetaMissing,
                throwIfValueMissing);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Value> Optional<T> getMetaItemValueOrThrow(Attribute attribute,
                                                                        String name,
                                                                        Class<T> valueClazz,
                                                                        boolean throwIfMetaMissing,
                                                                        boolean throwIfValueMissing)
        throws IllegalArgumentException {

        Optional<MetaItem> metaItem = attribute.getMetaItem(name);

        if (!metaItem.isPresent()) {
            if (throwIfMetaMissing) {
                throw new IllegalArgumentException("Required meta item is missing: " + name);
            }

            return Optional.empty();
        }

        Optional<Value> value = metaItem.get().getValue();

        if (!value.isPresent()) {
            if (throwIfValueMissing) {
                throw new IllegalArgumentException("Meta item value is missing: " + name);
            }
            return Optional.empty();
        }

        if (valueClazz != Value.class && value.get().getType().getModelType() != valueClazz) {
            throw new IllegalArgumentException("Meta item value is of incorrect type: expected="
                + valueClazz.getName() + "; actual=" + value.get().getType().getModelType().getName());
        }

        return Optional.of((T)value.get());
    }

    public static <T extends Value> Optional<T> convertToValue(Value value, Class<T> toType) {
        return convertToValue(value, ValueType.fromModelType(toType));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Value> Optional<T> convertToValue(Value value, ValueType toType) {
        if (value == null) {
            return Optional.empty();
        }

        T outputValue = null;

        if (toType == value.getType()) {
            outputValue = (T) value;
        } else if (toType == ValueType.STRING) {
            outputValue = (T) Values.create(value.toString());
        } else {
            switch (value.getType()) {

                case STRING:
                    switch (toType) {

                        case NUMBER:
                            try {
                                double dbl = Double.parseDouble(value.toString());
                                outputValue = (T) Values.create(dbl);
                            } catch (NumberFormatException e) {
                                return Optional.empty();
                            }
                            break;
                        case BOOLEAN:
                            if ("ON".equalsIgnoreCase(value.toString())) {
                                outputValue = (T) Values.create(true);
                            } else if ("OFF".equalsIgnoreCase(value.toString())) {
                                outputValue = (T) Values.create(false);
                            } else {
                                outputValue = (T) Values.create(Boolean.parseBoolean(value.toString()));
                            }
                            break;
                        case ARRAY:
                        case OBJECT:
                            try {
                                outputValue = (T) Values.parse(value.toString()).orElse(null);
                            } catch (ValueException e) {
                                return Optional.empty();
                            }
                    }
                    break;
                case NUMBER:
                    switch (toType) {

                        case BOOLEAN:
                            outputValue = (T) Values.getNumber(value)
                                .map(Double::intValue)
                                .map(i -> i == 0 ? Boolean.FALSE : i == 1 ? Boolean.TRUE : null)
                                .map(Values::create)
                                .orElse(null);
                            break;
                    }
                    break;
                case BOOLEAN:

                    switch (toType) {

                        case NUMBER:
                            outputValue = (T) Values.create(((BooleanValue)value).getBoolean() ? 1 : 0);
                            break;
                    }
                    break;
                case ARRAY:

                    // Only works when the array has a single value
                    ArrayValue arrayValue = (ArrayValue)value;
                    if (arrayValue.length() == 1) {

                        Value firstValue = arrayValue.get(0).orElse(null);

                        if (firstValue != null) {
                            return convertToValue(firstValue, toType);
                        }
                    }
            }
        }

        return Optional.ofNullable(outputValue);
    }

    public static <T extends Value> Optional<T> convertToValue(Object object, ObjectWriter writer) {
        try {
            return Optional.of(convertToValueOrThrow(object, writer));
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }

    public static <T extends Value> T convertToValueOrThrow(Object object, ObjectWriter writer) throws IOException {
        if (object == null || writer == null) {
            throw new IllegalArgumentException("Value and writer must be defined");
        }

        Value v;
        v = parse(writer.writeValueAsString(object)).orElse(null);
        return (T)v;
    }

    public static <T> Optional<T> convertFromValue(Value value, Class<T> clazz, ObjectReader reader) {
        try {
            return Optional.of(convertFromValueOrThrow(value, clazz, reader));
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }

    public static <T> T convertFromValueOrThrow(Value value, Class<T> clazz, ObjectReader reader) throws IOException {
        if (value == null || clazz == null || reader == null) {
            throw new IllegalArgumentException("Value, class and reader must be defined");
        }

        String str = value.toJson();
        return reader.forType(clazz).readValue(str);
    }

    public static <T> T[] reverseArray(T[] array, Class<T> clazz) {
        if (array == null) {
            return null;
        }
        T[] newArray = createArray(array.length, clazz);
        int j = 0;
        for (int i=array.length; i>0; i--) {
            newArray[j] = array[i-1];
            j++;
        }
        return newArray;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(int size, Class<T> clazz) {
        return (T[]) Array.newInstance(clazz, size);
    }

    /**
     * @param o A timestamp string as 'HH:mm:ss' or 'HH:mm'.
     * @return Epoch time or 0 if there is a problem parsing the timestamp string.
     */
    public static long parseTimestamp(Object o) {
        String timestamp = "";
        try {
            timestamp = o.toString();
        }catch (Exception e){
            return (0L);
        }
        SimpleDateFormat sdf;
        if (timestamp.length() == 8) {
            sdf = new SimpleDateFormat("HH:mm:ss");
        } else if (timestamp.length() == 5) {
            sdf = new SimpleDateFormat("HH:mm");
        } else {
            return (0L);
        }
        try {
            return (sdf.parse(timestamp).getTime());
        } catch (ParseException e) {
            return (0L);
        }
    }

    /**
     * @param timestamp Epoch time
     * @return The timestamp formatted as 'HH:mm' or <code>null</code> if the timestamp is <= 0.
     */
    public static String formatTimestamp(long timestamp) {
        if (timestamp <= 0)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return (sdf.format(new Date(timestamp)));
    }

    /**
     * @param timestamp Epoch time
     * @return The timestamp formatted as 'HH:mm:ss' or <code>null</code> if the timestamp is <= 0.
     */
    public static String formatTimestampWithSeconds(long timestamp) {
        if (timestamp <= 0)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return (sdf.format(new Date(timestamp)));
    }

    /**
     * @param timestamp Epoch time
     * @return The timestamp formatted as 'EEE' or <code>null</code> if the timestamp is <= 0.
     */
    public static String formatDayOfWeek(long timestamp) {
        if (timestamp <= 0)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        return (sdf.format(new Date(timestamp)));
    }

    /**
     * @param o       A timestamp string as 'HH:mm' or '-'.
     * @param minutes The minutes to increment/decrement from timestamp.
     * @return Timestamp string as 'HH:mm', modified with the given minutes or the current time + 60 minutes if
     * the given timestamp was '-' or the given timestamp couldn't be parsed.
     */
    public static String shiftTime(Object o, int minutes) {
        String timestamp = o.toString();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = null;
        if (timestamp != null && timestamp.length() >= 1 && timestamp.substring(0, 1).equals("-")) {
            date = new Date();
            date.setTime(date.getTime() + 60 * 60000);
        } else {
            try {
                date = sdf.parse(timestamp);
                date.setTime(date.getTime() + minutes * 60000);
            } catch (ParseException ex) {
                date = new Date();
                date.setTime(date.getTime() + 60 * 60000);
            }
        }
        return (sdf.format(date));
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @SafeVarargs
    public static <T> List<T> joinCollections(Collection<T>...collections) {
        if (collections == null || collections.length == 0) {
            return Collections.emptyList();
        }

        List<T> newCollection = null;

        for (Collection<T> collection : collections) {
            if (collection == null) {
                continue;
            }

            if (newCollection == null) {
                newCollection = new ArrayList<>(collection);
            } else {
                newCollection.addAll(collection);
            }
        }
        return newCollection;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Class<T> targetType, Object object) {
        Map<String, Object> props = JSON.convertValue(object, Map.class);
        return JSON.convertValue(props, targetType);
    }
}
