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
package org.openremote.model.asset;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.apache.commons.lang3.StringUtils;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ModelDescriptor;
import org.openremote.model.value.NameHolder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

/**
 * Describes a type of {@link Asset}; the {@link #getName()} must be unique within the context of the manager within
 * which the Asset resides; as {@link #getName()} is just {@link Class#getSimpleName()} of {@link #getType()} it is
 * important that Asset class names (excluding the package) do not clash
 * <p>
 * Each {@link AssetDescriptor} will discover its' own {@link AttributeDescriptor}s using reflection (see
 * {@link #getAttributeDescriptors}) by looking for static public fields of type {@link
 * AttributeDescriptor} with the {@link ModelDescriptor} on its' own class and on all super classes up to {@link
 * Asset}, this way asset types inherit {@link AttributeDescriptor}s, and an inherited {@link AttributeDescriptor}
 * cannot be overridden, any attempt to override will result in an {@link IllegalStateException}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "descriptorType")
@JsonTypeName("asset")
public class AssetDescriptor<T extends Asset> implements NameHolder {

    protected String name;
    protected Class<T> type;
    protected AttributeDescriptor<?>[] attributeDescriptors;
    protected String icon;
    protected String colour;

    public AssetDescriptor(String icon, String colour, Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors) {
        this.name = type.getSimpleName();
        this.icon = icon;
        this.colour = colour;
        this.type = type;
        this.attributeDescriptors = extractAttributeDescriptors(type, additionalAttributeDescriptors);
    }
    public AssetDescriptor(String icon, String colour, Class<T> type) {
        this(icon, colour, type, null);
    }

    /**
     * The unique name of this descriptor and corresponds to the simple class name of {@link #getType()}.
     */
    @Override
    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * {@link AttributeDescriptor}s are extracted automatically by traversing the type hierarchy starting at the
     * specified type and working up to the {@link Asset} base type; {@link AttributeDescriptor}s must be specified
     * as public static fields to be recognised.
     */
    public AttributeDescriptor<?>[] getAttributeDescriptors() {
        return attributeDescriptors;
    }

    public String getIcon() {
        return icon;
    }

    public String getColour() {
        return colour;
    }

    protected static <T extends Asset> AttributeDescriptor<?>[] extractAttributeDescriptors(Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors) throws IllegalArgumentException, IllegalStateException {
        Map<String, AttributeDescriptor<?>> descriptors = new HashMap<>();
        Class<?> currentType = type;

        Consumer<AttributeDescriptor<?>> descriptorConsumer = attributeDescriptor -> {
            if (descriptors.containsKey(attributeDescriptor.getName())) {
                throw new IllegalArgumentException("Duplicate attribute descriptor name '" + attributeDescriptor.getName() + "' for asset type hierarchy: " + type.getName());
            }
            descriptors.put(attributeDescriptor.getName(), attributeDescriptor);
        };

        while (currentType != Object.class) {
            Arrays.stream(type.getDeclaredFields())
                .filter(field ->
                    field.getType() == AttributeDescriptor.class && isStatic(field.getModifiers()) && isPublic(field.getModifiers()) && field.getDeclaredAnnotation(ModelDescriptor.class) != null)
                .map(descriptorField -> {
                    try {
                        AttributeDescriptor<?> descriptor = (AttributeDescriptor<?>)descriptorField.get(null);
                        // Check for corresponding getter
                        String pascalCaseName = StringUtils.capitalize(descriptor.getName());
                        Map<String, Method> getterMap = Arrays.stream(type.getDeclaredMethods()).filter(AssetDescriptor::isGetter).collect(Collectors.toMap(Method::getName, Function.identity()));
                        Method method = getterMap.containsKey("get" + pascalCaseName) ? getterMap.get("get" + pascalCaseName) : getterMap.get("is" + pascalCaseName);
                        if (method == null || method.getReturnType() != descriptor.getValueType().getType()) {
                            throw new IllegalArgumentException("Attribute descriptor '" + descriptor.getName() + "' doesn't have a corresponding getter in asset class: " + type.getName());
                        }
                        return descriptor;
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("Failed to extract attribute descriptors from asset class: " + type.getName(), e);
                    }
                }).forEach(descriptorConsumer);
            currentType = currentType.getSuperclass();
        }

        if (additionalAttributeDescriptors != null) {
            Arrays.stream(additionalAttributeDescriptors).forEach(descriptorConsumer);
        }

        return descriptors.values().toArray(new AttributeDescriptor[0]);
    }

    protected static boolean isGetter(Method method) {
        if (Modifier.isPublic(method.getModifiers()) &&
            method.getParameterTypes().length == 0) {
            if (method.getName().matches("^get[A-Z].*") &&
                !method.getReturnType().equals(void.class))
                return true;
            if (method.getName().matches("^is[A-Z].*") &&
                method.getReturnType().equals(boolean.class))
                return true;
        }
        return false;
    }
}
