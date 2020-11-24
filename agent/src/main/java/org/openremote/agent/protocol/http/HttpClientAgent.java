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
package org.openremote.agent.protocol.http;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.MetaItemDescriptor;
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueType;

import java.util.Optional;

public class HttpClientAgent extends Agent {

    public static final ValueDescriptor<HttpMethod> VALUE_HTTP_METHOD = new ValueDescriptor<>("HTTP method", HttpMethod.class);

    public static final AttributeDescriptor<String> BASE_URI = new AttributeDescriptor<>("baseURI", ValueType.STRING);
    public static final AttributeDescriptor<Boolean> FOLLOW_REDIRECTS = new AttributeDescriptor<>("followRedirects", ValueType.BOOLEAN);
    public static final AttributeDescriptor<ValueType.MultivaluedStringMap> REQUEST_HEADERS = new AttributeDescriptor<>("requestHeaders", ValueType.MULTIVALUED_STRING_MAP);
    public static final AttributeDescriptor<ValueType.MultivaluedStringMap> REQUEST_QUERY_PARAMETERS = new AttributeDescriptor<>("requestQueryParameters", ValueType.MULTIVALUED_STRING_MAP);
    public static final AttributeDescriptor<Integer> REQUEST_TIMEOUT_MILLIS = new AttributeDescriptor<>("requestTimeoutMillis", ValueType.POSITIVE_INTEGER);

    public static final MetaItemDescriptor<ValueType.MultivaluedStringMap> META_REQUEST_HEADERS = new MetaItemDescriptor<>("requestHeaders", ValueType.MULTIVALUED_STRING_MAP);
    public static final MetaItemDescriptor<ValueType.MultivaluedStringMap> META_REQUEST_QUERY_PARAMETERS = new MetaItemDescriptor<>("requestQueryParameters", ValueType.MULTIVALUED_STRING_MAP);
    public static final MetaItemDescriptor<Integer> META_REQUEST_POLLING_MILLIS = new MetaItemDescriptor<>("requestPollingMillis", ValueType.POSITIVE_INTEGER);
    public static final MetaItemDescriptor<Boolean> META_REQUEST_PAGING_MODE = new MetaItemDescriptor<>("requestPagingMode", ValueType.BOOLEAN);
    public static final MetaItemDescriptor<String> META_REQUEST_PATH = new MetaItemDescriptor<>("requestPath", ValueType.STRING);
    public static final MetaItemDescriptor<HttpMethod> META_REQUEST_METHOD = new MetaItemDescriptor<>("requestMethod", VALUE_HTTP_METHOD);
    public static final MetaItemDescriptor<String> META_REQUEST_CONTENT_TYPE = new MetaItemDescriptor<>("requestContentType", ValueType.STRING);
    public static final MetaItemDescriptor<String> META_POLLING_ATTRIBUTE = new MetaItemDescriptor<>("pollingAttribute", ValueType.STRING);

    public HttpClientAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends HttpClientAgent, S extends Protocol<T>> HttpClientAgent(String name, AgentDescriptor<T, S> descriptor) {
        super(name, descriptor);
    }

    public Optional<String> getBaseURI() {
        return getAttributes().getValue(BASE_URI);
    }

    public Optional<Boolean> getFollowRedirects() {
        return getAttributes().getValue(FOLLOW_REDIRECTS);
    }

    public Optional<ValueType.MultivaluedStringMap> getRequestHeaders() {
        return getAttributes().getValue(REQUEST_HEADERS);
    }

    public Optional<ValueType.MultivaluedStringMap> getRequestQueryParameters() {
        return getAttributes().getValue(REQUEST_QUERY_PARAMETERS);
    }

    public Optional<Integer> getRequestTimeoutMillis() {
        return getAttributes().getValue(REQUEST_TIMEOUT_MILLIS);
    }

    @Override
    public Protocol getProtocolInstance() {
        return new HttpClientProtocol(this);
    }
}
