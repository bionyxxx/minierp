package com.minierp.config;

import java.time.Instant;
import java.time.LocalDate;
import java.util.TimeZone;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonFormat;

import tools.jackson.databind.json.JsonMapper;

/**
 * Locks the JSON date contract so it cannot drift between library versions:
 * {@link Instant} as ISO-8601 UTC with fixed precision, {@link LocalDate} as
 * {@code yyyy-MM-dd}. Jackson 3 never writes dates as numeric timestamps, so
 * the explicit patterns below are the whole contract.
 */
@Configuration
public class JacksonConfig {

    /** Fixed {@link Instant} contract: {@code 2026-11-24T10:00:00Z}. */
    public static final String INSTANT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /** Fixed {@link LocalDate} contract: {@code 2026-11-24}. */
    public static final String LOCAL_DATE_PATTERN = "yyyy-MM-dd";

    @Bean
    JsonMapperBuilderCustomizer dateFormatsCustomizer() {
        return (JsonMapper.Builder builder) -> builder
                .withConfigOverride(Instant.class, override -> override.setFormat(
                        JsonFormat.Value.forPattern(INSTANT_PATTERN).withTimeZone(TimeZone.getTimeZone("UTC"))))
                .withConfigOverride(LocalDate.class,
                        override -> override.setFormat(JsonFormat.Value.forPattern(LOCAL_DATE_PATTERN)));
    }
}
