package com.github.thundax.bacon.common.web.advice;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiResponseWebAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WrappedTestController(), new PlainTestController())
                .setControllerAdvice(new ApiResponseBodyAdvice(), new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldWrapAnnotatedControllerResponse() throws Exception {
        mockMvc.perform(get("/wrapped/value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data").value("value"))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void shouldWrapAnnotatedControllerVoidResponse() throws Exception {
        mockMvc.perform(get("/wrapped/void"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void shouldLeavePlainControllerResponseUntouched() throws Exception {
        mockMvc.perform(get("/plain/value"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("value"));
    }

    @Test
    void shouldConvertAnnotatedControllerExceptionToApiResponse() throws Exception {
        mockMvc.perform(get("/wrapped/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("bad request"));
    }

    @Test
    void shouldConvertMissingRequestParameterToApiResponse() throws Exception {
        mockMvc.perform(get("/wrapped/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Missing required parameter: requiredParam"));
    }

    @Test
    void shouldConvertTypeMismatchToApiResponse() throws Exception {
        mockMvc.perform(get("/wrapped/type-mismatch").param("count", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid parameter: count"));
    }

    @Test
    void shouldConvertUnreadableBodyToApiResponse() throws Exception {
        mockMvc.perform(post("/wrapped/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request body is invalid or unreadable"));
    }

    @WrappedApiController
    @RestController
    static class WrappedTestController {

        @GetMapping("/wrapped/value")
        String value() {
            return "value";
        }

        @GetMapping("/wrapped/void")
        void empty() {
        }

        @GetMapping("/wrapped/bad-request")
        String badRequest() {
            throw new BadRequestException("bad request");
        }

        @GetMapping("/wrapped/missing-param")
        String missingParam(@RequestParam("requiredParam") String requiredParam) {
            return requiredParam;
        }

        @GetMapping("/wrapped/type-mismatch")
        int typeMismatch(@RequestParam("count") int count) {
            return count;
        }

        @PostMapping("/wrapped/body")
        RequestBodyValue body(@RequestBody RequestBodyValue body) {
            return body;
        }
    }

    @RestController
    static class PlainTestController {

        @GetMapping("/plain/value")
        String value() {
            return "value";
        }
    }

    record RequestBodyValue(String value) {
    }
}
