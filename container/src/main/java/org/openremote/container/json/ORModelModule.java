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
package org.openremote.container.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.asset.AssetDescriptorImpl;
import org.openremote.model.attribute.*;
import org.openremote.model.geo.Position;
import org.openremote.model.json.ValueJsonDeserializer;
import org.openremote.model.json.ValueJsonSerializer;
import org.openremote.model.util.AssetModelUtil;
import org.openremote.model.value.*;

import java.io.IOException;
import java.util.function.Function;

public class ORModelModule extends SimpleModule {

    public static class DescriptorDeserializer<T, U extends T> extends JsonDeserializer<T> {

        protected Function<String, T> descriptorNameFinder;
        protected Class<U> implClass;

        public DescriptorDeserializer(Function<String, T> descriptorNameFinder, Class<U> implClass) {
            this.descriptorNameFinder = descriptorNameFinder;
            this.implClass = implClass;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = p.getCodec().readTree(p);
            if (node instanceof TextNode) {
                return descriptorNameFinder.apply(node.textValue());
            } else {
                return p.getCodec().treeToValue(node, implClass);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public ORModelModule() {
        super("ORModelModule", new Version(1, 0, 0, "latest", null, null));
        ValueJsonSerializer valueSerializer = new ValueJsonSerializer();
        ValueJsonDeserializer valueDeserializer = new ValueJsonDeserializer();
        this.addSerializer(Value.class, valueSerializer);
        this.addDeserializer(Value.class, valueDeserializer);
        this.addDeserializer(ObjectValue.class, valueDeserializer);
        this.addDeserializer(ArrayValue.class, valueDeserializer);
        this.addDeserializer(StringValue.class, valueDeserializer);
        this.addDeserializer(NumberValue.class, valueDeserializer);
        this.addDeserializer(BooleanValue.class, valueDeserializer);
        this.addDeserializer(AssetDescriptor.class, new DescriptorDeserializer<>(
            name -> AssetModelUtil.getAssetDescriptor(name).orElse(null),
            AssetDescriptorImpl.class
        ));
        this.addDeserializer(AttributeDescriptor.class, new DescriptorDeserializer<>(
            name -> AssetModelUtil.getAttributeDescriptor(name).orElse(null),
            AttributeDescriptorImpl.class
        ));
        this.addDeserializer(AttributeValueDescriptor.class, new DescriptorDeserializer<>(
            name -> AssetModelUtil.getAttributeValueDescriptor(name).orElse(null),
            AttributeValueDescriptorImpl.class
        ));
        this.addDeserializer(MetaItemDescriptor.class, new DescriptorDeserializer<>(
            urn -> AssetModelUtil.getMetaItemDescriptor(urn).orElse(null),
            MetaItemDescriptorImpl.class
        ));
    }
}
