/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.rest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;

import static java.util.Objects.requireNonNull;

/**
 * A {@code ProxyRestConnectionDetail} is a {@link ConnectionDetail} that
 * takes a {@link ProxyRestService} {@link Class} and a url {@code String}.
 */
public class ProxyRestConnectionDetail implements ConnectionDetail {
    private Class<? extends ProxyRestService> serviceClass;
    private String url;

    public ProxyRestConnectionDetail() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends Service> S createService() {
        requireNonNull(serviceClass, "serviceClass is required");
        final ProxyRestService service;
        try {
            service = serviceClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create service " + serviceClass.getName(), e);
        }
        service.setBaseUrl(url);
        return (S) service;
    }

    public ProxyRestConnectionDetail serviceClass(final Class<? extends ProxyRestService> serviceClass) {
        requireNonNull(serviceClass, "The service class cannot be set to null.");
        this.serviceClass = serviceClass;
        return this;
    }

    public ProxyRestConnectionDetail url(final String url) {
        requireNonNull(url, "The url cannot be set to null.");
        this.url = url;
        return this;
    }

    public Class<? extends ProxyRestService> getServiceClass() {
        requireNonNull(serviceClass, "The service class has not been set.");
        return serviceClass;
    }

    public void setServiceClass(final Class<? extends ProxyRestService> serviceClass) {
        serviceClass(serviceClass);
    }

    public String getUrl() {
        requireNonNull(url, "The url has not been set.");
        return url;
    }

    public void setUrl(final String url) {
        url(url);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ProxyRestConnectionDetail that = (ProxyRestConnectionDetail) o;

        return new EqualsBuilder()
                .append(serviceClass, that.serviceClass)
                .append(url, that.url)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 37)
                .append(serviceClass)
                .append(url)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serviceClass", serviceClass)
                .append("url", url)
                .toString();
    }
}
