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
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.asset.impl.ElectricityChargerAsset;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueType;

import java.util.Optional;

public class HttpClientAgent extends Agent<HttpClientAgent, HttpClientProtocol, HttpClientAgent.HttpClientAgentLink> {

    public static class HttpClientAgentLink extends AgentLink {

        protected ValueType.MultivaluedStringMap headers;
        protected ValueType.MultivaluedStringMap queryParameters;
        protected Integer pollingMillis;
        protected Boolean pagingMode;
        protected String path;
        protected HttpMethod method;
        protected String contentType;
        protected String pollingAttribute;

        public HttpClientAgentLink(String id) {
            super(id);
        }

        public Optional<ValueType.MultivaluedStringMap> getHeaders() {
            return Optional.ofNullable(headers);
        }

        public void setHeaders(ValueType.MultivaluedStringMap headers) {
            this.headers = headers;
        }

        public Optional<ValueType.MultivaluedStringMap> getQueryParameters() {
            return Optional.ofNullable(queryParameters);
        }

        public void setQueryParameters(ValueType.MultivaluedStringMap queryParameters) {
            this.queryParameters = queryParameters;
        }

        public Optional<Integer> getPollingMillis() {
            return Optional.ofNullable(pollingMillis);
        }

        public void setPollingMillis(Integer pollingMillis) {
            this.pollingMillis = pollingMillis;
        }

        public Optional<Boolean> getPagingMode() {
            return Optional.ofNullable(pagingMode);
        }

        public void setPagingMode(Boolean pagingMode) {
            this.pagingMode = pagingMode;
        }

        public Optional<String> getPath() {
            return Optional.ofNullable(path);
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Optional<HttpMethod> getMethod() {
            return Optional.ofNullable(method);
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        public Optional<String> getContentType() {
            return Optional.ofNullable(contentType);
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Optional<String> getPollingAttribute() {
            return Optional.ofNullable(pollingAttribute);
        }

        public void setPollingAttribute(String pollingAttribute) {
            this.pollingAttribute = pollingAttribute;
        }
    }

    public static final ValueDescriptor<HttpMethod> VALUE_HTTP_METHOD = new ValueDescriptor<>("HTTP method", HttpMethod.class);

    public static final AttributeDescriptor<String> BASE_URI = new AttributeDescriptor<>("baseURI", ValueType.STRING);
    public static final AttributeDescriptor<Boolean> FOLLOW_REDIRECTS = new AttributeDescriptor<>("followRedirects", ValueType.BOOLEAN);
    public static final AttributeDescriptor<ValueType.MultivaluedStringMap> REQUEST_HEADERS = new AttributeDescriptor<>("requestHeaders", ValueType.MULTIVALUED_STRING_MAP);
    public static final AttributeDescriptor<ValueType.MultivaluedStringMap> REQUEST_QUERY_PARAMETERS = new AttributeDescriptor<>("requestQueryParameters", ValueType.MULTIVALUED_STRING_MAP);
    public static final AttributeDescriptor<Integer> REQUEST_TIMEOUT_MILLIS = new AttributeDescriptor<>("requestTimeoutMillis", ValueType.POSITIVE_INTEGER);

    public static final AgentDescriptor<HttpClientAgent, HttpClientProtocol, HttpClientAgentLink> DESCRIPTOR = new AgentDescriptor<>(
        HttpClientAgent.class, HttpClientProtocol.class, HttpClientAgentLink.class
    );

    public HttpClientAgent(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getBaseURI() {
        return getAttributes().getValue(BASE_URI);
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpClientAgent> T setBaseURI(String value) {
        getAttributes().getOrCreate(BASE_URI).setValue(value);
        return (T)this;
    }

    public Optional<Boolean> getFollowRedirects() {
        return getAttributes().getValue(FOLLOW_REDIRECTS);
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpClientAgent> T setFollowRedirects(Boolean value) {
        getAttributes().getOrCreate(FOLLOW_REDIRECTS).setValue(value);
        return (T)this;
    }

    public Optional<ValueType.MultivaluedStringMap> getRequestHeaders() {
        return getAttributes().getValue(REQUEST_HEADERS);
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpClientAgent> T setRequestHeaders(ValueType.MultivaluedStringMap value) {
        getAttributes().getOrCreate(REQUEST_HEADERS).setValue(value);
        return (T)this;
    }

    public Optional<ValueType.MultivaluedStringMap> getRequestQueryParameters() {
        return getAttributes().getValue(REQUEST_QUERY_PARAMETERS);
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpClientAgent> T setRequestQueryParameters(ValueType.MultivaluedStringMap value) {
        getAttributes().getOrCreate(REQUEST_QUERY_PARAMETERS).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getRequestTimeoutMillis() {
        return getAttributes().getValue(REQUEST_TIMEOUT_MILLIS);
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpClientAgent> T setRequestTimeoutMillis(Integer value) {
        getAttributes().getOrCreate(REQUEST_TIMEOUT_MILLIS).setValue(value);
        return (T)this;
    }

    @Override
    public HttpClientProtocol getProtocolInstance() {
        return new HttpClientProtocol(this);
    }
}
