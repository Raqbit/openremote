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
package org.openremote.model.v2;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.model.attribute.AttributeLink;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public final class ValueTypes {

    public static final ValueDescriptor<Boolean> BOOLEAN = new ValueDescriptor<>("Boolean", Boolean.class);

    public static final ValueDescriptor<Integer> INTEGER = new ValueDescriptor<>("Integer", Integer.class);

    public static final ValueDescriptor<String> STRING = new ValueDescriptor<>("String", String.class);

    public static final ValueDescriptor<ArrayNode> ARRAY = new ValueDescriptor<>("Array", ArrayNode.class);

    public static final ValueDescriptor<ObjectNode> OBJECT = new ValueDescriptor<>("Array", ObjectNode.class);

    @Min(0)
    public static final ValueDescriptor<Integer> POSITIVE_INTEGER = new ValueDescriptor<>("PositiveInteger", Integer.class);

    public static final ValueDescriptor<String> PASSWORD = new ValueDescriptor<>("Password", String.class, new MetaDescriptor[] {
        MetaTypes.SECRET
    });

    @Size(min = 22, max = 22)
    public static final ValueDescriptor<String> ASSET_ID = new ValueDescriptor<>("AssetId", String.class);


    public static final ValueDescriptor<AttributeLink> ATTRIBUTE_LINK = new ValueDescriptor<>("AttributeLink", AttributeLink.class);

    protected ValueTypes() {
    }
}
