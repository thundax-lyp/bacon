package com.github.thundax.bacon.upms.interfaces.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.command.DepartmentApplicationService;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class DepartmentControllerContractTest {

    private DepartmentApplicationService departmentApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        departmentApplicationService = mock(DepartmentApplicationService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new DepartmentController(departmentApplicationService))
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldRejectBlankCodeWhenCreatingDepartment() throws Exception {
        mockMvc.perform(
                        post("/upms/departments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"code":" ","name":"Operations","parentId":1,"leaderUserId":2001}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(departmentApplicationService);
    }

    @Test
    void shouldRejectTooLongNameWhenUpdatingDepartment() throws Exception {
        mockMvc.perform(
                        put("/upms/departments/{departmentId}", 101L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"code":"OPS","name":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx","parentId":1,"leaderUserId":2001,"sort":1}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(departmentApplicationService);
    }

    @Test
    void shouldTrimCodeAndNameBeforeCallingApplicationService() throws Exception {
        when(departmentApplicationService.createDepartment(
                        eq(DepartmentCode.of("OPS")), eq("Operations"), eq(null), eq(UserId.of(2001L))))
                .thenReturn(new DepartmentDTO(101L, "OPS", "Operations", null, 2001L, 1, "ENABLED"));

        mockMvc.perform(
                        post("/upms/departments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"code":" OPS ","name":" Operations ","parentId":null,"leaderUserId":2001}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OPS"))
                .andExpect(jsonPath("$.name").value("Operations"));
    }
}
