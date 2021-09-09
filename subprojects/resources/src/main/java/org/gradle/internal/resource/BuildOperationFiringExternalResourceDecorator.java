/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.resource;

import org.gradle.api.Action;
import org.gradle.api.resources.ResourceException;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.resource.metadata.ExternalResourceMetaData;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

public class BuildOperationFiringExternalResourceDecorator implements ExternalResource {
    private final ExternalResourceName resourceName;
    private final BuildOperationExecutor buildOperationExecutor;
    private final ExternalResource delegate;

    public BuildOperationFiringExternalResourceDecorator(ExternalResourceName resourceName, BuildOperationExecutor buildOperationExecutor, ExternalResource delegate) {
        this.resourceName = resourceName;
        this.buildOperationExecutor = buildOperationExecutor;
        this.delegate = delegate;
    }

    @Override
    public URI getURI() {
        return delegate.getURI();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public ExternalResourceMetaData getMetaData() {
        return delegate.getMetaData();
    }

    @Nullable
    @Override
    public List<String> list() throws ResourceException {
        return delegate.list();
    }

    @Override
    public ExternalResourceWriteResult put(final ReadableContent source) throws ResourceException {
        return delegate.put(source);
    }

    @Override
    public ExternalResourceReadResult<Void> writeToIfPresent(File destination) throws ResourceException {
        return delegate.writeToIfPresent(destination);
    }

    @Override
    public ExternalResourceReadResult<Void> writeTo(File destination) throws ResourceException {
        return delegate.writeTo(destination);
    }

    @Override
    public ExternalResourceReadResult<Void> writeTo(OutputStream destination) throws ResourceException {
        return delegate.writeTo(destination);
    }

    @Override
    public ExternalResourceReadResult<Void> withContent(final Action<? super InputStream> readAction) throws ResourceException {
        return withContent(readAction);
    }

    @Override
    public <T> ExternalResourceReadResult<T> withContent(final ContentAction<? extends T> readAction) throws ResourceException {
        return delegate.withContent(readAction);
    }

    @Override
    public <T> ExternalResourceReadResult<T> withContent(final ContentAndMetadataAction<? extends T> readAction) throws ResourceException {
        return delegate.withContent(readAction);
    }

    @Nullable
    @Override
    public <T> ExternalResourceReadResult<T> withContentIfPresent(final ContentAction<? extends T> readAction) throws ResourceException {
        return withContentIfPresent(readAction);
    }

    @Nullable
    @Override
    public <T> ExternalResourceReadResult<T> withContentIfPresent(final ContentAndMetadataAction<? extends T> readAction) throws ResourceException {
        return delegate.withContentIfPresent(readAction);
    }
}
