package com.github.thundax.bacon.storage.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.storage.api.request.AbortMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.CompleteMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.InitMultipartUploadFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectDeleteFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectGetFacadeRequest;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadMultipartPartFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.api.response.MultipartUploadPartFacadeResponse;
import com.github.thundax.bacon.storage.api.response.MultipartUploadSessionFacadeResponse;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.storage.infra.config.StorageRemoteClientProperties;
import java.io.InputStream;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class StoredObjectFacadeRemoteImpl implements StoredObjectCommandFacade, StoredObjectReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;
    private final String providerToken;

    public StoredObjectFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            StorageRemoteClientProperties properties,
            @Value("${bacon.remote.storage-base-url:http://bacon-storage-service/api}") String baseUrl) {
        Duration connectTimeout = properties.getConnectTimeout();
        Duration readTimeout = properties.getReadTimeout();
        this.restClient = connectTimeout == null || readTimeout == null
                ? restClientFactory.create(baseUrl)
                : restClientFactory.create(baseUrl, connectTimeout, readTimeout);
        this.providerToken = properties.getProviderToken();
    }

    @Override
    public StoredObjectFacadeResponse uploadObject(UploadObjectFacadeRequest request) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("ownerType", request.getOwnerType());
        if (request.getCategory() != null) {
            bodyBuilder.part("category", request.getCategory());
        }
        bodyBuilder
                .part("file", new NamedInputStreamResource(request.getInputStream(), request.getOriginalFilename()))
                .contentType(resolveMediaType(request.getContentType()));
        return request(restClient.post())
                .uri("/providers/storage/objects/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(StoredObjectFacadeResponse.class);
    }

    @Override
    public MultipartUploadSessionFacadeResponse initMultipartUpload(InitMultipartUploadFacadeRequest request) {
        return request(restClient.post())
                .uri(
                        "/providers/storage/objects/multipart/init?ownerType={ownerType}&ownerId={ownerId}&category={category}"
                                + "&originalFilename={originalFilename}&contentType={contentType}&totalSize={totalSize}&partSize={partSize}",
                        request.getOwnerType(),
                        request.getOwnerId(),
                        request.getCategory(),
                        request.getOriginalFilename(),
                        request.getContentType(),
                        request.getTotalSize(),
                        request.getPartSize())
                .retrieve()
                .body(MultipartUploadSessionFacadeResponse.class);
    }

    @Override
    public MultipartUploadPartFacadeResponse uploadMultipartPart(UploadMultipartPartFacadeRequest request) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("partNumber", request.getPartNumber());
        bodyBuilder
                .part("file", new NamedInputStreamResource(request.getInputStream(), "part-" + request.getPartNumber()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM);
        return request(restClient.post())
                .uri(
                        "/providers/storage/objects/multipart/{uploadId}/parts?ownerType={ownerType}&ownerId={ownerId}",
                        request.getUploadId(),
                        request.getOwnerType(),
                        request.getOwnerId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(MultipartUploadPartFacadeResponse.class);
    }

    @Override
    public StoredObjectFacadeResponse completeMultipartUpload(CompleteMultipartUploadFacadeRequest request) {
        return request(restClient.post())
                .uri(
                        "/providers/storage/objects/multipart/{uploadId}/complete?ownerType={ownerType}&ownerId={ownerId}",
                        request.getUploadId(),
                        request.getOwnerType(),
                        request.getOwnerId())
                .retrieve()
                .body(StoredObjectFacadeResponse.class);
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadFacadeRequest request) {
        request(restClient.delete())
                .uri(
                        "/providers/storage/objects/multipart/{uploadId}?ownerType={ownerType}&ownerId={ownerId}",
                        request.getUploadId(),
                        request.getOwnerType(),
                        request.getOwnerId())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public StoredObjectFacadeResponse getObjectByNo(StoredObjectGetFacadeRequest request) {
        return request(restClient.get())
                .uri("/providers/storage/objects/{storedObjectNo}", request.getStoredObjectNo())
                .retrieve()
                .body(StoredObjectFacadeResponse.class);
    }

    @Override
    public void markObjectReferenced(StoredObjectReferenceFacadeRequest request) {
        request(restClient.post())
                .uri(
                        "/providers/storage/objects/{storedObjectNo}/references?ownerType={ownerType}&ownerId={ownerId}",
                        request.getStoredObjectNo(),
                        request.getOwnerType(),
                        request.getOwnerId())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void clearObjectReference(StoredObjectReferenceFacadeRequest request) {
        request(restClient.delete())
                .uri(
                        "/providers/storage/objects/{storedObjectNo}/references?ownerType={ownerType}&ownerId={ownerId}",
                        request.getStoredObjectNo(),
                        request.getOwnerType(),
                        request.getOwnerId())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteObject(StoredObjectDeleteFacadeRequest request) {
        request(restClient.delete())
                .uri("/providers/storage/objects/{storedObjectNo}", request.getStoredObjectNo())
                .retrieve()
                .toBodilessEntity();
    }

    private RestClient.RequestBodyUriSpec request(RestClient.RequestBodyUriSpec spec) {
        if (StringUtils.hasText(providerToken)) {
            spec.header(PROVIDER_TOKEN_HEADER, providerToken);
        }
        return spec;
    }

    private RestClient.RequestHeadersUriSpec<?> request(RestClient.RequestHeadersUriSpec<?> spec) {
        if (StringUtils.hasText(providerToken)) {
            spec.header(PROVIDER_TOKEN_HEADER, providerToken);
        }
        return spec;
    }

    private MediaType resolveMediaType(String contentType) {
        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
    }

    private static final class NamedInputStreamResource extends InputStreamResource {

        private final String filename;

        private NamedInputStreamResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public long contentLength() {
            return -1L;
        }
    }
}
