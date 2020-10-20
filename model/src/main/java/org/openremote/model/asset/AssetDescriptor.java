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

import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.AttributeDescriptorProvider;
import org.openremote.model.v2.NameProvider;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

/**
 * Describes a type of {@link Asset}; the {@link #getType()} must be unique within the context of the manager within
 * which the Asset resides.
 * <p>
 * A custom project can add its own descriptors through {@link org.openremote.model.asset.AssetModelProvider}.
 * <p>
 */
public class AssetDescriptor<T extends Asset> implements NameProvider, AttributeDescriptorProvider {

    protected String name;
    protected Class<T> type;
    protected org.openremote.model.v2.AttributeDescriptor<?>[] attributeDescriptors;
    protected String icon;
    protected String colour;

    /**
     * {@link AttributeDescriptor}s are extracted automatically by traversing the type hierarchy starting at the
     * specified type and working up to the {@link Asset} base type; {@link AttributeDescriptor}s must be specified
     * as public static fields to be recognised.
     * <p>
     * Additional {@link AttributeDescriptor}s can be supplied where required for a given type, this is useful for
     * custom asset types that aren't backed by a class but in general well known {@link Asset} types should have a
     * corresponding class see {@link Asset} for more details.
     */
    public AssetDescriptor(String name, String icon, String colour, Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors) {
        this.name = name;
        this.icon = icon;
        this.colour = colour;
        this.type = type;
        this.attributeDescriptors = extractAttributeDescriptors(type, additionalAttributeDescriptors);
    }
    public AssetDescriptor(String name, String icon, String colour, Class<T> type) {
        this(name, icon, colour, type, null);
    }

    @Override
    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public AttributeDescriptor<?>[] getAttributeDescriptors() {
        return attributeDescriptors;
    }

    public String getIcon() {
        return icon;
    }

    public String getColour() {
        return colour;
    }

    public static <T extends Asset> AttributeDescriptor<?>[] extractAttributeDescriptors(Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors) {
        Map<String, AttributeDescriptor<?>> descriptors = Arrays.stream(type.getDeclaredFields())
            .filter(field ->
                field.getType() == AttributeDescriptor.class && isStatic(field.getModifiers()) && isPublic(field.getModifiers()))
            .map(descriptorField -> {
                try {
                    return (AttributeDescriptor<?>)descriptorField.get(null);
                } catch (IllegalAccessException e) {
                   throw new IllegalArgumentException("Failed to extract attribute descriptors from asset class: " + type.getName(), e);
                }
            }).collect(Collectors.toMap(AttributeDescriptor::getName, Function.identity()));

        if (additionalAttributeDescriptors != null) {
            Arrays.stream(additionalAttributeDescriptors).forEach(additionalDescriptor -> {
                if (descriptors.containsKey(additionalDescriptor.getName())) {
                    throw new IllegalArgumentException("Additional attribute descriptor conflicts with attribute descriptor extract from asset type hierarchy: " + additionalDescriptor.getName());
                }
                descriptors.put(additionalDescriptor.getName(), additionalDescriptor);
            });
        }

        return descriptors.values().toArray(new AttributeDescriptor[0]);
    }
}
