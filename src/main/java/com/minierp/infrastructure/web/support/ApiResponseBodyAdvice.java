package com.minierp.infrastructure.web.support;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Wraps every JSON body produced by MiniERP controllers and exception handlers
 * in an {@link ApiResponse} envelope. Scoped to {@code com.minierp} so
 * framework endpoints (actuator, springdoc) keep their native format.
 */
@RestControllerAdvice(basePackages = "com.minierp")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof ApiResponse<?>) {
            return body;
        }
        if (body instanceof ProblemDetail problem) {
            // The envelope is no longer a bare RFC 9457 document.
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return ApiResponses.failure(problem);
        }
        return ApiResponses.success(body);
    }
}
