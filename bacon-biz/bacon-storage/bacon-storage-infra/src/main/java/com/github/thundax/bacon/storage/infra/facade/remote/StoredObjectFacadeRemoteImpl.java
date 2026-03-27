package com.github.thundax.bacon.storage.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class StoredObjectFacadeRemoteImpl implements StoredObjectFacade {

    private final RestClient restClient;

    public StoredObjectFacadeRemoteImpl(RestClientFactory restClientFactory,
                                        @Value("${bacon.remote.storage-base-url:http://127.0.0.1:8086/api}") String baseUrl) {
        this.restClient = restClientFactory.create(baseUrl);
    }

    @Override
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("ownerType", command.getOwnerType());
        if (command.getTenantId() != null) {
            bodyBuilder.part("tenantId", command.getTenantId());
        }
        if (command.getCategory() != null) {
            bodyBuilder.part("category", command.getCategory());
        }
        bodyBuilder.part("file", new NamedInputStreamResource(command.getInputStream(), command.getOriginalFilename()))
                .contentType(resolveMediaType(command.getContentType()));
        return restClient.post()
                .uri("/providers/storage/objects/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(StoredObjectDTO.class);
    }

    @Override
    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        return restClient.post()
                .uri("/providers/storage/objects/multipart/init?ownerType={ownerType}&ownerId={ownerId}&tenantId={tenantId}&category={category}"
                                + "&originalFilename={originalFilename}&contentType={contentType}&totalSize={totalSize}&partSize={partSize}",
                        command.getOwnerType(), command.getOwnerId(), command.getTenantId(), command.getCategory(), command.getOriginalFilename(),
                        command.getContentType(), command.getTotalSize(), command.getPartSize())
                .retrieve()
                .body(MultipartUploadSessionDTO.class);
    }

    @Override
    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("partNumber", command.getPartNumber());
        bodyBuilder.part("file", new NamedInputStreamResource(command.getInputStream(), "part-" + command.getPartNumber()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM);
        return restClient.post()
                .uri("/providers/storage/objects/multipart/{uploadId}/parts?ownerType={ownerType}&ownerId={ownerId}&tenantId={tenantId}",
                        command.getUploadId(), command.getOwnerType(), command.getOwnerId(), command.getTenantId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(MultipartUploadPartDTO.class);
    }

    @Override
    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        return restClient.post()
                .uri("/providers/storage/objects/multipart/{uploadId}/complete?ownerType={ownerType}&ownerId={ownerId}&tenantId={tenantId}",
                        command.getUploadId(), command.getOwnerType(), command.getOwnerId(), command.getTenantId())
                .retrieve()
                .body(StoredObjectDTO.class);
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadCommand command) {
        restClient.delete()
                .uri("/providers/storage/objects/multipart/{uploadId}?ownerType={ownerType}&ownerId={ownerId}&tenantId={tenantId}",
                        command.getUploadId(), command.getOwnerType(), command.getOwnerId(), command.getTenantId())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public StoredObjectDTO getObjectById(Long objectId) {
        return restClient.get()
                .uri("/providers/storage/objects/{objectId}", objectId)
                .retrieve()
                .body(StoredObjectDTO.class);
    }

    @Override
    public void markObjectReferenced(Long objectId, String ownerType, String ownerId) {
        restClient.post()
                .uri("/providers/storage/objects/{objectId}/references?ownerType={ownerType}&ownerId={ownerId}",
                        objectId, ownerType, ownerId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void clearObjectReference(Long objectId, String ownerType, String ownerId) {
        restClient.delete()
                .uri("/providers/storage/objects/{objectId}/references?ownerType={ownerType}&ownerId={ownerId}",
                        objectId, ownerType, ownerId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteObject(Long objectId) {
        restClient.delete()
                .uri("/providers/storage/objects/{objectId}", objectId)
                .retrieve()
                .toBodilessEntity();
    }

    private MediaType resolveMediaType(String contentType) {
        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
    }

    private static final class NamedInputStreamResource extends InputStreamResource {

        private final String filename;

        private NamedInputStreamResource(java.io.InputStream inputStream, String filename) {
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
