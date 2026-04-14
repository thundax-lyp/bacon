package com.github.thundax.bacon.storage.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
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
public class StoredObjectFacadeRemoteImpl implements StoredObjectFacade {

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
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("ownerType", command.getOwnerType());
        if (command.getCategory() != null) {
            bodyBuilder.part("category", command.getCategory());
        }
        bodyBuilder
                .part("file", new NamedInputStreamResource(command.getInputStream(), command.getOriginalFilename()))
                .contentType(resolveMediaType(command.getContentType()));
        return request(restClient.post())
                .uri("/providers/storage/objects/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(StoredObjectDTO.class);
    }

    @Override
    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        return request(restClient.post())
                .uri(
                        "/providers/storage/objects/multipart/init?ownerType={ownerType}&ownerId={ownerId}&category={category}"
                                + "&originalFilename={originalFilename}&contentType={contentType}&totalSize={totalSize}&partSize={partSize}",
                        command.getOwnerType(),
                        command.getOwnerId(),
                        command.getCategory(),
                        command.getOriginalFilename(),
                        command.getContentType(),
                        command.getTotalSize(),
                        command.getPartSize())
                .retrieve()
                .body(MultipartUploadSessionDTO.class);
    }

    @Override
    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("partNumber", command.getPartNumber());
        bodyBuilder
                .part("file", new NamedInputStreamResource(command.getInputStream(), "part-" + command.getPartNumber()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM);
        return request(restClient.post())
                .uri(
                        "/providers/storage/objects/multipart/{uploadId}/parts?ownerType={ownerType}&ownerId={ownerId}",
                        command.getUploadId(),
                        command.getOwnerType(),
                        command.getOwnerId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(MultipartUploadPartDTO.class);
    }

    @Override
    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        return request(restClient.post())
                .uri(
                        "/providers/storage/objects/multipart/{uploadId}/complete?ownerType={ownerType}&ownerId={ownerId}",
                        command.getUploadId(),
                        command.getOwnerType(),
                        command.getOwnerId())
                .retrieve()
                .body(StoredObjectDTO.class);
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadCommand command) {
        request(restClient.delete())
                .uri(
                        "/providers/storage/objects/multipart/{uploadId}?ownerType={ownerType}&ownerId={ownerId}",
                        command.getUploadId(),
                        command.getOwnerType(),
                        command.getOwnerId())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public StoredObjectDTO getObjectById(String objectId) {
        return request(restClient.get())
                .uri("/providers/storage/objects/{objectId}", objectId)
                .retrieve()
                .body(StoredObjectDTO.class);
    }

    @Override
    public void markObjectReferenced(String objectId, String ownerType, String ownerId) {
        request(restClient.post())
                .uri(
                        "/providers/storage/objects/{objectId}/references?ownerType={ownerType}&ownerId={ownerId}",
                        objectId,
                        ownerType,
                        ownerId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void clearObjectReference(String objectId, String ownerType, String ownerId) {
        request(restClient.delete())
                .uri(
                        "/providers/storage/objects/{objectId}/references?ownerType={ownerType}&ownerId={ownerId}",
                        objectId,
                        ownerType,
                        ownerId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteObject(String objectId) {
        request(restClient.delete())
                .uri("/providers/storage/objects/{objectId}", objectId)
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
