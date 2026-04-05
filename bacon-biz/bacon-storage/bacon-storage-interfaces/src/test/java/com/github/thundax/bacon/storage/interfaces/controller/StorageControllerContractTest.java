package com.github.thundax.bacon.storage.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StorageControllerContractTest {

    private MockMvc mockMvc;
    private StoredObjectApplicationService storedObjectApplicationService;
    private StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    @BeforeEach
    void setUp() {
        storedObjectApplicationService = Mockito.mock(StoredObjectApplicationService.class);
        storedObjectQueryApplicationService = Mockito.mock(StoredObjectQueryApplicationService.class);
        StorageController controller = new StorageController(storedObjectApplicationService,
                storedObjectQueryApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .build();
    }

    @Test
    void shouldWrapPageObjectsForAdminFrontend() throws Exception {
        when(storedObjectQueryApplicationService.pageObjects(any())).thenReturn(new StoredObjectPageResultDTO(
                List.of(new StoredObjectDTO(StoredObjectId.of(101L), "LOCAL_FILE", "default", "attachment/e.txt", "e.txt",
                        "text/plain", 5L, "/files/attachment/e.txt", "ACTIVE", "UNREFERENCED",
                        Instant.parse("2026-03-27T10:00:00Z"))), 1L, 1, 20));

        mockMvc.perform(get("/api/storage/objects")
                        .param("tenantId", "tenant-a")
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
        when(storedObjectQueryApplicationService.getObjectById("O100")).thenReturn(new StoredObjectDTO(
                StoredObjectId.of(100L), "LOCAL_FILE", "default", "attachment/a.txt", "a.txt", "text/plain", 3L,
                "/files/attachment/a.txt", "ACTIVE", "UNREFERENCED", Instant.parse("2026-03-27T10:00:00Z")));

        mockMvc.perform(get("/api/storage/objects/{objectId}", "O100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value("O100"));
    }

    @Test
    void shouldWrapDeleteForAdminFrontend() throws Exception {
        doNothing().when(storedObjectApplicationService).deleteObject("O100");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/storage/objects/{objectId}", "O100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldRejectIllegalPageSizeForAdminFrontend() throws Exception {
        mockMvc.perform(get("/api/storage/objects")
                        .param("pageNo", "1")
                        .param("pageSize", "201"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void shouldRejectIllegalEnumValueForAdminFrontend() throws Exception {
        mockMvc.perform(get("/api/storage/objects")
                        .param("storageType", "INVALID")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
