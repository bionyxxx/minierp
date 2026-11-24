package com.minierp.config;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.minierp.common.CorrelationIdFilter;
import com.minierp.common.ProblemMessages;
import com.minierp.common.ProblemTitles;
import com.minierp.common.ProblemTypeUris;
import com.minierp.common.ProblemTypes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Returns RFC 9457 problem details for security failures (401/403)
 * instead of an empty response body. Covers exceptions raised inside the
 * security filter chain; dispatcher-level failures are handled by
 * {@link com.minierp.common.GlobalExceptionHandler}.
 */
public class SecurityProblemDetailHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ProblemTypeUris problemTypeUris;

    public SecurityProblemDetailHandler(ObjectMapper objectMapper, ProblemTypeUris problemTypeUris) {
        this.objectMapper = objectMapper;
        this.problemTypeUris = problemTypeUris;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) {
        write(request, response, HttpStatus.UNAUTHORIZED, ProblemTypes.UNAUTHORIZED,
                ProblemTitles.UNAUTHORIZED, ProblemMessages.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) {
        write(request, response, HttpStatus.FORBIDDEN, ProblemTypes.FORBIDDEN,
                ProblemTitles.FORBIDDEN, ProblemMessages.FORBIDDEN);
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
            String slug, String title, String detail) {
        try {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
            problemDetail.setType(problemTypeUris.uri(slug));
            problemDetail.setTitle(title);
            problemDetail.setInstance(URI.create(request.getRequestURI()));
            String correlationId = CorrelationIdFilter.current();
            if (correlationId != null) {
                problemDetail.setProperty("correlationId", correlationId);
            }
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), problemDetail);
        }
        catch (Exception ex) {
            response.setStatus(status.value());
        }
    }
}
