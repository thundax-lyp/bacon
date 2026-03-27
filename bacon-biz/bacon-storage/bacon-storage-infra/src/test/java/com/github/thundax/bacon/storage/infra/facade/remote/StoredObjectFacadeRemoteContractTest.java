package com.github.thundax.bacon.storage.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StoredObjectFacadeRemoteContractTest {

    private static final String BASE_URL = "http://storage.test/api";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallUploadProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/storage/objects/upload"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        StoredObjectFacadeRemoteImpl facade = newFacade();
        facade.uploadObject(new UploadObjectCommand("GENERIC_ATTACHMENT", "tenant-a", "attachment",
                "a.txt", "text/plain", 3L, new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))));

        server.verify();
    }

    @Test
    void shouldCallMultipartInitProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/storage/objects/multipart/init"
                        + "?ownerType=GENERIC_ATTACHMENT&ownerId=owner-1&tenantId=tenant-a&category=attachment"
                        + "&originalFilename=a.txt&contentType=text%2Fplain&totalSize=1024&partSize=8388608"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        StoredObjectFacadeRemoteImpl facade = newFacade();
        facade.initMultipartUpload(new InitMultipartUploadCommand("GENERIC_ATTACHMENT", "owner-1", "tenant-a",
                "attachment", "a.txt", "text/plain", 1024L, 8L * 1024 * 1024));

        server.verify();
    }

    @Test
    void shouldCallMultipartPartProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/storage/objects/multipart/upload-1/parts"
                        + "?ownerType=GENERIC_ATTACHMENT&ownerId=owner-1&tenantId=tenant-a"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        StoredObjectFacadeRemoteImpl facade = newFacade();
        facade.uploadMultipartPart(new UploadMultipartPartCommand("upload-1", "GENERIC_ATTACHMENT", "owner-1",
                "tenant-a", 1, 3L, new ByteArrayInputStream(new byte[]{1, 2, 3})));

        server.verify();
    }

    @Test
    void shouldCallMultipartCompleteProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/storage/objects/multipart/upload-1/complete"
                        + "?ownerType=GENERIC_ATTACHMENT&ownerId=owner-1&tenantId=tenant-a"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        StoredObjectFacadeRemoteImpl facade = newFacade();
        facade.completeMultipartUpload(new CompleteMultipartUploadCommand("upload-1", "GENERIC_ATTACHMENT",
                "owner-1", "tenant-a"));

        server.verify();
    }

    @Test
    void shouldCallMultipartAbortProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/storage/objects/multipart/upload-1"
                        + "?ownerType=GENERIC_ATTACHMENT&ownerId=owner-1&tenantId=tenant-a"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        StoredObjectFacadeRemoteImpl facade = newFacade();
        facade.abortMultipartUpload(new AbortMultipartUploadCommand("upload-1", "GENERIC_ATTACHMENT",
                "owner-1", "tenant-a"));

        server.verify();
    }

    private StoredObjectFacadeRemoteImpl newFacade() {
        @SuppressWarnings("unchecked")
        ObjectProvider<RestClient.Builder> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable(Mockito.any())).thenReturn(restClientBuilder);
        RestClientFactory factory = new RestClientFactory(provider);
        return new StoredObjectFacadeRemoteImpl(factory, BASE_URL);
    }
}
