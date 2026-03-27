package com.github.thundax.bacon.storage.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;

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
        bodyBuilder.part("file", new NamedByteArrayResource(readBytes(command), command.getOriginalFilename()))
                .contentType(resolveMediaType(command.getContentType()));
        return restClient.post()
                .uri("/providers/storage/objects")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(StoredObjectDTO.class);
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

    private byte[] readBytes(UploadObjectCommand command) {
        try {
            return command.getInputStream().readAllBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read upload object command input stream", ex);
        }
    }

    private MediaType resolveMediaType(String contentType) {
        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
