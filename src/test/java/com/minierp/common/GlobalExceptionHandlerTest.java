package com.minierp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.accept.InvalidApiVersionException;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {

    private final ProblemTypeUris uris = new ProblemTypeUris("https://minierp.local/problems/");

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(uris);

    @Test
    void handleUnexpectedReturnsGenericProblemWithoutInternalDetails() {
        HttpServletRequest request = request("GET", "/api/items");

        ProblemDetail problem = handler.handleUnexpected(
                new IllegalStateException("secret internal detail"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals(uris.uri(ProblemTypes.INTERNAL_ERROR), problem.getType());
        assertEquals(ProblemTitles.INTERNAL_ERROR, problem.getTitle());
        assertEquals(ProblemMessages.INTERNAL_ERROR, problem.getDetail());
        assertEquals(URI.create("/api/items"), problem.getInstance());
        assertFalse(problem.getDetail().contains("secret internal detail"));
    }

    @Test
    void handleNotFoundKeepsDynamicDetailAndInstance() {
        HttpServletRequest request = request("GET", "/api/items/99");

        ProblemDetail problem = handler.handleNotFound(new NotFoundException("Item", 99L), request);

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals(uris.uri(ProblemTypes.NOT_FOUND), problem.getType());
        assertTrue(problem.getDetail().contains("99"));
        assertEquals(URI.create("/api/items/99"), problem.getInstance());
    }

    @Test
    void handleInvalidSortListsAllowedProperties() {
        HttpServletRequest request = request("GET", "/api/items");

        ProblemDetail problem = handler.handleInvalidSort(
                new InvalidSortException("hacker", Set.of("sku", "name")), request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals(uris.uri(ProblemTypes.INVALID_SORT), problem.getType());
        assertTrue(problem.getDetail().contains("hacker"));
        assertTrue(problem.getDetail().contains("sku"));
    }

    @Test
    void handleAccessDeniedReturnsForbiddenProblem() {
        HttpServletRequest request = request("POST", "/api/items");

        ProblemDetail problem = handler.handleAccessDenied(new AccessDeniedException("denied"), request);

        assertEquals(HttpStatus.FORBIDDEN.value(), problem.getStatus());
        assertEquals(uris.uri(ProblemTypes.FORBIDDEN), problem.getType());
        assertEquals(ProblemMessages.FORBIDDEN, problem.getDetail());
    }

    @Test
    void handleApiVersionRejectsUnsupportedVersion() {
        HttpServletRequest request = request("GET", "/api/items");

        ProblemDetail problem = handler.handleApiVersion(new InvalidApiVersionException("2.0"), request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals(uris.uri(ProblemTypes.INVALID_API_VERSION), problem.getType());
        assertEquals(ProblemTitles.INVALID_API_VERSION, problem.getTitle());
        assertTrue(problem.getDetail().contains("2.0"));
    }

    @Test
    void problemsCarryCorrelationIdFromMdc() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-42");
        try {
            ProblemDetail problem = handler.handleNotFound(new NotFoundException("Item", 99L),
                    request("GET", "/api/items/99"));

            assertEquals("corr-42", problem.getProperties().get("correlationId"));
        }
        finally {
            MDC.remove(CorrelationIdFilter.MDC_KEY);
        }
    }

    private HttpServletRequest request(String method, String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
