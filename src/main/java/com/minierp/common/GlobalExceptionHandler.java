package com.minierp.common;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Translates exceptions into RFC 9457 problem details. Extends
 * {@link ResponseEntityExceptionHandler} so framework errors (malformed JSON,
 * unsupported methods, unknown routes, ...) keep their proper HTTP status
 * instead of falling through to a generic 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private final ProblemTypeUris problemTypeUris;

    public GlobalExceptionHandler(ProblemTypeUris problemTypeUris) {
        this.problemTypeUris = problemTypeUris;
    }

    // ---------- domain exceptions ----------

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, ProblemTypes.NOT_FOUND, ProblemTitles.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ProblemTypes.BUSINESS_RULE_VIOLATION,
                ProblemTitles.BUSINESS_RULE_VIOLATION, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStateException.class)
    ProblemDetail handleInvalidState(InvalidStateException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, ProblemTypes.INVALID_STATE, ProblemTitles.INVALID_STATE,
                ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    ProblemDetail handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, ProblemTypes.DUPLICATE_RESOURCE, ProblemTitles.DUPLICATE_RESOURCE,
                ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSortException.class)
    ProblemDetail handleInvalidSort(InvalidSortException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, ProblemTypes.INVALID_SORT, ProblemTitles.INVALID_SORT,
                ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String detail = "Parameter %s dengan nilai '%s' tidak valid".formatted(ex.getName(), ex.getValue());
        return problem(HttpStatus.BAD_REQUEST, ProblemTypes.PARAMETER_MISMATCH, ProblemTitles.PARAMETER_MISMATCH,
                detail, request);
    }

    @ExceptionHandler({InvalidApiVersionException.class, MissingApiVersionException.class})
    ProblemDetail handleApiVersion(Exception ex, HttpServletRequest request) {
        String detail = ex instanceof InvalidApiVersionException invalid
                ? ProblemMessages.INVALID_API_VERSION_FORMAT.formatted(invalid.getVersion())
                : ProblemMessages.MISSING_API_VERSION;
        return problem(HttpStatus.BAD_REQUEST, ProblemTypes.INVALID_API_VERSION, ProblemTitles.INVALID_API_VERSION,
                detail, request);
    }

    // ---------- security exceptions raised inside the dispatcher ----------

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String detail = ex.getMessage() != null ? ex.getMessage() : ProblemMessages.LOGIN_FAILED;
        return problem(HttpStatus.UNAUTHORIZED, ProblemTypes.LOGIN_FAILED, ProblemTitles.LOGIN_FAILED, detail, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, ProblemTypes.FORBIDDEN, ProblemTitles.FORBIDDEN,
                ProblemMessages.FORBIDDEN, request);
    }

    // ---------- fallback ----------

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, ProblemTypes.INTERNAL_ERROR, ProblemTitles.INTERNAL_ERROR,
                ProblemMessages.INTERNAL_ERROR, request);
    }

    // ---------- framework exceptions ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldViolation(fe.getField(),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "tidak valid"))
                .toList();
        String detail = ProblemMessages.VALIDATION_FAILED_FORMAT.formatted(violations.size());
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION_FAILED,
                ProblemTitles.VALIDATION_FAILED, detail, requestUri(request));
        problem.setProperty("errors", violations);
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, ProblemTypes.MALFORMED_REQUEST,
                ProblemTitles.MALFORMED_REQUEST, ProblemMessages.MALFORMED_REQUEST, requestUri(request));
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Brands framework problem details that carry no explicit type yet, and
     * stamps every one of them with the request path.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, status, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            if (problem.getType() == null || ABOUT_BLANK.equals(problem.getType())) {
                problem.setType(problemTypeUris.uri(ProblemTypes.forStatus(status.value())));
                problem.setTitle(ProblemTitles.forStatus(status));
            }
            problem.setInstance(URI.create(requestUri(request)));
            addCorrelationId(problem);
        }
        return response;
    }

    // ---------- helpers ----------

    private ProblemDetail problem(HttpStatus status, String slug, String title, String detail, HttpServletRequest request) {
        return problem(status, slug, title, detail, request.getRequestURI());
    }

    private ProblemDetail problem(HttpStatus status, String slug, String title, String detail, String instance) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(problemTypeUris.uri(slug));
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(instance));
        addCorrelationId(problemDetail);
        return problemDetail;
    }

    private void addCorrelationId(ProblemDetail problem) {
        String correlationId = CorrelationIdFilter.current();
        if (correlationId != null
                && (problem.getProperties() == null || !problem.getProperties().containsKey("correlationId"))) {
            problem.setProperty("correlationId", correlationId);
        }
    }

    private String requestUri(WebRequest request) {
        return request instanceof ServletWebRequest servletWebRequest
                ? servletWebRequest.getRequest().getRequestURI()
                : request.getDescription(false).replace("uri=", "");
    }
}
