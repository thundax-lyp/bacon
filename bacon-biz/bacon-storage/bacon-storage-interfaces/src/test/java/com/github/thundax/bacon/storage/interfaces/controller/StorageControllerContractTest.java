package com.github.thundax.bacon.storage.interfaces.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class StorageControllerContractTest {

    private MockMvc mockMvc;
    private StoredObjectApplicationService storedObjectApplicationService;
    private StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    @BeforeEach
    void setUp() {
        storedObjectApplicationService = Mockito.mock(StoredObjectApplicationService.class);
        storedObjectQueryApplicationService = Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageController controller =
                new StorageController(storedObjectApplicationService, storedObjectQueryApplicationService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldWrapPageObjectsForAdminFrontend() throws Exception {
        when(storedObjectQueryApplicationService.pageObjects(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new StoredObjectPageResultDTO(
                        List.of(new StoredObjectDTO(
                                "O101",
                                "LOCAL_FILE",
                                "default",
                                "attachment/e.txt",
                                "e.txt",
                                "text/plain",
                                5L,
                                "/files/attachment/e.txt",
                                "ACTIVE",
                                "UNREFERENCED",
                                Instant.parse("2026-03-27T10:00:00Z"))),
                        1L,
                        1,
                        20));

        mockMvc.perform(get("/storage/objects")
                        .param("tenantId", "1")
                        .param("storageType", "LOCAL_FILE")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value("O101"));
    }

    @Test
    void shouldWrapGetObjectForAdminFrontend() throws Exception {
        when(storedObjectQueryApplicationService.getObjectById(100L))
                .thenReturn(new StoredObjectDTO(
                        "O100",
                        "LOCAL_FILE",
                        "default",
                        "attachment/a.txt",
                        "a.txt",
                        "text/plain",
                        3L,
                        "/files/attachment/a.txt",
                        "ACTIVE",
                        "UNREFERENCED",
                        Instant.parse("2026-03-27T10:00:00Z")));

        mockMvc.perform(get("/storage/objects/{objectId}", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value("O100"));
    }

    @Test
    void shouldWrapDeleteForAdminFrontend() throws Exception {
        doNothing().when(storedObjectApplicationService).deleteObject(100L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/storage/objects/{objectId}", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldRejectIllegalPageSizeForAdminFrontend() throws Exception {
        mockMvc.perform(get("/storage/objects").param("pageNo", "1").param("pageSize", "201"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void shouldRejectIllegalEnumValueForAdminFrontend() throws Exception {
        mockMvc.perform(get("/storage/objects")
                        .param("storageType", "INVALID")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void shouldRejectIllegalObjectStatusForAdminFrontend() throws Exception {
        mockMvc.perform(get("/storage/objects")
                        .param("objectStatus", "INVALID")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void shouldRejectIllegalReferenceStatusForAdminFrontend() throws Exception {
        mockMvc.perform(get("/storage/objects")
                        .param("referenceStatus", "INVALID")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
