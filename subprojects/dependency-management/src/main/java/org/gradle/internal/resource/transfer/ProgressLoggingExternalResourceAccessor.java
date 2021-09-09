/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.internal.resource.transfer;

import org.gradle.api.resources.ResourceException;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.CallableBuildOperation;
import org.gradle.internal.resource.ExternalResource;
import org.gradle.internal.resource.ExternalResourceName;
import org.gradle.internal.resource.ExternalResourceReadBuildOperationType;
import org.gradle.internal.resource.ExternalResourceReadMetadataBuildOperationType;
import org.gradle.internal.resource.metadata.ExternalResourceMetaData;

import javax.annotation.Nullable;
import java.net.URI;

public class ProgressLoggingExternalResourceAccessor extends AbstractProgressLoggingHandler implements ExternalResourceAccessor {
    private final ExternalResourceAccessor delegate;
    private final BuildOperationExecutor buildOperationExecutor;

    public ProgressLoggingExternalResourceAccessor(ExternalResourceAccessor delegate, ProgressLoggerFactory progressLoggerFactory, BuildOperationExecutor buildOperationExecutor) {
        super(progressLoggerFactory);
        this.delegate = delegate;
        this.buildOperationExecutor = buildOperationExecutor;
    }

    @Nullable
    @Override
    public <T> T withContent(ExternalResourceName location, boolean revalidate, ExternalResource.ContentAndMetadataAction<T> action) throws ResourceException {
        return buildOperationExecutor.call(new CallableBuildOperation<T>() {
            @Override
            public T call(BuildOperationContext context) {
                return delegate.withContent(location, revalidate, (inputStream, metaData) -> {
                    ResourceOperation downloadOperation = createResourceOperation(location.getUri(), ResourceOperation.Type.download, getClass(), metaData.getContentLength());
                    ProgressLoggingInputStream stream = new ProgressLoggingInputStream(inputStream, downloadOperation);
                    try {
                        T result = action.execute(stream, metaData);
                        context.setResult(new ReadOperationResult(stream.getBytesRead()));
                        return result;
                    } finally {
                        downloadOperation.completed();
                    }
                });
            }

            @Override
            public BuildOperationDescriptor.Builder description() {
                return createBuildOperationDetails(location);
            }
        });
    }

    @Override
    @Nullable
    public ExternalResourceMetaData getMetaData(ExternalResourceName location, boolean revalidate) {
        return buildOperationExecutor.call(new CallableBuildOperation<ExternalResourceMetaData>() {
            @Override
            public ExternalResourceMetaData call(BuildOperationContext context) {
                ExternalResourceMetaData result = delegate.getMetaData(location, revalidate);
                context.setResult(METADATA_RESULT);
                return result;
            }

            @Override
            public BuildOperationDescriptor.Builder description() {
                return BuildOperationDescriptor
                    .displayName("Metadata of " + location.getDisplayName())
                    .details(new MetadataOperationDetails(location.getUri()));
            }
        });
    }

    private BuildOperationDescriptor.Builder createBuildOperationDetails(ExternalResourceName resourceName) {
        ExternalResourceReadBuildOperationType.Details operationDetails = new ReadOperationDetails(resourceName.getUri());
        return BuildOperationDescriptor
            .displayName("Download " + resourceName.getDisplayName())
            .progressDisplayName(resourceName.getShortDisplayName())
            .details(operationDetails);
    }

    private static class MetadataOperationDetails extends LocationDetails implements ExternalResourceReadMetadataBuildOperationType.Details {
        private MetadataOperationDetails(URI location) {
            super(location);
        }

        @Override
        public String toString() {
            return "ExternalResourceReadMetadataBuildOperationType.Details{location=" + getLocation() + ", " + '}';
        }
    }

    private final static ExternalResourceReadMetadataBuildOperationType.Result METADATA_RESULT = new ExternalResourceReadMetadataBuildOperationType.Result() {
    };

    private static class ReadOperationDetails extends LocationDetails implements ExternalResourceReadBuildOperationType.Details {
        private ReadOperationDetails(URI location) {
            super(location);
        }

        @Override
        public String toString() {
            return "ExternalResourceReadBuildOperationType.Details{location=" + getLocation() + ", " + '}';
        }
    }

    private static class ReadOperationResult implements ExternalResourceReadBuildOperationType.Result {

        private final long bytesRead;

        private ReadOperationResult(long bytesRead) {
            this.bytesRead = bytesRead;
        }

        @Override
        public long getBytesRead() {
            return bytesRead;
        }

        @Override
        public String toString() {
            return "ExternalResourceReadBuildOperationType.Result{bytesRead=" + bytesRead + '}';
        }

    }
}
