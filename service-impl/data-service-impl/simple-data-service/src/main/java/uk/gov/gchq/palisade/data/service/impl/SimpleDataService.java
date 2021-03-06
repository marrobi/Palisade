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

package uk.gov.gchq.palisade.data.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.reader.DataReader;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.GetDataRequestConfig;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A SimpleDataService is a simple implementation of {@link DataService} that
 * applies the required policy rules before returning data.
 * </p>
 * <p>
 * It should only be used for examples/demos.
 * </p>
 * <p>
 * It does not currently apply any validation of the {@link ReadRequest}, so users are able to
 * request any data they want, which could be different to the original data they requested from Palisade.
 * </p>
 */
public class SimpleDataService implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDataService.class);

    private static final String PALISADE_IMPL_KEY = "sds.svc.palisade.svc";
    private static final String READER_IMPL_KEY = "sds.svc.reader.svc";

    private PalisadeService palisadeService;
    private DataReader reader;

    public SimpleDataService() {
    }

    public SimpleDataService palisadeService(final PalisadeService palisadeService) {
        requireNonNull(palisadeService, "The palisade service cannot be set to null.");
        this.palisadeService = palisadeService;
        return this;
    }

    public SimpleDataService reader(final DataReader reader) {
        requireNonNull(reader, "The data reader cannot be set to null.");
        this.reader = reader;
        return this;
    }

    @Override
    public CompletableFuture<ReadResponse> read(final ReadRequest request) {
        requireNonNull(request, "The request cannot be null.");
        LOGGER.debug("Creating async read: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Starting to read: {}", request);
            final GetDataRequestConfig getConfig = new GetDataRequestConfig()
                    .requestId(request.getRequestId())
                    .resource(request.getResource());
            LOGGER.debug("Calling palisade service with: {}", getConfig);
            final DataRequestConfig config = palisadeService.getDataRequestConfig(getConfig).join();
            LOGGER.debug("Palisade service returned: {}", config);

            final DataReaderRequest readerRequest = new DataReaderRequest()
                    .resource(request.getResource())
                    .user(config.getUser())
                    .context(config.getContext())
                    .rules(config.getRules().get(request.getResource()));

            LOGGER.debug("Calling reader with: {}", readerRequest);
            final DataReaderResponse readerResult = reader.read(readerRequest);
            LOGGER.debug("Reader returned: {}", readerResult);

            final ReadResponse response = new ReadResponse();
            if (null != readerResult.getData()) {
                response.data(readerResult.getData());
            }
            LOGGER.debug("Returning from read: {}", response);
            return response;
        });
    }

    public PalisadeService getPalisadeService() {
        requireNonNull(palisadeService, "The palisade service has not been set.");
        return palisadeService;
    }

    public void setPalisadeService(final PalisadeService palisadeService) {
        requireNonNull(palisadeService, "The palisade service cannot be set to null.");
        palisadeService(palisadeService);
    }

    @Override
    public void applyConfigFrom(final ServiceConfiguration config) throws NoConfigException {
        requireNonNull(config, "config");
        String serialisedPalisade = config.getOrDefault(PALISADE_IMPL_KEY, null);
        if (nonNull(serialisedPalisade)) {
            palisadeService = JSONSerialiser.deserialise(serialisedPalisade.getBytes(JSONSerialiser.UTF8), PalisadeService.class);
        } else {
            throw new NoConfigException("no service specified in configuration");
        }
        String serialisedReader = config.getOrDefault(READER_IMPL_KEY, null);
        if (nonNull(serialisedReader)) {
            reader = JSONSerialiser.deserialise(serialisedReader.getBytes(JSONSerialiser.UTF8), DataReader.class);
        } else {
            throw new NoConfigException("no service specified in configuration");
        }
    }

    @Override
    public void recordCurrentConfigTo(final ServiceConfiguration config) {
        requireNonNull(config, "config");
        config.put(DataService.class.getTypeName(), getClass().getTypeName());
        String serialised = new String(JSONSerialiser.serialise(palisadeService), JSONSerialiser.UTF8);
        config.put(PALISADE_IMPL_KEY, serialised);
        String serialisedReader = new String(JSONSerialiser.serialise(reader), JSONSerialiser.UTF8);
        config.put(READER_IMPL_KEY, serialisedReader);
    }

    public DataReader getReader() {
        requireNonNull(reader, "The data reader has not been set.");
        return reader;
    }

    public void setReader(final DataReader reader) {
        requireNonNull(reader, "The reader cannot be set to null.");
        reader(reader);
    }
}
