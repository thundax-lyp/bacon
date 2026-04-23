package com.github.thundax.bacon.storage.infra.facade.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
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
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class StorageFacadeRemoteContractTest {

    private static final String BASE_URL = "http://storage.test/api";
    private static final String PROVIDER_TOKEN = "storage-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallUploadAndMultipartProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/storage/commands/upload-object"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectJson("storage-1"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/storage/commands/init-multipart-upload"
                        + "?ownerType=GENERIC_ATTACHMENT&ownerId=owner-1&category=attachment"
                        + "&originalFilename=a.txt&contentType=text%2Fplain&totalSize=1024&partSize=512"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                          "uploadId": "upload-1",
                          "ownerType": "GENERIC_ATTACHMENT",
                          "ownerId": "owner-1",
                          "category": "attachment",
                          "originalFilename": "a.txt",
                          "contentType": "text/plain",
                          "totalSize": 1024,
                          "partSize": 512,
                          "uploadedPartCount": 0,
                          "uploadStatus": "INITIATED"
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/storage/commands/upload-multipart-part?uploadId=upload-1"
                        + "&ownerType=GENERIC_ATTACHMENT&ownerId=owner-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {"uploadId":"upload-1","partNumber":1,"etag":"etag-1"}
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/storage/commands/complete-multipart-upload?uploadId=upload-1"
                        + "&ownerType=GENERIC_ATTACHMENT&ownerId=owner-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectJson("storage-2"), MediaType.APPLICATION_JSON));
        StoredObjectFacadeRemoteImpl facade = newFacade();

        StoredObjectFacadeResponse uploaded = facade.uploadObject(new UploadObjectFacadeRequest(
                "GENERIC_ATTACHMENT",
                "attachment",
                "a.txt",
                "text/plain",
                3L,
                new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))));
        MultipartUploadSessionFacadeResponse session = facade.initMultipartUpload(new InitMultipartUploadFacadeRequest(
                "GENERIC_ATTACHMENT", "owner-1", "attachment", "a.txt", "text/plain", 1024L, 512L));
        MultipartUploadPartFacadeResponse part = facade.uploadMultipartPart(new UploadMultipartPartFacadeRequest(
                "upload-1", "GENERIC_ATTACHMENT", "owner-1", 1, 3L, new ByteArrayInputStream(new byte[] {1})));
        StoredObjectFacadeResponse completed = facade.completeMultipartUpload(
                new CompleteMultipartUploadFacadeRequest("upload-1", "GENERIC_ATTACHMENT", "owner-1"));

        assertThat(uploaded.getStoredObjectNo()).isEqualTo("storage-1");
        assertThat(session.getUploadStatus()).isEqualTo("INITIATED");
        assertThat(part.getEtag()).isEqualTo("etag-1");
        assertThat(completed.getStoredObjectNo()).isEqualTo("storage-2");
        server.verify();
    }

    @Test
    void shouldCallReadReferenceAndDeleteProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/storage/queries/object?storedObjectNo=storage-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectJson("storage-1"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/storage/commands/abort-multipart-upload?uploadId=upload-1"
                        + "&ownerType=GENERIC_ATTACHMENT&ownerId=owner-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL
                        + "/providers/storage/commands/mark-object-referenced?storedObjectNo=storage-1"
                        + "&ownerType=GENERIC_ATTACHMENT&ownerId=owner-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL
                        + "/providers/storage/commands/clear-object-reference?storedObjectNo=storage-1"
                        + "&ownerType=GENERIC_ATTACHMENT&ownerId=owner-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL + "/providers/storage/commands/delete-object?storedObjectNo=storage-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
        StoredObjectFacadeRemoteImpl facade = newFacade();

        StoredObjectFacadeResponse object = facade.getObjectByNo(new StoredObjectGetFacadeRequest("storage-1"));
        facade.abortMultipartUpload(new AbortMultipartUploadFacadeRequest("upload-1", "GENERIC_ATTACHMENT", "owner-1"));
        facade.markObjectReferenced(new StoredObjectReferenceFacadeRequest("storage-1", "GENERIC_ATTACHMENT", "owner-1"));
        facade.clearObjectReference(new StoredObjectReferenceFacadeRequest("storage-1", "GENERIC_ATTACHMENT", "owner-1"));
        facade.deleteObject(new StoredObjectDeleteFacadeRequest("storage-1"));

        assertThat(object.getStoredObjectNo()).isEqualTo("storage-1");
        server.verify();
    }

    private StoredObjectFacadeRemoteImpl newFacade() {
        @SuppressWarnings("unchecked")
        ObjectProvider<RestClient.Builder> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable(Mockito.any())).thenReturn(restClientBuilder);
        StorageRemoteClientProperties properties = new StorageRemoteClientProperties();
        properties.setConnectTimeout(null);
        properties.setReadTimeout(null);
        properties.setProviderToken(PROVIDER_TOKEN);
        return new StoredObjectFacadeRemoteImpl(new RestClientFactory(provider), properties, BASE_URL);
    }

    private String objectJson(String storedObjectNo) {
        return """
                {
                  "storedObjectNo": "%s",
                  "storageType": "LOCAL_FILE",
                  "bucketName": "default",
                  "objectKey": "attachment/a.txt",
                  "originalFilename": "a.txt",
                  "contentType": "text/plain",
                  "size": 3,
                  "accessEndpoint": "/files/attachment/a.txt",
                  "objectStatus": "ACTIVE",
                  "referenceStatus": "UNREFERENCED",
                  "createdAt": "2026-03-27T10:00:00Z"
                }
                """
                .formatted(storedObjectNo);
    }
}
