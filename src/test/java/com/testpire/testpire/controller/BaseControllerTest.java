package com.testpire.testpire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for standalone MockMvc controller tests.
 * No Spring context is loaded — controllers are instantiated with Mockito mocks.
 * The @RequireRole AOP aspect is not applied; auth is covered by the QA test suite.
 */
abstract class BaseControllerTest {

    MockMvc mockMvc;

    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
}
