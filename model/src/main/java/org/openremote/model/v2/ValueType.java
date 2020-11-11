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
import org.openremote.model.Constants;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.AttributeExecuteStatus;
import org.openremote.model.attribute.AttributeLink;
import org.openremote.model.attribute.AttributeRef;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.auth.OAuthGrant;
import org.openremote.model.auth.UsernamePassword;
import org.openremote.model.calendar.CalendarEvent;
import org.openremote.model.console.ConsoleProviders;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.util.TimeUtil;
import org.openremote.model.value.ColorRGB;

import javax.validation.constraints.*;

public final class ValueType {

    public static final ValueDescriptor<Boolean> BOOLEAN = new ValueDescriptor<>("Boolean", Boolean.class);

    public static final ValueDescriptor<Integer> INTEGER = new ValueDescriptor<>("Integer", Integer.class);

    public static final ValueDescriptor<Long> LONG = new ValueDescriptor<>("Long", Long.class);

    public static final ValueDescriptor<Double> NUMBER = new ValueDescriptor<>("Number", Double.class);

    @Min(1)
    public static final ValueDescriptor<String> STRING = new ValueDescriptor<>("String", String.class);

    public static final ValueDescriptor<ArrayNode> ARRAY = new ValueDescriptor<>("Array", ArrayNode.class);

    public static final ValueDescriptor<ObjectNode> OBJECT = new ValueDescriptor<>("Object", ObjectNode.class);

    @Min(0)
    public static final ValueDescriptor<Integer> POSITIVE_INTEGER = new ValueDescriptor<>("Positive integer", Integer.class);

    @DecimalMin("0.0")
    public static final ValueDescriptor<Double> POSITIVE_NUMBER = new ValueDescriptor<>("Positive number", Double.class);

    @Min(0)
    @Max(100)
    public static final ValueDescriptor<Integer> PERCENTAGE_INTEGER_0_100 = new ValueDescriptor<>("Positive integer 0-100", Integer.class,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_PERCENTAGE)
    );

    @Min(0)
    @Max(255)
    public static final ValueDescriptor<Integer> BYTE = new ValueDescriptor<>("Byte", Integer.class);

    public static final ValueDescriptor<Integer> TIMESTAMP = new ValueDescriptor<>("Timestamp", Integer.class);

    @Pattern(regexp = "^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:Z|[+-][01]\\d:[0-5]\\d)$")
    public static final ValueDescriptor<String> TIMESTAMP_ISO8601 = new ValueDescriptor<>("Timestamp ISO8601", String.class);

    public static final ValueDescriptor<Integer> DURATION = new ValueDescriptor<>("Duration", Integer.class,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_TIME_SECOND)
    );

    @Pattern(regexp = TimeUtil.DURATION_REGEXP)
    public static final ValueDescriptor<String> DURATION_STRING = new ValueDescriptor<>("Duration string", String.class);

    public static final ValueDescriptor<String> PASSWORD = new ValueDescriptor<>("Password", String.class,
        new MetaItem<>(MetaItemType.SECRET)
    );

    @Pattern(regexp = "[a-fA-F0-9]{6}")
    public static final ValueDescriptor<String> COLOUR_HEX = new ValueDescriptor<>("Colour HEX", String.class);

    @Pattern(regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    public static final ValueDescriptor<String> EMAIL = new ValueDescriptor<>("Email", String.class);

    @Pattern(regexp = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}")
    public static final ValueDescriptor<String> UUID = new ValueDescriptor<>("UUID", String.class);

    @Size(min = 22, max = 22)
    public static final ValueDescriptor<String> ASSET_ID = new ValueDescriptor<>("Asset ID", String.class);

    @Min(0)
    @Max(359)
    public static final ValueDescriptor<Integer> DIRECTION = new ValueDescriptor<>("Direction", Integer.class);

    @Min(1)
    @Max(65536)
    public static final ValueDescriptor<Integer> PORT = new ValueDescriptor<>("Port number", Integer.class);

    @NotNull
    @Pattern(regexp = "(^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$)|(^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$)")
    public static final ValueDescriptor<String> HOSTNAME_OR_IP_ADDRESS = new ValueDescriptor<>("Host", String.class);

    @NotNull
    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
    public static final ValueDescriptor<String> IP_ADDRESS = new ValueDescriptor<>("IP address", String.class);

    public static final ValueDescriptor<AttributeLink> ATTRIBUTE_LINK = new ValueDescriptor<>("Attribute link", AttributeLink.class);

    public static final ValueDescriptor<AttributeRef> ATTRIBUTE_REF = new ValueDescriptor<>("Attribute reference", AttributeRef.class);

    public static final ValueDescriptor<GeoJSONPoint> GEO_JSON_POINT = new ValueDescriptor<>("GeoJSON point", GeoJSONPoint.class);

    public static final ValueDescriptor<CalendarEvent> CALENDAR_EVENT = new ValueDescriptor<>("Calendar event", CalendarEvent.class);

    public static final ValueDescriptor<AttributeExecuteStatus> EXECUTION_STATUS = new ValueDescriptor<>("Execution status", AttributeExecuteStatus.class);

    public static final ValueDescriptor<ConnectionStatus> CONNECTION_STATUS = new ValueDescriptor<>("Connection status", ConnectionStatus.class);

    public static final ValueDescriptor<ConsoleProviders> CONSOLE_PROVIDERS = new ValueDescriptor<>("Console providers", ConsoleProviders.class);

    public static final ValueDescriptor<ColorRGB> COLOUR_RGB = new ValueDescriptor<>("Colour RGB", ColorRGB.class);

    public static final ValueDescriptor<OAuthGrant> OAUTH_GRANT = new ValueDescriptor<>("OAuth Grant", OAuthGrant.class);

    public static final ValueDescriptor<UsernamePassword> USERNAME_AND_PASSWORD = new ValueDescriptor<>("Username and password", UsernamePassword.class);

    protected ValueType() {
    }
}
